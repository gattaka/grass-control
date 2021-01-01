package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.speech.SpeechLogTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class HistoryWindow {

	private static final Logger logger = LoggerFactory.getLogger(HistoryWindow.class);

	public static void create() {
		Platform.runLater(HistoryWindow::createInPlatform);
	}

	private static void createInPlatform() {
		GridPane grid = new GridPane();
		Scene scene = new Scene(grid);
		Stage stage = new Stage();
		stage.setTitle("Historie příkazů");
		stage.setHeight(500);

		TableView<SpeechLogTO> table = new TableView<>();

		TableColumn<SpeechLogTO, String> timeCol = new TableColumn<>("Čas");
		timeCol.setMinWidth(120);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
		timeCol.setCellValueFactory(p -> new SimpleStringProperty(sdf.format(p.getValue().getTime())));
		timeCol.setSortable(true);
		timeCol.setSortType(SortType.DESCENDING);
		table.getColumns().add(timeCol);

		TableColumn<SpeechLogTO, String> commandCol = new TableColumn<>("Příkaz");
		commandCol.setMinWidth(300);
		commandCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCommand()));
		table.getColumns().add(commandCol);

		TableColumn<SpeechLogTO, String> scoreCol = new TableColumn<>("Score");
		scoreCol.setMinWidth(90);
		scoreCol.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getScore())));
		table.getColumns().add(scoreCol);

		TableColumn<SpeechLogTO, String> inRangeCol = new TableColumn<>("V rozsahu");
		inRangeCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().isInRange() ? "Ano" : "Ne"));
		table.getColumns().add(inRangeCol);

		populateTable(table);

		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(8));

		grid.add(table, 0, 0, 2, 1);

		Button closeBtn = new Button();
		closeBtn.setText("Zavřít");
		closeBtn.setOnAction(e -> stage.hide());

		Button refreshButton = new Button();
		refreshButton.setText("Obnovit");
		refreshButton.setOnAction(e -> populateTable(table));

		grid.add(refreshButton, 0, 1);
		grid.add(closeBtn, 1, 1);

		stage.setScene(scene);
		stage.show();

		try {
			stage.getIcons().add(new Image(TrayControl.getIconStream()));
		} catch (IOException e1) {
			logger.error("Icon load failed");
		}
	}

	private static void populateTable(TableView<SpeechLogTO> table) {
		List<SpeechLogTO> log = SpeechControl.getHistory();
		ObservableList<SpeechLogTO> data = FXCollections.observableArrayList(log);
		table.setItems(data);
		// musí být až po populate
		table.getSortOrder().add(table.getColumns().iterator().next());
	}
}
