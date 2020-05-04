package dev.volix.lib.brigadier.command;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;

/**
 * @author Tobias Büser
 */
public class ExecutionResult<S> {

    @Getter private final CommandInstance command;
    @Getter private final Code result;
    @Getter private final boolean passed;
    private final CompletableFuture<S> future;

    public ExecutionResult(final CommandInstance command, final Code result, final CompletableFuture<S> future) {
        this.command = command;
        this.result = result;
        this.passed = result == Code.PASSED;
        this.future = future;
    }

    public Optional<CompletableFuture<S>> getFuture() {
        return Optional.ofNullable(this.future);
    }

    public enum Code {

        PASSED,
        COMMAND_NOT_FOUND,
        TOO_FEW_ARGUMENTS,
        WRONG_SOURCE,
        NO_PERMISSION

    }

}
