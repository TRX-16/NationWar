package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class TeamInviteListMenu {
    private final NationWar plugin;
    public TeamInviteListMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "팀 초대 메뉴");
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(p)) continue;
            if (plugin.getTeamMain().getPlayerTeam(target.getUniqueId()).equals("방랑자")) {
                if (slot >= 54) break;
                inv.setItem(slot++, getPlayerHead(target));
            }
        }
        p.openInventory(inv);
    }

    private ItemStack getPlayerHead(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(p);
        meta.setDisplayName(p.getName());
        head.setItemMeta(meta);
        return head;
    }
}