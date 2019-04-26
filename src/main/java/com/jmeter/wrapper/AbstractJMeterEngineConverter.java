package com.jmeter.wrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractJMeterEngineConverter {

	protected static JMeterHelper jmeterHelper;
	protected static JMeterEngine engine;

	protected static JMeterHelper getOnlyOneJMeterHelper() {
		jmeterHelper = JMeterHelper.getJMeterHelper();
		return jmeterHelper;
	}
	
	protected static JMeterHelper getOnlyOneJMeterHelperAndInitGUI() {
		jmeterHelper = JMeterHelper.getJMeterHelper();
		jmeterHelper.initJMeterGui();
		return jmeterHelper;
	}

	protected static void setOnlyOneJMeterEngine() {
		if (engine == null) {
			engine = JMeterEngine.getJMeterEngine();
		}
	}

	protected static void setJMeterHome(String dir) {
		JMeterConfig config = JMeterConfig.getJMeterConfig();
		config.setJMeterDir(dir);
	}
	
	protected static void setDashboardReport(boolean buildDashboartReport) {
		JMeterConfig config = JMeterConfig.getJMeterConfig();
		config.setBuildDashboartReport(buildDashboartReport);;
	}

	protected static Map<String, String> runJMeterLoadTask(JMeterTask task,JMeterCallBack call) {
		Map<String, String> result = new HashMap<String, String>();
		if (null == engine) {
			result.put("fail", "engine is null");
		} else {
			result = engine.doTask(task,call);
		}
		return result;
	}
	
	protected static boolean stopJMeterLoadTest() throws IOException {
		if(null == engine) {
			return false;
		}
		return engine.stopJMeterProcess();
	}

}
