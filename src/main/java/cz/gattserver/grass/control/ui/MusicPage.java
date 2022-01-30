package cz.gattserver.grass.control.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import cz.gattserver.grass.control.ui.common.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;

@Route("music")
@PageTitle("Vyhledávání hudby")
// @PreserveOnRefresh
public class MusicPage extends MainPage {

	private static final long serialVersionUID = 226911270833721492L;

	private static final Logger logger = LoggerFactory.getLogger(MusicPage.class);

	private History<Command> history;
	private IndexNode currentNode;

	private ValueHolder<IndexNode> selectedNode;
	private ValueHolder<IndexNode> selectedNodeParent;

	private Span currentDirSpan;
	private Span seekTimeSpan;

	private HorizontalLayout nodeOperationsLayout;
	private HorizontalLayout nodeParentOperationsLayout;

	private Button backBtn;
	private Button forwardBtn;

	public MusicPage() {
		setSizeFull();

		selectedNode = new ValueHolder<>();
		selectedNodeParent = new ValueHolder<>();

		history = new History<>();
		currentNode = MusicIndex.getRootNode();

		Grid<IndexNode> grid = new Grid<>();
		grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
		grid.setSizeFull();

		Column<IndexNode> nazevColumn = grid.addColumn(new TextRenderer<IndexNode>(IndexNode::getPathName))
				.setHeader("Název").setResizable(true);
		Column<IndexNode> parentColumn = grid
				.addColumn(new TextRenderer<IndexNode>(n -> n.getParentNode().getPathName()))
				.setHeader("Nadřazený adresář").setResizable(true);

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		add(layout);

		grid.addItemDoubleClickListener(e -> {
			if (e.getColumn() == nazevColumn) {
				open(grid, e.getItem());
			} else if (e.getColumn() == parentColumn) {
				open(grid, e.getItem().getParentNode());
			}
		});
		grid.addItemClickListener(e -> {
			if (!grid.getSelectedItems().isEmpty()) {
				selectedNode.setValue(e.getItem());
				selectedNodeParent.setValue(e.getItem().getParentNode());
				nodeOperationsLayout.setEnabled(true);
				nodeParentOperationsLayout.setEnabled(true);
			} else {
				nodeOperationsLayout.setEnabled(false);
				nodeParentOperationsLayout.setEnabled(false);
			}
		});

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
			pushAndRunCommand(() -> populate(grid, searchField.getValue()));
		});
		searchBtn.setWidth(null);
		searchField.addKeyPressListener(Key.ENTER, e -> searchBtn.click());

		HorizontalLayout topLayout = new HorizontalLayout(new HorizontalLayout(backBtn, forwardBtn, searchBtn),
				searchField);
		topLayout.setWidthFull();
		layout.add(topLayout);

		currentDirSpan = new Span();
		layout.add(currentDirSpan);

		layout.add(grid);

		nodeOperationsLayout = createButtons(grid, selectedNode);
		nodeParentOperationsLayout = createButtons(grid, selectedNodeParent);

		FooterRow footer = grid.appendFooterRow();
		footer.getCell(nazevColumn).setComponent(nodeOperationsLayout);
		footer.getCell(parentColumn).setComponent(nodeParentOperationsLayout);

		seekTimeSpan = new Span();
		layout.add(seekTimeSpan);

		Button clearBtn = new Button("Vyčistit playlist", e -> {
			VLCControl.sendCommand(VLCCommand.CLEAR);
		});

		Button reindexBtn = new Button("Reindex", e -> {
			reindex(grid);
		});

		HorizontalLayout bottomLayout = new HorizontalLayout(clearBtn, reindexBtn);
		layout.add(bottomLayout);

		pushAndRunCommand(() -> {
			populate(grid, MusicIndex.getRootNode());
		});
	}

	private void updateSeekTimeSpan(long time, int count) {
		seekTimeSpan.setText("Seek time: " + time + "ms, " + count + " items");
		logger.info("seek time: " + time);
	}

	private void open(Grid<IndexNode> grid, IndexNode node) {
		if (!node.isDirectory())
			return;
		backBtn.setEnabled(true);
		pushAndRunCommand(() -> populate(grid, node));
	}

	private HorizontalLayout createButtons(Grid<IndexNode> grid, ValueHolder<IndexNode> nodeHolder) {
		Button playBtn = new Button("Přehrát", e -> {
			VLCControl.sendCommand(VLCCommand.ADD, nodeHolder.getValue().getPath().toString());
		});
		playBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

		Button enqueueBtn = new Button("Přidat", e -> {
			VLCControl.sendCommand(VLCCommand.ENQUEUE, nodeHolder.getValue().getPath().toString());
		});
		enqueueBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

		Button openBtn = new Button("Otevřít", e -> {
			open(grid, nodeHolder.getValue());
		});
		openBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

		HorizontalLayout hl = new HorizontalLayout(new HorizontalLayout(playBtn, enqueueBtn, openBtn)) {
			private static final long serialVersionUID = 8082960470793958627L;

			@Override
			public void setEnabled(boolean enabled) {
				openBtn.setEnabled(nodeHolder.getValue() == null ? false : nodeHolder.getValue().isDirectory());
				super.setEnabled(enabled);
			}
		};
		hl.setEnabled(false);
		hl.setSpacing(true);
		hl.setAlignItems(Alignment.CENTER);
		return hl;
	}

	private void reindex(Grid<IndexNode> grid) {
		MusicIndex.buildIndex();
		history = new History<>();
		pushAndRunCommand(() -> populate(grid, MusicIndex.getRootNode()));
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

	private void populate(Grid<IndexNode> grid, IndexNode node) {
		long start = System.currentTimeMillis();
		List<IndexNode> list = node.getSubnodes();
		
		populate(grid, list);
		
		updateSeekTimeSpan(System.currentTimeMillis() - start, list.size());
		currentDirSpan.setText("Zobrazení: '" + node.getPathName() + "'");
	}

	private void populate(Grid<IndexNode> grid, String filter) {
		long start = System.currentTimeMillis();
		List<IndexNode> list = new ArrayList<>();
		try {
			findRecursive(currentNode, filter, list);
		} catch (IOException e) {
			String msg = "Nezdařilo se získat přehled adresáře hudby";
			logger.error(msg, e);
			TrayControl.showMessage(msg);
			return;
		}

		populate(grid, list);

		updateSeekTimeSpan(System.currentTimeMillis() - start, list.size());
		currentDirSpan.setText("Vyhledání: '" + filter + "'");
	}

	private void populate(Grid<IndexNode> grid, List<IndexNode> list) {
		nodeOperationsLayout.setEnabled(false);
		nodeParentOperationsLayout.setEnabled(false);
		grid.setDataProvider(new ListDataProvider<>(list));
	}
}
