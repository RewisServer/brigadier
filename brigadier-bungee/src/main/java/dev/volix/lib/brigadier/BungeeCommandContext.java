package dev.volix.lib.brigadier;

import net.md_5.bungee.api.CommandSender;
import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;

/**
 * @author Tobias Büser
 */
public class BungeeCommandContext extends CommandContext<CommandSender> {

    public BungeeCommandContext(CommandSender commandSource, CommandInstance command, ParameterSet parameter) {
        super(commandSource, command, parameter);
    }

}
