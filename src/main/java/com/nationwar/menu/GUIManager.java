package com.nationwar.menu;

import com.nationwar.NationWar;
import com.nationwar.menu.menulist.*;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class GUIManager {
    private final NationWar plugin;

    public GUIManager(NationWar plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player p) { new MainMenu(plugin).open(p); }
    public void openTeamMenu(Player p) { new TeamMenu(plugin).open(p); }
    public void openTeamColorMenu(Player p) { new TeamColorMenu(plugin).open(p); }
    public void openTeamInviteListMenu(Player p) { new TeamInviteListMenu(plugin).open(p); }
    public void openTeamInviteConfirmMenu(Player p, Player target) { new TeamInviteConfirmMenu(plugin, target).open(p); }
    public void openTeamDeleteConfirmMenu(Player p) { new TeamDeleteConfirmMenu(plugin).open(p); }
    public void openTeamLeaveConfirmMenu(Player p) { new TeamLeaveConfirmMenu(plugin).open(p); }
    public void openCoreMenu(Player p) { new CoreMenu(plugin).open(p); }
    public void openInfoMenu(Player p) { new InfoMenu(plugin).open(p); }

}