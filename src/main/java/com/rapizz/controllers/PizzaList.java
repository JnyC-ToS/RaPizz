package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Pizza;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PizzaList implements Initializable {
	private final @NotNull Timer timer = new Timer();
	private @Nullable TimerTask searchTask;

	@FXML
	private TextField searchBar;
	@FXML
	private Label bestOffersTitle;
	@FXML
	private HBox bestOffersContainer;
	@FXML
	private Label pizzasTitle;
	@FXML
	private GridPane pizzasContainer;
	@FXML
	private Label searchTitle;
	@FXML
	private ProgressIndicator searchLoading;
	@FXML
	private Label searchEmpty;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.bestOffersTitle, this.bestOffersContainer, this.pizzasTitle, this.pizzasContainer, this.searchTitle, this.searchLoading, this.searchEmpty);
		// Hide best offers title when content is hidden
		this.bestOffersTitle.visibleProperty().bind(this.bestOffersContainer.visibleProperty());
		// Hide pizzas when loading is visible
		this.pizzasContainer.visibleProperty().bind(this.searchLoading.visibleProperty().not());
		// Hide best offers when a search exists
		this.bestOffersContainer.visibleProperty().bind(this.searchBar.textProperty().isEmpty());
		// Switch title when according to whether search exists or not
		this.pizzasTitle.visibleProperty().bind(this.searchTitle.visibleProperty().not());
		this.searchTitle.visibleProperty().bind(this.searchBar.textProperty().isNotEmpty());
		// Show empty result indicator when pizzas container is empty and not loading
		this.searchEmpty.visibleProperty().bind(Bindings.size(this.pizzasContainer.getChildren()).isEqualTo(0).and(this.searchLoading.visibleProperty().not()));

		List<Pizza> bestPizzas = RaPizz.DB.listBestPizzas();
		for (Pizza bestPizza : bestPizzas)
			this.bestOffersContainer.getChildren().add(RaPizz.loadFXML("BestPizzaItem", new PizzaItem(bestPizza)));

		this.displayPizzas(RaPizz.DB.listPizzas());
		this.searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null)
				return;
			if (this.searchTask == null)
				this.searchLoading.setVisible(true);
			else
				this.searchTask.cancel();
			this.searchTask = new TimerTask() {
				@Override
				public void run() {
					List<Pizza> results = newValue.isBlank() ? RaPizz.DB.listPizzas() : RaPizz.DB.findPizzas(newValue);
					Platform.runLater(() -> PizzaList.this.displayPizzas(results));
					PizzaList.this.searchTask = null;
				}
			};
			this.timer.schedule(this.searchTask, 1000);
		});
		RaPizz.addCloseListener(this.timer::cancel);
	}

	private void displayPizzas(@NotNull List<@NotNull Pizza> pizzas) {
		this.pizzasContainer.getChildren().clear();
		for (int i = 0; i < pizzas.size(); i++) {
			Pizza pizza = pizzas.get(i);
			int row = i / 3;
			int col = i % 3;
			this.pizzasContainer.add(RaPizz.loadFXML("PizzaItem", new PizzaItem(pizza)), col, row);
		}
		this.searchLoading.setVisible(false);
	}
}
