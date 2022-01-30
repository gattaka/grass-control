package cz.gattserver.grass.control.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;

@Route("")
@PageTitle("Grasscontrol")
public class MainPage extends Div implements PageConfigurator {

	private static final long serialVersionUID = -220383546549760661L;

	public MainPage() {
	}

	/**
	 * Na Jetty je default favicon.ico sice nalezena, ale ikona je totálně
	 * rozhozená (pixely jsou spíš šum), přes vaadin BootstrapListener je zase
	 * problém, že byť je vše nastaveno, jak má, tak se prostě nic nezavolá.
	 * Jediná možnost, která funguje je tedy deprecated PageConfigurator
	 */
	@Override
	public void configurePage(InitialPageSettings settings) {
		settings.addFavIcon("icon", "icons/favicon.png", "16x16");
	}

}
