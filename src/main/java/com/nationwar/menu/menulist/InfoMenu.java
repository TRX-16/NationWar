package com.nationwar.menu.menulist;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoMenu {
    private final NationWar plugin;
    public InfoMenu(NationWar plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "정보 메뉴");

        // 플레이 가이드 - config에서 동적으로 읽기
        int startHour  = plugin.getConfig().getInt("capture-time.start-hour", 19);
        int endHour    = plugin.getConfig().getInt("capture-time.end-hour", 22);
        int coreCount  = plugin.getConfig().getInt("core.count", 6);
        List<String> days = plugin.getConfig().getStringList("capture-time.days");
        String dayStr = String.join("/", days.stream()
                .map(d -> translateDay(d))
                .toArray(String[]::new));

        ItemStack guide = createItem(Material.KNOWLEDGE_BOOK, "§a§l플레이 가이드");
        ItemMeta gm = guide.getItemMeta();
        gm.setLore(Arrays.asList(
                "§71. §f/팀 <이름>으로 국가를 세우세요.",
                "§72. §f팀원을 모집하고 §e국가창고§f를 활용하세요.",
                "§73. §6" + dayStr + " " + startHour + ":00~" + endHour + ":00§f에 코어를 공격하세요.",
                "§74. §f모든 코어(§e" + coreCount + "§f개)를 차지하면 승리합니다!"
        ));
        guide.setItemMeta(gm);

        // 명령어 정보
        ItemStack commands = createItem(Material.DEBUG_STICK, "§b명령어 정보");
        ItemMeta cm = commands.getItemMeta();
        cm.setLore(Arrays.asList(
                "§f/메뉴 §7- 메인 메뉴 열기",
                "§f/팀 <이름> §7- 팀 생성",
                "§f/팀탈퇴 §7- 팀 탈퇴",
                "§f/팀정보 [팀이름] §7- 팀원 목록 조회",
                "§f/tpa <닉네임> §7- 팀원에게 텔레포트 요청",
                "§f/국가창고 §7- 팀 공용 창고 열기"
        ));
        commands.setItemMeta(cm);

        inv.setItem(10, guide);
        inv.setItem(13, commands);
        inv.setItem(16, createItem(Material.COMMAND_BLOCK, "§d제작자. Mintina_"));
        p.openInventory(inv);
    }

    private String translateDay(String day) {
        switch (day.toUpperCase()) {
            case "MONDAY":    return "월";
            case "TUESDAY":   return "화";
            case "WEDNESDAY": return "수";
            case "THURSDAY":  return "목";
            case "FRIDAY":    return "금";
            case "SATURDAY":  return "토";
            case "SUNDAY":    return "일";
            default:          return day;
        }
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack s = new ItemStack(m);
        ItemMeta mt = s.getItemMeta();
        mt.setDisplayName(n);
        s.setItemMeta(mt);
        return s;
    }
}