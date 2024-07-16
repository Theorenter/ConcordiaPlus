package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundRecipeShapeException extends Exception {
    public NotFoundRecipeShapeException(@NotNull final String message) {super(message);}
}