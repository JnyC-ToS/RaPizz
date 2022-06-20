package com.rapizz.models;

import com.rapizz.RaPizz;
import com.rapizz.SHA512;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Client {
	private final long id;
	private @NotNull String nom;
	private @NotNull String email;
	private final byte @NotNull[] motDePasse;
	private @NotNull BigDecimal compte;
	private short compteurFidelite;
	private @Nullable List<@NotNull OperationCompte> historique;

	public Client(long id, @NotNull String nom, @NotNull String email, byte @NotNull[] motDePasse, @NotNull BigDecimal compte, short compteurFidelite) {
		this.id = id;
		this.nom = nom;
		this.email = email;
		this.motDePasse = motDePasse;
		this.compte = compte;
		this.compteurFidelite = compteurFidelite;
	}

	public long getId() {
		return this.id;
	}

	public @NotNull String getNom() {
		return this.nom;
	}

	public void setNom(@NotNull String nom) {
		this.nom = nom;
	}

	public @NotNull String getEmail() {
		return this.email;
	}

	public void setEmail(@NotNull String email) {
		this.email = email;
	}

	public boolean checkPassword(@NotNull String password) {
		return Arrays.equals(this.motDePasse, SHA512.hash(password));
	}

	public @NotNull BigDecimal getCompte() {
		return this.compte;
	}

	public void setCompte(@NotNull BigDecimal compte) {
		this.compte = compte;
	}

	public short getCompteurFidelite() {
		return this.compteurFidelite;
	}

	public void setCompteurFidelite(short compteurFidelite) {
		this.compteurFidelite = compteurFidelite;
	}

	public @NotNull List<@NotNull OperationCompte> getHistorique() {
		if (this.historique == null)
			this.historique = RaPizz.DB.listHistoriqueOfClient(this.id);
		return this.historique;
	}

	public void clearCachedHistorique() {
		this.historique = null;
	}
}
