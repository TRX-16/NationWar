package com.nationwar.core;

import com.nationwar.NationWar;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class CoreMain {
    private final NationWar plugin;
    private final File coreFile;
    private CoreGson.CoreData coreData;
    private boolean gameStarted = false;

    public CoreMain(NationWar plugin) {
        this.plugin = plugin;
        this.coreFile = new File(plugin.getDataFolder(), "core.json");
        this.coreData = CoreGson.load(coreFile);
    }

    public CoreGson.CoreData getCoreData() { return coreData; }

    public boolean reloadCoreFromConfig() { // 추가됨
        this.coreData = CoreGson.load(coreFile);
        if (this.coreData == null || this.coreData.cores.isEmpty()) return false;

        for (CoreGson.CoreInfo info : coreData.cores) {
            Location loc = new Location(Bukkit.getWorlds().get(0), info.x, info.y, info.z);
            buildCorePlate(loc);
            spawnCoreGhast(info.id, loc);
        }
        return true;
    }

    public void spawnCoreGhast(int id, Location loc) {
        // [중요] .clone()을 사용하여 원본 데이터(core.json 좌표)가 수정되는 것을 방지합니다.
        // .add(0.5, 1, 0.5)를 통해 블록 정중앙 위쪽에 소환합니다.
        Location spawnLoc = loc.clone().add(0.5, 1.0, 0.5);

        Ghast ghast = (Ghast) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.GHAST);

        ghast.setCustomName("§f코어 " + id);
        ghast.setCustomNameVisible(true);
        ghast.setAI(false);
        ghast.setSilent(true);
        ghast.setRemoveWhenFarAway(false);
        ghast.setInvulnerable(false);

        double ghastHp = plugin.getConfig().getDouble("core.ghast-health", 1024.0);
        if (ghast.getAttribute(Attribute.MAX_HEALTH) != null) {
            ghast.getAttribute(Attribute.MAX_HEALTH).setBaseValue(ghastHp);
        }
        ghast.setHealth(ghastHp);

        ghast.setMetadata("core_id", new FixedMetadataValue(plugin, id));
    }

    public void LoadCores() {
        // 재시작 시 기존 코어 가스트 및 데이터 초기화 (중복 방지)
        removeAllCoreGhasts();
        coreData = new CoreGson.CoreData();

        // 기준서: 15000x15000 내 무작위 위치 지면 생성
        World world = Bukkit.getWorlds().get(0);
        Random random = new Random();
        int coreCount = plugin.getConfig().getInt("core.count", 6);
        int coreRange = plugin.getConfig().getInt("world.core-range", 15000);
        double coreHp = plugin.getConfig().getDouble("core.hp", 5000);

        for (int i = 0; i < coreCount; i++) {
            int x = random.nextInt(coreRange + 1) - coreRange / 2;
            int z = random.nextInt(coreRange + 1) - coreRange / 2;
            int y = world.getHighestBlockYAt(x, z);

            CoreGson.CoreInfo info = new CoreGson.CoreInfo();
            info.id = i;
            info.x = x; info.y = y; info.z = z;
            info.hp = coreHp;
            coreData.cores.add(info);

            Location loc = new Location(world, x, y, z);
            buildCorePlate(loc);
            spawnCoreGhast(i, loc);
        }
        saveCores();
    }

    // CoreMain 클래스 안에 추가/수정
    public void stopGame(String winnerTeam) {
        setGameStarted(false);
        removeAllCoreGhasts();

        // 게임 종료 시 대기 상태로 전환
        plugin.getGameState().deactivate();

        // 게임 종료 시 KD 초기화
        if (plugin.getPvpListener() != null) {
            plugin.getPvpListener().resetKD();
        }

        Bukkit.getLogger().info("[NationWar] 게임이 종료되었습니다. 우승팀: " + winnerTeam);
    }

    public void buildCorePlate(Location loc) {
        // 기준서: 5x5 화이트 콘크리트
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                loc.clone().add(x, 0, z).getBlock().setType(Material.WHITE_CONCRETE);
            }
        }
    }

    public void removeAllCoreGhasts() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(Ghast.class)) {
                if (entity.hasMetadata("core_id")) entity.remove();
            }
        }
    }

    public boolean isGameStarted() { return gameStarted; }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        plugin.getLogger().info("[Core] 보호막 상태 변경: " + (started ? "해제(공격가능)" : "가동(공격불가)"));
    }

    public void respawnAllCores() {
        removeAllCoreGhasts();

        World world = Bukkit.getWorlds().get(0); // 첫 번째 월드 사용 (world 이름 하드코딩 제거)
        for (CoreGson.CoreInfo core : getCoreData().cores) {
            Location loc = new Location(world, core.x, core.y, core.z);
            spawnCoreGhast(core.id, loc);
        }
        Bukkit.broadcastMessage(plugin.getConfig().getString("format.respawn-complete", "§6§l[!] §f모든 코어 가스트가 성공적으로 재생성되었습니다."));
    }

    public void startCaptureEvent() {
        new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {

                    // 핵심: 게임 시작 상태로 전환!
                    plugin.getCoreMain().setGameStarted(true);

                    this.cancel();
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startTimeChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                int hour = now.getHour();
                int min = now.getMinute();
                int sec = now.getSecond();

                int endHour = plugin.getConfig().getInt("capture-time.end-hour", 22);

                // 종료 시각 + 점령 요일인지 둘 다 확인
                if (hour == endHour && min == 0 && sec == 0 && isCaptureDay()) {
                    if (isGameStarted()) {
                        String timeoverTitle    = plugin.getConfig().getString("end-message.timeover-title",    "§c§lTIME OVER");
                        String timeoverSubtitle = plugin.getConfig().getString("end-message.timeover-subtitle", "§f점령 시간이 종료되었습니다.");
                        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle(timeoverTitle, timeoverSubtitle, 10, 70, 20);
                            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                        }
                        determineWinnerByCount();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void determineWinnerByCount() {
        Map<String, Integer> score = new HashMap<>();
        int totalCores = coreData.cores.size(); // 총 코어 개수 (6개)

        // 1. 팀별 점령 개수 계산
        for (CoreGson.CoreInfo core : coreData.cores) {
            if (core.owner != null && !core.owner.equals("없음") && !core.owner.isEmpty()) {
                score.put(core.owner, score.getOrDefault(core.owner, 0) + 1);
            }
        }

        // 2. 최고 점수 확인
        int maxScore = score.values().stream().max(Integer::compare).orElse(0);

        // [핵심 로직] 한 팀이 6개(모든 코어)를 점령하지 못했다면 아무 메시지 없이 종료
        if (maxScore < totalCores) {
            // 아무 메시지도 띄우지 않고 게임 상태만 정리합니다.
            stopGame("없음");
            return;
        }

        // 3. 만약 6개를 다 먹은 팀이 있다면 우승 발표 (이론상 실시간으로 이미 끝났겠지만 안전장치)
        String winner = score.entrySet().stream()
                .filter(e -> e.getValue() == totalCores)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("없음");

        if (!winner.equals("없음") && plugin.getCoreDamageListener() != null) {
            plugin.getCoreDamageListener().announceVictory(winner);
        } else {
            stopGame("없음");
        }
    }


    public void broadcastStartMessage() {
        String sep = plugin.getConfig().getString("format.separator",
                "§8§l[ §4§l! §8§l] §f------------------------------------------ §8§l[ §4§l! §8§l]");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(sep);
        Bukkit.broadcastMessage("");

        for (String line : plugin.getConfig().getStringList("start-message.lines")) {
            Bukkit.broadcastMessage(line);
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(sep);
        Bukkit.broadcastMessage("");

        String title    = plugin.getConfig().getString("start-message.title",    "§4§lWAR BEGINS");
        String subtitle = plugin.getConfig().getString("start-message.subtitle", "§e지금부터 모든 코어의 보호막이 해제됩니다!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
        }
    }

    /** 오늘이 점령 요일인지만 확인 */
    public boolean isCaptureDay() {
        java.time.DayOfWeek day = LocalDateTime.now().getDayOfWeek();
        java.util.List<String> configDays = plugin.getConfig().getStringList("capture-time.days");
        return configDays.stream().anyMatch(d -> d.equalsIgnoreCase(day.name()));
    }

    public boolean isCaptureTime() {
        int hour      = LocalDateTime.now().getHour();
        int startHour = plugin.getConfig().getInt("capture-time.start-hour", 19);
        int endHour   = plugin.getConfig().getInt("capture-time.end-hour", 22);
        return isCaptureDay() && (hour >= startHour && hour < endHour);
    }

    public void saveCores() { CoreGson.save(coreFile, coreData); }

}