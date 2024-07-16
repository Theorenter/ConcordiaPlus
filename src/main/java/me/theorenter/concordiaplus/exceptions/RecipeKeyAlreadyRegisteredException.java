package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class RecipeKeyAlreadyRegisteredException extends Exception {
    public RecipeKeyAlreadyRegisteredException(@NotNull final String message) {super(message);}
}
