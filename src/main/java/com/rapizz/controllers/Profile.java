package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Client;
import com.rapizz.models.OperationCompte;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Profile implements Initializable {
	private final Client client = RaPizz.getClient();

	@FXML
	private TextField name;
	@FXML
	private TextField email;
	@FXML
	private Label updateProfileError;
	@FXML
	private Label account;
	@FXML
	private TextField creditAccount;
	@FXML
	private Label creditAccountError;
	@FXML
	private Button creditAccountButton;
	@FXML
	private Label newAccount;
	@FXML
	private Label loyalty;
	@FXML
	private GridPane history;
	@FXML
	private Label emptyHistory;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.updateProfileError, this.creditAccountError, this.newAccount, this.emptyHistory);
		this.name.setText(this.client.getNom());
		this.email.setText(this.client.getEmail());
		this.updateProfileError.visibleProperty().bind(this.updateProfileError.textProperty().isNotEmpty());
		this.account.setText(RaPizz.formatMoney(this.client.getCompte(), false));
		this.newAccount.visibleProperty().bind(this.creditAccount.textProperty().isNotEmpty());
		this.creditAccountButton.disableProperty().bind(this.creditAccount.textProperty().isEmpty());
		this.loyalty.setText(this.client.getCompteurFidelite() + "/10");
		this.emptyHistory.visibleProperty().bind(Bindings.size(this.history.getChildren()).isEqualTo(0));
		this.updatePayementHistory();
		this.creditAccountError.setVisible(false);
		this.creditAccount.textProperty().addListener((observable, oldValue, newValue) -> {
			BigDecimal creditAccount = this.getCreditAccountValue();
			if (creditAccount != null)
				this.newAccount.setText("Nouveau solde : " + RaPizz.formatMoney(this.client.getCompte().add(creditAccount)));
		});
		this.account.prefWidthProperty().bind(((Pane) this.account.getParent()).widthProperty());
	}

	private void updatePayementHistory() {
		this.history.getChildren().clear();
		List<OperationCompte> historique = this.client.getHistorique();
		for (int i = 0; i < historique.size(); i++) {
			OperationCompte operation = historique.get(i);
			Label date = new Label(RaPizz.formatDate(operation.date()));
			date.getStyleClass().add("history-item");
			this.history.add(date, 0, i);
			AnchorPane wrapper = new AnchorPane();
			Label amount = new Label(RaPizz.formatMoney(operation.montant()));
			amount.getStyleClass().addAll("history-item", operation.montant().signum() < 0 ? "history-debit" : "history-credit");
			amount.setAlignment(Pos.CENTER_RIGHT);
			wrapper.getChildren().add(amount);
			AnchorPane.setTopAnchor(amount, 0d);
			AnchorPane.setRightAnchor(amount, 0d);
			AnchorPane.setBottomAnchor(amount, 0d);
			AnchorPane.setLeftAnchor(amount, 0d);
			this.history.add(wrapper, 1, i);
		}
	}

	@FXML
	private void updateProfile(@NotNull Event event) {
		this.updateProfileError.setText(null);
		if (this.name.getText().isBlank()) {
			this.updateProfileError.setText("Le nom est invalide");
			return;
		}
		if (this.email.getText().isBlank() || !this.email.getText().matches("[^@]*@[^@]*\\.[^@.]{2,}")) {
			this.updateProfileError.setText("L'adresse mail est invalide");
			return;
		}
		Client duplicate = RaPizz.DB.getClient(this.email.getText());
		if (duplicate != null && duplicate.getId() != this.client.getId()) {
			this.updateProfileError.setText("Adresse mail déjà utilisée");
			return;
		}
		this.client.setNom(this.name.getText());
		this.client.setEmail(this.email.getText());
		RaPizz.DB.updateClient(this.client);
	}

	@FXML
	private void updateAccount(@NotNull Event event) {
		BigDecimal creditAccount = this.getCreditAccountValue();
		if (creditAccount != null) {
			this.client.setCompte(this.client.getCompte().add(creditAccount));
			RaPizz.DB.updateClient(this.client);
			this.account.setText(RaPizz.formatMoney(this.client.getCompte(), false));
			this.creditAccount.setText("");
			this.client.clearCachedHistorique();
			this.updatePayementHistory();
		}
	}

	private @Nullable BigDecimal getCreditAccountValue() {
		this.creditAccountError.setVisible(false);
		String accountText = this.creditAccount.getText();
		if (accountText == null || accountText.isBlank())
			return null;
		try {
			return BigDecimal.valueOf(Double.parseDouble(accountText.replace(',', '.')));
		} catch (NumberFormatException e) {
			this.creditAccountError.setVisible(true);
			return null;
		}
	}

	@FXML
	private void logout(@NotNull ActionEvent event) {
		RaPizz.logout();
		RaPizz.setPage(null, "Home");
	}
}
