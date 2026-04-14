package com.nationwar.command;

import com.nationwar.NationWar;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TeamChestCommand implements CommandExecutor {
    private final NationWar plugin;
    public TeamChestCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        String team = plugin.getTeamMain().getPlayerTeam(p.getUniqueId());

        if (team.equals("방랑자")) {
            p.sendMessage(TextComponent.fromLegacyText("§c§l[!] §c팀이 없는 방랑자는 국가 창고를 사용할 수 없습니다."));
            return true;
        }

        Inventory inv = plugin.getTeamMain().getTeamChest().open(p, team);

        if (inv != null) {
            p.openInventory(inv);
            p.sendMessage(TextComponent.fromLegacyText("§e§l[!] §f국가 창고를 열었습니다. §7(닫을 때 자동 저장)"));
        }
        return true;
    }
}