package com.nationwar.listeners;

import com.nationwar.NationWar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InventoryCloseListener implements Listener {
    private final NationWar plugin;

    public InventoryCloseListener(NationWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        // "국가 창고" 타이틀 확인
        if (title.contains("국가 창고")) {
            Player player = (Player) event.getPlayer();

            // 타이틀에서 팀 이름 추출 (§0팀이름 국가 창고 -> 팀이름)
            String teamName = title.replace("§0", "").replace(" 국가 창고", "");

            // 1. 데이터 업데이트 및 저장 & 잠금 해제 & 로그 기록
            // 아까 TeamChest에 만든 close 메서드를 호출합니다.
            plugin.getTeamMain().getTeamChest().close(player, teamName, event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String teamName = plugin.getTeamMain().getPlayerTeam(player.getUniqueId());

        // 창고를 열어둔 채로 나갔다면 현재 인벤토리 저장 후 잠금 해제
        if (plugin.getTeamMain().isStorageLocked(teamName)) {
            // 열려있는 인벤토리가 해당 팀 창고인지 확인 후 저장
            if (player.getOpenInventory() != null) {
                String title = player.getOpenInventory().getTitle();
                if (title.contains("국가 창고")) {
                    plugin.getTeamMain().getTeamChest().close(player, teamName, player.getOpenInventory().getTopInventory());
                    plugin.getLogger().warning("[Storage] " + player.getName() + "님이 창고 사용 중 퇴장 — 저장 후 잠금 해제 완료.");
                    return;
                }
            }
            // 인벤토리가 닫혀있는데 잠금만 남은 경우 (비정상 상태) 잠금만 해제
            plugin.getTeamMain().unlockStorage(teamName);
            plugin.getLogger().warning("[Storage] " + player.getName() + "님 퇴장 — 비정상 잠금 강제 해제.");
        }
    }
}