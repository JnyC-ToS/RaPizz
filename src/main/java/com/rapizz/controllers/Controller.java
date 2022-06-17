package com.rapizz.controllers;

import com.rapizz.RaPizz;
import com.rapizz.models.Pizza;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {
	private final @NotNull Timer timer = new Timer();
	private @Nullable TimerTask searchTask;

	@FXML
	private ScrollPane scrollPane;
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
		// Fix insanely slow default scroll pane
		this.scrollPane.getContent().setOnScroll(scrollEvent -> {
			double deltaY = scrollEvent.getDeltaY() * 0.01;
			this.scrollPane.setVvalue(this.scrollPane.getVvalue() - deltaY);
		});

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
		for (Pizza bestPizza : bestPizzas) {
			try {
				this.bestOffersContainer.getChildren().add(RaPizz.getFXMLLoader("BestPizzaItem", new PizzaItem(bestPizza)).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.displayPizzas(RaPizz.DB.listPizzas());
		this.searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
			if (this.searchTask == null)
				this.searchLoading.setVisible(true);
			else
				this.searchTask.cancel();
			this.searchTask = new TimerTask() {
				@Override
				public void run() {
					List<Pizza> results = newValue.isBlank() ? RaPizz.DB.listPizzas() : RaPizz.DB.findPizzas(newValue);
					Platform.runLater(() -> Controller.this.displayPizzas(results));
					Controller.this.searchTask = null;
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
			try {
				this.pizzasContainer.add(RaPizz.getFXMLLoader("PizzaItem", new PizzaItem(pizza)).load(), col, row);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.searchLoading.setVisible(false);
	}

	@FXML
	private void onHomeButtonClicked(@NotNull ActionEvent event) {
		System.out.println("Home");
	}

	@FXML
	private void onStatsButtonClicked(@NotNull ActionEvent event) {
		System.out.println("Stats");
	}

	@FXML
	private void onProfileButtonClicked(@NotNull ActionEvent event) {
		System.out.println("Profile");
	}

	@FXML
	private void onOrdersButtonClicked(@NotNull ActionEvent event) {
		System.out.println("Orders");
	}
}
