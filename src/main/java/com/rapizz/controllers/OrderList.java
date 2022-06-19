package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Order;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OrderList implements Initializable {
	@FXML
	private GridPane ordersContainer;
	@FXML
	private Label empty;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.empty);
		this.empty.visibleProperty().bind(Bindings.size(this.ordersContainer.getChildren()).isEqualTo(0));
		List<Order> orders = RaPizz.DB.listOrdersOfClient(RaPizz.getClient().getId());
		this.ordersContainer.getChildren().clear();
		for (int i = 0; i < orders.size(); i++) {
			Order order = orders.get(i);
			int row = i / 3;
			int col = i % 3;
			this.ordersContainer.add(RaPizz.loadFXML("OrderItem", new OrderItem(order)), col, row);
		}
	}
}
