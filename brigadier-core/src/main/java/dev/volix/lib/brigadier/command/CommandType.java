package dev.volix.lib.brigadier.command;

/**
 * @author Tobias Büser
 */
public enum CommandType {

    /**
     * Root command instance (e.g. {@code /fly [sub]} where {@code fly} is the root command)
     */
    ROOT,

    /**
     * Subcommand instance (e.g. {@code /fly [sub]} where {@code sub} is the sub command)
     */
    SUB

}
