package cz.gattserver.grass.control.ui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.speech.SpeechLogTO;

public class HistoryWindow extends Frame {

	private static final long serialVersionUID = 2446738778779580737L;

	private static final Logger logger = LoggerFactory.getLogger(HistoryWindow.class);

	public HistoryWindow() {
		super("Historie příkazů");

		List<SpeechLogTO> list = SpeechControl.getHistory();

		setSize(600, 500);
		setLocationRelativeTo(null);

		try {
			setIconImage(TrayControl.getIcon());
		} catch (IOException e) {
			logger.error("Icon load failed");
		}

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				HistoryWindow.this.setVisible(false);
			}
		});

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Object[][] items = new Object[list.size()][4];
		for (int i = 0; i < list.size(); i++) {
			SpeechLogTO to = list.get(i);
			items[i][0] = sdf.format(to.getTime());
			items[i][1] = to.getCommand();
			items[i][2] = to.getScore();
			items[i][3] = to.isInRange() ? "Ano" : "Ne";
		}
		Object[] columnNames = new Object[] { "Čas", "Příkaz", "Score", "V rozsahu" };

		JTable table = new JTable(items, columnNames);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		add(scrollPane);
	}
}
