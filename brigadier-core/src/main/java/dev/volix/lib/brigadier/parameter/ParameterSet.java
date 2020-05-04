package dev.volix.lib.brigadier.parameter;

import dev.volix.lib.brigadier.Brigadier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * @author Tobias Büser
 */
public class ParameterSet {

    /**
     * The pattern of the different parameter. Either splitted with ' ' or with '"'<br>
     * Example: '/command subcommand arg0 arg1 "arg2 arg2.1 arg2.3" arg4' results in these arguments:<br>
     * 'arg0', 'arg1', 'arg2 arg2.1 arg2.3', 'arg4'
     */
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("([\"][^\"]*[\"])|([^\" ]+)");

    /**
     * The list of parameters
     */
    @Getter private final List<String> parameters;

    /**
     * The current parameter index. Will be increased with every {@link #getNext()}
     */
    private int paramIndex = 0;

    public ParameterSet(final List<String> parameters) {
        this.parameters = parameters;
    }

    public ParameterSet(final String commandLine) {
        this(retrieveArguments(commandLine));
    }

    /**
     * Gets all arguments from given command line with allowing '"' parts to be an argument group.
     *
     * @param commandLine The arguments as command line string, can't be {@code null}
     *
     * @return The list of parameters
     *
     * @throws NullPointerException If {@code commandLine} is {@code null}
     */
    public static List<String> retrieveArguments(final String commandLine, final boolean ignoreEmpty) {
        if(commandLine == null)
            throw new NullPointerException("command line can't be null");
        if (commandLine.isEmpty()) {
            return new ArrayList<>();
        }

        final List<String> arguments = new ArrayList<>();
        final Matcher matcher = PARAMETER_PATTERN.matcher(commandLine);
        while (matcher.find()) {
            arguments.add(matcher.group().replaceAll("\"", ""));
        }

        if ((commandLine.endsWith(" ") && !ignoreEmpty) || arguments.isEmpty())
            arguments.add("");
        return arguments;
    }

    public static List<String> retrieveArguments(final String commandLine) {
        return retrieveArguments(commandLine, false);
    }

    /**
     * @return The {@link #parameters} size
     */
    public int size() {
        return this.parameters.size();
    }

    /**
     * @return The {@link #parameters} as string
     */
    public String getCommandLine() {
        return String.join(" ", this.parameters);
    }

    /**
     * Get the raw parameter at given {@code index}
     *
     * @param index The index of the parameter
     *
     * @return The parameter as string
     */
    public String get(final int index) {
        if (index >= this.parameters.size() || index < 0)
            return null;
        return this.parameters.get(index);
    }

    public String get() {
        return this.get(this.paramIndex);
    }

    /**
     * @return The parameter at index {@link #paramIndex}--
     */
    public String getLast() {
        return this.get(this.paramIndex--);
    }

    /**
     * @return The parameter at index {@link #paramIndex}++
     */
    public String getNext() {
        return this.get(this.paramIndex++);
    }

    /**
     * Resets the current parameter index
     */
    public void resetIndex() {
        this.paramIndex = 0;
    }

    /**
     * Gets an argument range as string {@link List}
     *
     * @param from The first index inclusive
     * @param to   The last index exclusive
     *
     * @return The list of parameters inside given range
     */
    public List<String> getRange(final int from, final int to) {
        return this.getParameters().subList(from, to);
    }

    public List<String> getRange(final int from) {
        return this.getRange(from, this.size());
    }

    /**
     * Gets a specific parameter casted to {@code typeClass} if the type is registered with
     * {@link Brigadier#registerTypes(ParameterType[])}
     *
     * @param index        the parameter index, can't be out of bounds.
     * @param typeClass    the class of the parameter (e.g. {@link Integer#getClass()})
     * @param defaultValue instead of returning null, return the default value
     * @param <T>          type of the parameter
     *
     * @return The value or {@code defaultValue} if {@code null}
     */
    public <T> T get(int index, final Class<T> typeClass, final T defaultValue) {
        if (index < 0) index = 0;

        final Optional<ParameterType<T>> typeOptional = Brigadier.getInstance().getRegisteredType(typeClass);
        final String param = this.get(index);
        if (!typeOptional.isPresent() || param == null)
            return defaultValue;
        final ParameterType<T> type = typeOptional.get();

        T value;
        try {
            value = type.parse(param);
        } catch (final NumberFormatException ex) {
            value = null;
        }
        return value == null ? defaultValue : value;
    }

    public <T> Optional<T> get(final int index, final Class<T> typeClass) {
        return Optional.ofNullable(this.get(index, typeClass, null));
    }

    /**
     * Gets an {@link Integer} parameter at given index
     *
     * @param index        the parameter index, can't be out of bounds.
     * @param defaultValue instead of returning null, return the default value
     *
     * @return The parameter as {@link Integer}
     */
    public Integer getInt(final int index, final Integer defaultValue) {
        return this.get(index, Integer.class, defaultValue);
    }

    public Optional<Integer> getInt(final int index) {
        return this.get(index, Integer.class);
    }

    /**
     * Gets an {@link Double} parameter at given index
     *
     * @param index        the parameter index, can't be out of bounds.
     * @param defaultValue instead of returning null, return the default value
     *
     * @return The parameter as {@link Double}
     */
    public Double getDouble(final int index, final Double defaultValue) {
        return this.get(index, Double.class, defaultValue);
    }

    public Optional<Double> getDouble(final int index) {
        return this.get(index, Double.class);
    }

    /**
     * Gets an {@link Boolean} parameter at given index
     *
     * @param index        the parameter index, can't be out of bounds.
     * @param defaultValue instead of returning null, return the default value
     *
     * @return The parameter as {@link Boolean}
     */
    public Boolean getBoolean(final int index, final Boolean defaultValue) {
        return this.get(index, Boolean.class, defaultValue);
    }

    public Optional<Boolean> getBoolean(final int index) {
        return this.get(index, Boolean.class);
    }

    /**
     * Gets an enum from given {@code paramIndex} and {@code enumClass}
     *
     * @param paramIndex   The index of the argument
     * @param enumClass    The class of the {@link Enum}
     * @param defaultValue Instead of returning {@code null}, return this value
     * @param <E>          The enum type
     *
     * @return The enum object
     */
    public <E extends Enum> E getEnum(final int paramIndex, final Class<E> enumClass, final E defaultValue) {
        final String param = this.get(paramIndex);
        if (param == null) return defaultValue;
        final Enum[] enums = enumClass.getEnumConstants();

        // if the string is an integer, get the enum directly from the ordinal
        Integer ordinal = null;
        try {
            ordinal = Integer.parseInt(param);
        } catch (NumberFormatException ex) {
            // do nothing
        }

        if (ordinal != null) {
            if (ordinal >= enums.length || ordinal < 0) return defaultValue;
            return (E) enums[ordinal];
        }

        for (final Enum e : enums) {
            if (param.equalsIgnoreCase(e.name()))
                return (E) e;
        }
        return defaultValue;
    }

    public <E extends Enum> Optional<E> getEnum(final int paramIndex, final Class<E> enumClass) {
        return Optional.ofNullable(this.getEnum(paramIndex, enumClass, null));
    }

}
