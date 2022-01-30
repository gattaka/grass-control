package cz.gattserver.grass.control.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.speech.SpeechLogTO;

@Route("speech")
@PageTitle("Hlasové ovládání")
public class SpeechPage extends MainPage {

	private static final long serialVersionUID = -4163366877491175781L;

	private static final Logger logger = LoggerFactory.getLogger(SpeechPage.class);

	public SpeechPage() {
		setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		add(layout);

		Grid<SpeechLogTO> grid = new Grid<>();
		grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
		grid.setSizeFull();

		grid.addColumn(new LocalDateTimeRenderer<SpeechLogTO>(SpeechLogTO::getTime)).setHeader("Čas");
		grid.addColumn(new TextRenderer<SpeechLogTO>(SpeechLogTO::getCommand)).setHeader("Příkaz");

		layout.add(grid);

		Button refreshButton = new Button("Obnovit", e -> {
			populate(grid);
		});
		layout.add(refreshButton);
	}

	private void populate(Grid<SpeechLogTO> grid) {
		List<SpeechLogTO> log = SpeechControl.getHistory();
		grid.setDataProvider(new ListDataProvider<>(log));
	}
}
