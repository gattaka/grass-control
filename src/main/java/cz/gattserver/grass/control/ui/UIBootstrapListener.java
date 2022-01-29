package cz.gattserver.grass.control.ui;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vaadin.flow.server.BootstrapListener;
import com.vaadin.flow.server.BootstrapPageResponse;

public class UIBootstrapListener implements BootstrapListener {

	private static final long serialVersionUID = -7909309188394747766L;

	public void modifyBootstrapPage(BootstrapPageResponse response) {
		Document document = response.getDocument();
		Element head = document.head();

		// <link rel="icon" href="/favicon.ico" type="image/x-icon">
		Element link = document.createElement("link");
		link.attr("href", "icons/favicon.png");
		link.attr("type", "image/x-icon");
		head.appendChild(link);
	}
}