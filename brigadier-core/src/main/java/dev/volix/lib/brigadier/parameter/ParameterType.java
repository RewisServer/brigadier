package dev.volix.lib.brigadier.parameter;

/**
 * @author Tobias Büser
 */
public interface ParameterType<T> {

    /**
     * Resolves given {@code string} to {@code T}
     *
     * @param string the string to be resolved
     *
     * @return the resolved object, can be null
     */
    T parse(final String string);

    /**
     * @return The class of the parameter type
     */
    Class<T> getTypeClass();

}
