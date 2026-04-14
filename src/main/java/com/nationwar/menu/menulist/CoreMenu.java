package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import com.nationwar.core.CoreGson;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class CoreMenu {
    private final NationWar plugin;
    public CoreMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "코어 메뉴");
        String team = plugin.getTeamMain().getPlayerTeam(p.getUniqueId());
        List<CoreGson.CoreInfo> cores = plugin.getCoreMain().getCoreData().cores;

        int[] slots = {10, 11, 12, 14, 15, 16};

        for (int i = 0; i < 6; i++) {
            // 데이터가 없을 경우 배리어로 표시하여 에러 방지
            if (i >= cores.size()) {
                inv.setItem(slots[i], createItem(Material.BARRIER, "§c데이터 없음", null));
                continue;
            }

            CoreGson.CoreInfo core = cores.get(i);
            boolean isOwner = core.owner.equals(team);
            Material mat = isOwner ? Material.BEACON : Material.BARRIER;

            List<String> lore = new ArrayList<>();
            lore.add("§7좌표: X: " + (int)core.x + " Y: " + (int)core.y + " Z: " + (int)core.z);
            lore.add(isOwner ? "§a§l[클릭 시 텔레포트 가능]" : "§c§l[점령 시 텔레포트 가능]");

            inv.setItem(slots[i], createItem(mat, "§f코어 " + i, lore));
        }
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n, List<String> lore) {
        ItemStack s = new ItemStack(m);
        ItemMeta mt = s.getItemMeta();
        mt.setDisplayName(n);
        if (lore != null) mt.setLore(lore);
        s.setItemMeta(mt);
        return s;
    }
}