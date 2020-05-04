package dev.volix.lib.brigadier.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Getter;
import dev.volix.lib.brigadier.command.Command;

/**
 * Wrapper class for {@link Command#usage()}
 *
 * @author Tobias Büser
 */
public class CommandUsage {

    /**
     * The pattern of a valid {@link Command#usage()}
     */
    public static final Pattern USAGE_PATTERN = Pattern.compile("(([<\\[(])[a-zA-Z_0-9:|()-]+([>\\])])( )?)+");

    /**
     * This pattern defines a parameter which needs to be passed
     */
    private static final Pattern PARAM_NEEDED = Pattern.compile("<[0-9a-zA-Z_:|()-]+>");

    /**
     * Label of the command
     */
    @Getter private final String label;

    /**
     * The raw command usage
     */
    @Getter private final String base;

    /**
     * The parameter of the usage
     */
    @Getter private final List<String> params = new ArrayList<>();

    /**
     * Map of parameter as key and as value if the param is needed
     */
    private final Map<String, Boolean> paramMap = new HashMap<>();

    public CommandUsage(final String label, final String base) {
        this.label = label;
        this.base = base;

        if (!USAGE_PATTERN.matcher(base).matches())
            return;
        for (String match : base.split(" ")) {
            final boolean needed = PARAM_NEEDED.matcher(match).matches();
            match = needed ? match.replaceAll("[<>]", "") : match.contains("[")
                ? match.replaceAll("[\\[\\]]", "") : match;

            this.params.add(match);
            this.paramMap.put(match, needed);
        }
    }

    /**
     * Get the needed size of arguments
     *
     * @return The size as int
     */
    public int getNeededSize() {
        int count = 0;
        for (final String param : this.params) {
            if (!this.paramMap.get(param))
                break;
            count++;
        }
        return count;
    }

    /**
     * Get the parameter at given index
     *
     * @param index The index
     *
     * @return The parameter
     */
    public String getParam(final int index) {
        if (index >= this.params.size() || index < 0)
            return null;
        return this.params.get(index);
    }

    /**
     * Checks if the given key is needed by checking the bool inside the paramMap
     * {@literal <}{@literal >} = needed; [] = optional
     *
     * @param key The key
     *
     * @return The result
     */
    public boolean isNeeded(final String key) {
        return this.paramMap.containsKey(key) && this.paramMap.get(key);
    }

    public boolean isNeeded(final int index) {
        final String key = this.getParam(index);
        return key != null && this.isNeeded(key);
    }

    @Override
    public String toString() {
        return this.label + (this.base.isEmpty() ? "" : " " + this.base);
    }

}
