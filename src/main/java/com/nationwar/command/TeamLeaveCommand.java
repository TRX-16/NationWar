package com.nationwar.command;

import com.nationwar.NationWar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamLeaveCommand implements CommandExecutor {
    private final NationWar plugin;
    public TeamLeaveCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (plugin.getTeamMain().getPlayerTeam(p.getUniqueId()).equals("방랑자")) {
            p.sendMessage("§c소속된 팀이 없습니다.");
            return true;
        }

        plugin.getGUIManager().openTeamLeaveConfirmMenu(p);
        return true;
    }
}