package org.simplemc.simplereserve

data class ReserveConfig(
    val method: ReserveMethod,
    val serverFullMessage: String,
    val full: FullMethodConfig,
    val kick: KickMethodConfig,
) {
    enum class ReserveMethod {
        KICK,
        FULL,
        BOTH,
        NONE,
        ;

        val fullEnabled: Boolean get() = this == FULL || this == BOTH
        val kickEnabled: Boolean get() = this == KICK || this == BOTH
    }
    data class FullMethodConfig(
        val capacity: Int,
        val kickFallback: Boolean,
        val overCapacityMessage: String,
    ) {
        init {
            check(capacity > 0) { "Full method capacity must be greater than 0." }
        }
    }

    data class KickMethodConfig(val message: String)
}
