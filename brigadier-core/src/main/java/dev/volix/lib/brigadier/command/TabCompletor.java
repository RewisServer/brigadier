package dev.volix.lib.brigadier.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for command specific tab completion methods
 *
 * @author Tobias Büser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TabCompletor {

    /**
     * The label of the command connected with this tab completion.
     * If the label is empty, the tab completion will be applied to
     * every command registered in the same tree structure.
     *
     * @return The target's command label
     */
    String command() default "";

}
