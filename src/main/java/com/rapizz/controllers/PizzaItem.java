package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Client;
import com.rapizz.models.Ingredient;
import com.rapizz.models.Order;
import com.rapizz.models.Pizza;
import com.rapizz.models.PizzaSize;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PizzaItem implements Initializable {
	private final @NotNull Pizza pizza;
	private final @NotNull Popup infoPopup = new Popup();
	private final @NotNull PopupController popupController;

	public PizzaItem(@NotNull Pizza pizza) {
		this.pizza = pizza;
		this.popupController = new PopupController(this, false);
	}

	@FXML
	private Region root;
	@FXML
	private ImageView image;
	@FXML
	private Label name;
	@FXML
	private Label rank;
	@FXML
	private Label price;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		this.image.setImage(RaPizz.getImage("pizzas/" + this.pizza.getId() + ".jpg"));
		this.name.setText(this.pizza.getNom());
		this.rank.setText("#" + this.pizza.getClassement());
		this.price.setText(RaPizz.formatMoney(this.pizza.getPrix()));
		this.infoPopup.setAutoHide(true);
		this.infoPopup.setX(0);
		this.infoPopup.setY(0);
		Pane popup = RaPizz.loadFXML("PizzaInfoPopup", this.popupController);
		RaPizz.setDefaultStylesheets(popup);
		popup.prefWidthProperty().bind(RaPizz.getMainStage().widthProperty());
		popup.prefHeightProperty().bind(RaPizz.getMainStage().heightProperty());
		this.infoPopup.getContent().add(popup);
		this.root.setOnMouseClicked(event -> {
			this.infoPopup.show(RaPizz.getMainStage());
			this.popupController.setOrderMode(true);
		});
	}

	@FXML
	private void onInfoButtonClicked(@NotNull ActionEvent event) {
		this.infoPopup.show(RaPizz.getMainStage());
		this.popupController.setOrderMode(false);
		event.consume();
	}

	public static class PopupController implements Initializable {
		private final @NotNull PizzaItem main;
		private final @NotNull BooleanProperty orderMode;
		private final @NotNull ToggleGroup size = new ToggleGroup();

		public PopupController(@NotNull PizzaItem main, boolean orderMode) {
			this.main = main;
			this.orderMode = new SimpleBooleanProperty(orderMode);
		}

		public boolean isOrderMode() {
			return this.orderMode.get();
		}

		public void setOrderMode(boolean orderMode) {
			this.orderMode.set(orderMode);
		}

		public @NotNull BooleanProperty orderModeProperty() {
			return this.orderMode;
		}

		@FXML
		private Label pizzaName;
		@FXML
		private ImageView popupImage;
		@FXML
		private VBox pizzaInfo;
		@FXML
		private Label popupName;
		@FXML
		private VBox ingredients;
		@FXML
		private VBox confirmOrder;
		@FXML
		private HBox sizes;
		@FXML
		private Label sizedPrice;
		@FXML
		private VBox notEnoughMoney;
		@FXML
		private Button confirmOrderButton;
		@FXML
		private Label account;

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			RaPizz.bindManagedToVisible(this.pizzaName, this.pizzaInfo, this.confirmOrder, this.notEnoughMoney);
			this.pizzaName.visibleProperty().bind(this.confirmOrder.visibleProperty());
			this.pizzaInfo.visibleProperty().bind(this.confirmOrder.visibleProperty().not());
			this.confirmOrder.visibleProperty().bind(this.orderMode);
			this.pizzaName.textProperty().bind(this.main.name.textProperty());
			this.popupImage.imageProperty().bind(this.main.image.imageProperty());
			this.popupName.textProperty().bind(Bindings.concat(this.main.name.textProperty(), " - ", this.main.price.textProperty()));
			this.notEnoughMoney.visibleProperty().bind(this.confirmOrderButton.disabledProperty());
			this.confirmOrderButton.textProperty().bind(Bindings.concat("Payer ", this.sizedPrice.textProperty()));
			this.main.infoPopup.setOnShowing(this::load);
			this.size.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == null)
					return;
				PizzaSize size = (PizzaSize) newValue.getUserData();
				BigDecimal sizedPrice = this.main.pizza.getPrix().multiply(BigDecimal.valueOf(size.modificateurPrix()));
				String price = RaPizz.formatMoney(sizedPrice);
				this.sizedPrice.setText(price);
				BigDecimal account = RaPizz.getClient().getCompte();
				this.account.setText("Solde actuel : " + RaPizz.formatMoney(account));
				this.confirmOrderButton.setDisable(sizedPrice.compareTo(account) >= 0);
			});
		}

		private void load(@Nullable Object ignored) {
			if (this.ingredients.getChildren().isEmpty()) {
				List<Ingredient> ingredients = this.main.pizza.getIngredients();
				for (Ingredient ingredient : ingredients) {
					Label label = new Label(ingredient.nom());
					label.getStyleClass().add("pizza-ingredient");
					this.ingredients.getChildren().add(label);
				}
			}
			if (this.sizes.getChildren().isEmpty()) {
				List<PizzaSize> pizzaSizes = RaPizz.DB.listPizzaSizes();
				boolean first = true;
				for (PizzaSize pizzaSize : pizzaSizes) {
					ToggleButton button = new ToggleButton(pizzaSize.nom());
					button.setToggleGroup(this.size);
					button.setUserData(pizzaSize);
					if (first) {
						button.setSelected(true);
						first = false;
					}
					button.getStyleClass().add("pizza-size");
					this.sizes.getChildren().add(button);
				}
			}
		}

		@FXML
		private void consumeEvent(@NotNull Event event) {
			event.consume();
		}

		@FXML
		private void onPopupClosed(@NotNull Event event) {
			this.main.infoPopup.hide();
		}

		@FXML
		private void onOrderButtonClicked(@NotNull ActionEvent event) {
			this.orderMode.set(true);
		}

		@FXML
		private void onConfirmOrderButtonClicked(@NotNull ActionEvent event) {
			Client client = RaPizz.getClient();
			boolean gratuite = client.getCompteurFidelite() >= 10;
			client.setCompteurFidelite(gratuite ? 0 : (short) (client.getCompteurFidelite() + 1));
			Order order = RaPizz.DB.createOrder(client.getId(), this.main.pizza.getId(), ((PizzaSize) this.size.getSelectedToggle().getUserData()).id(), gratuite);
			if (order != null) {
				if (order.prix().signum() != -1)
					client.setCompte(client.getCompte().subtract(order.prix()));
				RaPizz.setPage("Commande #" + order.id(), "OrderDetails", new OrderDetails(order));
				this.main.infoPopup.hide();
			}
		}

		@FXML
		private void onCreditAccountClicked(@NotNull MouseEvent event) {
			// TODO open profile page
		}
	}
}
