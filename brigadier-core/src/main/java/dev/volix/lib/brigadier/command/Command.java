package dev.volix.lib.brigadier.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tobias Büser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * The label of the command the user has to use
     * Example: /fly ('fly'=label)
     * Same goes for sub commands
     * Example: /fly change .. ('change'=subcommand label)
     *
     * @return the label
     */
    String label();

    /**
     * Aliases for the {@link #label()}
     * The user can either use the {@link #label()} or one of the aliases
     *
     * @return The aliases
     */
    String[] aliases() default {""};

    /**
     * Parent of the command.
     *
     * @return The parent
     */
    String parent() default "";

    /**
     * The description of the command.
     * Example: {@code Changes your gamemode} (for /gamemode)
     *
     * @return The description
     */
    String desc() default "";

    /**
     * The permission the user needs to execute this command.
     * This also works with sub commands.
     * Example: {@code plugin.fly} (for the command fly)
     *
     * @return The permission
     */
    String permission() default "";

    /**
     * The usage of this command
     * Example: {@code change <mode> <player>} would translate to {@code /fly change <mode> <player>}
     *
     * @return The usage
     */
    String usage() default "";

    /**
     * The command sender that is allowed to use the command
     *
     * @return The command target
     */
    Class<?> target() default Object.class;

    /**
     * Determines if the command method should be executed asynchronously.
     *
     * @return The current state. {@code true} for asynchronous execution
     */
    boolean async() default false;

}
