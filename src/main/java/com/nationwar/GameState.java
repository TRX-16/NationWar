package com.nationwar;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameState implements Listener {

    public enum State {
        WAITING,   // 대기 중 — 시간 정지, AI 제거, 플레이어 점프만 가능
        ACTIVE     // 게임 진행 중
    }

    private State state = State.WAITING;
    private final NationWar plugin;

    public GameState(NationWar plugin) {
        this.plugin = plugin;
    }

    public State getState() { return state; }
    public boolean isActive() { return state == State.ACTIVE; }

    /** 게임 시작/재개 시 호출 */
    public void activate() {
        state = State.ACTIVE;

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        }

        // 모든 몹 AI 복원
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Mob mob) {
                    if (!entity.hasMetadata("core_id")) { // 코어 가스트 제외
                        mob.setAI(true);
                    }
                }
            }
        }

        // 플레이어 이동 제한 해제
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setWalkSpeed(0.2f);
        }

        Bukkit.broadcastMessage("§a§l[!] §f게임이 시작되었습니다. 모든 제한이 해제됩니다.");
    }

    /** 게임 대기/종료 시 호출 */
    public void deactivate() {
        state = State.WAITING;

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        }

        // 모든 몹 AI 제거
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Mob mob) {
                    if (!entity.hasMetadata("core_id")) {
                        mob.setAI(false);
                    }
                }
            }
        }

        // 플레이어 이동 속도 0 (점프는 속도로 막지 않음 — PlayerMoveEvent로 처리)
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setWalkSpeed(0.0f);
        }

        Bukkit.broadcastMessage("§c§l[!] §f게임 대기 중: 시간과 몹 AI가 정지되었습니다.");
    }

    /** 서버 종료 시 월드룰 복원 */
    public void restore() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setWalkSpeed(0.2f);
        }
    }

    /** 대기 중 플레이어 이동 제한 — 점프(Y축 상승)만 허용 */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (state == State.ACTIVE) return;

        Player p = event.getPlayer();
        // 관리자는 제한 없음
        if (p.hasPermission("nationwar.admin")) return;

        double fromX = event.getFrom().getX();
        double fromZ = event.getFrom().getZ();
        double toX   = event.getTo().getX();
        double toZ   = event.getTo().getZ();

        // XZ 좌표가 바뀌면 이전 위치로 되돌림 (Y축 변화는 허용 = 점프 가능)
        if (Math.abs(toX - fromX) > 0.01 || Math.abs(toZ - fromZ) > 0.01) {
            event.getTo().setX(fromX);
            event.getTo().setZ(fromZ);
        }
    }

    /** 신규 접속 플레이어에게도 대기 상태 적용 */
    public void applyToNewPlayer(Player p) {
        if (state == State.WAITING) {
            p.setWalkSpeed(0.0f);
        }
    }
}