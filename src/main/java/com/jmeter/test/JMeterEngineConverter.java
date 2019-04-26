package com.jmeter.test;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import com.jmeter.wrapper.AbstractJMeterEngineConverter;
import com.jmeter.wrapper.JMeterCallBack;
import com.jmeter.wrapper.JMeterHelper;
import com.jmeter.wrapper.JMeterTask;

public class JMeterEngineConverter extends AbstractJMeterEngineConverter{


	private static String JMeterDir;
	private static String JMeterReportDir;
	private static String outPutXmlDir;

	static {
		Properties properties = new Properties();
		try (InputStream in = JMeterEngineConverter.class.getResourceAsStream("/application.properties");

		) {
			properties.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JMeterDir = properties.getProperty("JMeterDir");
		JMeterReportDir = properties.getProperty("JMeterReportDir");
		outPutXmlDir = properties.getProperty("outPutXmlDir");
		boolean dashboardReport = Boolean.valueOf(properties.getProperty("DashboardReport", "false"));
		properties.clear();
		properties = null;
		setJMeterHome(JMeterDir);// 必须
		setDashboardReport(dashboardReport);// 不生成jmeter的报告 非必需
		jmeterHelper = getOnlyOneJMeterHelper();// 必须
		jmeterHelper.initJMeterGui(); // 必须
		setOnlyOneJMeterEngine();// 必须
	}

	private JMeterEngineConverter() {

	}

	public static JMeterHelper getJMeterHelper() {
		return jmeterHelper;
	}

	public static Map<String, String> doLoadTest(JMeterTask task) {
		if (task.getScriptFile() == null) {// 如果是运行jmx脚本 不用设置脚本输出路径 这样就不会再生成脚本了
			String jmxOutPutDir = outPutXmlDir + "/task" + task.getTaskId();
			task.setScriptOutPutFile(jmxOutPutDir + "/test.jmx");
		}
		// 设置报告输出路径
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
		String date = df.format(new Date());
		String reportDir = JMeterReportDir + "/task" + task.getTaskId() + "/" + date;
		task.setReportDir(reportDir);
		// 创建一个回调实例 并开始运行task 一次只能运用一个task
		JMeterCallBack callBack = new JMeterCallBackImpl(task.getTaskId(),reportDir);// 必须
		Map<String, String> result = runJMeterLoadTask(task, callBack);
		return result;
	}

	public static void stopJMeter() {
		try {
			JMeterCallBackImpl.isNormalStop = false;
			// 主动停止jmeter 会生成报告
			boolean isKilledJMeter = stopJMeterLoadTest();
			if (!isKilledJMeter) {
				JMeterCallBackImpl.isNormalStop = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JMeterCallBackImpl.isNormalStop = true;
		}
	}

}
