package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundConcordiaItemException extends Exception {
    public NotFoundConcordiaItemException(@NotNull final String message) {super(message);}
}
