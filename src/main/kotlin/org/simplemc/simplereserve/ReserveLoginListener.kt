package org.simplemc.simplereserve

import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL
import org.bukkit.plugin.Plugin
import org.simplemc.simplereserve.ReserveConfig.ReserveMethod

class ReserveLoginListener(private val plugin: Plugin, private val config: ReserveConfig) : Listener, AutoCloseable {
    init {
        when (config.method) {
            ReserveMethod.NONE -> plugin.logger.finer { "Reserve Method is NONE, skipping listener registration." }
            else -> {
                plugin.server.pluginManager.registerEvents(this, plugin)
                plugin.logger.finer { "Created ReserveLoginListener with config:\n$config" }
            }
        }
    }

    /**
     * Allow players to join a full server if permitted
     *
     * @param event the login event
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerLogin(event: PlayerLoginEvent) {
        plugin.logger.info { config.full.kickFallback.toString() }
        if (event.result != KICK_FULL) return

        val allowFull = config.method.fullEnabled && event.player.hasPermission("simplereserve.enter.full")
        val canEnterFull = allowFull && plugin.server.hasOverCapacity
        val allowKick =
            (config.method.kickEnabled && event.player.hasPermission("simplereserve.enter.kick")) || (allowFull && config.full.kickFallback)

        when {
            canEnterFull -> {
                event.allow()
                plugin.logger.info { "Allowed player ${event.player.displayName} to join full server!" }
            }

            allowFull && !allowKick -> event.disallow(KICK_FULL, config.full.overCapacityMessage)
            allowKick -> kickJoin(event.player, event)
            else -> event.disallow(KICK_FULL, config.serverFullMessage)
        }
    }

    /**
     * Perform a join via kick method
     *
     * @param player player logging in
     * @param event  login event
     */
    private fun kickJoin(player: Player, event: PlayerLoginEvent) {
        plugin.server.onlinePlayers.firstOrNull { p -> !p.hasPermission("simplereserve.kick.prevent") }?.let { toKick ->
            toKick.kickPlayer(config.kick.message)
            event.allow()

            plugin.logger.info {
                "Allowed player ${player.displayName} to join full server by kicking player ${toKick.displayName}!"
            }
        } ?: event.disallow(KICK_FULL, "Unable to find any kickable players to make room!")
    }

    // if server has overcapacity available according to reserve capacity
    private val Server.hasOverCapacity: Boolean get() = onlinePlayers.size < maxPlayers + config.full.capacity

    override fun close() {
        HandlerList.unregisterAll(this) // clear event binds (important for reload)
    }
}
