package com.nationwar.listeners;

import com.nationwar.NationWar;
import com.nationwar.core.CoreGson;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;
import java.util.UUID;

public class CoreDamageListener implements Listener {
    private final NationWar plugin;

    public CoreDamageListener(NationWar plugin) { this.plugin = plugin; }

    @EventHandler
    public void onGeneralDamage(EntityDamageEvent event) {
        // 코어 가스트인지 확인
        if (!(event.getEntity() instanceof Ghast) || !event.getEntity().hasMetadata("core_id")) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // TNT, 크리퍼, 침대/앵커 폭발 등 모든 폭발 데미지 차단
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setCancelled(true);
            return;
        }

        // 불, 용암, 낙하 데미지 차단
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.LAVA ||
                cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGhastDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        // 1. 죽은 엔티티가 가스트이고 코어 메타데이터가 있는지 확인
        if (event.getEntity() instanceof Ghast && event.getEntity().hasMetadata("core_id")) {
            Ghast deadGhast = (Ghast) event.getEntity();
            int coreId = deadGhast.getMetadata("core_id").get(0).asInt();

            // coreId 범위 초과 방어 (코어 리셋 직후 죽은 가스트 처리)
            if (coreId < 0 || coreId >= plugin.getCoreMain().getCoreData().cores.size()) return;

            CoreGson.CoreInfo info = plugin.getCoreMain().getCoreData().cores.get(coreId);
            Location originalLoc = new Location(deadGhast.getWorld(), info.x, info.y, info.z);

            // 2. 아이템 드롭 방지
            event.getDrops().clear();
            event.setDroppedExp(0);

            // 3. 즉시 재생성 (1틱 뒤에 실행)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // 이제 originalLoc(정수)을 넘기면, spawnCoreGhast 내부에서 .clone().add()가
                // 딱 한 번만 실행되어 정확히 블록 중앙에 소환됩니다.
                plugin.getCoreMain().spawnCoreGhast(coreId, originalLoc);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        // 1. 코어 확인
        if (!(event.getEntity() instanceof Ghast) || !event.getEntity().hasMetadata("core_id")) return;

        // 2. 기본적으로 모든 데미지 취소 (보호막)
        event.setCancelled(true);

        // 3. 공격자 확인
        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();

        // 4. 점령 시간 및 게임 시작 여부 통합 체크
        if (!plugin.getCoreMain().isCaptureTime() || !plugin.getCoreMain().isGameStarted()) {
            String shieldMsg = plugin.getConfig().getString("format.shield-active", "§c§l[!] 보호막 작동 중! (점령 시간이 아닙니다)");
            damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(shieldMsg));
            return;
        }

        // 5. 팀 확인
        String teamName = plugin.getTeamMain().getPlayerTeam(damager.getUniqueId());
        if (teamName.equals("방랑자")) {
            damager.sendMessage("§c방랑자는 코어를 공격할 수 없습니다.");
            return;
        }

        Ghast ghast = (Ghast) event.getEntity();
        int coreId = ghast.getMetadata("core_id").get(0).asInt();
        CoreGson.CoreInfo core = plugin.getCoreMain().getCoreData().cores.get(coreId);

        if (core.owner.equals(teamName)) {
            damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§9§l[!] 우리 팀의 코어는 공격할 수 없습니다!"));
            //p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f); // 선택사항: 거절 효과음
            return;
        }

        // [추가] 가스트의 실제 체력을 즉시 회복시켜서 죽지 않게 함
        ghast.setHealth(ghast.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue());

        // 우리가 관리하는 코어 체력에서 차감
        double damage = event.getFinalDamage();
        core.hp -= damage;
        double maxHp = plugin.getConfig().getDouble("core.hp", 5000);

