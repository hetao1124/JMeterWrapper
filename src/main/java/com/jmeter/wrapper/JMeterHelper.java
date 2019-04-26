package com.jmeter.wrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.AbstractProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.threads.ThreadGroup;

public class JMeterHelper {

	private static JMeterHelper helper;
	private static Byte[] b = new Byte[1];

	private Map<String, JMeterGUIComponent> guiCache = new ConcurrentHashMap<>();
	private Map<String, TestBeanGUI> beanGuiCache = new ConcurrentHashMap<>();

	private JMeterHelper() {

	}

	static JMeterHelper getJMeterHelper() {
		if (helper == null) {
			synchronized (b) {
				if (helper == null) {
					helper = new JMeterHelper();
				}
			}
		}
		return helper;
	}
	
	//get task
	public JMeterTask getJMeterTask(TestPlan tp) {
		return new JMeterTask(tp);
	}
	
	public JMeterTask getJMeterTask() {
		return new JMeterTask();
	}

	// gui
	public void initJMeterGui() {
		JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir()+"/jmeter.properties");
		JMeterUtils.initLocale();
		this.getTestPlan();
		this.getThreadGroup();
		this.getHttpTestSample();
		this.getHTTPArguments();
		this.getArguments();
		this.getLoopController();
		this.getResponseAssertion();
		this.getRegexExtractor();
		this.getBeanShellPreProcessor();
	}

	public TestPlan getTestPlan() {
		return (TestPlan) this.createTestElementFromGui("TestPlanGui");
	}

	public ThreadGroup getThreadGroup() {
		return (ThreadGroup) this.createTestElementFromGui("ThreadGroupGui");
	}

	public HTTPSamplerBase getHttpTestSample() {
		return (HTTPSamplerBase) this.createTestElementFromGui("HttpTestSampleGui");
	}

	public Arguments getHTTPArguments() {
		return (Arguments) this.createTestElementFromGui("HTTPArgumentsPanel");
	}

	public Arguments getArguments() {
		return (Arguments) this.createTestElementFromGui("ArgumentsPanel");
	}

	public LoopController getLoopController() {
		return (LoopController) this.createTestElementFromGui("LoopControlPanel");
	}

	public ResponseAssertion getResponseAssertion() {
		return (ResponseAssertion) this.createTestElementFromGui("AssertionGui");
	}

	public RegexExtractor getRegexExtractor() {
		return (RegexExtractor) this.createTestElementFromGui("RegexExtractorGui");
	}

	public BeanShellPreProcessor getBeanShellPreProcessor() {
		TestBeanGUI testBeanGUI = this.getTestBeanGui("BeanShellPreProcessor");
		return (BeanShellPreProcessor) testBeanGUI.createTestElement();
	}
	
	public HeaderManager getHeaderManager() {
		return (HeaderManager) this.createTestElementFromGui("HeaderPanel");
	}
	
	public void setBeanShellScript(BeanShellPreProcessor beanshell, String script) {
		JMeterProperty jprop = AbstractProperty.createProperty(script);
		jprop.setName("script");
		beanshell.setProperty(jprop);
	}

	// testBeanGui

	private TestBeanGUI getTestBeanGui(String name) {
		TestBeanGUI beanGUI = this.beanGuiCache.get(name);
		if (beanGUI == null) {
			switch (name) {
			case "BeanShellPreProcessor":
				BeanShellPreProcessor beanShellPreProcessor = new BeanShellPreProcessor();
				beanGUI = new TestBeanGUI(beanShellPreProcessor.getClass());
				break;
			}
			this.beanGuiCache.put(name, beanGUI);
		}
		return beanGUI;
	}

	// gui

	private TestElement createTestElementFromGui(String guiName) {
		JMeterGUIComponent com = this.get(guiName);
		TestElement te = this.createTestElement(com);
		return te;
	}

	private TestElement createTestElement(JMeterGUIComponent com) {
		TestElement te = com.createTestElement();
		return te;
	}

	private JMeterGUIComponent get(String guiName) {
		JMeterGUIComponent com = this.guiCache.get(guiName);
		if (com == null) {
			com = this.getJMeterGUIComponent(guiName);
			this.put(guiName, com);
		}
		return com;
	}

	private void put(String guiName, JMeterGUIComponent com) {
		this.guiCache.put(guiName, com);
	}

	private JMeterGUIComponent getJMeterGUIComponent(String guiName) {
		JMeterGUIComponent com = null;
		switch (guiName) {
		case "TestPlanGui":
			com = new TestPlanGui();
			break;
		case "ThreadGroupGui":
			com = new ThreadGroupGui();
			break;
		case "HttpTestSampleGui":
			com = new HttpTestSampleGui();
			break;
		case "AssertionGui":
			com = new AssertionGui();
			break;
		case "RegexExtractorGui":
			com = new RegexExtractorGui();
			break;
		case "ArgumentsPanel":
			com = new ArgumentsPanel();
			break;
		case "HTTPArgumentsPanel":
			com = new HTTPArgumentsPanel();
			break;
		case "LoopControlPanel":
			com = new LoopControlPanel(false);
			break;
		case "HeaderPanel":
			com = new HeaderPanel();
			break;
		}
		return com;
	}

}
