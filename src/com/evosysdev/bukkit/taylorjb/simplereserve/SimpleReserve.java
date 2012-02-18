package com.evosysdev.bukkit.taylorjb.simplereserve;

import org.bukkit.plugin.java.JavaPlugin;

public class SimpleReserve extends JavaPlugin
{
    /**
     * set up the block listener and Permissions on enable
     */
    public void onEnable()
    {
        validateConfig();
        
        ReserveType reserveMethod;
        
        try
        {
            reserveMethod = ReserveType.valueOf(getConfig().getString("reserve.type").toUpperCase());
        }
        catch (IllegalArgumentException iae)
        { // config not set right(enum can't be valueOf'd)
            getConfig().set("reserve.type", "both"); // config not set right, default to both
            saveConfig();
            
            reserveMethod = ReserveType.valueOf(getConfig().getString("reserve.type").toUpperCase());
        }
        
        new SimpleReserveListener(reserveMethod,
                getConfig().getInt("reserve.full.cap", 5),
                getConfig().getBoolean("reserve.full.reverttokick", false),
                getConfig().getString("kick-message", "Kicked to make room for reserved user!"),
                getConfig().getString("full-message", "The server is full!"),
                this);
        
        getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }
    
    /**
     * Validate nodes, if they don't exist or are wrong, set them
     * and resave config
     * 
     * Unfortunately we cannot use defaults because contains will
     * return true if node set in default OR config, and we want to
     * update the config file in case it has changed from version to
     * version.
     * (thatssodumb.jpg, rage.mkv, etc etc)
     */
    private void validateConfig()
    {
        boolean updated = false;
        
        // settings
        if (!getConfig().contains("reserve.type"))
        {
            getConfig().set("reserve.type", "both");
            updated = true;
        }

        if (!getConfig().contains("reserve.full.cap"))
        {
            getConfig().set("reserve.full.cap", 5);
            updated = true;
        }
        
        if (!getConfig().contains("reserve.full.reverttokick"))
        {
            getConfig().set("reserve.full.reverttokick", false);
            updated = true;
        }
        
        if (!getConfig().contains("kick-message"))
        {
            getConfig().set("kick-message", "Kicked to make room for reserved user!");
            updated = true;
        }
        
        if (!getConfig().contains("full-message"))
        {
            getConfig().set("full-message", "The server is full!");
            updated = true;
        }
        
        // if nodes have been updated, update header then save
        if (updated)
        {
            // set header for information
            getConfig().options().header(
                    "Config nodes:\n" +
                    "\n" +
                    "reserve.type(enum/string): Type of reserve slots, options:\n" +
                    "    full,kick,both,none\n" +
                    "reserve.full.cap(int): Max players allowed over capacity if using 'full' method, 0 for no max\n" +
                    "reserve.full.reverttokick(boolean): Should we fall back to kick method if we reach max over capacity using full?\n" +
                    "kick-message(string): Message player will recieve when kicked to let reserve in\n" + 
                    "full-message(string): Message player will recieve when unable to join full server\n" +
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
