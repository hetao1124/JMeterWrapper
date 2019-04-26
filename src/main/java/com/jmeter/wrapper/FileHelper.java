package com.jmeter.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

	public static String readFile2String(File file) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
			String s = null;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				result.append(s + "\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString().trim();
	}

	public static List<String> readFile2List(File file) {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
			String s = null;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				list.add(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void makeDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			File pFile = file.getParentFile();
			if (pFile.exists()) {
				file.mkdir();
			} else {
				makeDir(pFile.getAbsolutePath());
			}
		}
	}

	public static int getFileLineNum(File file) {
		LineNumberReader lineNumberReader = null;
		try {
			lineNumberReader = new LineNumberReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 注意加1，实际上是读取换行符，所以需要+1
		try {
			lineNumberReader.skip(Long.MAX_VALUE);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int lineNumber = lineNumberReader.getLineNumber() + 1;
		try {
			lineNumberReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lineNumber;
	}

	public static void clearFile(File file) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);

			fileWriter.write("");

			fileWriter.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public static boolean clearDir(String path){
		File file = new File(path);
		if(!file.exists()){//判断是否待删除目录是否存在
			return false;
		}
		
		String[] content = file.list();//取得当前目录下所有文件和文件夹
		for(String name : content){
			File temp = new File(path, name);
			if(temp.isDirectory()){//判断是否是目录
				clearDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
				temp.delete();//删除空目录
			}else{
				if(!temp.delete()){//直接删除文件
					System.out.println("Failed to delete " + name);
				}
			}
		}
		return true;
	}


}
