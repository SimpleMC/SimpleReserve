package com.evosysdev.bukkit.taylorjb.simplereserve;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleReserve extends JavaPlugin
{
    // create the listener
    private final SimpleReservePlayerListener playerListener = new SimpleReservePlayerListener(this);
    
    private ReserveType reserveMethod; // method to use for reserve slots

    /**
     * set up the block listener and Permissions on enable
     */
    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Highest, this);
        
        try
        {
            reserveMethod = ReserveType.valueOf(getConfig().getString("reserve.type").toUpperCase());
        }
        catch (NullPointerException npe)
        { // should happen on first-run when config doesn't exist
            getConfig().set("reserve.type", "both");
            getConfig().set("reserve.types", "full,kick,both,none"); // tell people the options
            saveConfig();

            reserveMethod = ReserveType.valueOf(getConfig().getString("reserve.type").toUpperCase());
        }
        catch (IllegalArgumentException iae)
        { // config not set right(enum can't be valueOf'd)
            getConfig().set("reserve.type", "both"); // config not set right, default to both
            saveConfig();

            reserveMethod = ReserveType.valueOf(getConfig().getString("reserve.type").toUpperCase());
        }

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled!");
    }

    /**
     * plugin disabled
     */
    public void onDisable()
    {
        System.out.println("SimpleReserve disabled!");
    }

    /**
     * Get the method we are using for reserve slots
     * 
     * @return reserve method
     */
    public ReserveType getReserveMethod()
    {
        return reserveMethod;
    }

    /**
     * enum for type of reserve we are using(kick, full bypass, or either(both))
     * 
     * @author TJ
     * 
     */
    public enum ReserveType
    {
        KICK, FULL, BOTH, NONE
    }
}
