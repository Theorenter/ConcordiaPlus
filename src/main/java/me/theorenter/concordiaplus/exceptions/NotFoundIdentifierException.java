package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundIdentifierException extends Exception {
    public NotFoundIdentifierException(@NotNull final String message) {super(message);}
}
