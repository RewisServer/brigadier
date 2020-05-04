package dev.volix.lib.brigadier;

import java.util.List;
import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

/**
 * @author Tobias Büser
 */
public class BukkitBrigadierAdapter extends BrigadierAdapter<CommandSender> {

    @Override
    public void handleRegister(final String label, final CommandInstance instance) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(label, new BukkitCommand(instance.getLabel(), instance.getDescription(),
            instance.getUsage().getBase(), instance.getAliases()) {
            @Override
            public boolean execute(final CommandSender sender, final String label, final String[] args) {
                Brigadier.getInstance().executeCommand(sender, label, args);
                return true;
            }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws IllegalArgumentException {
                return Brigadier.getInstance().executeTabCompletion(sender,
                    this.getLabel() + " " + String.join(" ", args));
            }
        });
    }

    @Override
    public boolean checkPermission(final CommandSender commandSource, final CommandInstance command) {
        return commandSource.hasPermission(command.getPermission());
    }

    @Override
    public void runAsync(final Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitBrigadierPlugin.getInstance(), runnable);
    }

    @Override
    public Class<CommandSender> getCommandSourceClass() {
        return CommandSender.class;
    }

    @Override
    public CommandContext<CommandSender> constructCommandContext(final CommandSender commandSource, final CommandInstance command, final ParameterSet parameter) {
        return new BukkitCommandContext(commandSource, command, parameter);
    }

}
