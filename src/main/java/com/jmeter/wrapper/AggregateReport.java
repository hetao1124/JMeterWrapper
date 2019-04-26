package com.jmeter.wrapper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.SamplingStatCalculator;

public class AggregateReport {

	private final Map<String, SamplingStatCalculator> tableRows = new HashMap<>();
	private final String TOTAL_ROW_LABEL = "总体";

	private long startPoint;

	private boolean empty = true;

	public AggregateReport() {
		tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
	}
	
	public int size() {
		return tableRows.size();
	}
	
	public Set<String> keySet() {
		return tableRows.keySet();
	}
	
	public String[] get(String key) {
		SamplingStatCalculator s =tableRows.get(key);
		String[] aggregate_data = new String[] { s.getLabel(),  String.valueOf(s.getCount()),
				String.valueOf(s.getErrorPercentage()), String.valueOf(s.getMeanAsNumber()), 
				String.valueOf(s.getPercentPoint(0.9)),String.valueOf(s.getPercentPoint(0.95)), String.valueOf(s.getPercentPoint(0.99)),
				String.valueOf(s.getMin()),String.valueOf(s.getMax()), String.valueOf(s.getMedian()), 
				String.valueOf(s.getRate()),String.valueOf(s.getKBPerSecond()),String.valueOf(s.getSentKBPerSecond())};
		//label,samples,error,average,line90,line95,line99,min,max,median,throughput,received,sent
		return aggregate_data;
	}
	
	public void clear() {
		tableRows.clear();
		startPoint = 0l;
		empty = true;
	}

	public void makeAggregateReport(String filename, long startPoint) throws Exception {
		CSVLogReader dataReader = null;
		try {
			dataReader = new CSVLogReader(new File(filename), startPoint);
			String[] parts;
			while ((parts = dataReader.readCSVLine()) != null) {
				SampleEvent event = makeResultFromDelimitedString(parts);
				if (event != null) {
					final SampleResult result = event.getResult();
					SamplingStatCalculator row = tableRows.computeIfAbsent(result.getSampleLabel(), label -> {
						SamplingStatCalculator newRow = new SamplingStatCalculator(label);
						return newRow;
					});
					row.addSample(result);
					tableRows.get(TOTAL_ROW_LABEL).addSample(result);
					if (empty)
						empty = false;
				}
			}
			this.startPoint = dataReader.getReadedNum();
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != dataReader)
				dataReader.close();
		}
	}

	public boolean isEmpty() {
		return empty;
	}

	public long getPoint() {
		return startPoint;
	}

	private SampleEvent makeResultFromDelimitedString(final String[] parts) throws Exception {

		SampleResult result = null;
		String hostname = "";// $NON-NLS-1$
		long timeStamp = 0;
		long elapsed = 0;
		String text = null;
		//String field = null; // Save the name for error reporting
		int i = 0;
		try {
			if (true) {
				//field = CSVSaveService.TIME_STAMP;
				text = parts[i++];
				Date stamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(text);
				timeStamp = stamp.getTime();
			}

			if (true) {
				//field = CSVSaveService.CSV_ELAPSED;
				text = parts[i++];
				elapsed = Long.parseLong(text);
			}

			if (true) {
				result = new SampleResult(timeStamp, elapsed);
			}

			if (true) {
				//field = CSVSaveService.LABEL;
				text = parts[i++];
				result.setSampleLabel(text);
			}
			if (true) {
				//field = CSVSaveService.RESPONSE_CODE;
				text = parts[i++];
				result.setResponseCode(text);
			}
			if (true) {
				//field = CSVSaveService.RESPONSE_CODE;
				text = parts[i++];
				result.setResponseMessage(text);
			}
			if (true) {
				//field = CSVSaveService.THREAD_NAME;
				text = parts[i++];
				result.setThreadName(text);
			}

			if (true) {
				//field = CSVSaveService.DATA_TYPE;
				text = parts[i++];
				result.setDataType(text);
			}

			if (true) {
				//field = CSVSaveService.SUCCESSFUL;
				text = parts[i++];
				result.setSuccessful(Boolean.valueOf(text).booleanValue());
			}

			if (true) {
				//field = CSVSaveService.CSV_BYTES;
				text = parts[i++];
				result.setBytes(Long.parseLong(text));
			}

			if (true) {
				//field = CSVSaveService.CSV_SENT_BYTES;
				text = parts[i++];
				result.setSentBytes(Long.parseLong(text));
			}

			if (true) {
				//field = CSVSaveService.CSV_THREAD_COUNT1;
				text = parts[i++];
				result.setGroupThreads(Integer.parseInt(text));

				//field = CSVSaveService.CSV_THREAD_COUNT2;
				text = parts[i++];
				result.setAllThreads(Integer.parseInt(text));
			}

			if (true) {
				//field = CSVSaveService.CSV_LATENCY;
				text = parts[i++];
				result.setLatency(Long.parseLong(text));
			}

			if (true) {
				//field = CSVSaveService.CSV_IDLETIME;
				text = parts[i++];
				result.setIdleTime(Long.parseLong(text));
			}
			if (true) {
				//field = CSVSaveService.CSV_CONNECT_TIME;
				text = parts[i++];
				result.setConnectTime(Long.parseLong(text));
			}

		} catch (NumberFormatException | ParseException e) {
			throw new Exception(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new Exception(e);
		}
		return new SampleEvent(result, "", hostname);
	}
}
