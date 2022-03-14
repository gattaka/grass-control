package cz.gattserver.grass.control;

import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.vaadin.flow.server.startup.ServletContextListeners;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.ui.MusicIndex;
import cz.gattserver.grass.control.ui.common.TrayControl;

public class Main {

	public static void main(String[] args) throws Exception {
		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();

		new Thread(() -> MusicIndex.buildIndex()).start();

		// src/main/webapp nesmí být prázdná složka, jinak ji maven nepřidá do
		// buildu a tohle pak bude padat na NPE
		URL webRootLocation = Main.class.getResource("/webapp/");
		URI webRootUri = webRootLocation.toURI();

		WebAppContext context = new WebAppContext();
		context.setBaseResource(Resource.newResource(webRootUri));
		context.setContextPath("/");
		context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");
		context.setConfigurationDiscovered(true);
		context.setConfigurations(new Configuration[] { new AnnotationConfiguration(), new WebInfConfiguration(),
				new WebXmlConfiguration(), new MetaInfConfiguration() });
		context.getServletContext().setExtendedListenerTypes(true);
		context.addEventListener(new ServletContextListeners());

		Server server = new Server(8765);
		server.setHandler(context);
		server.start();
		server.join();
	}
}
