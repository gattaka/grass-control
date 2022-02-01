package cz.gattserver.grass.control.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;

/**
 * @author gattaka
 *
 */
public class MusicPageUIBundle {

	private Grid<IndexNode> grid;
	private Span seekTimeSpan;
	private Span currentDirSpan;

	public MusicPageUIBundle(Grid<IndexNode> grid, Span seekTimeSpan, Span currentDirSpan) {
		super();
		this.grid = grid;
		this.seekTimeSpan = seekTimeSpan;
		this.currentDirSpan = currentDirSpan;
	}

	public Grid<IndexNode> getGrid() {
		return grid;
	}

	public void setGrid(Grid<IndexNode> grid) {
		this.grid = grid;
	}

	public Span getSeekTimeSpan() {
		return seekTimeSpan;
	}

	public void setSeekTimeSpan(Span seekTimeSpan) {
		this.seekTimeSpan = seekTimeSpan;
	}

	public Span getCurrentDirSpan() {
		return currentDirSpan;
	}

	public void setCurrentDirSpan(Span currentDirSpan) {
		this.currentDirSpan = currentDirSpan;
	}

}
