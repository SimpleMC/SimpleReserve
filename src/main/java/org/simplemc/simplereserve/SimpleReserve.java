package org.simplemc.simplereserve;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simple reserve slot plugin the Bukkit API
 *
 * @author Taylor Becker
 */
public class SimpleReserve extends JavaPlugin
{
    /**
     * set up the block listener and Permissions on enable
     */
    public void onEnable()
    {
        loadConfig();

        getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }

    /**
     * Load the config options from config
     */
    private void loadConfig()
    {
        // ensure config file/options valid
        validateConfig();

        // load
        ConfigurationSection config = getConfig().getConfigurationSection("reserve"); // our config
        ReserveType reserveMethod; // type of reservations

        try
        {
            reserveMethod = ReserveType.valueOf(config.getString("type").toUpperCase());
        }
        // config not set right(enum can't be valueOf'd)
        catch (IllegalArgumentException iae)
        {
            // config not set right, default to both
            config.set("type", "both");
            saveConfig();
            getLogger().info(getDescription().getName() + " config file updated, please check settings!");

            reserveMethod = ReserveType.valueOf(config.getString("type").toUpperCase());
        }

        new SimpleReserveListener(reserveMethod,
                config.getInt("full.cap", 5),
                config.getBoolean("full.reverttokick", false),
                config.getString("kick-message", "Kicked to make room for reserved user!"),
                config.getString("full-message", "The server is full!"),
                config.getString("reserve-full-message", "All reserve slots are full!"),
                this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("simplereserve"))
        {
            if (sender.hasPermission("simplereserve"))
            {
                // reload command
                if (args.length > 0 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")))
                {
                    if (sender.hasPermission("simplereserve.reload"))
                    {
                        reloadConfig(); // reload file
                        loadConfig(); // read config

                        getLogger().fine("Config reloaded.");
                        sender.sendMessage("SimpleReserve config reloaded");
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                    }
                }
                // help command
                else
                {
                    sender.sendMessage(ChatColor.AQUA + "/" + getCommand("simplereserve").getName() + ChatColor.WHITE
                            + " | " + ChatColor.BLUE
                            + getCommand("simplereserve").getDescription());
                    sender.sendMessage("Usage: " + ChatColor.GRAY + getCommand("simplereserve").getUsage());
                }
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
            }
            return true;
        }

        return false;
    }

    /**
     * Validate nodes, if they don't exist or are wrong, set them and resave config
     * <p>
     * Unfortunately we cannot use defaults because there is no reasonable way of copying the defaults
     * into the live config file while maintaining an updated header. We could use saveDefaultConfig
     * but this doesn't help us when changing the config options and a config file already exists and
     * copyDefaults destroys the header.
     * </p>
     */
    private void validateConfig()
    {
        boolean updated = false;
        FileConfiguration config = getConfig(); // our config

        // settings
        if (!config.contains("reserve.type"))
        {
            config.set("reserve.type", "both");
            updated = true;
        }

        if (!config.contains("reserve.full.cap"))
        {
            config.set("reserve.full.cap", 5);
            updated = true;
        }

        if (!config.contains("reserve.full.reverttokick"))
        {
            config.set("reserve.full.reverttokick", false);
            updated = true;
        }

        if (!config.contains("reserve.kick-message"))
        {
            config.set("reserve.kick-message", "Kicked to make room for reserved user!");
            updated = true;
        }

        if (!config.contains("reserve.full-message"))
        {
            config.set("reserve.full-message", "The server is full!");
            updated = true;
        }

        if (!config.contains("reserve.reserve-full-message"))
        {
            config.set("reserve.reserve-full-message", "All reserve slots full!");
            updated = true;
        }

        // if nodes have been updated, update header then save
        if (updated)
        {
            // set header for information
            config
                    .options()
                    .header(
                            "Config nodes:\n"
                                    +
                                    "\n"
                                    +
                                    "reserve.type(enum/string): Type of reserve slots, options:\n"
                                    +
                                    "    full,kick,both,none\n"
                                    +
                                    "reserve.full.cap(int): Max players allowed over capacity if using 'full' method, 0 for no max\n"
                                    +
                                    "reserve.full.reverttokick(boolean): Should we fall back to kick method if we reach max over capacity using full?\n"
                                    +
                                    "kick-message(string): Message player will recieve when kicked to let reserve in\n"
                                    +
                                    "full-message(string): Message player will recieve when unable to join full server\n"
                                    +
                                    "reserve-full-message(string): Message player with reserve privileges will recieve when all reserve slots are full\n"
                                    +
                                    "\n" +
                                    "Reserve Types Overview:\n" +
                                    "-----------------------\n" +
                                    "\n" +
                                    "Full: Allow reserves to log on past the server limit\n" +
                                    "Kick: Attempt to kick a player without kick immunity to make room\n" +
                                    "Both: Both methods of reservation based on Permission\n" +
                                    "    NOTE: If a player has permission for kick and full, full applies\n" +
                                    "None: No reservation. Effectively disables mod without needing to remove\n" +
                                    "");

            // save
            saveConfig();
            getLogger().info(getDescription().getName() + " config file updated, please check settings!");
        }
    }

    /**
     * plugin disabled
     */
    public void onDisable()
    {
        System.out.println("SimpleReserve disabled!");
    }
}
