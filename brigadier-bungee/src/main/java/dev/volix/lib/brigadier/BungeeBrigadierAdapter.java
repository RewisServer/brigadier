package dev.volix.lib.brigadier;

import java.util.List;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;

/**
 * @author Tobias Büser
 */
public class BungeeBrigadierAdapter extends BrigadierAdapter<CommandSender> implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabCompletion(final TabCompleteEvent event) {
        if (event.isCancelled())
            return;

        final Connection connection = event.getSender();
        if (connection instanceof ProxiedPlayer) {
            final ProxiedPlayer player = (ProxiedPlayer) connection;

            final List<String> suggestions = Brigadier.getInstance().executeTabCompletion(player, event.getCursor());
            if (suggestions.size() > 0)
                event.getSuggestions().clear();
            event.getSuggestions().addAll(suggestions);
        }
    }

    @Override
    public void handleRegister(final String label, final CommandInstance instance) {
        ProxyServer.getInstance().getPluginManager().registerCommand(BungeeBrigadierPlugin.getInstance(),
            new Command(instance.getLabel(), instance.getPermission(), instance.getAliases().toArray(new String[0])) {
                @Override
                public void execute(final CommandSender sender, final String[] args) {
                    Brigadier.getInstance().executeCommand(sender, instance.getLabel(), args);
                }
            });
    }

    @Override
    public boolean checkPermission(final CommandSender commandSource, final CommandInstance command) {
        return commandSource.hasPermission(command.getPermission());
    }

    @Override
    public void runAsync(final Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeBrigadierPlugin.getInstance(), runnable);
    }

    @Override
    public Class<CommandSender> getCommandSourceClass() {
        return CommandSender.class;
    }

    @Override
    public CommandContext<CommandSender> constructCommandContext(final CommandSender commandSource, final CommandInstance command, final ParameterSet parameter) {
        return new BungeeCommandContext(commandSource, command, parameter);
    }
}
