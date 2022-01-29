package cz.gattserver.grass.control.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Grasscontrol")
public class MainPage extends Div {

	private static final long serialVersionUID = -220383546549760661L;

	public MainPage() {

		add("Test");

	}

}
