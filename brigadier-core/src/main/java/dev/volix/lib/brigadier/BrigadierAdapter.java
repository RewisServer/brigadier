package dev.volix.lib.brigadier;

import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.command.ExecutionResult;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;

/**
 * @author Tobias Büser
 */
public abstract class BrigadierAdapter<S> {

    /**
     * Will be executed after the {@link Brigadier} registered the command
     * in its instance.
     *
     * @param label    The label of the command
     * @param instance The instance of the command
     */
    public abstract void handleRegister(final String label, final CommandInstance instance);


    /**
     * Will be called before the command parser changes the current
     * command tree node.
     * Determines if the given {@code commandSource} is able to
     * execute given {@code command}
     *
     * @param commandSource The source of the command execution
     * @param command       The command to be executed
     *
     * @return The result as boolean.
     *
     * @see ExecutionResult
     */
    public abstract boolean checkPermission(final S commandSource, final CommandInstance command);

    /**
     * Will be called when for example a command wants to
     * execute a command asynchronously.
     * If the runnable will be ignored in this method, it could
     * lead to unexpected behaviour.
     *
     * @param runnable The runnable to be executed asynchronously.
     */
    public abstract void runAsync(final Runnable runnable);

    /**
     * Gets the class for the command source executing the commands.
     * Can be an {@link Integer#getClass()} or whatever.
     *
     * @return The class of the source
     */
    public abstract Class<S> getCommandSourceClass();

    /**
     * Constructs a custom command context for given parameters.
     * It is recommended to extend the {@link CommandContext} class
     * and create a fitting instance which implements the command source
     * correctly as well. For example a specific context especially
     * for {@link Integer}s, because the {@code commandSource} is an
     * integer.
     *
     * @param commandSource The source executing given {@code command}
     * @param command       The command to be executed
     * @param parameter     The parameter the source passed to the executor
     *
     * @return The constructed {@link CommandContext}.
     */
    public abstract CommandContext<S> constructCommandContext(final S commandSource, final CommandInstance command, final ParameterSet parameter);

}
