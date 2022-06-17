package com.rapizz.models;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record Ingredient(short id, @NotNull String nom, @NotNull BigDecimal cout) {}
