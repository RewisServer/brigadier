package dev.volix.lib.brigadier.context;

import lombok.Getter;
import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.parameter.ParameterSet;

/**
 * The context of the command is a wrapper for the command itself, the command
 * source and some specific methods.
 *
 * @param <S> The type of command source
 *
 * @author Tobias Büser
 */
public abstract class CommandContext<S> {

    /**
     * Source of the command (can be null)
     */
    @Getter private final S commandSource;

    /**
     * The instance of the command
     */
    @Getter private final CommandInstance command;

    /**
     * The arguments parsed into a wrapper class
     */
    @Getter private final ParameterSet parameter;

    public CommandContext(final S commandSource, final CommandInstance command, final ParameterSet parameter) {
        this.commandSource = commandSource;
        this.command = command;
        this.parameter = parameter;
    }

}
