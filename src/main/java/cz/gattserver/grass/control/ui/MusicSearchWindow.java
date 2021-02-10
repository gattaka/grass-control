package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.ui.common.TrayControl;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MusicSearchWindow {

	private static final Logger logger = LoggerFactory.getLogger(MusicSearchWindow.class);

	private static final Path ROOT = Path.of("d:\\Hudba\\");

	public static void create() {
		Platform.runLater(MusicSearchWindow::createInPlatform);
	}

	private static void createInPlatform() {
		Stage stage = new Stage();
		stage.setTitle("Vyhledávání hudby");
		stage.setHeight(800);
		stage.setWidth(1200);

		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(10));
		layout.setSpacing(10);
		Scene scene = new Scene(layout);

		TableView<Path> table = new TableView<>();
		VBox.setVgrow(table, Priority.ALWAYS);

		table.setRowFactory(tv -> {
			TableRow<Path> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Path path = row.getItem();
					if (Files.isDirectory(path)) {
						populateTable(table, path);
					} else {
						VLCControl.sendCommand(VLCCommand.ADD, path.toString());
					}
				}
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
		layout.getChildren().add(searchLine);

		TableColumn<Path, String> actionCol = new TableColumn<>("Operace");
		Callback<TableColumn<Path, String>, TableCell<Path, String>> cellFactory = new Callback<TableColumn<Path, String>, TableCell<Path, String>>() {
			@Override
			public TableCell<Path, String> call(final TableColumn<Path, String> param) {
				final TableCell<Path, String> cell = new TableCell<Path, String>() {

					final HBox line = new HBox();
					final Button playBtn = new Button("Přehrát");
					final Button playDirBtn = new Button("Přehrát nadřazený adresář");
					final Button enterDirBtn = new Button("Přejít do adresáře");

					{
						line.getChildren().addAll(playBtn, playDirBtn, enterDirBtn);
						line.setSpacing(10);
						line.setPadding(new Insets(0));
					}

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							Path path = getTableView().getItems().get(getIndex());
							playBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.ADD, path.toString()));
							playDirBtn.setOnAction(
									e -> VLCControl.sendCommand(VLCCommand.ADD, path.getParent().toString()));
							playDirBtn.setDisable(path.getParent().equals(ROOT));
							enterDirBtn.setOnAction(e -> populateTable(table, path));
							enterDirBtn.setDisable(!Files.isDirectory(path));
							setGraphic(line);
							setText(null);
						}
					}
				};
				return cell;
			}
		};
		actionCol.setCellFactory(cellFactory);
		table.getColumns().add(actionCol);

		TableColumn<Path, String> nameCol = new TableColumn<>("Název");
		nameCol.setMinWidth(300);
		nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFileName().toString()));
		table.getColumns().add(nameCol);

		TableColumn<Path, String> scoreCol = new TableColumn<>("Nadřazený adresář");
		scoreCol.setMinWidth(500);
		scoreCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getParent().toString()));
		table.getColumns().add(scoreCol);

		populateTable(table);

		layout.getChildren().add(table);

		HBox buttonLine = new HBox();
		buttonLine.setSpacing(10);
		HBox.setHgrow(textField, Priority.ALWAYS);
		layout.getChildren().add(buttonLine);

		Button clearPlaylistBtn = new Button();
		clearPlaylistBtn.setText("Vyčistit playlist");
		clearPlaylistBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.CLEAR));

		Button closeBtn = new Button();
		closeBtn.setText("Zavřít");
		closeBtn.setOnAction(e -> stage.hide());

		buttonLine.getChildren().addAll(clearPlaylistBtn, closeBtn);

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

	private static void populateTable(TableView<Path> table, Path path) {
		table.setDisable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Path> list = new ArrayList<>();
				try {
					list.addAll(Files.list(path).collect(Collectors.toList()));
				} catch (IOException e) {
					String msg = "Nezdařilo se získat přehled adresáře hudby";
					logger.error(msg, e);
					TrayControl.showMessage(msg);
				}
				Platform.runLater(() -> {
					if (list != null) {
						ObservableList<Path> data = FXCollections.observableArrayList(list);
						table.setItems(data);
						// musí být až po populate
						table.getSortOrder().add(table.getColumns().iterator().next());
					}
					table.setDisable(false);
				});
			}
		}).start();
	}

	private static void populateTable(TableView<Path> table, String filter) {
		table.setDisable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Path> list = new ArrayList<>();
				try {

					findRecursive(ROOT, filter, list);
				} catch (IOException e) {
					String msg = "Nezdařilo se získat přehled adresáře hudby";
					logger.error(msg, e);
					TrayControl.showMessage(msg);
				}
				Platform.runLater(() -> {
					if (list != null) {
						ObservableList<Path> data = FXCollections.observableArrayList(list);
						table.setItems(data);
						// musí být až po populate
						table.getSortOrder().add(table.getColumns().iterator().next());
					}
					table.setDisable(false);
				});
			}
		}).start();
	}

	private static void populateTable(TableView<Path> table) {
		List<Path> list;
		try {
			list = Files.list(ROOT).collect(Collectors.toList());
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
