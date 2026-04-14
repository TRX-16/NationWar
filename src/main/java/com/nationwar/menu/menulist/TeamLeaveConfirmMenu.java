package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamLeaveConfirmMenu {
    private final NationWar plugin;
    public TeamLeaveConfirmMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "팀 탈퇴 확인 메뉴");
        inv.setItem(4,  createItem(Material.BARRIER,        "§f정말 팀에서 나가시겠습니까?"));
        inv.setItem(19, createItem(Material.LIME_CONCRETE,  "§a확인"));
        inv.setItem(25, createItem(Material.RED_CONCRETE,   "§c취소"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String name) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}