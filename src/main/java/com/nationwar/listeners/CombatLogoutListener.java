package com.nationwar.listeners;

import com.nationwar.NationWar;
import com.nationwar.team.TeamMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CombatLogoutListener implements Listener {

    private final NationWar plugin;
    private final TeamMain teamMain;

    // UUID, 전투 종료 시간(ms)
    private final HashMap<UUID, Long> combatMap = new HashMap<>();

    private static final int COMBAT_TIME = 20; // 20초

    public CombatLogoutListener(NationWar plugin) {
        this.plugin = plugin;
        this.teamMain = plugin.getTeamMain();

        // 액션바 타이머
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (!combatMap.containsKey(uuid)) continue;

                    long end = combatMap.get(uuid);
                    int remain = (int) ((end - now) / 1000);

                    if (remain <= 0) {
                        combatMap.remove(uuid);
                        player.sendActionBar(Component.empty());
                        continue;
}

                    player.sendActionBar(
                            Component.text("⚔ PVP 중 | ")
                                    .color(NamedTextColor.RED)
                                    .append(Component.text("남은 시간: " + remain + "초",
                                            NamedTextColor.YELLOW))
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // PVP 감지
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        // 같은 팀이면 PVP 무시 (방랑자 판정도 TeamMain에서 처리됨)
        if (teamMain.sameTeam(victim, attacker)) return;

        long endTime = System.currentTimeMillis() + (COMBAT_TIME * 1000L);

        combatMap.put(victim.getUniqueId(), endTime);
        combatMap.put(attacker.getUniqueId(), endTime);
    }

    // 전투 로그아웃
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!combatMap.containsKey(uuid)) return;

        long end = combatMap.get(uuid);
        if (System.currentTimeMillis() < end) {
            player.setHealth(0.0);
            Bukkit.broadcast(
                    Component.text("☠ " + player.getName() + "님이 전투 중 도망쳐 사망했습니다.",
                            NamedTextColor.RED)
            );
        }

        combatMap.remove(uuid);
    }
}
