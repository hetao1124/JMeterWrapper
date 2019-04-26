package com.jmeter.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;

public class JMeterTask {
	
	private long taskId;
	private String taskName;
	private String remoteServices;
	private String reportDir;
	private String scriptOutPutFile;
	private String scriptFile;
	
	private Map<TestElement, JMeterTreeNode> nodeMap = new HashMap<>();
	private JMeterTreeModel treeModel;
	
	JMeterTask(){
		this.remoteServices = "localhost";
		this.taskId = 0;
	}
	
	JMeterTask(TestPlan tp){
		this.remoteServices = "localhost";
		this.taskId = 0;
		this.insertInto(tp, null);
	}

	public String getRemoteServices() {
		return remoteServices;
	}

	public void setRemoteServices(String remoteServices) {
		this.remoteServices = remoteServices;
	}

	public long getTaskId() {
		return taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	
	public String getReportDir() {
		return reportDir;
	}

	public void setReportDir(String reportDir) {
		this.reportDir = reportDir;
	}

	public void setScriptOutPutFile(String scriptOutPutFile) {
		if(null != scriptOutPutFile) {
			if(scriptOutPutFile.endsWith(".jmx"))
			this.scriptOutPutFile = scriptOutPutFile;
		}
	}
	
	public void setScriptFile(String scriptFile) {
		if(null != scriptFile) {
			File file =new File(scriptFile);
			if(file.exists() && file.isFile() && scriptFile.endsWith(".jmx"))
			this.scriptFile = scriptFile;
		}
	}
	
	public String getScriptFile() {
		return this.scriptFile;
	}

	// Tree
	public void insertInto(TestElement te, TestElement pNode) {
		if (te == null)
			return;
		if (pNode == null) {
			if (this.treeModel == null && te instanceof TestPlan) {
				this.treeModel = new JMeterTreeModel(te);
				JMeterTreeNode testPlanNode = (JMeterTreeNode) ((JMeterTreeNode) treeModel.getRoot()).getChildAt(0);
				this.nodeMap.put(te, testPlanNode);
			}
		} else {
			if (this.treeModel == null)
				return;
			JMeterTreeNode parentNode = this.nodeMap.get(pNode);
			if (parentNode == null)
				return;
			JMeterTreeNode newNode = new JMeterTreeNode(te, treeModel);
			newNode.setEnabled(true);
			treeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
			this.nodeMap.put(te, newNode);
		}
	}
	
	// save
	private void saveTree(String outputFile) throws Exception {
		if (this.treeModel == null)
			return;
		HashTree hashTree = this.treeModel.getTestPlan();
		try {
			FileOutputStream ostream = new FileOutputStream(outputFile);
			SaveHashTree.saveTree(hashTree, ostream);
		} catch (Exception e) {
			throw e;
		}
	}

	
	String getJMeterScriptFile() throws Exception {
		if(this.scriptFile!=null) {
			return this.scriptFile;
		}
		if(this.scriptOutPutFile == null) {
			return null;
		}
		try {
			File jmx = new File(this.scriptOutPutFile);
			FileHelper.makeDir(jmx.getParent());
			this.saveTree(this.scriptOutPutFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return this.scriptOutPutFile;
	}
}
