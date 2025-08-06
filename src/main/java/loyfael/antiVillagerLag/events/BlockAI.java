package loyfael.antiVillagerLag.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import loyfael.antiVillagerLag.AntiVillagerLag;
import loyfael.antiVillagerLag.utils.VillagerUtilities;

public class BlockAI {
    public static boolean call(Villager villager, AntiVillagerLag plugin, Player player) {
        if (!plugin.getConfig().getBoolean("toggleableoptions.useblocks")) return false;
        Location loc = villager.getLocation();
        Material mat_below = villager.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()).getType();
        return VillagerUtilities.standingon_blocks.contains(mat_below);
    }
}
