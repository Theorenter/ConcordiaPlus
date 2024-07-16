package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class IllegalRecipeTypeException extends Exception {
    public IllegalRecipeTypeException(@NotNull final String message) {super(message);}
}
