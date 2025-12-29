package me.adamix.mercury.data.exception;

import org.jetbrains.annotations.NotNull;

public class MissingFieldException extends Exception {
    public MissingFieldException(@NotNull String fieldName) {
        super("Missing field with '" + fieldName + "' name");
    }
}
