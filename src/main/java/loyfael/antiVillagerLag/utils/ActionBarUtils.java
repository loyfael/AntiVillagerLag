package loyfael.antiVillagerLag.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import loyfael.antiVillagerLag.AntiVillagerLag;

public class ActionBarUtils {

    public static void sendVillagerStatus(Player player, Villager villager, AntiVillagerLag plugin) {
        if (!plugin.getConfig().getBoolean("action-bar.enabled", true)) return;
        
        String message;
        if (VillagerUtilities.hasMarker(villager, plugin)) {
            if (VillagerUtilities.getMarker(villager, plugin)) {
                // Villager is optimized (AI disabled)
                message = plugin.getConfig().getString("action-bar.status-optimized", "&a[OPTIMIZED] &7Ready for trading");
            } else {
                // Villager is active (AI enabled)
                message = plugin.getConfig().getString("action-bar.status-active", "&e[ACTIVE] &7Mobile villager");
            }
        } else {
            message = plugin.getConfig().getString("action-bar.status-new", "&7[NEW] &eRight-click to setup");
        }
        
        message = VillagerUtilities.colorcodes.cm(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
    
    public static void sendCooldownInfo(Player player, long minutes, long seconds, AntiVillagerLag plugin) {
        if (!plugin.getConfig().getBoolean("action-bar.enabled", true)) return;
        
        String message = plugin.getConfig().getString("action-bar.cooldown", "&c⏰ Cooldown: %avlminutes%m %avlseconds%s");
        message = message.replaceAll("%avlminutes%", String.valueOf(minutes));
        message = message.replaceAll("%avlseconds%", String.valueOf(seconds));
        message = VillagerUtilities.colorcodes.cm(message);
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
