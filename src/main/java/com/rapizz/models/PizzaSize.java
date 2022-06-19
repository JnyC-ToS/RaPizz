package com.rapizz.models;

import org.jetbrains.annotations.NotNull;

public record PizzaSize(byte id, @NotNull String nom, float modificateurPrix) {}
