package com.rapizz.controllers;

import com.rapizz.RaPizz;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Controller {
	@FXML
	private BorderPane top;
	@FXML
	private ScrollPane scrollPane;

	public void setTopVisible(boolean topVisible) {
		this.top.setVisible(topVisible);
	}

	public void setPageContent(@NotNull String fxml, @Nullable Object controller) {
		Node oldContent = this.scrollPane.getContent();
		if (oldContent != null)
			oldContent.setOnScroll(null);
		Node content = RaPizz.loadFXML(fxml, controller);
		this.scrollPane.setContent(content);
		content.setOnScroll(this::scrollEvent);
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
		// TODO open profile page
	}

	@FXML
	private void onOrdersButtonClicked(@NotNull ActionEvent event) {
		RaPizz.setPage("Commandes", "OrderList");
	}
}
