package com.nationwar.command;

import com.nationwar.NationWar;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TpaCommand implements CommandExecutor {
    private final NationWar plugin;
    public TpaCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length < 1) return false;

        // --- 수락 로직 ---
        if (args[0].equals("수락")) {
            UUID requesterUUID = plugin.getTpaMain().getRequest(p.getUniqueId());
            if (requesterUUID == null) {
                p.sendMessage(TextComponent.fromLegacyText("§c받은 TPA 요청이 없습니다."));
                return true;
            }

            Player requester = Bukkit.getPlayer(requesterUUID);
            if (requester != null && requester.isOnline()) {
                requester.teleport(p.getLocation()); // 텔레포트 실행
                requester.sendMessage(TextComponent.fromLegacyText("§a" + p.getName() + " 님이 TPA를 수락했습니다."));
                p.sendMessage(TextComponent.fromLegacyText("§a" + requester.getName() + " 님을 내 위치로 이동시켰습니다."));
            }
            plugin.getTpaMain().removeRequest(p.getUniqueId());
            return true;
        }

        if (args[0].equals("거절")) {
            plugin.getTpaMain().removeRequest(p.getUniqueId());
            p.sendMessage(TextComponent.fromLegacyText("§cTPA 요청을 거절했습니다."));
            return true;
        }

        // --- 요청 보내기 로직 ---
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(TextComponent.fromLegacyText("§c해당 플레이어를 찾을 수 없습니다."));
            return true;
        }

        if (!plugin.getTeamMain().sameTeam(p, target)) {
            p.sendMessage(TextComponent.fromLegacyText("§c팀원에게만 tpa를 보낼 수 있습니다."));
            return true;
        }

        long cooldown = plugin.getTpaMain().getRemainCoolDown(p.getUniqueId());
        if (cooldown > 0) {
            p.sendMessage(TextComponent.fromLegacyText("§c쿨타임이 " + (cooldown / 1000 / 60) + "분 남았습니다."));
            return true;
        }

        plugin.getTpaMain().sendRequest(p, target);

        TextComponent msg = new TextComponent("§e" + p.getName() + "님의 TPA 요청: ");

        TextComponent accept = new TextComponent("§a[수락] ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa 수락"));

        TextComponent deny = new TextComponent("§c[거절]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa 거절"));

        msg.addExtra(accept);
        msg.addExtra(deny);

        target.spigot().sendMessage(msg);
        p.sendMessage(TextComponent.fromLegacyText("§a" + target.getName() + " 님에게 TPA 요청을 보냈습니다."));

        return true;
    }
}