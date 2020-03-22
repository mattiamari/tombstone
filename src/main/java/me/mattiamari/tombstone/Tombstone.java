package me.mattiamari.tombstone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Tombstone extends JavaPlugin
{
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }

    private class DeathListener implements Listener {

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            Location loc = event.getEntity().getLocation();
            String msg = String.format("Sei morto qui (X Y Z) -> " + ChatColor.YELLOW + "%.0f  %.0f  %.0f",
                                       loc.getX(), loc.getY(), loc.getZ());
            
            event.setDeathMessage(event.getDeathMessage() + "."
                + ChatColor.GRAY + ChatColor.ITALIC + " Press F to Pay Respects");

            getLogger().info(String.format("%s died at %.0f %.0f %.0f",
                event.getEntity().getName(),
                loc.getX(), loc.getY(), loc.getZ()));
            
            // Save drop to put it in a chest later
            ItemStack[] drop = (ItemStack[])event.getDrops().toArray(new ItemStack[0]);

            BukkitScheduler scheduler = Bukkit.getScheduler();
            Block deathBlock = event.getEntity().getWorld().getBlockAt(loc);

            Runnable placeTombstone = new Runnable() {
                @Override
                public void run() {
                    deathBlock.setType(Material.POTTED_WHITE_TULIP);
                    deathBlock.getRelative(BlockFace.NORTH).setType(Material.TORCH);
                }
            };

            Runnable placeTombstoneWithChest = new Runnable() {
                @Override
                public void run() {
                    deathBlock.setType(Material.CHEST);
                    deathBlock.getRelative(BlockFace.EAST).setType(Material.POTTED_WHITE_TULIP);
                    deathBlock.getRelative(BlockFace.WEST).setType(Material.TORCH);
                    Chest chest = Chest.class.cast(deathBlock.getState());
                    chest.getInventory().setContents(drop);
                }
            };

            event.getEntity().sendMessage(msg);

            if (drop.length > 0) {
                event.getDrops().clear();
                scheduler.scheduleSyncDelayedTask(Tombstone.this, placeTombstoneWithChest);
            } else {
                scheduler.scheduleSyncDelayedTask(Tombstone.this, placeTombstone);
            } 
        }
    }
}
