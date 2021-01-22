package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class MusicSearchWindow {

	private static final Logger logger = LoggerFactory.getLogger(MusicSearchWindow.class);

	private static final String ROOT = "d:\\Hudba\\";

	public static void create() {
		Platform.runLater(MusicSearchWindow::createInPlatform);
	}

	private static void createInPlatform() {
		GridPane grid = new GridPane();
		Scene scene = new Scene(grid);
		Stage stage = new Stage();
		stage.setTitle("Vyhledávání hudby");
		stage.setHeight(500);

		TableView<Path> table = new TableView<>();
		GridPane.setHgrow(table, Priority.ALWAYS);
		GridPane.setVgrow(table, Priority.ALWAYS);

		table.setRowFactory(tv -> {
			TableRow<Path> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty()))
					VLCControl.sendCommand(VLCCommand.ADD, row.getItem().toString());
			});
			return row;
		});

		TextField textField = new TextField();

		EventHandler<ActionEvent> searchHandler = e -> populateTable(table, textField.getText());
		textField.setOnAction(searchHandler);

		Button searchBtn = new Button();
		searchBtn.setText("Hledat");
		searchBtn.setOnAction(searchHandler);

		HBox searchLine = new HBox();
		searchLine.getChildren().addAll(textField, searchBtn);
		searchLine.setSpacing(10);
		HBox.setHgrow(textField, Priority.ALWAYS);
		grid.add(searchLine, 0, 0, 2, 1);
		GridPane.setHgrow(searchLine, Priority.ALWAYS);

		TableColumn<Path, String> nameCol = new TableColumn<>("Název");
		nameCol.setMinWidth(300);
		nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFileName().toString()));
		table.getColumns().add(nameCol);

		TableColumn<Path, String> scoreCol = new TableColumn<>("Adresář");
		scoreCol.setMinWidth(400);
		scoreCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getParent().toString()));
		table.getColumns().add(scoreCol);

		populateTable(table);

		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(8));

		grid.add(table, 0, 1, 2, 1);

		HBox buttonLine = new HBox();
		buttonLine.setSpacing(10);
		HBox.setHgrow(textField, Priority.ALWAYS);
		grid.add(buttonLine, 0, 2, 2, 1);
		GridPane.setHgrow(buttonLine, Priority.ALWAYS);

		Button playBtn = new Button();
		playBtn.setText("Přidat do playlistu");
		playBtn.setOnAction(
				e -> VLCControl.sendCommand(VLCCommand.ADD, table.getSelectionModel().getSelectedItem().toString()));

		Button clearPlaylistBtn = new Button();
		clearPlaylistBtn.setText("Vyčistit playlist");
		clearPlaylistBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.CLEAR));

		Button closeBtn = new Button();
		closeBtn.setText("Zavřít");
		closeBtn.setOnAction(e -> stage.hide());

		buttonLine.getChildren().addAll(playBtn, clearPlaylistBtn, closeBtn);

		stage.setScene(scene);
		stage.show();

		try {
			stage.getIcons().add(new Image(TrayControl.getIconStream()));
		} catch (IOException e1) {
			logger.error("Icon load failed");
		}
	}

	private static void findRecursive(Path path, String filter, List<Path> results) throws IOException {
		for (Path p : Files.list(path).collect(Collectors.toList())) {
			if (p.getFileName().toString().toLowerCase().matches(".*" + filter.toLowerCase() + ".*"))
				results.add(p);
			if (Files.isDirectory(p))
				findRecursive(p, filter, results);
		}
	}

	private static void populateTable(TableView<Path> table, String filter) {
		Path path = Path.of(ROOT);
		table.setDisable(true);
		Platform.runLater(() -> {
			try {
				List<Path> list = new ArrayList<>();
				findRecursive(path, filter, list);
				ObservableList<Path> data = FXCollections.observableArrayList(list);
				table.setItems(data);
				// musí být až po populate
				table.getSortOrder().add(table.getColumns().iterator().next());
			} catch (IOException e) {
				String msg = "Nezdařilo se získat přehled adresáře hudby";
				logger.error(msg, e);
				TrayControl.showMessage(msg);
			}
			table.setDisable(false);
		});
	}

	private static void populateTable(TableView<Path> table) {
		Path path = Path.of(ROOT);
		List<Path> list;
		try {
			list = Files.list(path).collect(Collectors.toList());
		} catch (IOException e) {
			String msg = "Nezdařilo se získat přehled adresáře hudby";
			logger.error(msg, e);
			TrayControl.showMessage(msg);
			return;
		}
		ObservableList<Path> data = FXCollections.observableArrayList(list);
		table.setItems(data);
		// musí být až po populate
		table.getSortOrder().add(table.getColumns().iterator().next());
	}
}