        String hpBar = plugin.getConfig().getString("format.core-hp-bar", "§e[CORE {id}] §fHP: §c{hp} §7/ {max}")
                .replace("{id}", String.valueOf(coreId))
                .replace("{hp}", String.valueOf((int) core.hp))
                .replace("{max}", String.valueOf((int) maxHp));
        damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(hpBar));

        if (core.hp <= 0) {
            handleCapture(core, coreId, teamName);
        }
    }

    private void checkWinner(String potentialWinner) {
        if (potentialWinner.equals("없음") || potentialWinner.equals("방랑자")) return;

        int totalCores = plugin.getCoreMain().getCoreData().cores.size();
        int ownedCount = 0;
        for (CoreGson.CoreInfo core : plugin.getCoreMain().getCoreData().cores) {
            if (potentialWinner.equals(core.owner)) ownedCount++;
        }

        if (ownedCount >= totalCores) {
            announceVictory(potentialWinner);
        }
    }

    private void spawnVictoryFireworks(Location loc) {
        // 폭죽 객체 생성
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();

        // 폭죽 효과 설정 (금색과 흰색이 섞인 화려한 폭죽)
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(org.bukkit.Color.YELLOW) // 금색
                .withFade(org.bukkit.Color.WHITE)  // 흰색으로 사라짐
                .with(FireworkEffect.Type.BALL_LARGE) // 큰 구체 형태
                .trail(true)  // 잔상 효과
                .flicker(true) // 반짝임 효과
                .build();

        fwm.addEffect(effect);
        fwm.setPower(1); // 날아가는 높이 (1이면 적당히 낮게 터짐)
        fw.setFireworkMeta(fwm);
    }

    private void handleCapture(CoreGson.CoreInfo core, int id, String team) {
        core.owner = team;
        core.hp = plugin.getConfig().getDouble("core.hp", 5000);
        plugin.getCoreMain().saveCores();

        // 점령 알림 + 전체 현황
        String msg = plugin.getConfig().getString("capture-message.broadcast", "§6§l[!] §e{team} §f팀이 §6{id}번 코어§f를 점령했습니다!")
                .replace("{team}", team).replace("{id}", String.valueOf(id));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(msg);
        broadcastCoreStatus();
        Bukkit.broadcastMessage(" ");

        // 승리 판정
        checkWinner(team);
    }

    private void broadcastCoreStatus() {
        java.util.Map<String, Integer> score = new java.util.HashMap<>();
        int unclaimed = 0;
        for (com.nationwar.core.CoreGson.CoreInfo c : plugin.getCoreMain().getCoreData().cores) {
            if (c.owner == null || c.owner.equals("없음") || c.owner.isEmpty()) unclaimed++;
            else score.put(c.owner, score.getOrDefault(c.owner, 0) + 1);
        }
        String prefix = plugin.getConfig().getString("format.core-status-prefix", "§7현재 점령 현황 §8| ");
        StringBuilder sb = new StringBuilder(prefix);
        for (java.util.Map.Entry<String, Integer> e : score.entrySet()) {
            sb.append("§e").append(e.getKey()).append(" §f").append(e.getValue()).append("개  ");
        }
        if (unclaimed > 0) sb.append("§7미점령 ").append(unclaimed).append("개");
        Bukkit.broadcastMessage(sb.toString());
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        // null 체크를 추가하여 안전하게 보스바 제거
        if (plugin.getDistanceDetect() != null) {
            plugin.getDistanceDetect().removeBossBar(event.getPlayer());
        }
    }

    public void announceVictory(String winnerTeam) {
        // 1. 전 서버 타이틀 및 화려한 공지
        String title    = plugin.getConfig().getString("end-message.victory-title",    "§6§lVICTORY");
        String subtitle = plugin.getConfig().getString("end-message.victory-subtitle",  "§e{team} §f팀이 국가전쟁에서 우승했습니다!")
                .replace("{team}", winnerTeam);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendTitle(title, subtitle, 20, 100, 20);
            online.playSound(online.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

            online.sendMessage(plugin.getConfig().getString("format.victory-separator", "§f§m-----------------------------------"));
            online.sendMessage(plugin.getConfig().getString("format.victory-header",    "§e§l[!] §f국가전쟁 종료"));
            online.sendMessage(plugin.getConfig().getString("format.victory-winner",    "§e최종 우승팀: §f{team}").replace("{team}", winnerTeam));
            online.sendMessage(plugin.getConfig().getString("format.victory-separator", "§f§m-----------------------------------"));

            spawnVictoryFireworks(online.getLocation());
        }

        // 2. [핵심] 게임 종료 로직 실행 (보호막 가동 및 가스트 제거)
        plugin.getCoreMain().stopGame(winnerTeam);
    }
}