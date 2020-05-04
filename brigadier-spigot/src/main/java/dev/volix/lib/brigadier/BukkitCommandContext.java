package dev.volix.lib.brigadier;

import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;
import org.bukkit.command.CommandSender;

/**
 * @author Tobias Büser
 */
public class BukkitCommandContext extends CommandContext<CommandSender> {

    public BukkitCommandContext(final CommandSender commandSource, final CommandInstance command, final ParameterSet parameter) {
        super(commandSource, command, parameter);
    }

}
