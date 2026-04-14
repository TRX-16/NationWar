package com.nationwar.listeners;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpListener implements Listener {
    private final NationWar plugin;

    // 킬뎃 추적: UUID -> [kills, deaths]
    private final Map<UUID, int[]> kd = new HashMap<>();

    public PvpListener(NationWar plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        // 기준서: 같은 팀원끼리는 서로 데미지를 입힐 수 없다.
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (plugin.getTeamMain().sameTeam(victim, attacker)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // 피해자 데스 카운트
        int[] victimKD = kd.computeIfAbsent(victim.getUniqueId(), k -> new int[]{0, 0});
        victimKD[1]++;

        String victimTeam = plugin.getTeamMain().getPlayerTeam(victim.getUniqueId());
        String victimDisplay = "§7[" + victimTeam + "] §f" + victim.getName()
                + " §7(" + victimKD[0] + "킬/" + victimKD[1] + "데스)";

        if (killer != null) {
            // 킬러 킬 카운트
            int[] killerKD = kd.computeIfAbsent(killer.getUniqueId(), k -> new int[]{0, 0});
            killerKD[0]++;

            String killerTeam = plugin.getTeamMain().getPlayerTeam(killer.getUniqueId());
            String killerDisplay = "§e[" + killerTeam + "] §f" + killer.getName()
                    + " §7(" + killerKD[0] + "킬/" + killerKD[1] + "데스)";

            Bukkit.broadcastMessage("§8[킬로그] " + killerDisplay + " §c⚔ §r" + victimDisplay);
        } else {
            // 비전투 사망 (자연사 등)
            Bukkit.broadcastMessage("§8[킬로그] " + victimDisplay + " §7이(가) 사망했습니다.");
        }
    }

    /** 게임 종료 시 KD 초기화 */
    public void resetKD() { kd.clear(); }

    /** 특정 플레이어 KD 조회 (외부 접근용) */
    public int[] getKD(UUID uuid) {
        return kd.getOrDefault(uuid, new int[]{0, 0});
    }
}