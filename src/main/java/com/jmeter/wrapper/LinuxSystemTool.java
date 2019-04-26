package com.jmeter.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LinuxSystemTool {

	private static final String MEM_INFO_FILE = "/proc/meminfo";
	private static final String CPU_INFO_FILE = "/proc/stat";

	public static Map<String, Double> getMemInfo() throws FileNotFoundException, IOException {
		File file = new File(MEM_INFO_FILE);
		Map<String, Double> memInfo = new HashMap<String, Double>();
		double MemTotal = 0;
		double MemFree = 0;
		double MemAvailable = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String str = null;
			while ((str = br.readLine()) != null) {
				String[] row = str.split("\\s+");
				String key = row[0].substring(0, row[0].length() - 1);
				if("MemTotal".equals(key)) {
					MemTotal = Double.valueOf(row[1]);
				}else if("MemFree".equals(key)) {
					MemFree = Double.valueOf(row[1]);
				}else if("MemAvailable".equals(key)) {
					MemAvailable = Double.valueOf(row[1]);
				}
			}
		}
		double MemFreePercent = (MemFree / MemTotal)*100;
		MemFreePercent = (double)Math.round(MemFreePercent*100)/100;
		memInfo.put("MemFreePercent", MemFreePercent);
		double MemAvailablePercent = (MemAvailable / MemTotal)*100;
		MemAvailablePercent = (double)Math.round(MemAvailablePercent*100)/100;
		memInfo.put("MemAvailablePercent", MemAvailablePercent);
		
		return memInfo;
	}

//	private static String formatSize(String kbStr) {
//		long size = Long.valueOf(kbStr);
//		NumberFormat numberFormat = NumberFormat.getInstance();
//		numberFormat.setMaximumFractionDigits(2);
//		String sizeString = "";
//		if (size <= 0) {
//			sizeString = "0M";
//		} else if (size < 1048576) {
//			sizeString = numberFormat.format((double) size / 1024) + "M";
//		} else {
//			sizeString = numberFormat.format((double) size / 1048576) + "G";
//		}
//		return sizeString;
//	}

	public static Map<String, Double> getCpuInfo() throws IOException, InterruptedException {
		File file1 = new File(CPU_INFO_FILE);
		long idle1 = 0l;
		long user1 = 0l;
		long system1 = 0l;
		long total1 = 0l;
		try (BufferedReader br = new BufferedReader(new FileReader(file1));) {
			String headLine =  br.readLine();
			String[] temp = headLine.split("\\s+");
			idle1 = Long.valueOf(temp[4]);
			user1 = Long.valueOf(temp[1]);
			system1 =  Long.valueOf(temp[3]);
			for(String s:temp) {
				if(!s.equals("cpu")) {
					total1 = total1+Long.valueOf(s);
				}
			}
		}
		Thread.sleep(1000);
		File file2 = new File(CPU_INFO_FILE);
		long idle2 = 0l;
		long user2 = 0l;
		long system2 = 0l;
		long total2 = 0l;
		try (BufferedReader br = new BufferedReader(new FileReader(file2));) {
			String headLine =  br.readLine();
			String[] temp = headLine.split("\\s+");
			idle2 = Long.valueOf(temp[4]);
			user2 = Long.valueOf(temp[1]);
			system2 =  Long.valueOf(temp[3]);
			for(String s:temp) {
				if(!s.equals("cpu")) {
					total2 = total2+Long.valueOf(s);
				}
			}
		}
		double totlal = total2-total1;
		double idlePrecent = ((double)(idle2-idle1)/totlal)*100;
		idlePrecent = (double)Math.round(idlePrecent*100)/100;
		
		double userPrecent = ((double)(user2-user1)/totlal)*100;
		userPrecent = (double)Math.round(userPrecent*100)/100;
		
		double systemPrecent =((double)(system2-system1)/totlal)*100;
		systemPrecent = (double)Math.round(systemPrecent*100)/100;
		
		Map<String,Double> cpuInfo = new HashMap<String,Double>();
		cpuInfo.put("idlePrecent", idlePrecent);
		cpuInfo.put("userPrecent", userPrecent);
		cpuInfo.put("systemPrecent", systemPrecent);
		return cpuInfo;
		
	}

}
