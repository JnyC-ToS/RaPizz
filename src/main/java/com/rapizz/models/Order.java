package com.rapizz.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record Order(long id,
                    @NotNull Pizza pizza,
                    @NotNull PizzaSize taille,
                    @NotNull Timestamp dataCommande,
                    boolean gratuite,
                    @Nullable Livreur livreur,
                    @Nullable Vehicule vehicule,
                    @Nullable Timestamp dateLivraison,
                    boolean enRetard,
                    @NotNull BigDecimal prix) {
	public @NotNull State state() {
		if (this.dateLivraison != null)
			return State.DELIVERED;
		if (this.livreur != null)
			return State.DELIVERING;
		return State.PENDING;
	}

	public enum State {
		PENDING("En attente"),
		DELIVERING("En cours"),
		DELIVERED("Livrée");

		private final @NotNull String nom;

		State(@NotNull String nom) {
			this.nom = nom;
		}

		@Override
		public String toString() {
			return this.nom;
		}
	}
}
