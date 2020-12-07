package cz.gattserver.grass.control.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.speech.SpeechLogTO;

public class HistoryWindow extends JFrame {

	private static final long serialVersionUID = 2446738778779580737L;

	private static final Logger logger = LoggerFactory.getLogger(HistoryWindow.class);

	private JTable table;

	public HistoryWindow() {
		super("Historie příkazů");

		setLayout(new BorderLayout());

		JPanel buttonsLayout = new JPanel();
		add(buttonsLayout, BorderLayout.PAGE_START);

		Button okButton = new Button("Zavřít");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HistoryWindow.this.setVisible(false);
			}
		});
		buttonsLayout.add(okButton);

		Button refreshButton = new Button("Obnovit");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateTable();
			}
		});
		buttonsLayout.add(refreshButton);

		setSize(800, 500);
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

		table = new JTable();

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.PAGE_END);

		populateTable();

	}

	private void populateTable() {
		List<SpeechLogTO> list = SpeechControl.getHistory();
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
		table.setModel(new DefaultTableModel(items, columnNames));
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
	}
}
