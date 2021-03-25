package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private IndexNode index;

	private Boolean initialized = false;

	private Stage stage;
	private TableView<IndexNode> table;
	private History<Command> history;
	private Button backBtn;
	private Button forwardBtn;

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

		backBtn = new Button();
		backBtn.setText("<");

		forwardBtn = new Button();
		forwardBtn.setText(">");

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

		TableColumn<IndexNode, String> nameCol = new TableColumn<>("Název");
		Callback<TableColumn<IndexNode, String>, TableCell<IndexNode, String>> nameColCellFactory = new Callback<TableColumn<IndexNode, String>, TableCell<IndexNode, String>>() {
			@Override
			public TableCell<IndexNode, String> call(final TableColumn<IndexNode, String> param) {
				final TableCell<IndexNode, String> cell = new TableCell<IndexNode, String>() {

					final HBox line = new HBox();
					final Button playBtn = new Button("Přehrát");
					final Button enqueueBtn = new Button("Přidat");
					final Button openBtn = new Button("Otevřít");

					{
						line.getChildren().addAll(playBtn, enqueueBtn, openBtn);
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
							IndexNode node = getTableView().getItems().get(getIndex());
							playBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.ADD, node.getPath().toString()));
							enqueueBtn.setOnAction(
									e -> VLCControl.sendCommand(VLCCommand.ENQUEUE, node.getPath().toString()));
							openBtn.setOnAction(e -> {
								pushAndRunCommand(() -> populateTable(node));
								backBtn.setDisable(false);
							});
							openBtn.setDisable(!node.isDirectory());
							setGraphic(line);
							setText(node.getPathName());
						}
					}
				};
				return cell;
			}
		};
		nameCol.setCellFactory(nameColCellFactory);
		nameCol.setMinWidth(500);
		table.getColumns().add(nameCol);

		TableColumn<IndexNode, String> parentDirCol = new TableColumn<>("Nadřazený adresář");
		Callback<TableColumn<IndexNode, String>, TableCell<IndexNode, String>> parentDirColCellFactory = new Callback<TableColumn<IndexNode, String>, TableCell<IndexNode, String>>() {
			@Override
			public TableCell<IndexNode, String> call(final TableColumn<IndexNode, String> param) {
				final TableCell<IndexNode, String> cell = new TableCell<IndexNode, String>() {

					final HBox line = new HBox();
					final Button playBtn = new Button("Přehrát");
					final Button enqueueBtn = new Button("Přidat");
					final Button openBtn = new Button("Otevřít");

					{
						line.getChildren().addAll(playBtn, enqueueBtn, openBtn);
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
							IndexNode node = getTableView().getItems().get(getIndex()).getParentNode();
							playBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.ADD, node.getPath().toString()));
							enqueueBtn.setOnAction(
									e -> VLCControl.sendCommand(VLCCommand.ENQUEUE, node.getPath().toString()));
							openBtn.setOnAction(e -> {
								pushAndRunCommand(() -> populateTable(node));
								backBtn.setDisable(false);
							});
							setGraphic(line);
							setText(node.getPathName());
						}
					}
				};
				return cell;
			}
		};
		parentDirCol.setCellFactory(parentDirColCellFactory);
		parentDirCol.setMinWidth(500);
		table.getColumns().add(parentDirCol);

		reindex();

		layout.getChildren().add(table);

		HBox buttonLine = new HBox();
		buttonLine.setSpacing(10);
		HBox.setHgrow(textField, Priority.ALWAYS);
		layout.getChildren().add(buttonLine);

		Button clearPlaylistBtn = new Button();
		clearPlaylistBtn.setText("Vyčistit playlist");
		clearPlaylistBtn.setOnAction(e -> VLCControl.sendCommand(VLCCommand.CLEAR));

		Button reindexBtn = new Button();
		reindexBtn.setText("Reindex");
		reindexBtn.setOnAction(e -> reindex());

		Button closeBtn = new Button();
		closeBtn.setText("Zavřít");
		closeBtn.setOnAction(e -> stage.hide());

		buttonLine.getChildren().addAll(clearPlaylistBtn, reindexBtn, closeBtn);

		stage.setScene(scene);

		try {
			stage.getIcons().add(new Image(TrayControl.getIconStream()));
		} catch (IOException e1) {
			logger.error("Icon load failed");
		}

		initialized = true;
	}

	private void reindex() {
		index = new IndexNode(null, ROOT);
		buildIndex(index);
		history = new History<>();
		pushAndRunCommand(() -> populateTable(index));
		backBtn.setDisable(true);
		forwardBtn.setDisable(true);
	}

	private void pushAndRunCommand(Command cmd) {
		cmd.run();
		history.push(cmd);
	}

	private void buildIndex(IndexNode node) {
		try {
			Files.list(node.getPath()).forEach(p -> {
				IndexNode newNode = new IndexNode(node, p);
				node.addSubnode(newNode);
				if (Files.isDirectory(p))
					buildIndex(newNode);
			});
		} catch (IOException e) {
			logger.error("Path listing for path " + node.getPath() + " failed", e);
		}
	}

	private void findRecursive(IndexNode currentNode, String filter, List<IndexNode> results) throws IOException {
		for (IndexNode node : currentNode.getSubnodes()) {
			Pattern pattern = Pattern.compile(".*" + filter.toLowerCase() + ".*");
			Matcher m = pattern.matcher(node.getPathNameLowerCase());
			if (m.matches())
				results.add(node);
			if (node.isDirectory())
				findRecursive(node, filter, results);
		}
	}

	private void populateTable(IndexNode node) {
		table.setDisable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					ObservableList<IndexNode> data = FXCollections.observableArrayList(node.getSubnodes());
					table.setItems(data);
					// musí být až po populate
					table.getSortOrder().add(table.getColumns().iterator().next());
					table.setDisable(false);
					table.refresh();
				});
			}
		}).start();
	}

	private void populateTable(String filter) {
		table.setDisable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<IndexNode> list = new ArrayList<>();
				try {
					findRecursive(index, filter, list);
				} catch (IOException e) {
					String msg = "Nezdařilo se získat přehled adresáře hudby";
					logger.error(msg, e);
					TrayControl.showMessage(msg);
				}
				Platform.runLater(() -> {
					if (list != null) {
						ObservableList<IndexNode> data = FXCollections.observableArrayList(list);
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
