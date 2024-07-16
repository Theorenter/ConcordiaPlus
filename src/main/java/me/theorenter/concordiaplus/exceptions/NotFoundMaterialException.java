package me.theorenter.concordiaplus.exceptions;

import org.jetbrains.annotations.NotNull;

public class NotFoundMaterialException extends Exception {
    public NotFoundMaterialException(@NotNull final String message) {super(message);}
}
