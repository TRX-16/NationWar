package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenu {
    private final NationWar plugin;
    public MainMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "메인 메뉴");
        inv.setItem(10, createItem(Material.CHEST, "§f팀 메뉴"));
        inv.setItem(13, createItem(Material.BEACON, "§f코어 메뉴"));
        inv.setItem(16, createItem(Material.BOOK, "§f정보 메뉴"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m); ItemMeta mt = s.getItemMeta(); mt.setDisplayName(n); s.setItemMeta(mt); return s;
    }
}