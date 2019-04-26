package com.jmeter.sample_sender;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;

public class SampleResultToServer {

	private static int errorTimes = 5;
	private static int normalTimes = 1;
	private final static String  receviePath= ":18081/jmeter/recevieSampleResult";
	private static Object lock = new Object();


	public static void toService(SampleResult result,String master_host) {
		if (master_host == null) {
			System.out.println("master_host is null");
			return;
		}
		if (receviePath == null) {
			System.out.println("receviePath is null");
			return;
		}
		if (result == null) {
			System.out.println("result is null");
			return;
		}
		String requestUrl = "http://" + master_host + receviePath;
		try {
			if (result instanceof HTTPSampleResult) {

				if (result.isSuccessful()) {
					if (normalTimes > 0) {
						synchronized (lock) {
							if (normalTimes > 0) {
								httpPostSerialObject(requestUrl, result);
								normalTimes--;
							}
						}
					}
				} else {
					if (errorTimes > 0) {
						synchronized (lock) {
							if (errorTimes > 0) {
								System.out.println("errorTimes:"+errorTimes);
								httpPostSerialObject(requestUrl, result);
								errorTimes--;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void clear() {
		errorTimes = 5;
		normalTimes = 1;
	}

	private static String httpPostSerialObject(String requestUrl, Object serializedObject){
		if (serializedObject == null)
			return null;
		HttpURLConnection httpUrlConn = null;
		ObjectOutputStream oos = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		StringBuilder buffer = new StringBuilder();
		try {
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpURLConnection) url.openConnection();
			// 设置content_type=SERIALIZED_OBJECT
			// 如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException
			httpUrlConn.setRequestProperty("Content-Type", "application/x-java-serialized-object");
			httpUrlConn.setConnectTimeout(5000);
			httpUrlConn.setReadTimeout(5000);
			// 设置是否向httpUrlConn输出，因为是post请求，参数要放在http正文内，因此需要设为true, 默认情况下是false
			httpUrlConn.setDoOutput(true);
			// 设置是否从httpUrlConn读入，默认情况下是true
			httpUrlConn.setDoInput(true);
			// 不使用缓存
			httpUrlConn.setUseCaches(false);
			// 设置请求方式，默认是GET
			httpUrlConn.setRequestMethod("POST");
			httpUrlConn.connect();
			// httpUrlConn.connect();
			// 此处getOutputStream会隐含的进行connect，即：如同调用上面的connect()方法，
			// 所以在开发中不调用上述的connect()也可以，不过建议最好显式调用
			// write object(impl Serializable) using ObjectOutputStream
			oos = new ObjectOutputStream(httpUrlConn.getOutputStream());
			oos.writeObject(serializedObject);
			oos.flush();
			// outputStream不是一个网络流，充其量是个字符串流，往里面写入的东西不会立即发送到网络，
			// 而是存在于内存缓冲区中，待outputStream流关闭时，根据输入的内容生成http正文。所以这里的close是必须的
			oos.close();
			inputStream = httpUrlConn.getInputStream();// 注意，实际发送请求的代码段就在这里
			inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
		} catch (Exception e) {
			e.printStackTrace();;
		} finally {
			try {
				if (oos != null)
					oos.close();
				if (bufferedReader != null)
					bufferedReader.close();
				if (httpUrlConn != null)
					httpUrlConn.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}

}
