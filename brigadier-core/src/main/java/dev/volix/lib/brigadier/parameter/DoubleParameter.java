package dev.volix.lib.brigadier.parameter;

/**
 * @author Tobias Büser
 */
public class DoubleParameter implements ParameterType<Double> {

    @Override
    public Double parse(final String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public Class<Double> getTypeClass() {
        return Double.class;
    }

}
