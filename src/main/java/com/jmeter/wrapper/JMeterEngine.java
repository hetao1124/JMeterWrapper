package com.jmeter.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class JMeterEngine {

	private boolean isStarted;
	private JMeterConfig jmeterConfig;
	private static JMeterEngine engine;
	private static byte[] b = new byte[1];
	private static byte[] c = new byte[1];

	private String reportDir;
	private final String dashboardDirName = "/report_dashboard";

	public static boolean isWindows;

	private JMeterEngine() {
		this.jmeterConfig = JMeterConfig.getJMeterConfig();
	}

	static JMeterEngine getJMeterEngine() {
		if (engine == null) {
			synchronized (b) {
				if (engine == null) {
					engine = new JMeterEngine();
					if (System.getProperty("os.name").startsWith("Windows")) {
						isWindows = true;
					}
				}
			}
		}
		return engine;
	}

	Map<String, String> doTask(JMeterTask task, JMeterCallBack call) {
		Map<String, String> rs = new HashMap<String, String>();
		if (task == null) {
			rs.put("[JMeterEngine]fail", "task is null");
			return rs;
		}
		if (isStarted) {
			rs.put("[JMeterEngine]fail", "task is started1");
			return rs;
		}
		synchronized (c) {
			if (isStarted) {
				rs.put("[JMeterEngine]fail", "task is started2");
				return rs;
			}
			isStarted = true;
			System.out.println("[JMeterEngine]set \"start\" status is true");
		}
		String jmx = null;
		try {
			jmx = task.getJMeterScriptFile();
		} catch (Exception e) {
			rs.put("[JMeterEngine]fail", e.toString());
			return rs;
		}
		if (jmx == null) {
			rs.put("[JMeterEngine]fail", "get jxm fail,jmx is null");
			return rs;
		}
		this.reportDir = task.getReportDir();
		StringBuilder sb = new StringBuilder();
		sb.append("jmeter -n").append(" ");
		sb.append("-t").append(" ").append(jmx).append(" ");
		sb.append("-l").append(" ").append(reportDir).append("/log.jtl").append(" ");
		sb.append("-j").append(" ").append(reportDir).append("/jmeter.log");
		if (this.isNotLocaHost(task.getRemoteServices())) {
			sb.append(" -R ").append(task.getRemoteServices());
		}
		if(jmeterConfig.isBuildDashboartReport()) {
			sb.append(" -e");
			sb.append(" -o ").append(reportDir).append(dashboardDirName);
		}
		File dir = this.getJMeterBinDir();
		if (dir.isDirectory() && dir.exists()) {
			FileHelper.makeDir(reportDir);
			FileHelper.clearDir(reportDir);
			String exeComm = "./" + sb.toString();
			if (isWindows)
				exeComm = "cmd /c " + sb.toString();
			this.startJMeterProcess(dir, exeComm, call);
			rs.put("[JMeterEngine]success", reportDir);
		} else {
			rs.put("[JMeterEngine]fail", dir.getAbsolutePath() + " is error");
		}
		return rs;
	}

	private File getJMeterBinDir() {
		File file = new File(jmeterConfig.getJMeterBinDir());
		return file;
	}

	private boolean isNotLocaHost(String ips) {
		String[] ipsArray = ips.split(",");
		if (ipsArray.length == 1 && "localhost".equals(ipsArray[0])) {
			return false;
		} else {
			return true;
		}
	}

	boolean stopJMeterProcess() throws IOException {
		System.out.println("[stopJMeterProcess]start stop JMeter... ");
		Runtime r = Runtime.getRuntime();
		BufferedReader br = null;
		Process process = null;
		String[] comm;
		if (isWindows) {
			comm = new String[] {
					"cmd /c wmic process where caption=\"java.exe\" get processid,caption,commandline /value" };
		} else {
			comm = new String[] { "/bin/sh", "-c", "ps -ef | grep ApacheJMeter | grep -v grep" };
		}
		try {
			if (isWindows) {
				process = r.exec(comm[0]);
			} else {
				process = r.exec(comm);
			}
			br = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
			String inline;
			String ProcessId = null;
			boolean haveJMeterProcess = false;
			while (null != (inline = br.readLine())) {
				if (haveJMeterProcess) {
					if (isWindows && inline.contains("ProcessId")) {
						ProcessId = inline.split("=")[1];
						break;
					}
				}
				if (inline.contains("ApacheJMeter.jar")) {
					haveJMeterProcess = true;
					System.out.println("[stopJMeterProcess]cmd output:" + inline);
					if (!isWindows) {
						ProcessId = inline.split("\\s+")[1];
						break;
					}
				}
			}
			if (ProcessId != null) {
				System.out.println("[stopJMeterProcess]ProcessId is " + ProcessId);
				String taskkill = "kill -s 9 " + ProcessId;
				if (isWindows)
					taskkill = "cmd /c taskkill /F /PID " + ProcessId;
				System.out.println("[cmd]" + taskkill);
				process = r.exec(taskkill);
				try {
					process.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("[stopJMeterProcess]stop JMeter success");
				// 生成报告
				if(this.jmeterConfig.isBuildDashboartReport()) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							for (int i = 0; i < 11; i++) {
								System.out.println("[stopJMeterProcess]wait generateReport...");
								if (isStarted == false) {
									try {
										generateReport();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
								if (i == 10) {
									System.out.println("[stopJMeterProcess]wait generateReport time out,exit.");
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}).start();
				}
				return true;
			} else {
				System.out.println("[stopJMeterProcess]JMeter is stoped,exit. ");
				return false;
			}

		} catch (IOException e) {
			throw e;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (process != null) {
				try {
					process.getInputStream().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				process.destroy();
				process = null;
			}
		}
	}

	private void generateReport() throws IOException {
		String dashboardDir = this.reportDir + dashboardDirName;
		File dashboardDirFile = new File(dashboardDir);
		if (dashboardDirFile.exists()) {
			File[] fileList = dashboardDirFile.listFiles();
			if (fileList != null && fileList.length > 0) {
				return;
			}
		}
		String jtlFile = this.reportDir + "/log.jtl";
		File file = new File(jtlFile);
		if (!file.exists()) {
			System.out.println("[stopJMeterProcess]" + jtlFile + " is not exists");
			return;
		}
		CSVLogReader csvReader = new CSVLogReader(file);	
		System.out.println("[stopJMeterProcess]build jtl...");
		FileWriter fileWriter = null;
		try {
			file.delete();
			file.createNewFile();
			fileWriter = new FileWriter(file);
			String s;
			while ((s = csvReader.readCSVLineToStr()) != null){
				fileWriter.write(s + "\n");
			}
			fileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			try {
				csvReader.close();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// jmeter 生成报告
		System.out.println("[stopJMeterProcess]run JMeter to generate report...");
		Runtime r = Runtime.getRuntime();
		Process process = null;
		BufferedReader br = null;
		String comm = "./jmeter -g " + jtlFile + " -o " + dashboardDir;
		if (isWindows)
			comm = "cmd /c jmeter -g " + jtlFile + " -o " + dashboardDir;
		System.out.println("[cmd]" + comm);
		try {
			process = r.exec(comm, null, this.getJMeterBinDir());
			br = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
			String inline;
			while (null != (inline = br.readLine())) {
				System.out.println("[JMeter]" + inline);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
			if (process != null) {
				process.destroy();
				process = null;
			}
			System.out.println("[stopJMeterProcess]jmeter generate report end");
		}
	}

	private void startJMeterProcess(File dir, String comm, JMeterCallBack call) {
		String jmeterErr = "error";
		new Thread(new Runnable() {
			@Override
			public void run() {
				Runtime r = Runtime.getRuntime();
				BufferedReader br = null;
				Process process = null;
				boolean isException = false;
				try {
					if (!isWindows) {
						System.out.println("[cmd] chmod 777 jmeter");
						process = r.exec("chmod 777 jmeter", null, dir);
					}
					System.out.println("[cmd]" + comm);
					process = r.exec(comm, null, dir);
					System.out.println("[JMeterEngine]start test");
					call.taskStartedCallBack();
					br = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
					String inline;
					while (null != (inline = br.readLine())) {
						System.out.println("[JMeter]" + inline);
						if (inline.contains(jmeterErr)) {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					isException = true;
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (process != null) {
						try {
							process.getInputStream().close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						process.destroy();
						process = null;
					}
					System.out.println("[JMeterEngine]end test");
					isStarted = false;
					System.out.println("[JMeterEngine]set \"start\" status is false");
					if (isException == false) {
						System.out.println("[JMeterEngine]JMeterCallBack taskFinishedCallBack---->");
						call.taskFinishedCallBack();
					}
				}
			}
		}).start();
	}

}
