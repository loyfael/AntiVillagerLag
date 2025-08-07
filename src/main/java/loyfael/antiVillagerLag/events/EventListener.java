package loyfael.antiVillagerLag.events;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import loyfael.antiVillagerLag.AntiVillagerLag;
import loyfael.antiVillagerLag.utils.UpdateChecker;
import loyfael.antiVillagerLag.utils.VillagerUtilities;
import loyfael.antiVillagerLag.utils.VillagerCache;

public class EventListener implements Listener {

    AntiVillagerLag plugin;


    public EventListener(AntiVillagerLag plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (!event.getRightClicked().getType().equals(EntityType.VILLAGER)) return;
        Villager villager = (Villager) event.getRightClicked();

        // Setup new Villagers
        if (!VillagerUtilities.hasMarker(villager, plugin)) {
            VillagerUtilities.setAiCooldown(villager, plugin, 0L);
            VillagerUtilities.setLevelCooldown(villager, plugin, 0L);
            VillagerUtilities.setLastRestock(villager, plugin);
            VillagerUtilities.setMarker(villager, plugin, true);
        }

        // SIMPLE CHECK: Is it on an emerald block?
        boolean block_result = BlockAI.call(villager, plugin, player);

        if (block_result) {
            // ON EMERALD BLOCK → OPTIMIZE AND ALLOW TRADING
            if (VillagerUtilities.getMarker(villager, plugin)) {
                VillagerUtilities.setMarker(villager, plugin, false);
                villager.setAware(false);
                villager.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS,
                    Integer.MAX_VALUE,
                    255,
                    false,
                    false
                ));
                player.sendMessage(VillagerUtilities.colorcodes.cm("&a✅ Villageois stabilisé ! Vous pouvez commercer."));
            }
            // CONTINUE NORMALLY - The villager can be clicked
        } else {
            // NOT ON EMERALD BLOCK → BLOCK COMPLETELY
            if (!VillagerUtilities.getMarker(villager, plugin)) {
                VillagerUtilities.setMarker(villager, plugin, true);
                villager.setAware(true);
                villager.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS);
            }
            player.sendMessage(VillagerUtilities.colorcodes.cm("&4❌ &cPlacez ce villageois sur un &a&lbloc d'émeraude &cpour commercer."));
            event.setCancelled(true);
            return;
        }

        // SIMPLIFIED REST OF CODE (only for villagers on emerald block)
        long currentTime = System.currentTimeMillis() / 1000;
        long vilLevelCooldown = VillagerUtilities.getLevelCooldown(villager, plugin);

        // If the villager is leveling up, block temporarily
        if (vilLevelCooldown > currentTime) {
            String message = plugin.getConfig().getString("messages.cooldown-levelup-message");
            long level_sec = vilLevelCooldown - currentTime;
            message = message.replaceAll("%avlseconds%", Long.toString(level_sec));
            event.getPlayer().sendMessage(VillagerUtilities.colorcodes.cm(message));
            villager.shakeHead();
            event.setCancelled(true);
            return;
        }

        // COMPLETE REMOVAL of manual restock
        // Restock is now handled automatically server-side

        // LAZY RESTOCK: Check and restock automatically if needed
        // Only when a player interacts - ultra-optimized!
        if (!VillagerUtilities.getMarker(villager, plugin)) {
            checkAndRestockIfNeeded(villager);
        }
    }

    /**
     * Checks and restocks a villager only if necessary (optimized lazy approach)
     * Improved performance with cache and player feedback
     */
    private void checkAndRestockIfNeeded(org.bukkit.entity.Villager villager) {
        try {
            long worldTick = villager.getWorld().getFullTime();
            long vilTick = VillagerUtilities.getLastRestock(villager, plugin);

            // Cache to avoid repeated calculations
            Long nextRestockTick = getNextRestockTick(worldTick);
            if (nextRestockTick == null) return; // No restock scheduled

            // Restock only if necessary
            if (worldTick >= nextRestockTick && vilTick < nextRestockTick) {
                VillagerUtilities.restock(villager);
                VillagerUtilities.setLastRestock(villager, plugin);

                // Discrete visual feedback
                villager.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    villager.getLocation().add(0, 2, 0),
                    3, 0.3, 0.3, 0.3, 0
                );
            }
        } catch (Exception e) {
            // Ignore errors silently
        }
    }

    // Cache for next restock to avoid recalculations
    private static long cachedWorldTick = -1;
    private static Long cachedNextRestock = null;

    /**
     * Calculates the next restock tick with caching
     * Avoids repeated calculations for better performance
     */
    private Long getNextRestockTick(long worldTick) {
        // Use cache if data is recent (less than 100 ticks = 5 seconds)
        if (cachedWorldTick >= 0 && Math.abs(worldTick - cachedWorldTick) < 100) {
            return cachedNextRestock;
        }

        long currentDayTick = worldTick % 24000;
        long beginningOfDayTick = worldTick - currentDayTick;

        Long nextRestock = null;
        for (long restockTime : VillagerUtilities.restock_times) {
            long todayRestock = beginningOfDayTick + restockTime;

            if (worldTick < todayRestock) {
                nextRestock = (nextRestock == null) ? todayRestock : Math.min(nextRestock, todayRestock);
            }
        }

        // If no restock today, take the first one tomorrow
        if (nextRestock == null && !VillagerUtilities.restock_times.isEmpty()) {
            nextRestock = beginningOfDayTick + 24000 + VillagerUtilities.restock_times.get(0);
        }

        // Cache it
        cachedWorldTick = worldTick;
        cachedNextRestock = nextRestock;

        return nextRestock;
    }

    // Optimized events with priority and fast filtering
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void inventoryMove(InventoryClickEvent event) {
        // Quick checks first
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!plugin.getConfig().getBoolean("toggleableoptions.preventtrading")) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        // REAL-TIME SECURITY: Check villager position on every trading click
        boolean block_result = BlockAI.call(vil, plugin, player);
        if (!block_result) { // FALSE = the villager is NOT on an emerald block (not optimized)
            // The villager is not optimized, block the trade
            VillagerUtilities.setMarker(vil, plugin, true);
            vil.setAware(true);
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(VillagerUtilities.colorcodes.cm("&c🚫 Le villageois doit être sur un bloc d'émeraude pour commercer !"));
            return;
        }

        // Additional check with marker - only optimized villagers (marker=false) can trade
        if (VillagerUtilities.hasMarker(vil, plugin) && VillagerUtilities.getMarker(vil, plugin)) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(VillagerUtilities.colorcodes.cm("&c🚫 Ce villageois doit être stabilisé pour commercer. Placez-le sur un bloc d'émeraude."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void villagerTradeClick(TradeSelectEvent event) {
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!plugin.getConfig().getBoolean("toggleableoptions.preventtrading")) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        // REAL-TIME SECURITY: Check villager position on every trade selection
        boolean block_result = BlockAI.call(vil, plugin, player);
        if (!block_result) { // FALSE = the villager is NOT on an emerald block (not optimized)
            // The villager is not optimized, block the trade
            VillagerUtilities.setMarker(vil, plugin, true);
            vil.setAware(true);
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(VillagerUtilities.colorcodes.cm("&c⚡ Les vents perturbent les échanges ! Émeraude céleste requise."));
            return;
        }

        // Additional check with marker - only optimized villagers (marker=false) can trade
        if (VillagerUtilities.hasMarker(vil, plugin) && VillagerUtilities.getMarker(vil, plugin)) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(VillagerUtilities.colorcodes.cm("&c⚡ Ce marcheur n'est pas optimisé dans Nuvalis !"));
        }
    }

    // Critical optimization - use cache instead of PersistentData
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCancelVillagerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Villager && event.getDamager() instanceof Zombie)) return;

        Villager vil = (Villager) event.getEntity();
        VillagerCache.VillagerData data = VillagerCache.getVillagerData(vil, plugin);

        // Protect only optimized villagers (AI disabled)
        if (data != null && !data.aiState) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("avl.notify.update")) return;
        new UpdateChecker(plugin, 102949).getVersion(version -> {
            if (plugin.getDescription().getVersion().equals(version)) {
                event.getPlayer().sendMessage(ChatColor.GREEN + "AntiVillagerLag is up to date!");
            } else {
                event.getPlayer().sendMessage(ChatColor.GREEN + "There is an update for AntiVillagerLag! https://www.spigotmc.org/resources/antivillagerlag.102949/");
            }
        });
    }

    // Event to handle Villager updating
    @EventHandler
    public void afterTrade(InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();
        if(player.hasPermission("avl.disable"))
            return;
        // check if inventory belongs to a Villager Trade Screen
        if (event.getInventory().getHolder() == null) return;
        if (event.getInventory().getHolder() instanceof WanderingTrader) return;
        if(event.getInventory().getType() != InventoryType.MERCHANT) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        // make sure the villager is disabled
        if (!VillagerUtilities.hasMarker(vil, plugin)) return;
        if (VillagerUtilities.getMarker(vil, plugin)) return;

        // handle leveling
        VillagerLevelManager.call(vil, plugin, player);
    }

}
