package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamMenu {
    private final NationWar plugin;
    public TeamMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "팀 메뉴");
        String team = plugin.getTeamMain().getPlayerTeam(p.getUniqueId());

        if (team.equals("방랑자")) {
            p.sendMessage("§c소속된 팀이 없습니다. 먼저 팀을 만들거나 초대를 수락하세요.");
            return;
        }

        if (plugin.getTeamMain().isLeader(team, p)) {
            // 팀장: 색상 설정 / 초대 / 삭제 / 탈퇴
            inv.setItem(10, createItem(Material.CYAN_DYE,    "§f팀 색 설정 메뉴"));
            inv.setItem(13, createItem(Material.PLAYER_HEAD, "§f플레이어 리스트 메뉴"));
            inv.setItem(16, createItem(Material.BARRIER,     "§f팀 삭제 확인 메뉴"));
            inv.setItem(22, createItem(Material.OAK_DOOR,    "§c팀 탈퇴"));
        } else {
            // 일반 팀원: 창고 / 탈퇴
            inv.setItem(11, createItem(Material.ENDER_CHEST, "§f국가창고"));
            inv.setItem(15, createItem(Material.OAK_DOOR,    "§c팀 탈퇴"));
        }
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m); ItemMeta mt = s.getItemMeta(); mt.setDisplayName(n); s.setItemMeta(mt); return s;
    }
}