package dev.volix.lib.brigadier;

import dev.volix.lib.brigadier.parameter.IntegerParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import dev.volix.lib.brigadier.command.Command;
import dev.volix.lib.brigadier.command.CommandInstance;
import dev.volix.lib.brigadier.command.CommandReader;
import dev.volix.lib.brigadier.command.CommandType;
import dev.volix.lib.brigadier.command.ExecutionResult;
import dev.volix.lib.brigadier.parameter.BooleanParameter;
import dev.volix.lib.brigadier.parameter.DoubleParameter;
import dev.volix.lib.brigadier.parameter.ParameterSet;
import dev.volix.lib.brigadier.parameter.ParameterType;

/**
 * @author Tobias Büser
 */
public class Brigadier {

    private static final ParameterType[] DEFAULT_TYPES = new ParameterType[] {
		    new BooleanParameter(), new IntegerParameter(), new DoubleParameter()
    };

    private static Brigadier instance;

    @Getter private final Map<Class<?>, ParameterType> registeredParameters = new HashMap<>();
    @Getter private final Map<String, CommandInstance> registeredCommands = new HashMap<>();
    private List<CommandInstance> cachedCommandsUnwound = null;

    private BrigadierAdapter adapter;
    @Setter @Getter private Object defaultResultHandler;

    private Brigadier() {
        this.registerTypes(DEFAULT_TYPES);
    }

    public static Brigadier getInstance() {
        if (instance == null) {
            instance = new Brigadier();
        }
        return instance;
    }

    public static BrigadierAdapter getAdapter() {
        return Brigadier.getInstance().adapter;
    }

    /**
     * Gets a registered command from {@link #registeredCommands} with given {@code label}.
     * The match will be true, even if only one alias of the command ({@link Command#aliases()})
     * matches the label.
     * Note: Only {@link CommandType#ROOT}s are registered in the mapping. To search for
     * every command use {@link #getCommandUnwound(String)} instead.
     *
     * @param label The label, can't be null or empty
     *
     * @return An {@link Optional} of the command. {@link Optional#empty()} if not found.
     *
     * @throws NullPointerException     If the {@code label} is null
     * @throws IllegalArgumentException If the {@code label} {@link String#isEmpty()}
     */
    public Optional<CommandInstance> getCommand(final String label) {
        if(label == null)
            throw new NullPointerException("label can't be null");
        if(label.isEmpty())
            throw new IllegalArgumentException("label can't be empty");

        return this.registeredCommands.values().stream()
            .filter(instance -> instance.getLabel().equalsIgnoreCase(label)
                || instance.getAliases().contains(label.toLowerCase()))
            .findFirst();
    }

    /**
     * Simply resets the cache.
     * After the next {@link #getCommandsUnwound()} call, the cache will be
     * refilled again.
     */
    public void clearUnwoundCache() {
        this.cachedCommandsUnwound = null;
    }

    /**
     * Gives back a list of every command known to the system, which includes
     * sub commands as well as root commands.
     * It caches the unwound commands after every {@link #register(Object...)} process,
     * so that the actual action of getting the unwound commands is a lot faster.
     * Also, it returns the original list of the cache, so if you edit this list,
     * the cache will also change. Simply setting the cached commands to null will solve this problem -
     * use
     *
     * @return The list of unwound commands as a flat structure, instead of a tree structure.
     */
    public List<CommandInstance> getCommandsUnwound() {
        if (this.cachedCommandsUnwound != null) {
            return this.cachedCommandsUnwound;
        }
        final List<CommandInstance> unwoundCommands = new ArrayList<>(this.registeredCommands.values());
        this.registeredCommands.values().forEach(instance -> unwoundCommands.addAll(instance.getChildrenRecursively()));
        return this.cachedCommandsUnwound = unwoundCommands;
    }

