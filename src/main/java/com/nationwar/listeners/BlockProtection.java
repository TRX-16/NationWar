package com.nationwar.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockProtection implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        // 기준서: 코어 플랫폼(화이트 콘크리트) 보호
        if (event.getBlock().getType() == Material.WHITE_CONCRETE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        // 기준서: 폭발 시 화이트 콘크리트 보호
        event.blockList().removeIf(block -> block.getType() == Material.WHITE_CONCRETE);
    }
}