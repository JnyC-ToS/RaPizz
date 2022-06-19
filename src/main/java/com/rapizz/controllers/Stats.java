package com.rapizz.controllers;

import com.rapizz.Database;
import com.rapizz.RaPizz;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class Stats implements Initializable {
	@FXML
	private VBox unusedVehicules;
	@FXML
	private VBox ordersPerClient;
	@FXML
	private Label ordersAvg;
	@FXML
	private VBox clientsGreaterThanAvg;
	@FXML
	private Label bestClient;
	@FXML
	private Label worseDeliveryMan;
	@FXML
	private VBox vehiculesUsed;
	@FXML
	private Label mostWantedPizza;
	@FXML
	private Label leastWantedPizza;
	@FXML
	private Label bestIngredient;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setResults(this.unusedVehicules, "SELECT nom FROM vehicule v LEFT OUTER JOIN commande c ON v.id = c.vehicule WHERE c.id IS NULL", resultSet -> resultSet.getString("nom"));
		this.setResults(this.ordersPerClient, "SELECT nom, count(o.id) AS nbcommandes FROM commande o RIGHT OUTER JOIN client c ON c.id = o.client GROUP BY c.id ORDER BY nbcommandes DESC", resultSet -> resultSet.getString("nom") + " : " + resultSet.getInt("nbcommandes") + " commande(s)");
		this.ordersAvg.setText(RaPizz.formatMoney(RaPizz.DB.rawQuerySingleResult("SELECT avg(prix_commande(id)) AS moyenne FROM commande WHERE NOT gratuite AND NOT en_retard(id)", BigDecimal.class)));
		this.setResults(this.clientsGreaterThanAvg, "SELECT nom , sum(prix_commande(o.id)) AS total FROM commande o INNER JOIN client c ON o.client = c.id WHERE NOT gratuite AND NOT en_retard(o.id) GROUP BY client HAVING sum(prix_commande(o.id)) > (SELECT avg(prix_commande(id)) AS moyenne FROM commande WHERE NOT gratuite AND NOT en_retard(id)) ORDER BY total DESC", resultSet -> resultSet.getString("nom") + " : " + RaPizz.formatMoney(resultSet.getBigDecimal("total")) + " dépensé(s)");
		this.bestClient.setText(RaPizz.DB.rawQuerySingleResult("SELECT nom, sum(prix_commande(o.id)) AS depenses FROM commande o INNER JOIN client c ON o.client = c.id WHERE NOT gratuite AND NOT en_retard(o.id) GROUP BY client ORDER BY depenses DESC LIMIT 1", String.class));
		this.worseDeliveryMan.setText(RaPizz.DB.rawQuerySingleResult("SELECT nom, count(o.id) AS retards FROM commande o INNER JOIN livreur l ON o.livreur = l.id WHERE en_retard(o.id) GROUP BY livreur ORDER BY retards DESC LIMIT 1", String.class));
		this.setResults(this.vehiculesUsed, "SELECT nom, count(o.id) AS nbutilisation FROM commande o INNER JOIN vehicule v ON o.vehicule = v.id WHERE livreur = (SELECT livreur FROM commande WHERE en_retard(id) GROUP BY livreur ORDER BY count(id) DESC LIMIT 1) GROUP BY vehicule ORDER BY nbutilisation DESC", resultSet -> resultSet.getString("nom") + " : " + resultSet.getInt("nbutilisation") + " utilisation(s)");
		this.mostWantedPizza.setText(RaPizz.DB.rawQuerySingleResult("SELECT nom, count(c.id) AS nbdemandes FROM pizza p LEFT OUTER JOIN commande c ON p.id = c.pizza GROUP BY pizza ORDER BY nbdemandes DESC LIMIT 1", String.class));
		this.leastWantedPizza.setText(RaPizz.DB.rawQuerySingleResult("SELECT nom, count(c.id) AS nbdemandes FROM pizza p LEFT OUTER JOIN commande c ON p.id = c.pizza GROUP BY pizza ORDER BY nbdemandes LIMIT 1", String.class));
		this.bestIngredient.setText(RaPizz.DB.rawQuerySingleResult("SELECT i.nom AS nom, count(c.id) AS nbdemandes FROM ingredient i LEFT OUTER JOIN compose _ ON i.id = _.ingredient LEFT OUTER JOIN pizza p ON _.pizza = p.id LEFT OUTER JOIN commande c ON p.id = c.pizza GROUP BY i.id ORDER BY nbdemandes DESC LIMIT 1", String.class));
	}

	private void setResults(@NotNull VBox box, @Language("MySQL") @NotNull String request, @NotNull Database.SQLRowFunction<@NotNull String> display) {
		RaPizz.DB.rawQuery(request, resultSet -> {
			Label label = new Label(display.applyRow(resultSet));
			label.getStyleClass().add("stats-value");
			box.getChildren().add(label);
		});
	}
}
