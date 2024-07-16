package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class IllegalItemTypeException extends Exception {
    public IllegalItemTypeException(@NotNull final String message) {super(message);}
}
