package com.nationwar.command;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TeamInfoCommand implements CommandExecutor {
    private final NationWar plugin;
    public TeamInfoCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        // 인자가 있으면 해당 팀 조회, 없으면 내 팀 조회
        String teamName;
        if (args.length > 0) {
            teamName = args[0];
            if (!plugin.getTeamMain().getData().teams.containsKey(teamName)) {
                p.sendMessage("§c존재하지 않는 팀입니다.");
                return true;
            }
        } else {
            teamName = plugin.getTeamMain().getPlayerTeam(p.getUniqueId());
            if (teamName.equals("방랑자")) {
                p.sendMessage("§c소속된 팀이 없습니다.");
                return true;
            }
        }

        List<String> members = plugin.getTeamMain().getData().teams.get(teamName);
        String leaderUUID = plugin.getTeamMain().getData().leaders.get(teamName);
        String colorStr = plugin.getTeamMain().getData().colors.getOrDefault(teamName, "WHITE");

        p.sendMessage("§8§m-----------------------------");
        p.sendMessage("§e§l" + teamName + " §7팀 정보");
        p.sendMessage("§7색상: §f" + colorStr);
        p.sendMessage("§7인원: §f" + members.size() + "명");
        p.sendMessage("§7팀원 목록:");

        for (String uuidStr : members) {
            UUID uuid = UUID.fromString(uuidStr);
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            boolean isOnline = op.isOnline();
            boolean isLeader = uuidStr.equals(leaderUUID);

            String status  = isOnline ? "§a[온라인]" : "§8[오프라인]";
            String role    = isLeader ? " §6[팀장]" : "";
            String name    = op.getName() != null ? op.getName() : uuidStr;

            p.sendMessage("  " + status + " §f" + name + role);
        }
        p.sendMessage("§8§m-----------------------------");
        return true;
    }
}