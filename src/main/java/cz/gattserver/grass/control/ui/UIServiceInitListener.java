package cz.gattserver.grass.control.ui;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class UIServiceInitListener implements VaadinServiceInitListener {

	private static final long serialVersionUID = -3103132052309635500L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addBootstrapListener(new UIBootstrapListener());

		event.addDependencyFilter((dependencies, filterContext) -> {
			// DependencyFilter to add/remove/change dependencies sent to
			// the client
			return dependencies;
		});

		event.addRequestHandler((session, request, response) -> {
			// RequestHandler to change how responses are handled
			return false;
		});
	}

}