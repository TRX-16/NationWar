package com.nationwar.command;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;


public class GameContinueCommand implements CommandExecutor {
    private final NationWar plugin;
    public GameContinueCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nationwar.admin")) {
            sender.sendMessage(TextComponent.fromLegacyText("§c§l[!] §c권한이 부족합니다."));
            return true;
        }

        plugin.getCoreMain().respawnAllCores();
        plugin.getGameState().activate();
        plugin.getCoreMain().setGameStarted(true);

        // 수정: fromLegacyText 적용
        sender.sendMessage(TextComponent.fromLegacyText("§a§l[!] §f코어 복구 및 보호막 해제가 완료되었습니다."));
        Bukkit.broadcastMessage("§6§l[!] §f국가전쟁이 재개됩니다.");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
        return true;
    }
}