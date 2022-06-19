package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Order;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ResourceBundle;

public class OrderItem implements Initializable {
	private final @NotNull Order order;

	public OrderItem(@NotNull Order order) {
		this.order = order;
	}

	@FXML
	private Region root;
	@FXML
	private ImageView image;
	@FXML
	private Label name;
	@FXML
	private Label info;
	@FXML
	private Label size;
	@FXML
	private Label price;
	@FXML
	private Label state;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		this.image.setImage(RaPizz.getImage("pizzas/" + this.order.pizza().getId() + ".jpg"));
		this.name.setText(this.order.pizza().getNom());
		this.info.setText("Commande #" + this.order.id() + " - " + RaPizz.formatDate(this.order.dataCommande()));
		this.size.setText("Taille : " + this.order.taille().nom());
		this.price.setText(RaPizz.formatMoney(this.order.prix()));
		this.state.setText(this.order.state().toString());
		this.state.getStyleClass().add("order-state-" + this.order.state().name().toLowerCase());
		this.root.setOnMouseClicked(event -> RaPizz.setPage("Commande #" + this.order.id(), "OrderDetails", new OrderDetails(this.order)));
	}
}
