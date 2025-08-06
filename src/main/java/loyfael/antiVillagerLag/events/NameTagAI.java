package loyfael.antiVillagerLag.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import loyfael.antiVillagerLag.AntiVillagerLag;
import loyfael.antiVillagerLag.utils.VillagerUtilities;

public class NameTagAI {

    public static boolean call(Villager villager, AntiVillagerLag plugin, Player player) {
        if (!plugin.getConfig().getBoolean("toggleableoptions.userenaming")) return false;
        //  Ensure item is a nametag
        ItemStack nametag = player.getInventory().getItemInMainHand();
        if (!nametag.getType().equals(Material.NAME_TAG)) {
            String name = villager.getCustomName();
            if (name != null) name = name.toLowerCase().replaceAll("(?i)[§&][0-9A-FK-ORXLo]", "");
            return VillagerUtilities.disabling_names.contains(name);
        }
        if (!nametag.getItemMeta().hasDisplayName()) {
            String villagerName = villager.getCustomName().replaceAll("(?i)[§&][0-9A-FK-ORXLo]", "");
            return VillagerUtilities.disabling_names.contains(villagerName.toLowerCase());
        }
        //  Should the villager be disabled?
        String itemName = nametag.getItemMeta().getDisplayName().replaceAll("(?i)[§&][0-9A-FK-ORXLo]", "");
        return VillagerUtilities.disabling_names.contains(itemName.toLowerCase());
    }

}