package com.rapizz.models;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record OperationCompte(long id, @NotNull BigDecimal montant, @NotNull Timestamp date) {
	public boolean credit() {
		return this.montant.signum() == 1;
	}
}
