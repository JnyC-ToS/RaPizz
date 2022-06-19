package com.rapizz.models;

import org.jetbrains.annotations.NotNull;

public record Vehicule(short id, @NotNull String nom, @NotNull VehiculeType type) {}
