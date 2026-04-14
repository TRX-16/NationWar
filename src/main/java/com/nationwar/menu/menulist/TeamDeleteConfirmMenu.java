package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamDeleteConfirmMenu {
    private final NationWar plugin;
    public TeamDeleteConfirmMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "팀 삭제 확인 메뉴");
        inv.setItem(4, createItem(Material.IRON_SWORD, "§f팀 삭제 확인"));
        inv.setItem(19, createItem(Material.LIME_CONCRETE, "§a확인"));
        inv.setItem(25, createItem(Material.RED_CONCRETE, "§c취소"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m); ItemMeta mt = s.getItemMeta(); mt.setDisplayName(n); s.setItemMeta(mt); return s;
    }
}