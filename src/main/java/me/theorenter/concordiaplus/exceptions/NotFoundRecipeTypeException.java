package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundRecipeTypeException extends Exception {
    public NotFoundRecipeTypeException(@NotNull final String message) {super(message);}
}
