package cz.gattserver.grass.control.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.Main;

public class MessageWindow extends JWindow {

	private static final long serialVersionUID = -7019332829942824879L;

	private static final Logger logger = LoggerFactory.getLogger(MessageWindow.class);

	private static final int DELAY = 5000;

	// K této kolekci se musí přistupovat přes synchronized
	private static List<MessageWindow> activeWindows = new ArrayList<MessageWindow>();

	private static void addWindow(MessageWindow caller) {
		synchronized (activeWindows) {
			logger.trace("MessageWindow '" + caller.getMessage() + "' addWindow ");
			for (MessageWindow w : activeWindows) {
				Point p = caller.getLocation();
				caller.setLocation(p.x, p.y - w.getHeight());
			}
			activeWindows.add(caller);
		}
	}

	private static void removeWindow(MessageWindow caller) {
		synchronized (activeWindows) {
			logger.trace("MessageWindow '" + caller.getMessage() + "' removeWindow ");
			activeWindows.remove(caller);
			for (MessageWindow w : activeWindows) {
				Point p = w.getLocation();
				w.setLocation(p.x, p.y + caller.getHeight());
			}
		}
	}

	public static float toPerc(int value) {
		return value / 255f;
	}

	private static BufferedImage loadImage(String imageName) {
		InputStream is = Main.class.getClassLoader().getResourceAsStream(imageName);
		try {
			return ImageIO.read(is);
		} catch (IOException e1) {
			throw new IllegalStateException("Image '" + imageName + "' was not found");
		}
	}

	private String message;

	public MessageWindow(String message, MessageLevel level) {
		this.message = message;
		logger.trace("MessageWindow '" + message + "' creation started");

		setAlwaysOnTop(true);
		setBackground(new Color(toPerc(244), toPerc(241), toPerc(230)));
		// setBackground(new Color(toPerc(216), toPerc(205), toPerc(166)));

		JPanel borderPane = new JPanel();
		borderPane.setBorder(new MatteBorder(1, 1, 1, 1, new Color(toPerc(179), toPerc(179), toPerc(179))));
		borderPane.setBackground(new Color(0, 0, 0, 0));
		add(borderPane);

		JPanel contentPane = new JPanel();
		int marginSize = 2;
		contentPane.setBorder(new EmptyBorder(marginSize, marginSize, marginSize, marginSize));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setBackground(new Color(0, 0, 0, 0));
		borderPane.add(contentPane);

		ImageIcon headerIcon = new ImageIcon(loadImage("favicon.png"));
		JLabel headerLabel = new JLabel("Grass control info", headerIcon, JLabel.LEFT);
		contentPane.add(headerLabel);

		contentPane.add(Box.createVerticalStrut(5));
		contentPane.add(new JSeparator(SwingConstants.HORIZONTAL));
		contentPane.add(Box.createVerticalStrut(5));

		String messageIconName = null;
		switch (level) {
		case ERROR:
			messageIconName = "block_16.png";
			break;
		case WARN:
			messageIconName = "warn_16.png";
			break;
		case INFO:
		default:
			messageIconName = "info_16.png";
			break;
		}
		ImageIcon messageIcon = new ImageIcon(loadImage(messageIconName));
		JLabel messageLabel = new JLabel(message, messageIcon, JLabel.LEFT);
		contentPane.add(messageLabel);

		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = getWidth();
		int h = getHeight();
		int x = (int) screenSize.getWidth() - w - 10;
		int y = (int) screenSize.getHeight() - h - 40;
		setBounds(x, y, w, h);

		setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				MessageWindow w = MessageWindow.this;
				addWindow(w);
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					MessageWindow w = MessageWindow.this;
					Thread.sleep(DELAY);
					while (w.getOpacity() > 0) {
						Thread.sleep(50);
						w.setOpacity(Math.max(w.getOpacity() - 0.05f, 0));
					}
					w.setVisible(false);
					removeWindow(w);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		logger.trace("MessageWindow '" + message + "' creation done");
	}

	public String getMessage() {
		return message;
	}
}
