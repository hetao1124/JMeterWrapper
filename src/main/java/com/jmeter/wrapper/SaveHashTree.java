package com.jmeter.wrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

public class SaveHashTree {
	
	public static void saveTree(HashTree hashTree,FileOutputStream ostream) throws IOException{
		convertSubTree(hashTree);
		SaveService.saveTree(hashTree, ostream);
	}
	
	// package protected to allow access from test code
	private static void convertSubTree(HashTree tree) {
		for (Object o : new LinkedList<>(tree.list())) {
			JMeterTreeNode item = (JMeterTreeNode) o;
			convertSubTree(tree.getTree(item));
			TestElement testElement = item.getTestElement(); // requires
																// JMeterTreeNode
			tree.replaceKey(item, testElement);
		}
	}

}
