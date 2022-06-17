package com.rapizz.models;

import com.rapizz.RaPizz;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class Pizza {
	private final short id;
	private final @NotNull String nom;
	private final @NotNull BigDecimal prix;
	private final int classement;
	private @Nullable List<@NotNull Ingredient> ingredients;

	public Pizza(short id, @NotNull String nom, @NotNull BigDecimal prix, int classement) {
		this.id = id;
		this.nom = nom;
		this.prix = prix;
		this.classement = classement;
	}

	public short getId() {
		return this.id;
	}

	public @NotNull String getNom() {
		return this.nom;
	}

	public @NotNull BigDecimal getPrix() {
		return this.prix;
	}

	public int getClassement() {
		return this.classement;
	}

	public @NotNull List<@NotNull Ingredient> getIngredients() {
		if (this.ingredients == null)
			this.ingredients = RaPizz.DB.listIngredientsOfPizza(this.id);
		return this.ingredients;
	}
}
