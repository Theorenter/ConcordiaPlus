package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundItemTypeException extends Exception {
    public NotFoundItemTypeException(@NotNull final String message) {super(message);}
}
