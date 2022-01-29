package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import cz.gattserver.grass.control.ui.common.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;

@Route("music")
@PageTitle("Vyhledávání hudby")
// @PreserveOnRefresh
public class MusicSearch extends Div {

	private static final long serialVersionUID = 226911270833721492L;

	private static final Logger logger = LoggerFactory.getLogger(MusicSearch.class);

	private History<Command> history;
	private IndexNode currentNode;

	private Button backBtn;
	private Button forwardBtn;

	public MusicSearch() {
		setSizeFull();

		history = new History<>();
		currentNode = MusicIndex.getRootNode();

		Grid<IndexNode> grid = new Grid<>();
		grid.setSizeFull();

		grid.addColumn(new TextRenderer<IndexNode>(IndexNode::getPathName)).setHeader("Název");
		grid.addColumn(new ComponentRenderer<HorizontalLayout, IndexNode>(n -> createButtons(grid, n)));
		grid.addColumn(new TextRenderer<IndexNode>(n -> n.getParentNode().getPathName()))
				.setHeader("Nadřazený adresář");
		grid.addColumn(new ComponentRenderer<HorizontalLayout, IndexNode>(n -> createButtons(grid, n.getParentNode())));

		HeaderRow headerRow = grid.appendHeaderRow();
		// TODO

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		add(layout);

		backBtn = new Button("<", e -> {
			history.back().run();
			backBtn.setEnabled(!history.isFirst());
			forwardBtn.setEnabled(true);
		});
		backBtn.setEnabled(false);

		forwardBtn = new Button(">", e -> {
			history.forward().run();
			forwardBtn.setEnabled(!history.isLast());
			backBtn.setEnabled(true);
		});
		forwardBtn.setEnabled(false);

		TextField searchField = new TextField();
		searchField.setWidthFull();

		Button searchBtn = new Button("Hledat", e -> {
			pushAndRunCommand(() -> populateTable(grid, searchField.getValue()));
		});
		searchBtn.setWidth(null);

		HorizontalLayout topLayout = new HorizontalLayout(new HorizontalLayout(backBtn, forwardBtn, searchBtn),
				searchField);
		topLayout.setWidthFull();
		layout.add(topLayout);

		layout.add(grid);

		Button clearBtn = new Button("Vyčistit playlist", e -> {
			VLCControl.sendCommand(VLCCommand.CLEAR);
		});

		Button reindexBtn = new Button("Reindex", e -> {
			reindex(grid);
		});

		HorizontalLayout bottomLayout = new HorizontalLayout(clearBtn, reindexBtn);
		layout.add(bottomLayout);

		pushAndRunCommand(() -> {
			populateTable(grid, MusicIndex.getRootNode());
		});
	}

	private HorizontalLayout createButtons(Grid<IndexNode> grid, IndexNode node) {
		Button playBtn = new Button("Přehrát", e -> {
			VLCControl.sendCommand(VLCCommand.ADD, node.getPath().toString());
		});
		Button enqueueBtn = new Button("Přidat", e -> {
			VLCControl.sendCommand(VLCCommand.ENQUEUE, node.getPath().toString());
		});
		Button openBtn = new Button("Otevřít", e -> {
			backBtn.setEnabled(true);
			pushAndRunCommand(() -> populateTable(grid, node));
		});
		openBtn.setEnabled(node.isDirectory());
		HorizontalLayout hl = new HorizontalLayout(new HorizontalLayout(playBtn, enqueueBtn, openBtn));
		return hl;
	}

	private void reindex(Grid<IndexNode> grid) {
		MusicIndex.buildIndex();
		history = new History<>();
		pushAndRunCommand(() -> populateTable(grid, MusicIndex.getRootNode()));
		backBtn.setEnabled(false);
		forwardBtn.setEnabled(false);
	}

	private void pushAndRunCommand(Command cmd) {
		cmd.run();
		history.push(cmd);
		if (!history.isFirst())
			backBtn.setEnabled(true);
		forwardBtn.setEnabled(false);
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

	private void populateTable(Grid<IndexNode> grid, IndexNode node) {
		// FetchCallback<IndexNode, IndexNode> fetchCallback = q -> {
		// node.getSubnodes().subList(0, 0)
		// }.stream();
		// CountCallback<IndexNode, IndexNode> countCallback = q ->
		// getBooksFacade().countBooks(filterTO);
		// dataProvider = DataProvider.fromFilteringCallbacks(fetchCallback,
		// countCallback);
		// grid.setDataProvider(dataProvider);
		grid.setDataProvider(new ListDataProvider<>(node.getSubnodes()));
	}

	private void populateTable(Grid<IndexNode> grid, String filter) {
		List<IndexNode> list = new ArrayList<>();
		try {
			findRecursive(currentNode, filter, list);
		} catch (IOException e) {
			String msg = "Nezdařilo se získat přehled adresáře hudby";
			logger.error(msg, e);
			TrayControl.showMessage(msg);
		}
		if (list != null)
			grid.setDataProvider(new ListDataProvider<>(list));
	}
}