    /**
     * Gets all registered command, no matter if the command is a parent
     * or a child. Unwind means to remove the tree structure and having it
     * in a flat structure.
     *
     * @param label The label to find the command from within this flat structure. The label can
     *              also have the syntax of a path, then it will search for the given path.
     *
     * @return An {@link Optional} of the command. {@link Optional#empty()} if not found.
     *
     * @throws NullPointerException     If the {@code label} is null
     * @throws IllegalArgumentException If the {@code label} {@link String#isEmpty()}
     */
    public Optional<CommandInstance> getCommandUnwound(final String label) {
        if(label == null)
            throw new NullPointerException("label can't be null");
        if(label.isEmpty())
            throw new IllegalArgumentException("label can't be empty");

        final List<CommandInstance> unwoundCommands = this.getCommandsUnwound();
        return unwoundCommands.stream()
            .filter(instance -> instance.getLabel().equalsIgnoreCase(label)
                || instance.getAliases().contains(label.toLowerCase())
                || instance.getPath().equalsIgnoreCase(label))
            .findFirst();
    }

    /**
     * Sets the {@link BrigadierAdapter} for handling different instance
     * specific functions.
     *
     * @param adapter The adapter, can't be null
     *
     * @throws NullPointerException  If the {@code adapter} is {@code null}
     * @throws IllegalStateException If the {@link #adapter} is already set
     */
    public void setAdapter(final BrigadierAdapter adapter) {
        if(adapter == null)
            throw new NullPointerException("adapter can't be null");
        if(this.adapter != null)
            throw new IllegalArgumentException("there is already a brigadier instance set");

        this.adapter = adapter;
    }

    /**
     * Gets a registered parameter by given {@code typeClass}
     *
     * @param typeClass class of the parameter {@link ParameterType#getTypeClass()}, can't be null
     * @param <T>       the type
     *
     * @return {@link Optional} of stored {@link ParameterType}
     *
     * @throws NullPointerException If the {@code typeClass} is {@code null}
     */
    public <T> Optional<ParameterType<T>> getRegisteredType(final Class<T> typeClass) {
        if(typeClass == null)
            throw new NullPointerException("typeClass can't be null");

        return Optional.ofNullable(this.registeredParameters.get(typeClass));
    }

    /**
     * Registers given {@link ParameterType}s in {@link #registeredParameters}.
     *
     * @param types The respective {@link ParameterType}s
     *
     * @throws IllegalArgumentException If a parameter type with the same class is already registered
     */
    public void registerTypes(final ParameterType... types) {
        for (final ParameterType type : types) {
            if(this.registeredParameters.containsKey(type.getTypeClass()))
                throw new IllegalArgumentException(String.format("a parameter type of class %s is already registered!", type.getTypeClass()));

            this.registeredParameters.put(type.getTypeClass(), type);
        }
    }

    /**
     * Executes a {@link CommandInstance} stored inside the {@link #registeredCommands}.
     * If given {@code args} is empty, the execution will result in a failure.
     * The given {@code args} should contain the root label at index {@code 0}, as
     * otherwise we can't get the command to execute. Examplary {@code args}:
     * <pre>
     *     ban Superioz REASON 14d
     *     0   1        2      3
     * </pre>
     * If the label at index {@code 0} starts with a {@code /}, the label will get cut down
     * to remove the slash.
     *
     * @param commandSource The sender of the command, can be null
     * @param label         The label of the command
     * @param args          The command line, where index {@code 0} is the command label, can't be null
     * @param <S>           The type of the command source
     *
     * @throws NullPointerException If the {@code args} are null
     */
    public <S> ExecutionResult<S> executeCommand(final S commandSource, String label, final String[] args) {
        if(args == null)
            throw new NullPointerException("args can't be null");

        if (label.startsWith("/"))
            label = label.substring(1);

        final Optional<CommandInstance> command = this.getCommand(label);
        if (!command.isPresent()) {
            return new ExecutionResult<>(null, ExecutionResult.Code.COMMAND_NOT_FOUND, null);
        }
        final ExecutionResult<S> result = command.get().execute(commandSource, args);
        command.get().handleResult(commandSource, result);
        return result;
    }

