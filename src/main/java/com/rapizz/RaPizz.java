package com.rapizz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.sql.SQLException;
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

	private static final @NotNull List<@NotNull Runnable> closeListeners = new ArrayList<>();

	@Override
	public void start(@NotNull Stage stage) throws Exception {
		loadFonts("Roboto", "Baloo2");
		Scene scene = new Scene(getFXMLLoader("RaPizz").load(), 640, 480);
		stage.setScene(scene);
		stage.setTitle("Pizza Style - RaPizz");
		stage.getIcons().add(getImage("logo.png"));
		stage.setMaximized(true);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		closeListeners.forEach(Runnable::run);
	}

	public static void main(@NotNull String @NotNull[] args) {
		launch(args);
	}

	public static @NotNull FXMLLoader getFXMLLoader(@NotNull String name) {
		return new FXMLLoader(RaPizz.class.getResource("views/" + name + ".fxml"));
	}

	public static @NotNull FXMLLoader getFXMLLoader(@NotNull String name, @NotNull Object controller) {
		FXMLLoader loader = getFXMLLoader(name);
		loader.setController(controller);
		return loader;
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
}
