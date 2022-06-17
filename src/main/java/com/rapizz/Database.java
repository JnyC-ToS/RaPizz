package com.rapizz;

import com.rapizz.models.Ingredient;
import com.rapizz.models.Pizza;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private final @NotNull Connection connection;

	public Database(@NotNull String host, int port, @NotNull String user, @NotNull String pass, @NotNull String database) throws SQLException {
		this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
	}

	public @NotNull List<@NotNull Pizza> listPizzas() {
		return this.listPizzas("ORDER BY nom");
	}

	public @NotNull List<@NotNull Pizza> listBestPizzas() {
		return this.listPizzas("LIMIT 3");
	}

	public @NotNull List<@NotNull Pizza> findPizzas(@NotNull String search) {
		return this.listPizzas("WHERE nom LIKE '%" + search.replaceAll("[\\\\%_]", "\\$0") + "%' ORDER BY nom");
	}

	private @NotNull List<@NotNull Pizza> listPizzas(@NotNull String query) {
		List<Pizza> pizzas = new ArrayList<>();
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT id, nom, prix, classement FROM classement_pizza " + query);
			while (resultSet.next()) {
				pizzas.add(new Pizza(
						resultSet.getShort("id"),
						resultSet.getString("nom"),
						resultSet.getBigDecimal("prix"),
						resultSet.getInt("classement")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pizzas;
	}

	public @NotNull List<@NotNull Ingredient> listIngredientsOfPizza(short pizzaId) {
		List<Ingredient> ingredients = new ArrayList<>();
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT i.id AS id, i.nom AS nom, i.cout AS cout FROM ingredient i INNER JOIN compose c ON i.id = c.ingredient WHERE c.pizza = ?")) {
			preparedStatement.setShort(1, pizzaId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				ingredients.add(new Ingredient(
						resultSet.getShort("id"),
						resultSet.getString("nom"),
						resultSet.getBigDecimal("cout")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ingredients;
	}

	public void displayTable(@NotNull String table) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM " + table)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			System.out.println("=====");
			System.out.println(table);
			System.out.println("=====");
			System.out.println();
			for (int i = 1; i <= metaData.getColumnCount(); i++)
				System.out.println(metaData.getColumnLabel(i));
			System.out.println();
			while (resultSet.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++)
					System.out.println(resultSet.getObject(i));
				System.out.println();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
