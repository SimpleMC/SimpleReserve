package org.simplemc.simplereserve;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * Listener for simple reserve plugin to handle logins
 *
 * @author Taylor Becker
 */
public class SimpleReserveListener implements Listener
{
    private SimpleReserve plugin; // plugin using the listener

    private ReserveType reserveMethod; // method to use for reserve slots
    private int capOver; // capacity allowed of server capacity
    private boolean revertToKick; // in event of reaching cap over cap, should full fall back on kicking?
    private String kickMessage, // message to send player when kicked to make room
            fullMessage, // message to send player who can't join full server
            reserveFullMessage; // message to send player who can join full but no available slots

    /**
     * Initialize the player listener
     *
     * @param reserveMethod method to use for reserve slots
     * @param capOver       capacity allowed of server capacity
     * @param revertToKick  in event of reaching cap over cap, should full fall back on kicking?
     * @param kickMessage   message to send player when kicked to make room
     * @param fullMessage   message to send player who can't join full server
     * @param plugin        plugin using the listener
     */
    public SimpleReserveListener(ReserveType reserveMethod, int capOver, boolean revertToKick, String kickMessage,
                                 String fullMessage, String reserveFullMessage, SimpleReserve plugin)
    {
        setSettings(reserveMethod, capOver, revertToKick, kickMessage, fullMessage, reserveFullMessage);
        this.plugin = plugin;

        // register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        plugin.getLogger().finer(
                String.format("Created SimpleReserveListener with kickMessage: \"%s\" and fullMessage: \"%s\"",
                        kickMessage, fullMessage));
    }

    /**
     * Set reserve listener settings
     *
     * @param reserveMethod method to use for reserve slots
     * @param capOver       capacity allowed of server capacity
     * @param revertToKick  in event of reaching cap over cap, should full fall back on kicking?
     * @param kickMessage   message to send player when kicked to make room
     * @param fullMessage   message to send player who can't join full server
     */
    public void setSettings(ReserveType reserveMethod, int capOver, boolean revertToKick, String kickMessage,
                            String fullMessage, String reserveFullMessage)
    {
        this.reserveMethod = reserveMethod;
        this.capOver = capOver;
        this.revertToKick = revertToKick;
        this.kickMessage = kickMessage;
        this.fullMessage = fullMessage;
        this.reserveFullMessage = reserveFullMessage;
    }

    /**
     * If server is full, let event through if player has not been denied yet
     *
     * @param event the login event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        // is the server full?
        if (event.getResult() == Result.KICK_FULL)
        {
            Player player = event.getPlayer(); // player doing event

            // full entry
            if ((reserveMethod == ReserveType.BOTH || reserveMethod == ReserveType.FULL)
                    && player.hasPermission("simplereserve.enter.full"))
            {
                // allow player in as long as the server hasn't hit the max over cap
                if (!serverTooFull())
                {
                    event.allow();
                    plugin.getLogger().info(
                            "Allowed player " + event.getPlayer().getDisplayName() + " to join full server!");
                }
                // server's too full to let more in, attempt to kick someone to make room
                else if (revertToKick)
                {
                    kickJoin(player, event);
                }
                // server's too full and fallback kick is disabled
                else
                {
                    event.disallow(Result.KICK_FULL, reserveFullMessage);
                }
            }
            // kick entry
            else if ((reserveMethod == ReserveType.BOTH || reserveMethod == ReserveType.KICK)
                    && player.hasPermission("simplereserve.enter.kick"))
            {
                kickJoin(player, event);
            }
            // player cannot join
            else
            {
                event.disallow(Result.KICK_FULL, fullMessage);
            }
        }
    }

    /**
     * Perform a join via kick method
     *
     * @param player player logging in
     * @param event  login event
     */
    private void kickJoin(Player player, PlayerLoginEvent event)
    {
        Player toKick = plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.hasPermission("simplereserve.kick.prevent")).findFirst().orElse(null);

        // found player to kick
        if (toKick != null)
        {
            toKick.kickPlayer(kickMessage);
            event.allow();

            plugin.getLogger().info(
                    String.format("Allowed player %s to join full server by kicking player %s!",
                            player.getDisplayName(), toKick.getDisplayName()));
        }
        else
        {
            // if we get here, no kickable player found...that would be unfortunate.
            event.disallow(Result.KICK_FULL, "Unable to find any kickable players to open spots!");
        }
    }

    /**
     * @return if server is more full than cap over cap
     */
    private boolean serverTooFull()
    {
        return capOver != 0
                && (plugin.getServer().getOnlinePlayers().size() >= plugin.getServer().getMaxPlayers() + capOver);
    }
}
