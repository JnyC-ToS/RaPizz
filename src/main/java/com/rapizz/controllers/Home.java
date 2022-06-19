package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Client;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {
	private final @NotNull ObjectProperty<State> state = new SimpleObjectProperty<>(State.HOME);

	@FXML
	private ImageView logo;
	@FXML
	private Label createAccountTitle;
	@FXML
	private Label loginTitle;
	@FXML
	private TextField name;
	@FXML
	private TextField email;
	@FXML
	private PasswordField password;
	@FXML
	private PasswordField passwordConfirm;
	@FXML
	private Button createAccountButton;
	@FXML
	private Button submitButton;
	@FXML
	private VBox error;
	@FXML
	private HBox mayLoginNotice;
	@FXML
	private HBox mayCreateAccountNotice;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.logo, this.createAccountTitle, this.loginTitle, this.name, this.email, this.password, this.passwordConfirm, this.createAccountButton, this.submitButton, this.error, this.mayLoginNotice, this.mayCreateAccountNotice);
		BooleanBinding isHome = this.state.isEqualTo(State.HOME);
		BooleanBinding isCreatingAccount = this.state.isEqualTo(State.CREATING_ACCOUNT);
		BooleanBinding isLoggingIn = this.state.isEqualTo(State.LOGGING_IN);
		BooleanBinding isNotHome = isHome.not();
		this.logo.visibleProperty().bind(isHome);
		this.name.visibleProperty().bind(isCreatingAccount);
		this.email.visibleProperty().bind(isNotHome);
		this.password.visibleProperty().bind(isNotHome);
		this.passwordConfirm.visibleProperty().bind(isCreatingAccount);
		this.mayLoginNotice.visibleProperty().bind(isLoggingIn.not());
		this.mayCreateAccountNotice.visibleProperty().bind(isLoggingIn);
		this.createAccountButton.visibleProperty().bind(isHome);
		this.submitButton.visibleProperty().bind(isNotHome);
		this.loginTitle.visibleProperty().bind(isLoggingIn);
		this.createAccountTitle.visibleProperty().bind(isCreatingAccount);
		this.error.visibleProperty().bind(Bindings.size(this.error.getChildren()).isNotEqualTo(0));
		RaPizz.hideTop();
	}

	@FXML
	private void createAccount(Event event) {
		this.state.set(State.CREATING_ACCOUNT);
	}

	@FXML
	private void login(MouseEvent event) {
		this.state.set(State.LOGGING_IN);
	}

	@FXML
	private void submit(ActionEvent event) {
		this.error.getChildren().clear();
		Client client = switch (this.state.get()) {
			case CREATING_ACCOUNT -> {
				if (this.invalid(this.name, this.email, this.password, this.passwordConfirm))
					yield null;
				if (!this.email.getText().matches("[^@]*@[^@]*\\.[^@.]{2,}")) {
					this.appendError("L'adresse mail est invalide");
					yield null;
				}
				if (!this.password.getText().equals(this.passwordConfirm.getText())) {
					this.passwordConfirm.setText("");
					this.appendError("Les deux mots de passe ne correspondent pas");
					yield null;
				}
				Client duplicate = RaPizz.DB.getClient(this.email.getText());
				if (duplicate != null) {
					this.appendError("Adresse mail déjà utilisée");
					yield null;
				}
				yield RaPizz.DB.createClient(this.name.getText(), this.email.getText(), this.password.getText());
			}
			case LOGGING_IN -> {
				if (this.invalid(this.email, this.password))
					yield null;
				Client match = RaPizz.DB.getClient(this.email.getText());
				if (match == null) {
					this.appendError("Adresse mail inconnue");
					yield null;
				}
				if (!match.checkPassword(this.password.getText())) {
					this.password.setText("");
					this.appendError("Mot de passe invalide");
					yield null;
				}
				yield match;
			}
			case HOME -> null;
		};
		if (client == null)
			return;
		RaPizz.login(client);
		RaPizz.showTop();
		RaPizz.setPage("Pizzas", "PizzaList");
	}

	private boolean invalid(@NotNull TextField @NotNull... fields) {
		boolean invalid = false;
		for (TextField field : fields) {
			if (field.getText().isBlank()) {
				this.appendError("Champ \"" + field.getPromptText() + "\" manquant");
				invalid = true;
			}
		}
		return invalid;
	}

	private void appendError(@NotNull String text) {
		Label error = new Label(text);
		error.getStyleClass().add("error");
		this.error.getChildren().add(error);
	}

	private enum State {
		HOME, CREATING_ACCOUNT, LOGGING_IN
	}
}
