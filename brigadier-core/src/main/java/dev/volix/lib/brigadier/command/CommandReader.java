package dev.volix.lib.brigadier.command;

import dev.volix.lib.brigadier.Brigadier;
import dev.volix.lib.brigadier.context.CommandContext;
import dev.volix.lib.brigadier.parameter.ParameterSet;
import dev.volix.lib.brigadier.util.Reflections;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Tobias Büser
 */
public class CommandReader {

    private final boolean capsulated;
    private final Object[] commandClassObjects;

    public CommandReader(final boolean capsulated, final Object... commandClassObjects) {
        this.capsulated = capsulated;
        this.commandClassObjects = commandClassObjects;
    }

    /**
     * Takes the {@link #commandClassObjects} and fetches every command
     * declared in this cluster.
     *
     * @return The list of commands found. Empty if no commands found
     */
    public List<CommandInstance> read() {
        final Map<String, CommandInstance> commands = new HashMap<>();
        final Map<Method, Object> commandMethods = new HashMap<>();
        final List<CommandInstance> roots = new ArrayList<>();

        // list all methods that are commandable
        for (final Object o : this.commandClassObjects) {
            for (final Method m : o.getClass().getDeclaredMethods()) {
                if (!this.checkMethod(m)) continue;
                commandMethods.put(m, o);
            }
        }

        // list all commands
        for (final Method m : commandMethods.keySet()) {
            final CommandInstance instance = new CommandInstance(commandMethods.get(m), m);
            if (instance.getCommandType() == CommandType.ROOT) roots.add(instance);

            commands.put(instance.getLabel(), instance);
        }

        if (!roots.isEmpty()) {
            for (final CommandInstance root : roots) {
                this.initRelations(root, commands);
            }
            this.initHandleMethods(roots, this.commandClassObjects);
        } else {
            this.initRelations(null, commands);
        }

        // check for empty result handlers
        final List<CommandInstance> emptyResultHandlers = roots.stream().filter(cmd -> cmd.resultHandlerMap.isEmpty()).collect(Collectors.toList());
        if (!emptyResultHandlers.isEmpty() && Brigadier.getInstance().getDefaultResultHandler() != null) {
            this.initHandleMethods(emptyResultHandlers, Brigadier.getInstance().getDefaultResultHandler());
        }

        return roots;
    }

    /**
     * Initialises the relationships between the root command and the other commands
     *
     * @param root     The root command
     * @param commands The commands
     */
    private void initRelations(final CommandInstance root, final Map<String, CommandInstance> commands) {
        for (final CommandInstance cmd : commands.values()) {
            CommandInstance parent = commands.get(cmd.getParentName());
            if (parent == null) {
                // if the command reader is not capsulated from the already
                // registered commands, we can search for our parent there as well.
                if (!this.capsulated) {
                    final Optional<CommandInstance> command = Brigadier.getInstance().getCommandUnwound(cmd.getParentName());
                    if (command.isPresent()) parent = command.get();
                    else continue;
                } else {
                    continue;
                }
            }

            if (cmd.getParent() == null) {
                cmd.parent = parent;
            }
            if (cmd.getRoot() == null) {
                cmd.root = root;
            }
            if (!parent.getChildrens().contains(cmd))
                parent.getChildren().put(cmd.getLabel(), cmd);
        }

        // init path string
        commands.values().forEach(instance -> {
            final String path = instance.getPath();

            if (path != null)
                return;
            final StringBuilder newPath = new StringBuilder(instance.getLabel());

            CommandInstance current = instance;
            CommandInstance parent;
            while ((parent = current.getParent()) != null) {
                current = parent;
                newPath.insert(0, parent.getLabel() + ".");
            }
            instance.path = newPath.toString();

            // if root is null, we now have the highest point, and we just set this instance
            // as the root
            if (instance.root == null && root == null) {
                instance.root = current;
            }
        });
    }

    /**
     * Initialises different handle methods for given commands
     *
     * @param rootCommands The root commands
     * @param classObjects The classes objects
     */
    private void initHandleMethods(final List<CommandInstance> rootCommands, final Object... classObjects) {
        for (final Object classObject : classObjects) {
            for (final Method declaredMethod : classObject.getClass().getDeclaredMethods()) {
                for (final CommandInstance cmd : rootCommands) {
                    if (this.checkTabCompleteMethod(declaredMethod)) {
                        cmd.tabCompletionMap.put(declaredMethod, classObject);
                    }
                    if (this.checkResultHandlerMethod(declaredMethod)) {
                        cmd.resultHandlerMap.put(declaredMethod, classObject);
                    }
                }
            }
        }
    }

    /**
     * Checks if the method is tabcompletable. Example method:
     * <pre>
     * &#64;TabCompletor
     * public List&#60;String&#62; onTabComplete(int index) {
     * }
     * </pre>
     * The method name is not important, though.
     *
     * @param method The method to be checked
     *
     * @return The result. {@code true} = the method has the tab completor structure.
     */
    private boolean checkTabCompleteMethod(final Method method) {
        return Reflections.checkMethodErasure(method, List.class, TabCompletor.class,
            new Class<?>[] {Brigadier.getAdapter().getCommandSourceClass(), int.class});
    }

    /**
     * Checks if the method is result handlable. Example method:
     * <pre>
     * &#64;ResultHandler
     * public &#60;S&#62; void onResultHandle(CommandContext&#60;S&#62; context, ExecutionResult&#60;S&#62; result) {
     * }
     * </pre>
     * The method name is not important, though.
     *
     * @param method The method to be checked
     *
     * @return The result. {@code true} = the method has the result handler structure.
     */
    private boolean checkResultHandlerMethod(final Method method) {
        return Reflections.checkMethodErasure(method, ResultHandler.class,
            new Class<?>[] {Brigadier.getAdapter().getCommandSourceClass(), CommandInstance.class, ExecutionResult.class});
    }

    /**
     * Checks if the method is commandable. Example method:
     * <pre>
     * &#64;Command(label = "command")
     * public &#60;S&#62; void onCommand(S commandSource, CommandContext&#60;S&#62; context, ParameterSet parameter) {
     *     // do something
     * }
     * </pre>
     * The method name is not important, though.
     *
     * @param method The method to be checked
     *
     * @return The result. {@code true} = the method has the tab completor structure.
     */
    private boolean checkMethod(final Method method) {
        final Class<?>[] parameters = new Class<?>[] {Brigadier.getAdapter().getCommandSourceClass(),
                                                      CommandContext.class, ParameterSet.class};
        return Reflections.checkMethodErasure(method, Command.class, parameters);
    }

}
