package cz.gattserver.grass.control.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MusicIndex {

	private static final Logger logger = LoggerFactory.getLogger(MusicIndex.class);

	private static final Path ROOT = Paths.get("d:\\Hudba\\");

	private static IndexNode rootNode;

	public static void buildIndex() {
		synchronized (MusicIndex.class) {
			logger.info("Building music index");
			buildIndex(rootNode = new IndexNode(null, ROOT));
			logger.info("Building music index finished");
		}
	}

	private static void buildIndex(IndexNode node) {
		try {
			Files.list(node.getPath()).forEach(p -> {
				IndexNode newNode = new IndexNode(node, p);
				node.addSubnode(newNode);
				if (Files.isDirectory(p))
					buildIndex(newNode);
			});
		} catch (IOException e) {
			logger.error("Path listing for path " + node.getPath() + " failed", e);
		}
	}

	public static IndexNode getRootNode() {
		synchronized (MusicIndex.class) {
			return rootNode;
		}
	}

}
