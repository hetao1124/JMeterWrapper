package com.jmeter.wrapper;

import org.apache.jmeter.util.JMeterUtils;

public class JMeterConfig {
	
	private static JMeterConfig config;
	private static byte[] b = new byte[1];
	
	private String JMeterBinDir;
	private String JMeterDir;
	
	private  boolean buildDashboartReport;
	
	public static boolean isWindows;


	private JMeterConfig() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			isWindows = true;
		}
	}
	
	static JMeterConfig getJMeterConfig() {
		if(config==null) {
			synchronized(b) {
				if(config==null) {
					config=new JMeterConfig();
				}
			}
		}
		return config;
	}
	
	String getJMeterDir() {
		return JMeterDir;
	}

	void setJMeterDir(String jMeterDir) {
		JMeterDir = jMeterDir;
		JMeterUtils.setJMeterHome(JMeterDir);
		this.JMeterBinDir = this.JMeterDir+"/bin";
	}

	String getJMeterBinDir() {
		return JMeterBinDir;
	}

	boolean isBuildDashboartReport() {
		return buildDashboartReport;
	}

	void setBuildDashboartReport(boolean buildDashboartReport) {
		this.buildDashboartReport = buildDashboartReport;
	}
	
	

}
