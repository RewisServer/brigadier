package dev.volix.lib.brigadier.parameter;

/**
 * @author Tobias Büser
 */
public class BooleanParameter implements ParameterType<Boolean> {

    @Override
    public Boolean parse(final String string) {
        return Boolean.parseBoolean(string);
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

}
