package dev.volix.lib.brigadier.command;

import dev.volix.lib.brigadier.Brigadier;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.context.CommandUsage;
import dev.volix.lib.brigadier.parameter.ParameterSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * A wrapper class of {@link Command} and the class which executes the command itself
 *
 * @author Tobias Büser
 */
public class CommandInstance {

    @Getter private String label;
    @Getter private String parentName;
    @Getter private List<String> aliases;
    @Getter private String description;
    @Getter private String permission;
    @Getter private CommandUsage usage;
    @Getter private Class<?> commandTarget;
    @Getter private boolean async;

    /**
     * The path of the command (e.g.: {@code root.parent.this})
     */
    @Getter String path;

    /**
     * Type of the command, either this command instance is the root
     * command ({@link CommandType#ROOT}) or a sub command of a root command
     * ({@link CommandType#SUB}).
     */
    @Getter private CommandType commandType;

    /**
     * The root command of this command instance.
     * If this instance is already the root command then this
     * instance equals {@code null}
     */
    @Getter CommandInstance root;

    /**
     * The parent command of this command instance.
     * If this instance is already the root command then {@code parent}
     * equals {@code null}, otherwise it's the subcommand directly in order
     * before this command.
     */
    @Getter CommandInstance parent;

    /**
     * All children of this command mapped with their
     * respective {@link Command#label()}
     */
    @Getter private final Map<String, CommandInstance> children = new HashMap<>();

    /**
     * The object instance of the class containing the command {@link #method}
     */
    @Getter private final Object methodClassObject;

    /**
     * The command method itself
     */
    @Getter private final Method method;

    /**
     * The class objects mapped with their respective tab completor method
     *
     * @see TabCompletor
     */
    Map<Method, Object> tabCompletionMap = new HashMap<>();

    /**
     * The class objects mapped with their respective result handler method
     *
     * @see ResultHandler
     */
    Map<Method, Object> resultHandlerMap = new HashMap<>();

    public CommandInstance(final Object methodClassObject, final Method method) {
        this.methodClassObject = methodClassObject;
        this.method = method;
        if (!method.isAnnotationPresent(Command.class))
            return;

        final Command command = method.getAnnotation(Command.class);
        this.label = command.label();
        this.parentName = command.parent();
        this.aliases = Arrays.stream(command.aliases())
            .map(String::toLowerCase).filter(string -> !string.isEmpty())
            .collect(Collectors.toList());
        this.description = command.desc();
        this.permission = command.permission();
        this.usage = new CommandUsage(this.label, command.usage());
        this.commandTarget = command.target() == Object.class ? Brigadier.getAdapter().getCommandSourceClass() : command.target();
        this.async = command.async();

        this.commandType = this.parentName.isEmpty() ? CommandType.ROOT : CommandType.SUB;
    }

    /**
     * Gets the path of the tree structure excluding this instance.
     *
     * @return The list of command instances in this path.
     */
    public List<String> getParentPath() {
        final List<String> whole = Arrays.asList(this.path.split("\\."));
        return whole.size() == 1 ? new ArrayList<>() : whole.subList(0, whole.size() - 1);
    }

    /**
     * Get the list of children of this instance.
     * I know, this method is named wrongly (child -> children), but
     * another method is already called "getChildren", sorry lol.
     *
     * @return The children as {@link CommandInstance} list. Empty if no children found
     */
    public List<CommandInstance> getChildrens() {
        return new ArrayList<>(this.children.values());
    }

    /**
     * Returns a list of all children, children of children, ...
     *
     * @return List of children recursively
     */
    public List<CommandInstance> getChildrenRecursively() {
        final List<CommandInstance> children = this.getChildrens();

        for (final CommandInstance child : new ArrayList<>(children)) {
            children.addAll(child.getChildrenRecursively());
        }
        return children;
    }

