package loyfael.antiVillagerLag.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.inventory.ItemStack;
import loyfael.antiVillagerLag.AntiVillagerLag;
import loyfael.antiVillagerLag.utils.ActionBarUtils;
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
        //  It's a villager
        if (!event.getRightClicked().getType().equals(EntityType.VILLAGER)) return;
        Villager villager = (Villager) event.getRightClicked();

        //  Setup new Villagers
        if (!VillagerUtilities.hasMarker(villager, plugin)) {
            VillagerUtilities.setAiCooldown(villager, plugin, 0L);
            VillagerUtilities.setLevelCooldown(villager, plugin, 0L);
            VillagerUtilities.setLastRestock(villager, plugin);
            VillagerUtilities.setMarker(villager, plugin, true);
        }

        //  Get Times
        long currentTime = System.currentTimeMillis() / 1000;
        long vilLevelCooldown = VillagerUtilities.getLevelCooldown(villager, plugin);
        long vilAiCooldown = VillagerUtilities.getAiCooldown(villager, plugin);
        long totalSeconds = vilAiCooldown - currentTime;
        long sec = totalSeconds % 60;
        long min = totalSeconds / 60;

        //  If the villager is leveling up
        if (vilLevelCooldown > currentTime) {
            String message = plugin.getConfig().getString("messages.cooldown-levelup-message");
            long level_sec = vilLevelCooldown - currentTime;
            message = message.replaceAll("%avlseconds%", Long.toString(level_sec));
            event.getPlayer().sendMessage(VillagerUtilities.colorcodes.cm(message));
            villager.shakeHead();
            event.setCancelled(true);
            return;
        }

        //  Should it be disabled?
        boolean nametag_result = NameTagAI.call(villager, plugin, player);
        boolean block_result = BlockAI.call(villager, plugin, player);
        boolean workblock_result = WorkblockAI.call(villager, plugin, player);
        boolean should_be_disabled = nametag_result || block_result || workblock_result;


        //  If villager AI is being toggled
        if (should_be_disabled == VillagerUtilities.getMarker(villager, plugin)) {
            //  If toggling is on cooldown
            if ((vilAiCooldown > currentTime) && !player.hasPermission("avl.cooldown.bypass")) {
                //Tell player it's on cooldown with action bar
                ActionBarUtils.sendCooldownInfo(player, min, sec, plugin);
                String message = plugin.getConfig().getString("messages.cooldown-ai-message");
                message = message.replaceAll("%avlminutes%", Long.toString(min));
                message = message.replaceAll("%avlseconds%", Long.toString(sec));
                event.getPlayer().sendMessage(VillagerUtilities.colorcodes.cm(message));
                event.setCancelled(true);
            //  If cooldown is over
            } else {
                boolean wasOptimized = VillagerUtilities.getMarker(villager, plugin);
                VillagerUtilities.setMarker(villager, plugin, !should_be_disabled);
                villager.setAware(!should_be_disabled);
                VillagerUtilities.setAiCooldown(villager, plugin, plugin.getConfig().getLong("ai-toggle-cooldown"));

                // Show visual and audio feedback
                showVillagerStateChange(villager, player, wasOptimized);

                // Enhanced feedback messages with action bar
                ActionBarUtils.sendVillagerStatus(player, villager, plugin);
                if (!should_be_disabled) {
                    // Villager was optimized, now being unoptimized (activated)
                    player.sendMessage(VillagerUtilities.colorcodes.cm(plugin.getConfig().getString("messages.villager-unoptimized")));
                    player.sendMessage(VillagerUtilities.colorcodes.cm(plugin.getConfig().getString("messages.villager-breeding-reminder")));
                } else {
                    // Villager was active, now being optimized (disabled)
                    player.sendMessage(VillagerUtilities.colorcodes.cm(plugin.getConfig().getString("messages.villager-optimized")));
                    player.sendMessage(VillagerUtilities.colorcodes.cm(plugin.getConfig().getString("messages.villager-performance-tip")));
                }

                //  If nametag shouldn't be consumed, give one back
                if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG) && !plugin.getConfig().getBoolean("toggleableoptions.usenametags")) {
                    ItemStack nametag = player.getInventory().getItemInMainHand();
                    if (!nametag.getItemMeta().hasDisplayName()) return;
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() + 1);
                }
            }
        }
        //  If the villager AI is not being toggled
        else {
            // Show current status with action bar
            ActionBarUtils.sendVillagerStatus(player, villager, plugin);

            //  If nametag shouldn't be consumed, give one back
            if (!VillagerUtilities.hasMarker(villager, plugin)) return;
            if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG) && !plugin.getConfig().getBoolean("toggleableoptions.usenametags")) {
                ItemStack nametag = player.getInventory().getItemInMainHand();
                if (!nametag.getItemMeta().hasDisplayName()) return;
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() + 1);
            }
        }

        //  Restock
        if (!VillagerUtilities.getMarker(villager, plugin)) {
            RestockVillager.call(villager, plugin, player);
        }
    }

    // Helper method for visual feedback - optimisé
    private void showVillagerStateChange(Villager villager, Player player, boolean wasOptimized) {
        if (plugin.getConfig().getBoolean("visual-feedback.enabled", true)) {
            if (wasOptimized) {
                // Villager activated - particules clouds
                villager.getWorld().spawnParticle(Particle.CLOUD,
                    villager.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.02);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 0.6f, 1.0f);
            } else {
                // Villager optimised - particules happy
                villager.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    villager.getLocation().add(0, 1.5, 0), 8, 0.3, 0.3, 0.3, 0.05);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.1f);
            }
        }
    }

    // Méthodes utilitaires ultra-optimisées
    private void showVillagerStatus(Player player, Villager villager, VillagerCache.VillagerData data) {
        ActionBarUtils.sendVillagerStatus(player, villager, plugin);

        if (data.aiState) {
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-status-optimized")));
        } else {
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-status-active")));
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.action-help")));
        }
    }

    private void sendOptimizedFeedbackMessage(Player player, boolean wasOptimized) {
        if (wasOptimized) {
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-optimized")));
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-performance-tip")));
        } else {
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-unoptimized")));
            player.sendMessage(VillagerUtilities.colorcodes.cm(
                plugin.getConfig().getString("messages.villager-breeding-reminder")));
        }
    }

    // Events optimisés avec priorité et filtrage rapide
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void inventoryMove(InventoryClickEvent event) {
        // Vérifications rapides en premier
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!plugin.getConfig().getBoolean("toggleableoptions.preventtrading")) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        VillagerCache.VillagerData data = VillagerCache.getVillagerData(vil, plugin);

        if (data == null || !data.aiState) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        player.sendMessage(VillagerUtilities.colorcodes.cm(
            plugin.getConfig().getString("messages.VillagerMustBeDisabled")));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void villagerTradeClick(TradeSelectEvent event) {
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!plugin.getConfig().getBoolean("toggleableoptions.preventtrading")) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        VillagerCache.VillagerData data = VillagerCache.getVillagerData(vil, plugin);

        if (data == null || !data.aiState) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        player.closeInventory();
        player.sendMessage(VillagerUtilities.colorcodes.cm(
            plugin.getConfig().getString("messages.VillagerMustBeDisabled")));
    }

    // Optimisation critique - utiliser le cache au lieu de PersistentData
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCancelVillagerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Villager && event.getDamager() instanceof Zombie)) return;

        Villager vil = (Villager) event.getEntity();
        VillagerCache.VillagerData data = VillagerCache.getVillagerData(vil, plugin);

        // Protéger seulement les villagers optimisés (AI disabled)
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
