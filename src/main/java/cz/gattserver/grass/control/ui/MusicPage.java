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
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
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
public class MusicPage extends MainPage {

	private static final long serialVersionUID = 226911270833721492L;

	private static final Logger logger = LoggerFactory.getLogger(MusicPage.class);

	private static History<Command> history = new History<>();
	private IndexNode currentNode;

	private HorizontalLayout nodeOperationsLayout;
	private HorizontalLayout nodeParentOperationsLayout;

	private Button backBtn;
	private Button forwardBtn;
	private Button upBtn;

	public MusicPage() {
		setSizeFull();

		currentNode = MusicIndex.getRootNode();

		Grid<IndexNode> grid = new Grid<>();
		grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
		grid.setSelectionMode(SelectionMode.MULTI);
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

		Span seekTimeSpan = new Span();
		Span currentDirSpan = new Span();

		MusicPageUIBundle bundle = new MusicPageUIBundle(grid, seekTimeSpan, currentDirSpan);

		grid.addItemClickListener(e -> {
			if (e.getClickCount() == 1) {
				if (grid.getSelectedItems().contains(e.getItem())) {
					grid.deselect(e.getItem());
				} else {
					grid.select(e.getItem());
				}
			} else {
				if (e.getColumn() == nazevColumn) {
					open(bundle, e.getItem());
				} else if (e.getColumn() == parentColumn) {
					open(bundle, e.getItem().getParentNode());
				}
			}
		});

		backBtn = new Button("<", e -> {
			history.back().run(bundle);
			updateHistoryButtons();
		});
		backBtn.setEnabled(false);

		forwardBtn = new Button(">", e -> {
			history.forward().run(bundle);
			updateHistoryButtons();
		});
		forwardBtn.setEnabled(false);

		upBtn = new Button("^", e -> {
			if (currentNode == null && currentNode.equals(MusicIndex.getRootNode()))
				return;
			open(bundle, currentNode.getParentNode());
			updateHistoryButtons();
		});

		TextField searchField = new TextField();
		searchField.setWidthFull();

		Button searchBtn = new Button("Hledat", e -> {
			pushAndRunCommand(bundle, b -> populate(b, searchField.getValue()));
			currentNode = null;
		});
		searchBtn.setWidth(null);
		searchField.addKeyPressListener(Key.ENTER, e -> searchBtn.click());

		HorizontalLayout topLayout = new HorizontalLayout(new HorizontalLayout(backBtn, forwardBtn, upBtn, searchBtn),
				searchField);
		topLayout.setWidthFull();
		layout.add(topLayout);

		layout.add(currentDirSpan);

		layout.add(grid);

		nodeOperationsLayout = createButtons(bundle, grid, false);
		nodeParentOperationsLayout = createButtons(bundle, grid, true);

		FooterRow footer = grid.appendFooterRow();
		footer.getCell(nazevColumn).setComponent(nodeOperationsLayout);
		footer.getCell(parentColumn).setComponent(nodeParentOperationsLayout);

		layout.add(seekTimeSpan);

		Button clearBtn = new Button("Vyčistit playlist", e -> {
			VLCControl.sendCommand(VLCCommand.CLEAR);
		});

		Button reindexBtn = new Button("Reindex", e -> {
			reindex(bundle);
		});

		HorizontalLayout bottomLayout = new HorizontalLayout(clearBtn, reindexBtn);
		layout.add(bottomLayout);

		if (history.isEmpty()) {
			pushAndRunCommand(bundle, b -> {
				populate(b, MusicIndex.getRootNode());
			});
		} else {
			history.getCurrent().run(bundle);
			updateHistoryButtons();
		}
	}

	private void updateHistoryButtons() {
		backBtn.setEnabled(!history.isFirst());
		forwardBtn.setEnabled(!history.isLast());
	}

	private void updateSeekTimeSpan(MusicPageUIBundle bundle, long time, int count) {
		bundle.getSeekTimeSpan().setText("Seek time: " + time + "ms, " + count + " items");
		logger.info("seek time: " + time);
	}

	private void open(MusicPageUIBundle bundle, IndexNode node) {
		if (node.isDirectory()) {
			currentNode = node;
			backBtn.setEnabled(true);
			pushAndRunCommand(bundle, b -> populate(b, node));
		} else {
			VLCControl.sendCommand(VLCCommand.ADD, node.getPath().toString());
		}
	}

	private HorizontalLayout createButtons(MusicPageUIBundle bundle, Grid<IndexNode> grid, boolean forParent) {
		Button playBtn = new Button("Přehrát", e -> {
			for (IndexNode item : grid.getSelectedItems()) {
				if (forParent)
					item = item.getParentNode();
				VLCControl.sendCommand(VLCCommand.ADD, item.getPath().toString());
			}
		});
		playBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
		playBtn.setEnabled(false);

		Button enqueueBtn = new Button("Přidat", e -> {
			for (IndexNode item : grid.getSelectedItems()) {
				if (forParent)
					item = item.getParentNode();
				VLCControl.sendCommand(VLCCommand.ENQUEUE, item.getPath().toString());
			}
		});
		enqueueBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
		enqueueBtn.setEnabled(false);

		Button openBtn = new Button("Otevřít", e -> {
			IndexNode item = grid.getSelectedItems().iterator().next();
			if (forParent)
				item = item.getParentNode();
			open(bundle, item);
		});
		openBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
		openBtn.setEnabled(false);

		grid.addSelectionListener(e -> {
			int size = e.getAllSelectedItems().size();
			playBtn.setEnabled(size > 0);
			enqueueBtn.setEnabled(size > 0);
			openBtn.setEnabled(size == 1 && e.getFirstSelectedItem().get().isDirectory());
		});

		HorizontalLayout hl = new HorizontalLayout(new HorizontalLayout(playBtn, enqueueBtn, openBtn));
		hl.setSpacing(true);
		hl.setAlignItems(Alignment.CENTER);
		return hl;
	}

	private void reindex(MusicPageUIBundle bundle) {
		MusicIndex.buildIndex();
		history = new History<>();
		pushAndRunCommand(bundle, b -> populate(b, MusicIndex.getRootNode()));
		backBtn.setEnabled(false);
		forwardBtn.setEnabled(false);
	}

	private void pushAndRunCommand(MusicPageUIBundle bundle, Command cmd) {
		cmd.run(bundle);
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

	private void populate(MusicPageUIBundle bundle, IndexNode node) {
		long start = System.currentTimeMillis();
		List<IndexNode> list = node.getSubnodes();

		populate(bundle, list);

		updateSeekTimeSpan(bundle, System.currentTimeMillis() - start, list.size());
		bundle.getCurrentDirSpan().setText("Zobrazení: '" + node.getPathName() + "'");
	}

	private void populate(MusicPageUIBundle bundle, String filter) {
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

		populate(bundle, list);

		updateSeekTimeSpan(bundle, System.currentTimeMillis() - start, list.size());
		bundle.getCurrentDirSpan().setText("Vyhledání: '" + filter + "'");
	}

	private void populate(MusicPageUIBundle bundle, List<IndexNode> list) {
		bundle.getGrid().setDataProvider(new ListDataProvider<>(list));
	}
}