    /**
     * Gets a children with given {@code label}
     *
     * @param label The label/alias of the command, can't be {@code null}
     *
     * @return {@link Optional} of the instance. {@link Optional#empty()} if null
     *
     * @throws NullPointerException If the {@code label} is {@code null}
     */
    public Optional<CommandInstance> getChild(final String label) {
        if(label == null)
            throw new NullPointerException("label can't be null");

        for (final CommandInstance command : this.getChildrens()) {
            if (command.getLabel().equalsIgnoreCase(label)
                || command.getAliases().contains(label.toLowerCase())) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the depth of the vagina fetched from the {@link #path} string
     *
     * @return The depth in inch squared
     */
    public int getDepth() {
        return this.path.split("\\.").length;
    }

    /**
     * Gets the last registered sub command of this instance from given {@code path}.
     *
     * @param path      The path to search the leaf from, can't be {@code null}
     * @param predicate The predicate to check each path step, can be {@code null}
     *
     * @return The last instance of the path or {@code this}, can be {@code null}
     *
     * @throws NullPointerException If the {@code path} is {@code null}
     */
    public CommandInstance getPathLeaf(String[] path, final Predicate<CommandInstance> predicate) {
        if(path == null)
            throw new NullPointerException("path can't be null");

        CommandInstance children = this;
        while (path.length != 0) {
            if (!predicate.test(children)) {
                return null;
            }

            final String[] finalPath = path;
            final Optional<CommandInstance> child = children.getChildren().values().stream()
                .filter(subChild -> subChild.getLabel().equalsIgnoreCase(finalPath[0])
                    || subChild.getAliases().contains(finalPath[0].toLowerCase())).findFirst();
            if (child.isPresent()) {
                children = child.get();

                path = Arrays.copyOfRange(path, 1, path.length);
                continue;
            }
            break;
        }
        return children;
    }

    public CommandInstance getPathLeaf(final String[] path) {
        return this.getPathLeaf(path, instance -> true);
    }

    /**
     * Executes this command class by using the given {@code commandSource} and {@code args}.
     *
     * @param commandSource The source of the command, can be null
     * @param args          The arguments, can't be null
     * @param <S>           The type of the source
     *
     * @return The result of the execution. {@link ExecutionResult.Code#PASSED} for success.
     *
     * @throws NullPointerException If the {@code args} are null
     */
    public <S> ExecutionResult<S> execute(final S commandSource, String[] args) {
        if(args == null)
            throw new NullPointerException("args can't be null");

        // first check for master permission
        if (!this.getPermission().isEmpty() && !Brigadier.getAdapter().checkPermission(commandSource, this)) {
            return new ExecutionResult<>(null, ExecutionResult.Code.NO_PERMISSION, null);
        }

        // get the command of the last argument
        // can be a root command or a sub command
        final CommandInstance children = this.getPathLeaf(args, instance
            -> instance.getPermission().isEmpty() || Brigadier.getAdapter().checkPermission(commandSource, instance));
        if (children == null) {
            return new ExecutionResult<>(null, ExecutionResult.Code.NO_PERMISSION, null);
        }

        args = Arrays.copyOfRange(args, children.getDepth() - 1, args.length);
        final ParameterSet parameter = new ParameterSet(String.join(" ", args).trim());

        // check source
        if (commandSource != null && !children.getCommandTarget().isAssignableFrom(commandSource.getClass())) {
            return new ExecutionResult<>(children, ExecutionResult.Code.WRONG_SOURCE, null);
        }

        // check length of arguments
        if (parameter.size() < children.getUsage().getNeededSize()) {
            return new ExecutionResult<>(children, ExecutionResult.Code.TOO_FEW_ARGUMENTS, null);
        }

        // execute command (async)
        final CompletableFuture<S> future = new CompletableFuture<>();
        if (children.isAsync()) {
            Brigadier.getAdapter().runAsync(() -> {
                children.invokeMethod(commandSource, parameter);
                future.complete(commandSource);
            });
        } else {
            children.invokeMethod(commandSource, parameter);
            future.complete(commandSource);
        }
        return new ExecutionResult<>(children, ExecutionResult.Code.PASSED, future);
    }

    /**
     * Get tab suggestions for given {@code index}.
     * If this command instance is not the root command of the
     * tree structure it will take the {@link #root} instance instead,
     * as the tab suggestion methods are only stored in the root instance.
     *
     * @param commandSource The source executing the tab completion
     * @param index         The index of the cursor, can't be less than {@code 1}
     * @param <S>           The type of command source
     *
     * @return the suggestions as string list
     */
    public <S> List<String> getTabSuggestions(final S commandSource, final int index) {
        if(index <= 0)
            throw new IllegalArgumentException(String.format("The index can't be less or equals zero, you supplied %s", index));

        final CommandInstance root = this.root == null ? this : this.root;

        final Set<Method> methods = root.tabCompletionMap.keySet();
        methods.removeIf(m -> {
            final TabCompletor completor = m.getAnnotation(TabCompletor.class);
            if (completor == null)
                return true;
            return !(completor.command().isEmpty() || completor.command().equalsIgnoreCase(root.label));
        });

        final List<String> suggestions = new ArrayList<>();
        for (final Method method : methods) {
            try {
                final List list = (List) method.invoke(root.tabCompletionMap.get(method), commandSource, index);
                for (final Object o : list) {
                    if (o instanceof String)
                        suggestions.add((String) o);
                }
            } catch (final IllegalAccessException | InvocationTargetException e) {
                // do nothing
            }
        }
        return suggestions;
    }

    /**
     * Handles giving result, if the root command contains a corresponding
     * {@link ResultHandler} method.
     *
     * @param commandSource The current command source
     * @param result        The result
     * @param <S>           The command source type
     */
    public <S> void handleResult(final S commandSource, final ExecutionResult<S> result) {
        final CommandInstance root = this.root == null ? this : this.root;
        final Set<Method> methods = root.resultHandlerMap.keySet();

        for (final Method method : methods) {
            try {
                method.invoke(root.resultHandlerMap.get(method),
                    commandSource, result.getCommand() == null ? this : result.getCommand(), result);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                // do nothing
            }
        }
    }

    /**
     * Simply invokes the command method ({@link #method}) with given parameters.
     *
     * @param commandSource The command source, can be any type and null
     * @param parameter     The parameters of the command
     * @param <S>           The type of the source
     */
    private <S> void invokeMethod(final S commandSource, final ParameterSet parameter) {
        final CommandContext<S> context = Brigadier.getAdapter().constructCommandContext(commandSource, this, parameter);

        try {
            this.getMethod().invoke(this.getMethodClassObject(), commandSource, context, parameter);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

}
