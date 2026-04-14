package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class TeamInviteConfirmMenu {
    private final NationWar plugin;
    private final Player target;
    public TeamInviteConfirmMenu(NationWar plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "팀 초대 확인 메뉴");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(target);
        sm.setDisplayName(target.getName());
        head.setItemMeta(sm);

        inv.setItem(4, head);
        inv.setItem(19, createItem(Material.LIME_CONCRETE, "§a수락"));
        inv.setItem(25, createItem(Material.RED_CONCRETE, "§c취소"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m); ItemMeta mt = s.getItemMeta(); mt.setDisplayName(n); s.setItemMeta(mt); return s;
    }
}