package com.nationwar.listeners;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final NationWar plugin;

    public JoinListener(NationWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // 0.5초 뒤에 실행 (플레이어 로딩 시간 고려)
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    plugin.getTeamMain().updateDisplay(online);
                }
                // 대기 상태면 신규 접속자에게도 이동 제한 적용
                plugin.getGameState().applyToNewPlayer(event.getPlayer());
            }
        }.runTaskLater(plugin, 10L);
    }
}