package org.simplemc.simplereserve

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.simplemc.simplereserve.ReserveConfig.FullMethodConfig
import org.simplemc.simplereserve.ReserveConfig.KickMethodConfig
import org.simplemc.simplereserve.ReserveConfig.ReserveMethod

/**
 * Simple reserve slot plugin the Bukkit API
 *
 * @author Taylor Becker
 */
class SimpleReserve : JavaPlugin() {

    lateinit var listener: ReserveLoginListener
    override fun onEnable() {
        listener = ReserveLoginListener(this, loadConfig())

        checkNotNull(getCommand("simplereservereload")).setExecutor(::reloadConfigCommand)

        logger.info { "${description.name} version ${description.version} enabled!" }
    }

    /**
     * Load the config options from config
     */
    private fun loadConfig(): ReserveConfig {
        // ensure config file/options valid
        validateConfig()

        // load
        val config = config.getConfigurationSection("reserve") // our config

        val reserveMethod: ReserveMethod = try {
            ReserveMethod.valueOf(config!!.getString("method")!!.uppercase())
        } catch (_: IllegalArgumentException) {
            // invalid method, default to both
            // config not set right, default to both
            config!!.set("method", "both")
            saveConfig()
            logger.info { "${description.name} config file updated, please check settings!" }

            ReserveMethod.valueOf(config.getString("method")!!.uppercase())
        }

        return ReserveConfig(
            method = reserveMethod,
            serverFullMessage = config.getString("server-full-message", "The server is full!")!!,
            full = FullMethodConfig(
                capacity = config.getInt("full.cap", 5),
                kickFallback = config.getBoolean("full.kick-fallback", false),
                overCapacityMessage = config.getString("full.over-capacity-message", "All reserve slots are full!")!!,
            ),
            kick = KickMethodConfig(
                message = config.getString(
                    "kick.message",
                    "Kicked to make room for reserved user!",
                )!!,
            ),
        )
    }

    private fun reloadConfigCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        vararg args: String,
    ): Boolean {
        listener.close()
        reloadConfig() // reload config file from disk
        listener = ReserveLoginListener(this, loadConfig())

        logger.fine { "Config reloaded." }
        sender.sendMessage("SimpleReserve config reloaded")
        return true
    }

    /**
     * Validate nodes, if they don't exist or are wrong, set them and resave config
     *
     * Unfortunately, we cannot use defaults because there is no reasonable way of copying the defaults into the live
     * config file while maintaining an updated header. We could use [saveDefaultConfig] but this doesn't help us when
     * changing available config options when a config file already exists.
     */
    private fun validateConfig() {
        var updated = false

        if (!config.contains("reserve.method")) {
            config.set("reserve.method", "both")
            updated = true
        }

        if (!config.contains("reserve.server-full-message")) {
            config.set("reserve.server-full-message", "The server is full!")
            updated = true
        }

        if (!config.contains("reserve.full.cap")) {
            config.set("reserve.full.cap", 5)
            updated = true
        }

        if (!config.contains("reserve.full.kick-fallback")) {
            config.set("reserve.full.kick-fallback", false)
            updated = true
        }

        if (!config.contains("reserve.full.over-capacity-message")) {
            config.set("reserve.full.over-capacity-message", "All reserve slots full!")
            updated = true
        }

        if (!config.contains("reserve.kick.message")) {
            config.set("reserve.kick.message", "Kicked to make room for reserved user!")
            updated = true
        }

        // if nodes have been updated, update header then save
        if (updated) {
            // set header for information
            config.options().setHeader(
                listOf(
                    "Config nodes:",
                    "",
                    "reserve.method(enum/string): Method to use for allowing reservees in, options:",
                    "    full,kick,both,none",
                    "reserve.server-full-message(string): Message player will receive when unable to join full server",
                    "reserve.full.cap(int): Max players allowed over capacity if using 'full' method, 0 for no max",
                    "reserve.full.kick-fallback(boolean): Should we fall back to kick method if we reach max over capacity using full?",
                    "reserve.full.over-capacity-message(string): Message player with reserve privileges will receive when all reserve slots are full",
                    "reserve.kick.message(string): Message player will receive when kicked to let reserve in",
                    "",
                    "Reserve Methods Overview:",
                    "-----------------------",
                    "",
                    "Full: Allow reserves to log on past the server limit",
                    "Kick: Attempt to kick a player without kick immunity to make room",
                    "Both: Both methods of reservation based on Permission",
                    "    NOTE: If a player has permission for kick and full, full takes precedence",
                    "None: No reservation. Effectively disables mod without needing to remove",
                    "",
                ),
            )

            // save
            saveConfig()
            logger.info { "${description.name} config file updated, please check settings!" }
        }
    }

    /**
     * Plugin disabled
     */
    override fun onDisable() {
        listener.close()
        println("SimpleReserve disabled!")
    }
}
