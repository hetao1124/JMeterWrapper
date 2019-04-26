package com.jmeter.test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jmeter.wrapper.AggregateReport;
import com.jmeter.wrapper.JMeterCallBack;

public class JMeterCallBackImpl implements JMeterCallBack {

	public static boolean isNormalStop = true;
	public static boolean isStarted;
	private final long taskId;
	private final String reportDir;
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String localIp = "master_ip";

	public JMeterCallBackImpl(long taskId, String reportDir) {
		this.taskId = taskId;
		this.reportDir = reportDir;
		try {
			this.localIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void taskFinishedCallBack() {
		// TODO Auto-generated method stub
		System.out.println("[JMeterCallBackImpl]----->taskFinishedCallBack");
		try {
			if (isNormalStop) {
				System.out.println("[JMeterCallBackImpl]----->isNormalStop:" + isNormalStop);
				String url = "http://localhost:18081/load/finishLoadTest?taskId=" + taskId;
				// Unirest.post(url).asString();
			} else {
				System.out.println("[JMeterCallBackImpl]----->isNormalStop:" + isNormalStop);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			isNormalStop = true;
			isStarted = false;
		}
	}

	@Override
	public void taskStartedCallBack() {
		// TODO Auto-generated method stub
		isStarted = true;
		System.out.println("[JMeterCallBackImpl] start makeATPReport:");
		this.makeATPReport();
	}

	private void makeATPReport() {
		final String filename = reportDir + "/log.jtl";
		new Thread(new Runnable() {
			long point = 0l;
			int timeout = 0;
			final String starttime = df.format(new Date());
			AggregateReport aggregateReport = new AggregateReport();
			Map<String,TPSResult> tpsMap = new HashMap<String,TPSResult>();
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					startMakeReport(filename);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					aggregateReport.clear();
					aggregateReport = null;
					tpsMap.clear();
					tpsMap = null;
					System.out.println("[JMeterCallBackImpl] close AggregateReport ");
				}
			}

			public void startMakeReport(String filename) throws Exception {
				File file = new File(filename);
				if (file.exists()) {
					while (isStarted) {
						System.out.println("[JMeterCallBackImpl] point is:" + point);
						aggregateReport.makeAggregateReport(filename, point);
						point = aggregateReport.getPoint();
						toService(aggregateReport);
						// wait 1s
						Thread.sleep(1000);
					}
				} else {
					System.out.println("[JMeterCallBackImpl] finding file:" + filename);
					Thread.sleep(1000);
					timeout++;
					if (timeout > 5) {
						System.out.println("[JMeterCallBackImpl] get file timeout(3s),file:" + filename);
						return;
					}
					startMakeReport(filename);
				}
			}

			private void toService(AggregateReport aggregateReport) {
				//System.out.println("[JMeterCallBackImpl]aggregateReport data:" + aggregateReport.toString());
				if (aggregateReport.isEmpty())
					return;
				try {
					JSONObject jsonResult = new JSONObject();
					jsonResult.put("masterip", localIp);
					jsonResult.put("starttime", starttime);
					jsonResult.put("endtime", df.format(new Date()));

					JSONArray aggregate = new JSONArray();
					for(String label:aggregateReport.keySet()) {
						String[] aggregate_sub = aggregateReport.get(label);
						aggregate.add(aggregate_sub);
						TPSResult tpsRs = tpsMap.computeIfAbsent(label,  labelName -> {
							return new TPSResult(labelName);
						});
						double tps = Double.valueOf(aggregate_sub[10]);//tps
						tpsRs.data.add(tps);//tps
						if(tps>tpsRs.maxTps) tpsRs.maxTps = tps;
					}
					jsonResult.put("aggregate", aggregate);

					JSONArray tps = new JSONArray();	
					for(String label :tpsMap.keySet()) {
						JSONObject tps_sub = new JSONObject();					
						List<Double> list = tpsMap.get(label).data;
						int size = list.size();
						Double[][] data= new Double[size][2];
						for (int i = 0; i < size; i++) {
							data[i][0] = (double) (i + 1);
							data[i][1] = (double) (Math.round(list.get(i) * 100) / 100.0);// 四舍五入
						}
						tps_sub.put("data", data);
						tps_sub.put("label", tpsMap.get(label).label);
						tps_sub.put("max", (double) (Math.round((tpsMap.get(label).maxTps) * 100) / 100.0));
						tps.add(tps_sub);
					}
					jsonResult.put("tps", tps);

					String rs = jsonResult.toJSONString();
					jsonResult.clear();
					rs = java.net.URLEncoder.encode(rs, "utf-8");
					String url = "http://localhost:18081/load/saveReport?jsonResult=" + rs + "&taskId=" + taskId;
					rs = null;
					//Unirest.post(url).asString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
			class TPSResult{
				List<Double> data;//tps
				String label;
				double maxTps;
				public TPSResult(String label) {
					this.label = label;
					this.data = new ArrayList<Double>();
				}
			}

		}).start();
	}

}
