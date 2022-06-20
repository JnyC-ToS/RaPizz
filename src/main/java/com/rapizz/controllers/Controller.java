package com.rapizz.controllers;

import com.rapizz.RaPizz;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
	@FXML
	private BorderPane top;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private BorderPane loader;

	@Override
	public void initialize(@NotNull URL location, @NotNull ResourceBundle resources) {
		RaPizz.bindManagedToVisible(this.top, this.loader);
		this.loader.setVisible(false);
	}

	public void setTopVisible(boolean topVisible) {
		this.top.setVisible(topVisible);
	}

	public void setPageContent(@NotNull String fxml, @Nullable Object controller) {
		Node oldContent = this.scrollPane.getContent();
		if (oldContent != null)
			oldContent.setOnScroll(null);
		this.loader.setVisible(true);
		Task<Node> loadTask = new Task<>() {
			@Override
			public Node call() {
				return RaPizz.loadFXML(fxml, controller);
			}
		};
		loadTask.setOnSucceeded(event -> {
			Node content = loadTask.getValue();
			this.scrollPane.setContent(content);
			this.loader.setVisible(false);
			content.setOnScroll(this::scrollEvent);
		});
		loadTask.setOnFailed(e -> {
			loadTask.getException().printStackTrace();
			Platform.exit();
		});
		new Thread(loadTask).start();
	}

	// Fix insanely slow default scroll pane
	private void scrollEvent(@NotNull ScrollEvent event) {
		double deltaY = event.getDeltaY() * 0.01;
		this.scrollPane.setVvalue(this.scrollPane.getVvalue() - deltaY);
	}

	@FXML
	private void onHomeButtonClicked(@NotNull ActionEvent event) {
		RaPizz.setPage("Pizzas", "PizzaList");
	}

	@FXML
	private void onStatsButtonClicked(@NotNull ActionEvent event) {
		RaPizz.setPage("Statistiques", "Stats");
	}

	@FXML
	private void onProfileButtonClicked(@NotNull ActionEvent event) {
		RaPizz.setPage("Profil", "Profile");
	}

	@FXML
	private void onOrdersButtonClicked(@NotNull ActionEvent event) {
		RaPizz.setPage("Commandes", "OrderList");
	}
}
