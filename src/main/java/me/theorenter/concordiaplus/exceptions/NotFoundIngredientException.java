package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundIngredientException extends Exception {
    public NotFoundIngredientException(@NotNull final String message) {super(message);}
}
