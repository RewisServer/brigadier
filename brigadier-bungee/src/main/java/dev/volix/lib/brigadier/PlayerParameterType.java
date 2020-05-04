package dev.volix.lib.brigadier;

import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import dev.volix.lib.brigadier.parameter.ParameterType;

/**
 * @author Tobias Büser
 */
public class PlayerParameterType implements ParameterType<ProxiedPlayer> {

    @Override
    public ProxiedPlayer parse(final String string) {
        try {
            final UUID uuid = UUID.fromString(string);
            return ProxyServer.getInstance().getPlayer(uuid);
        } catch (final Exception ex) {
            // string is not an unique id, ignore.
        }
        return ProxyServer.getInstance().getPlayer(string);
    }

    @Override
    public Class<ProxiedPlayer> getTypeClass() {
        return ProxiedPlayer.class;
    }

}
