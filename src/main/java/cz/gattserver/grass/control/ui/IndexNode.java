package cz.gattserver.grass.control.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IndexNode {

	private boolean directory;
	private String pathName;
	private String pathNameLowerCase;
	private Path path;

	private IndexNode parentNode;
	private List<IndexNode> subnodes = new ArrayList<>();

	public IndexNode(IndexNode parentNode, Path path) {
		this.parentNode = parentNode;
		this.path = path;
		this.pathName = path.getFileName().toString();
		this.pathNameLowerCase = this.pathName.toLowerCase();
		this.directory = Files.isDirectory(path);
	}

	public IndexNode getParentNode() {
		return parentNode;
	}

	public Path getPath() {
		return path;
	}

	public String getPathName() {
		return pathName;
	}

	public String getPathNameLowerCase() {
		return pathNameLowerCase;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void addSubnode(IndexNode node) {
		subnodes.add(node);
	}

	public List<IndexNode> getSubnodes() {
		return subnodes;
	}

}
