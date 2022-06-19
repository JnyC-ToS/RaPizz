package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Client;
import com.rapizz.models.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ResourceBundle;

public class OrderDetails implements Initializable {
	private final @NotNull Order order;

	public OrderDetails(@NotNull Order order) {
		this.order = order;
	}

	@FXML
	private Label orderId;
	@FXML
	private Label state;
	@FXML
	private Label dateOrder;
	@FXML
	private Label client;
	@FXML
	private Label livreurTitle;
	@FXML
	private Label livreur;
	@FXML
	private Label vehiculeTitle;
	@FXML
	private Label vehicule;
	@FXML
	private Label dateDeliveryTitle;
	@FXML
	private Label dateDelivery;
	@FXML
	private ImageView image;
	@FXML
	private Label name;
	@FXML
	private Label size;
	@FXML
	private Label price;
	@FXML
	private Label sizedPrice;
	@FXML
	private Button cancelButton;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.livreurTitle, this.livreur, this.vehiculeTitle, this.vehicule, this.dateDeliveryTitle, this.dateDelivery, this.cancelButton);
		this.livreurTitle.visibleProperty().bind(this.livreur.visibleProperty());
		this.vehiculeTitle.visibleProperty().bind(this.vehicule.visibleProperty());
		this.dateDeliveryTitle.visibleProperty().bind(this.dateDelivery.visibleProperty());
		this.cancelButton.setVisible(this.order.state() == Order.State.PENDING);
		this.orderId.setText("Commande #" + this.order.id());
		this.state.setText(this.order.state().toString());
		this.dateOrder.setText(RaPizz.formatDate(this.order.dataCommande()));
		this.client.setText(RaPizz.getClient().getNom());
		if (this.order.livreur() == null)
			this.livreur.setVisible(false);
		else
			this.livreur.setText(this.order.livreur().nom());
		if (this.order.vehicule() == null)
			this.vehicule.setVisible(false);
		else
			this.vehicule.setText(this.order.vehicule().nom() + " (" + this.order.vehicule().type().nom() + ")");
		if (this.order.dateLivraison() == null)
			this.dateDelivery.setVisible(false);
		else
			this.dateDelivery.setText(RaPizz.formatDate(this.order.dateLivraison()));
		this.image.setImage(RaPizz.getImage("pizzas/" + this.order.pizza().getId() + ".jpg"));
		this.name.setText(this.order.pizza().getNom());
		this.size.setText(this.order.taille().nom());
		this.price.setText(RaPizz.formatMoney(this.order.pizza().getPrix()));
		if (this.order.gratuite())
			this.sizedPrice.setText("Gratuite (points de fidélité)");
		else if (this.order.enRetard())
			this.sizedPrice.setText("Gratuite (livraison en retard)");
		else
			this.sizedPrice.setText(RaPizz.formatMoney(this.order.prix()));
	}

	@FXML
	private void backToOrderList(@NotNull ActionEvent event) {
		RaPizz.setPage("Commandes", "OrderList");
	}

	@FXML
	private void cancelOrder(@NotNull ActionEvent event) {
		Client client = RaPizz.getClient();
		RaPizz.DB.deleteOrder(this.order.id());
		if (this.order.gratuite())
			client.setCompteurFidelite((short) 10);
		else {
			client.setCompteurFidelite((short) Math.max(client.getCompteurFidelite() - 1, 0));
			client.setCompte(client.getCompte().add(this.order.prix()));
		}
		RaPizz.setPage("Commandes", "OrderList");
	}
}
