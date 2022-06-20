package com.rapizz;

import com.rapizz.models.Client;
import com.rapizz.models.Ingredient;
import com.rapizz.models.Livreur;
import com.rapizz.models.OperationCompte;
import com.rapizz.models.Order;
import com.rapizz.models.Pizza;
import com.rapizz.models.PizzaSize;
import com.rapizz.models.Vehicule;
import com.rapizz.models.VehiculeType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		return this.listPizzas("WHERE nom LIKE '%" + search.replaceAll("[\\\\%_]", "\\$0").replaceAll("'", "''") + "%' ORDER BY nom");
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

	public @NotNull List<@NotNull OperationCompte> listHistoriqueOfClient(long clientId) {
		List<OperationCompte> historique = new ArrayList<>();
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT id, montant, date_operation FROM historique_operations_compte WHERE compte = ? AND montant <> 0 ORDER BY date_operation DESC")) {
			preparedStatement.setLong(1, clientId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				historique.add(new OperationCompte(
						resultSet.getShort("id"),
						resultSet.getBigDecimal("montant"),
						resultSet.getTimestamp("date_operation")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return historique;
	}

	public @Nullable Order getOrder(long orderId) {
		List<Order> orders = this.listOrders("WHERE c.id = ? LIMIT 1", orderId);
		return orders.isEmpty() ? null : orders.get(0);
	}

	public @NotNull List<@NotNull Order> listOrdersOfClient(long clientId) {
		return this.listOrders("WHERE client = ? ORDER BY date_commande DESC", clientId);
	}

	private @NotNull List<@NotNull Order> listOrders(@NotNull String query, long id) {
		List<Order> orders = new ArrayList<>();
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT c.id AS order_id, p.id AS pizza_id, p.nom AS pizza_nom, p.prix AS pizza_prix, p.classement AS pizza_classement, s.id AS size_id, s.nom AS size_nom, s.modificateur_prix AS size_modificateur_prix, date_commande, gratuite, l.id AS livreur_id, l.nom AS livreur_nom, v.id AS vehicule_id, v.nom AS vehicule_nom, t.id AS vehicule_type_id, t.nom AS vehicule_type_nom, date_livraison, en_retard(c.id) AS en_retard, prix_commande(c.id) AS prix FROM commande c INNER JOIN classement_pizza p ON c.pizza = p.id INNER JOIN taille_pizza s ON c.taille = s.id LEFT OUTER JOIN livreur l ON c.livreur = l.id LEFT OUTER JOIN vehicule v ON c.vehicule = v.id LEFT OUTER JOIN type_vehicule t ON v.type = t.id " + query)) {
			preparedStatement.setLong(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				orders.add(new Order(
						resultSet.getLong("order_id"),
						new Pizza(
								resultSet.getShort("pizza_id"),
								resultSet.getString("pizza_nom"),
								resultSet.getBigDecimal("pizza_prix"),
								resultSet.getInt("pizza_classement")
						),
						new PizzaSize(
								resultSet.getByte("size_id"),
								resultSet.getString("size_nom"),
								resultSet.getFloat("size_modificateur_prix")
						),
						resultSet.getTimestamp("date_commande"),
						resultSet.getBoolean("gratuite"),
						resultSet.getString("livreur_nom") == null ? null : new Livreur(
								resultSet.getShort("livreur_id"),
								resultSet.getString("livreur_nom")
						),
						resultSet.getString("vehicule_nom") == null ? null : new Vehicule(
								resultSet.getShort("vehicule_id"),
								resultSet.getString("vehicule_nom"),
								new VehiculeType(
										resultSet.getByte("vehicule_type_id"),
										resultSet.getString("vehicule_type_nom")
								)
						),
						resultSet.getTimestamp("date_livraison"),
						resultSet.getBoolean("en_retard"),
						resultSet.getBigDecimal("prix")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orders;
	}

	public @NotNull List<@NotNull PizzaSize> listPizzaSizes() {
		List<PizzaSize> pizzaSizes = new ArrayList<>();
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT id, nom, modificateur_prix FROM taille_pizza ORDER BY modificateur_prix");
			while (resultSet.next()) {
				pizzaSizes.add(new PizzaSize(
						resultSet.getByte("id"),
						resultSet.getString("nom"),
						resultSet.getFloat("modificateur_prix")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pizzaSizes;
	}

	public @Nullable Client getClient(@NotNull String email) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT id, nom, email, mot_de_passe, compte, compteur_fidelite FROM client WHERE lower(email) = ?")) {
			preparedStatement.setString(1, email.toLowerCase());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return new Client(
						resultSet.getLong("id"),
						resultSet.getString("nom"),
						resultSet.getString("email"),
						resultSet.getBytes("mot_de_passe"),
						resultSet.getBigDecimal("compte"),
						resultSet.getByte("compteur_fidelite")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public @Nullable Client createClient(@NotNull String nom, @NotNull String email, @NotNull String password) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO client (nom, email, mot_de_passe) VALUE (?, ?, ?)")) {
			preparedStatement.setString(1, nom);
			preparedStatement.setString(2, email.toLowerCase());
			preparedStatement.setBytes(3, SHA512.hash(password));
			preparedStatement.executeUpdate();
			return this.getClient(email);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateClient(@NotNull Client client) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE client SET nom = ?, email = ?, compte = ? WHERE id = ?")) {
			preparedStatement.setString(1, client.getNom());
			preparedStatement.setString(2, client.getEmail().toLowerCase());
			preparedStatement.setBigDecimal(3, client.getCompte());
			preparedStatement.setLong(4, client.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public @Nullable Order createOrder(long clientId, short pizzaId, byte sizeId, boolean gratuite) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO commande (client, pizza, taille, gratuite) VALUE (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setLong(1, clientId);
			preparedStatement.setShort(2, pizzaId);
			preparedStatement.setByte(3, sizeId);
			preparedStatement.setBoolean(4, gratuite);
			preparedStatement.executeUpdate();
			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
			if (generatedKeys.next())
				return this.getOrder(generatedKeys.getLong(1));
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void deleteOrder(long orderId) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM commande WHERE id = ?")) {
			preparedStatement.setLong(1, orderId);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void rawQuery(@Language("MySQL") @NotNull String sql, @NotNull SQLRowConsumer forEachRow) {
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next())
				forEachRow.acceptRow(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public <T> @Nullable T rawQuerySingleResult(@Language("MySQL") @NotNull String sql, @NotNull Class<T> type) {
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(sql);
			return resultSet.next() ? resultSet.getObject(1, type) : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
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

	@FunctionalInterface
	public interface SQLRowConsumer {
		void acceptRow(@NotNull ResultSet resultSet) throws SQLException;
	}

	@FunctionalInterface
	public interface SQLRowFunction<R> {
		R applyRow(@NotNull ResultSet resultSet) throws SQLException;
	}
}