    /**
     * Takes given {@code cursor} to determine available suggestions.
     * If the command couldn't be found or if there are not suggestions available,
     * this method returns an empty list.
     * The first argument of given {@code cursor} is the label of the root {@link CommandInstance}
     * where the tab completion methods are stored. The other arguments are only used
     * to determine at which index the cursor is. For example:
     * <pre>
     * cursor:    /ban Superioz rea
     * index :    0    1        2
     * </pre>
     * The tab completion starts with {@code 1} at all times, as without the root label
     * we can't fetch the right command to get the tab completion methods from.
     *
     * @param commandSource The source who wants to tab complete.
     * @param cursor        The current whole commandline written by the source, can't be {@code null}.
     *                      e.g.: {@code /ban Superioz rea}
     * @param <S>           The type of the source
     *
     * @return The suggestions as string list. Empty if the command couldn't be found or
     * if there are not suggestions available.
     *
     * @throws NullPointerException If the given {@code cursor} is {@code null}
     */
    public <S> List<String> executeTabCompletion(final S commandSource, final String cursor) {
        if(cursor == null)
            throw new NullPointerException("cursor can't be null");

        final List<String> parameter = ParameterSet.retrieveArguments(cursor);
        if (parameter.size() == 1) {
            return new ArrayList<>();
        }

        String rootLabel = parameter.get(0);
        rootLabel = rootLabel.replaceFirst("[/]", "");
        final CommandInstance root = this.getCommand(rootLabel).orElse(null);
        if (root == null) {
            return new ArrayList<>();
        }

        final CommandInstance leaf = root.getPathLeaf(cursor.split(" "),
            commandInstance -> Brigadier.getAdapter().checkPermission(commandSource, commandInstance));
        if (leaf == null) {
            return new ArrayList<>();
        }

        final String currentBuffer = parameter.get(parameter.size() - 1);
        final List<String> suggestions = root.getTabSuggestions(commandSource, parameter.size() - 1);
        if (!currentBuffer.trim().isEmpty()) {
            suggestions.removeIf(suggestion -> !suggestion.startsWith(currentBuffer));
        }
        return suggestions;
    }

    /**
     * Registers {@link CommandInstance}s from given {@code classes} all at once, meaning
     * that all command methods will be taken into account, even across multiple
     * classes.
     * The search-for-command logic is passed on to a new {@link CommandReader} instance.
     * If a {@link CommandInstance} with the same label as a new found command already exists,
     * the command will be ignored and not handled otherwise.
     * For registering the command at other places (e.g. bukkit command map), this method
     * calls the initialized {@link BrigadierAdapter} ({@link #adapter}).
     *
     * @param classes The respective command classes
     *
     * @throws IllegalStateException If the {@link #adapter} hasn't been set yet
     * @see BrigadierAdapter#handleRegister(String, CommandInstance)
     */
    public CommandRegisterProcess register(final Object... classes) {
        if(this.adapter == null)
            throw new IllegalStateException("cannot register commands without an initialized BrigadierAdapter!");
        return new CommandRegisterProcess(Arrays.asList(classes));
    }

    /**
     * @author Tobias Büser
     */
    public class CommandRegisterProcess {

        /**
         * If the register process is capsulated, the process
         * can access already registered commands as parents.
         * <p>
         * By default, only classes put into the process are
         * used to search for fitting parents.
         */
        private boolean capsulated = true;

        /**
         * Seperated means that every class put into the process
         * will be registered seperately instead of using them as
         * one big tree structure.
         */
        private boolean seperated = false;

        private final List<Object> commandClasses;

        public CommandRegisterProcess(final List<Object> classes) {
            this.commandClasses = classes;
        }

        public CommandRegisterProcess capsulated(final boolean flag) {
            this.capsulated = flag;
            return this;
        }

        public CommandRegisterProcess seperated(final boolean flag) {
            this.seperated = flag;
            return this;
        }

        /**
         * Executes the registering progress.
         */
        public void execute() {
            final List<List<Object>> toRegister = this.seperated
                ? this.commandClasses.stream().map(Collections::singletonList).collect(Collectors.toList())
                : Collections.singletonList(this.commandClasses);
            for (final List<Object> instances : toRegister) {
                final List<CommandInstance> roots = new CommandReader(this.capsulated, instances.toArray()).read();

                for (final CommandInstance root : roots) {
                    if (root == null || Brigadier.this.registeredCommands.containsKey(root.getLabel()))
                        continue;

                    Brigadier.this.registeredCommands.put(root.getLabel(), root);
                    Brigadier.this.adapter.handleRegister(root.getLabel(), root);
                }
            }

            // recache the unwound commands, as the map itself changed
            Brigadier.this.cachedCommandsUnwound = null;
            Brigadier.this.getCommandsUnwound();
        }

    }

}
