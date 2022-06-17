package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Pizza;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ResourceBundle;

public class PizzaItem implements Initializable {
	private final @NotNull Pizza pizza;

	public PizzaItem(@NotNull Pizza pizza) {
		this.pizza = pizza;
	}

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
		this.price.setText(this.pizza.getPrix().toPlainString() + " €");
	}

	@FXML
	private void onInfoButtonClicked(@NotNull ActionEvent event) {
		System.out.println("Info : " + this.pizza.getNom());
	}
}
