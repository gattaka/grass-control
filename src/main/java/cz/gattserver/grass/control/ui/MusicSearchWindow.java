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
	private static final MusicSearchWindow INSTANCE = new MusicSearchWindow();

	private Boolean initialized = false;

	private Stage stage;
	private TableView<Path> table;
	private History<Command> history;

	public static void runOnInstance(Runnable cmd) {
		Platform.runLater(() -> {
			if (!INSTANCE.initialized)
				INSTANCE.init();
			cmd.run();
		});
	}

	public static void showInstance() {
		runOnInstance(() -> INSTANCE.stage.show());
	}

	public static void hideInstance() {
		runOnInstance(() -> INSTANCE.stage.hide());
	}

	private synchronized void init() {
		if (initialized)
			return;

		history = new History<>();

		stage = new Stage();
		stage.setTitle("Vyhledávání hudby");
		stage.setHeight(800);
		stage.setWidth(1200);

		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(10));
		layout.setSpacing(10);
		Scene scene = new Scene(layout);

		TextField textField = new TextField();

		EventHandler<ActionEvent> searchHandler = e -> {
			pushAndRunCommand(() -> populateTable(textField.getText()));
			populateTable(textField.getText());
		};
		textField.setOnAction(searchHandler);

		Button backBtn = new Button();
		backBtn.setText("<");
		backBtn.setDisable(true);

		Button forwardBtn = new Button();
		forwardBtn.setText(">");
		forwardBtn.setDisable(true);

		backBtn.setOnAction(e -> {
			history.back().run();
			backBtn.setDisable(history.isFirst());
			forwardBtn.setDisable(false);
		});

		forwardBtn.setOnAction(e -> {
			history.forward().run();
			forwardBtn.setDisable(history.isLast());
			backBtn.setDisable(false);
		});

		Button searchBtn = new Button();
		searchBtn.setText("Hledat");
		searchBtn.setOnAction(searchHandler);

		HBox searchLine = new HBox();
		searchLine.getChildren().addAll(backBtn, forwardBtn, textField, searchBtn);
		searchLine.setSpacing(10);
		HBox.setHgrow(textField, Priority.ALWAYS);
		layout.getChildren().add(searchLine);

		table = new TableView<>();
		VBox.setVgrow(table, Priority.ALWAYS);

		TableColumn<Path, String> nameCol = new TableColumn<>("Název");
		Callback<TableColumn<Path, String>, TableCell<Path, String>> nameColCellFactory = new Callback<TableColumn<Path, String>, TableCell<Path, String>>() {
			@Override
			public TableCell<Path, String> call(final TableColumn<Path, String> param) {
				final TableCell<Path, String> cell = new TableCell<Path, String>() {

					final HBox line = new HBox();
					final Button playBtn = new Button("Přehrát");
					final Button openBtn = new Button("Otevřít");

					{
						line.getChildren().addAll(playBtn, openBtn);
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
							openBtn.setOnAction(e -> {
								pushAndRunCommand(() -> populateTable(path));
								backBtn.setDisable(false);
							});
							openBtn.setDisable(!Files.isDirectory(path));
							setGraphic(line);
							setText(path.getFileName().toString());
						}
					}
				};
				return cell;
			}
		};
		nameCol.setCellFactory(nameColCellFactory);
		nameCol.setMinWidth(500);
		table.getColumns().add(nameCol);

		TableColumn<Path, String> parentDirCol = new TableColumn<>("Nadřazený adresář");
		Callback<TableColumn<Path, String>, TableCell<Path, String>> parentDirColCellFactory = new Callback<TableColumn<Path, String>, TableCell<Path, String>>() {
			@Override
			public TableCell<Path, String> call(final TableColumn<Path, String> param) {
				final TableCell<Path, String> cell = new TableCell<Path, String>() {

					final HBox line = new HBox();
					final Button playBtn = new Button("Přehrát");
					final Button openBtn = new Button("Otevřít");

					{
						line.getChildren().addAll(playBtn, openBtn);
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
							Path path = getTableView().getItems().get(getIndex()).getParent();
							playBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.ADD, path.toString()));
							openBtn.setOnAction(e -> {
								pushAndRunCommand(() -> populateTable(path));
								backBtn.setDisable(false);
							});
							setGraphic(line);
							setText(path.getFileName().toString());
						}
					}
				};
				return cell;
			}
		};
		parentDirCol.setCellFactory(parentDirColCellFactory);
		parentDirCol.setMinWidth(500);
		table.getColumns().add(parentDirCol);

		pushAndRunCommand(() -> populateTable(ROOT));

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

		try {
			stage.getIcons().add(new Image(TrayControl.getIconStream()));
		} catch (IOException e1) {
			logger.error("Icon load failed");
		}

		initialized = true;
	}

	private void pushAndRunCommand(Command cmd) {
		cmd.run();
		history.push(cmd);
	}

	private void findRecursive(Path path, String filter, List<Path> results) throws IOException {
		for (Path p : Files.list(path).collect(Collectors.toList())) {
			if (p.getFileName().toString().toLowerCase().matches(".*" + filter.toLowerCase() + ".*"))
				results.add(p);
			if (Files.isDirectory(p))
				findRecursive(p, filter, results);
		}
	}

	private void populateTable(Path path) {
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

	private void populateTable(String filter) {
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
}
