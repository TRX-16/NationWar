package com.nationwar.command;

import com.nationwar.NationWar;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Random;

public class GamestartCommand implements CommandExecutor {
    private final NationWar plugin;
    public GamestartCommand(NationWar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // OP이거나 해당 권한이 있어야만 실행 가능
        if (!sender.hasPermission("nationwar.admin")) {
            sender.sendMessage(TextComponent.fromLegacyText("§c§l[!] §c권한이 부족합니다."));
            return true;

        }


        World world = Bukkit.getWorlds().get(0);
        int borderSize = plugin.getConfig().getInt("world.border-size", 15000);
        int spawnRange = plugin.getConfig().getInt("world.spawn-range", 2000);

        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(borderSize);

        Random random = new Random();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc;
            while (true) {
                int x = random.nextInt(spawnRange * 2 + 1) - spawnRange;
                int z = random.nextInt(spawnRange * 2 + 1) - spawnRange;
                int y = world.getHighestBlockYAt(x, z);
                loc = new Location(world, x + 0.5, y + 1, z + 0.5);

                // 기준서: 주변 4칸 이내에 용암이 없는 안전한 곳
                boolean safe = true;
                for(int dx=-2; dx<=2; dx++) {
                    for(int dz=-2; dz<=2; dz++) {
                        if(loc.clone().add(dx, -1, dz).getBlock().getType() == Material.LAVA) safe = false;
                    }
                }
                if (safe) break;
            }
            p.teleport(loc);
        }
        startCaptureEvent();
        plugin.getCoreMain().LoadCores(); // 코어 생성 로직 호출
        return true;
    }

    public void startCaptureEvent() {
        new BukkitRunnable() {
            int count = 5;

            @Override
            public void run() {
                if (count > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§c§l" + count, "§f전쟁 시작까지...", 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                    }
                } else {
                    // 전쟁 시작 실제 연출 (레이드 혼 효과음은 여기서만)
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 1.0f);
                    }

                    // config 기반 안내 메시지 (broadcastStartMessage 위임)
                    plugin.getCoreMain().broadcastStartMessage();

                    plugin.getGameState().activate();
                    plugin.getCoreMain().setGameStarted(true);
                    this.cancel();
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1초 간격 실행
    }

}