package com.jmeter.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class CSVLogReader {

	private BufferedReader br;
	private int fieldsNum;
	private long readedNum;
	private int lineNum;
	private final int newline;//换行符长度
	

	public CSVLogReader(File file) throws IOException {
		if(JMeterConfig.isWindows) newline = 2;
		else newline = 1;
		br = new BufferedReader(new FileReader(file));
		String headLine = br.readLine();
		if (null != headLine) {
			String[] fields = headLine.split(",");
			this.fieldsNum = fields.length;
		}

	}

	public CSVLogReader(File file, Long readedNum) throws IOException {
		if(JMeterConfig.isWindows) newline = 2;
		else newline = 1;
		br = new BufferedReader(new FileReader(file));
		String headLine = br.readLine();
		if (null != headLine) {
			String[] fields = headLine.split(",");
			this.fieldsNum = fields.length;
		}
		this.readedNum = readedNum;
		this.seek(readedNum);
	}
	
	public void seek(long n) throws IOException {
		if(null != br) {
			this.readedNum = n;
			this.br.skip(n);
		}
	}

	public String[] readCSVLine() {
		if (null == br)
			return null;
		String s[] = null;
		try {
			String line = br.readLine();
			if (null != line) {
				s = line.split(",");
				if (this.fieldsNum != s.length) {
					s = null;
				} else {
					lineNum++;
					//linux +1; windons+2
					readedNum = readedNum + line.length() + newline;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			s = null;
		}
		return s;
	}

	public String readCSVLineToStr() {
		if (null == br)
			return null;
		String s = null;
		try {
			String line = br.readLine();
			if (null != line && this.fieldsNum != line.split(",").length) {
				line = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			s = null;
		}
		return s;
	}

	public void close() throws IOException {
		try {
			if (this.br != null)
				this.br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally {
			try {
				this.br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw e;
			}
		}
	}

	public long getReadedNum() {
		return readedNum;
	}

	public void setReadedNum(long readedNum) {
		this.readedNum = readedNum;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

//	public static void main(String[] args) throws IOException {
//		
//		BufferedReader br = new BufferedReader(new FileReader("/Users/tao.he2/Desktop/engine/JMeter/Report/task1/2019-01-04.18.18.16/log.jtl"));
//		//String str = br.readLine();
//		//long l = str.length();
//		br.skip(163);
//		System.out.println(br.readLine());
//	}
}
