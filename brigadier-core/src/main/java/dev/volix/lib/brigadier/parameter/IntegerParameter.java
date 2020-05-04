package dev.volix.lib.brigadier.parameter;

/**
 * @author Tobias Büser
 */
public class IntegerParameter implements ParameterType<Integer> {

    @Override
    public Integer parse(final String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public Class<Integer> getTypeClass() {
        return Integer.class;
    }

}
