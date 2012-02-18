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
    private Map<String, Long> denies; // cache of people to deny
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
    public SimpleReserveListener(String kickMessage, String fullMessage, SimpleReserve plugin)
    {
        this.kickMessage = kickMessage;
        this.fullMessage = fullMessage;
        
        this.plugin = plugin;
        denies = new HashMap<String, Long>();
        
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
        // if we don't know we should be denying a login when server is full,
        // we need to allow it though to check it
        if (event.getResult() == Result.KICK_FULL && denies.get(event.getPlayer().getName()) == null)
        {
            event.allow();
        }
        // if name is in denies and it has been for 5 minutes, remove it from denies in case permissions have changed and need to recheck
        else if (denies.get(event.getPlayer().getName()) != null && (System.currentTimeMillis() - denies.get(event.getPlayer().getName()) > 300000))
        {
            denies.remove(event.getPlayer().getName());
        }
    }
    
    /**
     * If server's full, check if player has a reserve slot, let through if they do
     * 
     * @param event
     *            the login event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // is the server full?
        if (plugin.getServer().getOnlinePlayers().length > plugin.getServer().getMaxPlayers())
        {
            Player player = event.getPlayer(); // player doing event
            
            // check if player has permission to join given the reserve method
            if (canJoin(player))
            {
                if (plugin.getReserveMethod() == SimpleReserve.ReserveType.BOTH || plugin.getReserveMethod() == SimpleReserve.ReserveType.FULL)
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
                    player.kickPlayer("Unable to find any kickable players to open spots!");
                }
            }
            // if player cannot join, add him to the list so we know next time and don't need to allow through login
            else
            {
                denies.put(player.getName(), System.currentTimeMillis());
                player.kickPlayer(fullMessage);
                plugin.getLogger().info("Disconnecting player " + player.getDisplayName() + " because the server is full.");
            }
        }
    }
    
    public boolean canJoin(Player p)
    {
        return ((plugin.getReserveMethod() == SimpleReserve.ReserveType.BOTH || plugin.getReserveMethod() == SimpleReserve.ReserveType.FULL) && p
                .hasPermission("simplereserve.enter.full"))
                || ((plugin.getReserveMethod() == SimpleReserve.ReserveType.BOTH || plugin.getReserveMethod() == SimpleReserve.ReserveType.KICK) && p
                        .hasPermission("simplereserve.enter.kick"));
    }
}
