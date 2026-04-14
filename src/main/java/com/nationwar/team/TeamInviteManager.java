package com.nationwar.team;

import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamInviteManager {
    private final NationWar plugin;
    private final Map<UUID, String> inviteRequests = new HashMap<>();

    public TeamInviteManager(NationWar plugin) {
        this.plugin = plugin;
    }

    public void sendInvite(UUID target, String teamName) {
        inviteRequests.put(target, teamName);

        // 초대 만료 타이머 (config: tpa.expire-seconds 재사용, 없으면 60초)
        int expireSec = plugin.getConfig().getInt("team.invite-expire-seconds", 60);

        new BukkitRunnable() {
            @Override
            public void run() {
                // 아직 같은 초대가 남아있을 때만 만료 처리
                if (teamName.equals(inviteRequests.get(target))) {
                    inviteRequests.remove(target);
                    Player t = Bukkit.getPlayer(target);
                    if (t != null && t.isOnline()) {
                        t.sendMessage("§7[!] §f" + teamName + " 팀의 초대가 만료되었습니다.");
                    }
                }
            }
        }.runTaskLater(plugin, expireSec * 20L);
    }

    public String getInvite(UUID target) {
        return inviteRequests.get(target);
    }

    public void removeInvite(UUID uuid) {
        inviteRequests.remove(uuid);
    }
}