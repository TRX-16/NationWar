package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamColorMenu {
    private final NationWar plugin;
    public TeamColorMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "팀 색 설정 메뉴");
        inv.setItem(0, createItem(Material.RED_WOOL, "§c빨강 색"));
        inv.setItem(1, createItem(Material.ORANGE_WOOL, "§6주황 색"));
        inv.setItem(2, createItem(Material.YELLOW_WOOL, "§e노랑 색"));
        inv.setItem(3, createItem(Material.LIME_WOOL, "§a연두 색"));
        inv.setItem(4, createItem(Material.GREEN_WOOL, "§2초록 색"));
        inv.setItem(5, createItem(Material.LIGHT_BLUE_WOOL, "§b하늘 색"));
        inv.setItem(6, createItem(Material.BLUE_WOOL, "§9파랑 색"));
        inv.setItem(7, createItem(Material.PURPLE_WOOL, "§5보라 색"));
        inv.setItem(8, createItem(Material.BLACK_WOOL, "§0검정 색"));
        inv.setItem(13, createItem(Material.WHITE_WOOL, "§f하양 색"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m); ItemMeta mt = s.getItemMeta(); mt.setDisplayName(n); s.setItemMeta(mt); return s;
    }
}