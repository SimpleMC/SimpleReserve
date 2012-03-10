package com.evosysdev.bukkit.taylorjb.simplereserve;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class SimpleReserveListener implements Listener
{
    private SimpleReserve plugin; // plugin using the listener
    
    private ReserveType reserveMethod; // method to use for reserve slots
    private int capOver; // capacity allowed of server capacity
    private boolean revertToKick; // in event of reaching cap over cap, should full fall back on kicking?
    private String kickMessage, // message to send player when kicked to make room
            fullMessage; // message to send player who can't join full server
            
    /**
     * Initialize the player listener
     * 
     * @param kickMessage
     *            message to send player when kicked to make room
     * @param fullMessage
     *            message to send player who can't join full server
     * @param plugin
     *            plugin using the listener
     */
    public SimpleReserveListener(ReserveType reserveMethod, int capOver, boolean revertToKick, String kickMessage, String fullMessage, SimpleReserve plugin)
    {
        this.reserveMethod = reserveMethod;
        this.capOver = capOver;
        this.revertToKick = revertToKick;
        this.kickMessage = kickMessage;
        this.fullMessage = fullMessage;
        
        this.plugin = plugin;
        
        // register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        plugin.getLogger().finer("Created SimpleReserveListener with kickMessage: \"" + kickMessage + "\" and fullMessage: \""+ fullMessage + "\"");
    }
    
    /**
     * If server is full, let event through if player has not been denied yet
     * 
     * @param event
     *            the login event
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        // is the server full?
        if (plugin.getServer().getOnlinePlayers().length > plugin.getServer().getMaxPlayers())
        {
            Player player = event.getPlayer(); // player doing event
            
            // check if player has permission to join given the reserve method
            if (canJoin(player))
            {
                if (!serverTooFull() && (reserveMethod == ReserveType.BOTH || reserveMethod == ReserveType.FULL))
                {
                    plugin.getLogger().info("Allowed player " + event.getPlayer().getDisplayName() + " to join full server!");
                }
                else
                {
                    Player[] players = plugin.getServer().getOnlinePlayers();
                    for (Player p : players)
                    {
                        // player does not have kick prevent power
                        if (!p.hasPermission("simplereserve.kick.prevent"))
                        {
                            p.kickPlayer(kickMessage);
                            plugin.getLogger().info("Allowed player " + player.getDisplayName() + " to join full server by kicking player " + p.getDisplayName() + "!");
                            return; // found and kicked a player, let the reserved player join, exit loop
                        }
                    }
                    
                    // if we get here, no kickable player found...that would be unfortunate.
                    event.disallow(Result.KICK_FULL, "Unable to find any kickable players to open spots!");
                }
            }
            // player cannot join
            else
            {
                event.disallow(Result.KICK_FULL, fullMessage);
            }
        }
    }
    
    /**
     * @return if server is more full than cap over cap
     */
    private boolean serverTooFull()
    {
        return capOver != 0 && (plugin.getServer().getOnlinePlayers().length >= plugin.getServer().getMaxPlayers() + capOver);
    }
    
    public boolean canJoin(Player p)
    {
        return ((reserveMethod == ReserveType.BOTH || reserveMethod == ReserveType.FULL) && p
                .hasPermission("simplereserve.enter.full"))
                || ((reserveMethod == ReserveType.BOTH || reserveMethod == ReserveType.KICK) && p
                        .hasPermission("simplereserve.enter.kick"));
    }
}
