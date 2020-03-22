package me.mattiamari.tombstone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.DoubleChestInventory;
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
            String msg = String.format("Sei morto qui (X Y Z) -> " + ChatColor.YELLOW + "%d  %d  %d",
                                       loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            
            event.setDeathMessage(event.getDeathMessage() + "."
                + ChatColor.GRAY + ChatColor.ITALIC + " Press F to Pay Respects");

            getLogger().info(String.format("%s died at %d %d %d",
                event.getEntity().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            
            // Save drop to put it in a chest later
            ItemStack[] drop = (ItemStack[])event.getDrops().toArray(new ItemStack[event.getDrops().size()]);

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

            Runnable placeTombstoneWithDoubleChest = new Runnable() {
                @Override
                public void run() {
                    deathBlock.setType(Material.CHEST);
                    deathBlock.getRelative(BlockFace.EAST).setType(Material.CHEST);
                    deathBlock.getRelative(BlockFace.NORTH).setType(Material.TORCH);
                    deathBlock.getRelative(BlockFace.NORTH_EAST).setType(Material.POTTED_WHITE_TULIP);

                    Chest chestLeft = Chest.class.cast(deathBlock.getState());
                    Chest chestRight = Chest.class.cast(deathBlock.getRelative(BlockFace.EAST).getState());

                    org.bukkit.block.data.type.Chest chestLeftData = (org.bukkit.block.data.type.Chest)chestLeft.getBlockData();
                    org.bukkit.block.data.type.Chest chestRightData = (org.bukkit.block.data.type.Chest)chestRight.getBlockData();

                    chestLeftData.setType(Type.LEFT);
                    chestRightData.setType(Type.RIGHT);

                    chestLeft.setBlockData(chestLeftData);
                    chestRight.setBlockData(chestRightData);

                    chestLeft.update();
                    chestRight.update();

                    ((DoubleChestInventory)chestLeft.getInventory()).setContents(drop);
                }
            };

            event.getEntity().sendMessage(msg);

            if (drop.length == 0) {
                scheduler.scheduleSyncDelayedTask(Tombstone.this, placeTombstone);
            } else if (drop.length <= 27) {
                event.getDrops().clear();
                scheduler.scheduleSyncDelayedTask(Tombstone.this, placeTombstoneWithChest);
            } else {
                event.getDrops().clear();
                scheduler.scheduleSyncDelayedTask(Tombstone.this, placeTombstoneWithDoubleChest);
            }
        }
    }
}
