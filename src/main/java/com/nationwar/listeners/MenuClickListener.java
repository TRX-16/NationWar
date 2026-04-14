package com.nationwar.listeners;

import com.nationwar.NationWar;
import com.nationwar.core.CoreGson;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuClickListener implements Listener {
    private final NationWar plugin;
    private final Map<UUID, Location> tpWait = new HashMap<>();

    public MenuClickListener(NationWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("메뉴") && !title.contains("확인")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player p = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        String team = plugin.getTeamMain().getPlayerTeam(p.getUniqueId());

        // 1. 메인 메뉴 (슬롯: 10, 13, 16)
        if (title.equals("메인 메뉴")) {
            if (slot == 10) plugin.getGUIManager().openTeamMenu(p);
            else if (slot == 13) plugin.getGUIManager().openCoreMenu(p);
            else if (slot == 16) plugin.getGUIManager().openInfoMenu(p);
        }

        // 2. 팀 메뉴 (슬롯: 10, 13, 16, 22 / 11, 15)
        else if (title.equals("팀 메뉴")) {
            if (plugin.getTeamMain().isLeader(team, p)) {
                if (slot == 10) plugin.getGUIManager().openTeamColorMenu(p);
                else if (slot == 13) plugin.getGUIManager().openTeamInviteListMenu(p);
                else if (slot == 16) plugin.getGUIManager().openTeamDeleteConfirmMenu(p);
                else if (slot == 22) plugin.getGUIManager().openTeamLeaveConfirmMenu(p);
            } else {
                if (slot == 11) p.performCommand("국가창고");
                else if (slot == 15) plugin.getGUIManager().openTeamLeaveConfirmMenu(p);
            }
        }

        // 3. 팀 색 설정 메뉴 (슬롯: 0~8, 13)
        else if (title.equals("팀 색 설정 메뉴")) {
            if (slot >= 0 && slot <= 8 || slot == 13) {
                // Material 이름 파싱 대신 아이템 displayName → ChatColor 직접 매핑 (LIGHT_BLUE 등 예외 방지)
                String colorName = resolveColorName(event.getCurrentItem().getType());
                if (colorName == null) return;

                plugin.getTeamMain().changeColor(team, colorName);
                p.sendMessage("§a팀 색상이 변경되었습니다.");
                p.closeInventory();
            }
        }

        // 4. 팀 초대 리스트 (플레이어 헤드 클릭)
        else if (title.equals("팀 초대 메뉴")) {
            ItemStack item = event.getCurrentItem();
            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta.getOwningPlayer() != null) {
                    Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                    if (target != null) plugin.getGUIManager().openTeamInviteConfirmMenu(p, target);
                }
            }
        }

        // 5. 팀 초대 확인 (슬롯: 19-수락, 25-취소)
        else if (title.equals("팀 초대 확인 메뉴")) {
            ItemStack head = event.getInventory().getItem(4);
            if (head == null || !(head.getItemMeta() instanceof SkullMeta)) return;
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta.getOwningPlayer() == null) return;
            Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());

            if (slot == 19 && target != null) { // [확인] 버튼 클릭 시
                if (plugin.getTeamInviteManager() == null) {
                    p.sendMessage("§c시스템 오류: 초대 매니저가 로드되지 않았습니다.");
                    return;
                }

                // 1. 데이터 저장
                plugin.getTeamInviteManager().sendInvite(target.getUniqueId(), team);

                // 2. 수락/거절 버튼이 포함된 통합 메시지 전송 (이 메서드 하나면 충분합니다)
                sendInviteMessage(target, team);

                p.sendMessage("§a" + target.getName() + "님에게 초대장을 보냈습니다.");
                p.closeInventory();
            } else if (slot == 25) {
                p.closeInventory();
            }
        }

        // 6. 팀 삭제 확인 (슬롯: 19-확인, 25-취소)
        else if (title.equals("팀 삭제 확인 메뉴")) {
            if (slot == 19) {
                plugin.getTeamMain().deleteTeam(team);
                p.sendMessage("§c팀이 삭제되었습니다.");
                p.closeInventory();
            } else if (slot == 25) {
                plugin.getGUIManager().openTeamMenu(p);
            }
        }

        // 7. 팀 탈퇴 확인 (슬롯: 19-확인, 25-취소)
        else if (title.equals("팀 탈퇴 확인 메뉴")) {
            if (slot == 19) {
                plugin.getTeamMain().leaveTeam(p);
                p.closeInventory();
            } else if (slot == 25) {
                p.closeInventory();
            }
        }

        // 7. 코어 메뉴 - 동적 슬롯 처리
        else if (title.equals("코어 메뉴")) {
            List<CoreGson.CoreInfo> cores = plugin.getCoreMain().getCoreData().cores;
            // 클릭한 슬롯에 아이템이 있고 BEACON이면 텔레포트 (내 팀 코어)
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.BEACON) {
                // 아이템 이름에서 코어 번호 추출 ("§f코어 N")
                String displayName = clicked.getItemMeta().getDisplayName();
                try {
                    int coreId = Integer.parseInt(displayName.replaceAll("§.", "").replace("코어 ", "").trim());
                    if (coreId >= 0 && coreId < cores.size()) {
                        handleCoreTeleport(p, coreId);
                    }
                } catch (NumberFormatException ignored) {}
            } else if (clicked != null && clicked.getType() == Material.BARRIER) {
                p.sendMessage("§c해당 코어를 소유하고 있지 않습니다.");
            }
        }
    }

    public void sendInviteMessage(Player target, String teamName) {
        target.sendMessage("");
        target.sendMessage("  §6§l[!] §e" + teamName + " §f팀의 초대가 도착했습니다!");

        // 1. 수락 버튼
        TextComponent accept = new TextComponent("  §a§l[ 수락 ]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/팀 수락"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f클릭 시 팀에 합류하고 팀장에게 이동합니다.").create()));

        // 2. 공백 (버튼 사이 간격)
        TextComponent space = new TextComponent("    ");

        // 3. 거절 버튼
        TextComponent deny = new TextComponent("§c§l[ 거절 ]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/팀 거절"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f초대를 무시합니다.").create()));

        // 4. 합쳐서 보내기 (이게 핵심입니다!)
        accept.addExtra(space);
        accept.addExtra(deny);

        target.spigot().sendMessage(accept);
        target.sendMessage("");
    }

    private String resolveColorName(Material material) {
        switch (material) {
            case RED_WOOL:        return "RED";
            case ORANGE_WOOL:     return "GOLD";
            case YELLOW_WOOL:     return "YELLOW";
            case LIME_WOOL:       return "GREEN";
            case GREEN_WOOL:      return "DARK_GREEN";
            case LIGHT_BLUE_WOOL: return "AQUA";
            case BLUE_WOOL:       return "BLUE";
            case PURPLE_WOOL:     return "DARK_PURPLE";
            case BLACK_WOOL:      return "BLACK";
            case WHITE_WOOL:      return "WHITE";
            default:              return null;
        }
    }

    private void handleCoreTeleport(Player p, int coreId) {
        if (coreId >= plugin.getCoreMain().getCoreData().cores.size()) return;

        CoreGson.CoreInfo core = plugin.getCoreMain().getCoreData().cores.get(coreId);
        Location target = new Location(p.getWorld(), core.x + 0.5, core.y + 1, core.z + 0.5);
        UUID uuid = p.getUniqueId();

        tpWait.put(uuid, p.getLocation());
        p.closeInventory();
        p.sendMessage("§a10초간 움직이지 마십시오. 텔레포트가 시작됩니다.");

        new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (!p.isOnline() || !tpWait.containsKey(uuid)) {
                    this.cancel();
                    return;
                }

                // 기준서: 10초간 움직이지 않아야 함 (0.1블록 이상 이동 시 취소)
                if (p.getLocation().distance(tpWait.get(uuid)) > 0.1) {
                    p.sendMessage("§c움직임이 감지되어 텔레포트가 취소되었습니다.");
                    tpWait.remove(uuid);
                    this.cancel();
                    return;
                }
                if (count > 0) {
                    p.sendTitle("§a§l" + count, "§f움직이지 마세요! 이동 중...", 0, 21, 0);
                    p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                }

                if (count <= 0) {
                    p.teleport(target);
                    p.sendTitle("§a§l이동 완료", "§f코어 지점에 도착했습니다.", 10, 20, 10);
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    tpWait.remove(uuid);
                    this.cancel();
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}