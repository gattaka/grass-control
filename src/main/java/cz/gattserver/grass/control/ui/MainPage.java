package cz.gattserver.grass.control.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinService;

@PageTitle("Grasscontrol")
public class MainPage extends VerticalLayout implements PageConfigurator {

    private static final long serialVersionUID = -220383546549760661L;

    private static final String MUSIC_NAME = "music";
    private static final String SPEECH_NAME = "speech";

    public MainPage() {
        setPadding(false);
        setSpacing(false);

        Tabs menu = new Tabs();
        menu.setWidthFull();
        add(menu);

        Tab music = new Tab("Hudba");
        Tab speech = new Tab("Hlasové ovládání");
        menu.add(music, speech);

        String[] chunks = VaadinService.getCurrentRequest().getPathInfo().split("/");
        if (chunks.length > 0) {
            String currentPage = chunks[chunks.length - 1];
            switch (currentPage) {
                case SPEECH_NAME:
                    menu.setSelectedTab(speech);
                    break;
                case MUSIC_NAME:
                default:
                    menu.setSelectedTab(music);
                    break;
            }
        }

        menu.addSelectedChangeListener(e -> {
            if (e.getSelectedTab() == music)
                UI.getCurrent().getPage().setLocation(MUSIC_NAME);
            if (e.getSelectedTab() == speech)
                UI.getCurrent().getPage().setLocation(SPEECH_NAME);
        });
    }

    /**
     * Na Jetty je default favicon.ico sice nalezena, ale ikona je totálně
     * rozhozená (pixely jsou spíš šum), přes vaadin BootstrapListener je zase
     * problém, že byť je vše nastaveno, jak má, tak se prostě nic nezavolá.
     * Jediná možnost, která funguje je tedy deprecated PageConfigurator
     */
    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addFavIcon("icon", "VAADIN/icons/favicon.png", "16x16");
    }

}
