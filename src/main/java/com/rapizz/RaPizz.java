package com.rapizz;

import com.rapizz.controllers.Controller;
import com.rapizz.models.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RaPizz extends Application {
	public static final @NotNull Database DB;

	static {
		try {
			DB = new Database(
					System.getenv("DB_HOST"),
					Integer.parseInt(System.getenv("DB_PORT")),
					System.getenv("DB_USER"),
					System.getenv("DB_PASS"),
					System.getenv("DB_DATABASE")
			);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	public static final @NotNull String APP_TITLE = "Pizza Style - RaPizz";

	private static final @NotNull List<@NotNull Runnable> closeListeners = new ArrayList<>();
	private static @Nullable Stage mainStage;
	private static @Nullable Controller mainController;
	private static @Nullable Client client;
	private static final @NotNull DecimalFormat moneyFormatter = new DecimalFormat("0.00");
	private static final @NotNull SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

	@Override
	public void start(@NotNull Stage stage) {
		mainStage = stage;
		loadFonts("Roboto", "Baloo2");
		Parent root = loadFXML("RaPizz", mainController = new Controller());
		RaPizz.setDefaultStylesheets(root);
		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		setPage(null, "Home");
		stage.getIcons().add(getImage("logo.png"));
		stage.setMaximized(true);
		stage.show();
	}

	@Override
	public void stop() {
		closeListeners.forEach(Runnable::run);
	}

	public static void main(@NotNull String @NotNull[] args) {
		launch(args);
	}

	public static @NotNull Stage getMainStage() {
		if (mainStage == null)
			throw new IllegalStateException("Application not running");
		return mainStage;
	}

	public static void hideTop() {
		if (mainController == null)
			throw new IllegalStateException("Application not running");
		mainController.setTopVisible(false);
	}

	public static void showTop() {
		if (mainController == null)
			throw new IllegalStateException("Application not running");
		mainController.setTopVisible(true);
	}

	public static boolean isLoggedIn() {
		return client != null;
	}

	public static void login(@NotNull Client client) {
		RaPizz.client = client;
	}

	public static @NotNull Client getClient() {
		if (client == null)
			throw new IllegalStateException("Client not logged in");
		return client;
	}

	public static void setPage(@Nullable String title, @NotNull String fxml) {
		setPage(title, fxml, null);
	}

	public static void setPage(@Nullable String title, @NotNull String fxml, @Nullable Object controller) {
		if (mainController == null)
			throw new IllegalStateException("Application not running");
		mainController.setPageContent(fxml, controller);
		getMainStage().setTitle(title == null ? APP_TITLE : title + " - " + APP_TITLE);
	}

	public static <T> @NotNull T loadFXML(@NotNull String name) {
		return loadFXML(name, null);
	}

	public static <T> @NotNull T loadFXML(@NotNull String name, @Nullable Object controller) {
		try {
			FXMLLoader loader = new FXMLLoader(RaPizz.class.getResource("views/" + name + ".fxml"));
			if (controller != null)
				loader.setController(controller);
			return loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void loadFonts(@NotNull String @NotNull... names) {
		for (String name : names)
			Font.loadFont(RaPizz.class.getClassLoader().getResourceAsStream("fonts/" + name + ".ttf"), 18);
	}

	public static @Nullable Image getImage(@NotNull String image) {
		InputStream stream = RaPizz.class.getClassLoader().getResourceAsStream("images/" + image);
		if (stream == null)
			return null;
		return new Image(stream);
	}

	public static void addCloseListener(@NotNull Runnable closeListener) {
		closeListeners.add(closeListener);
	}

	public static void bindManagedToVisible(@NotNull Node @NotNull... nodes) {
		for (Node node : nodes)
			node.managedProperty().bind(node.visibleProperty());
	}

	public static void setDefaultStylesheets(@NotNull Parent node) {
		node.getStylesheets().addAll("/styles/fx-reset.css", "/styles/style.css");
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable String formatMoney(@Nullable BigDecimal amount) {
		return amount == null ? null : moneyFormatter.format(amount) + " €";
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable String formatDate(@Nullable Timestamp date) {
		return date == null ? null : dateFormatter.format(date);
	}
}
