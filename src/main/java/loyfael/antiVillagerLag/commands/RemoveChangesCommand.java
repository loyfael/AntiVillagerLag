package loyfael.antiVillagerLag.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import loyfael.antiVillagerLag.AntiVillagerLag;
import loyfael.antiVillagerLag.utils.VillagerUtilities;

public class RemoveChangesCommand implements CommandExecutor {

    AntiVillagerLag plugin;
    private boolean confirming;

    public RemoveChangesCommand(AntiVillagerLag plugin) {
        this.plugin = plugin;
        confirming = false;
    }


    // First implementation of a removal command. Needs more work
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("avlremove")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage("Type '/avlremove confirm' to remove all changes made by this plugin");
                    confirming = true;
                    return true;
                } else if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
                    if (confirming) {
                        // Add code for removing all tags and renabling awareness
                        removeVillagerChanges();

                        player.sendMessage("All villagers have been removed.");
                        confirming = false;
                    } else {
                        player.sendMessage("No confirmation pending.");
                    }
                    return true;
                } else {
                    player.sendMessage("Invalid arguments. Usage: /avlremove or /avlremove confirm");
                    return true;
                }
            } else {
                // console command no confirmation needed
                if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
                    // Add code for removing all tags and renabling awareness
                    removeVillagerChanges();

                    sender.sendMessage("All changes have been removed.");
                    return true;
                } else {
                    sender.sendMessage("Invalid arguments. Usage: /avlremove confirm");
                    return true;
                }
            }
        }
        return true;
    }

    // Should work hopefully
    // Probably should create a new method to remove all special tags but for now renabling is enough
    private void removeVillagerChanges() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getLivingEntities()) {
                if (entity.getType() == EntityType.VILLAGER) {
                    Villager vil = (Villager) entity;
                    VillagerUtilities.CleanseTheVillagers(vil, plugin);
                }
            }
        }
    }
}

