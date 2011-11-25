package com.evosysdev.bukkit.taylorjb.simplereserve;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class SimpleReservePlayerListener extends PlayerListener
{
    private SimpleReserve plugin; // plugin using the listener

    /**
     * Initialize the player listener
     * 
     * @param plugin
     *            plugin using the listener
     */
    public SimpleReservePlayerListener(SimpleReserve plugin)
    {
        this.plugin = plugin;
    }

    @Override
    /**
     * If server's full, check if player has a reserve slot, let through if they do
     * @param event the login event
     */
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        // full and has permissions to enter
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL && (event.getPlayer().hasPermission("simplereserve.enter.full") || event.getPlayer().hasPermission("simplereserve.enter.kick")))
        {
            // enter full method
            if ((plugin.getReserveMethod() == SimpleReserve.ReserveType.BOTH || plugin.getReserveMethod() == SimpleReserve.ReserveType.FULL) && event.getPlayer().hasPermission("simplereserve.enter.full"))
            {
                event.allow();
                Logger.getLogger("minecraft").info("Allowed player " + event.getPlayer().getDisplayName() + " to join full server!");
            }
            // enter by kicking another
            else if ((plugin.getReserveMethod() == SimpleReserve.ReserveType.BOTH || plugin.getReserveMethod() == SimpleReserve.ReserveType.KICK) && event.getPlayer().hasPermission("simplereserve.enter.kick"))
            {
                Player[] players = plugin.getServer().getOnlinePlayers();
                for (Player p : players)
                {
                    // player does not have kick prevent power
                    if (!p.hasPermission("simplereserve.kick.prevent"))
                    {
                        p.kickPlayer("Kicked to make room for reserved user!");
                        event.allow();
                        Logger.getLogger("minecraft").info("Allowed player " + event.getPlayer().getDisplayName() + " to join full server by kicking player " + p.getDisplayName() + "!");
                        return; // found and kicked a player, let the reserved player join, exit loop
                    }
                }
                
                // if we get here, no kickable player found...that would be unfortunate.
                event.setKickMessage("Unable to find any kickable players to open spots!");
            }
        }
    }
}
