package com.jmeter.sample_sender;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.rmi.RemoteException;
import org.apache.jmeter.samplers.AbstractSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MySampleSender extends AbstractSampleSender implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6751890357225531345L;

	/** empty array which can be returned instead of null */
	private static final byte[] EMPTY_BA = new byte[0];

	private static final Logger log = LoggerFactory.getLogger(MySampleSender.class);

	private final RemoteSampleListener listener;
	private final SampleSender decoratedSender;
	// Configuration items, set up by readResolve
	private transient volatile boolean stripAlsoOnError = true;
	
	private String master_host;

	/**
	 * @deprecated only for use by test code
	 */
	@Deprecated
	public MySampleSender() {
		log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
		listener = null;
		decoratedSender = null;
	}

	public MySampleSender(RemoteSampleListener listener) throws Exception {
		SampleSender decorate = null;
		try {
			Class<?> clazz = Class.forName("org.apache.jmeter.samplers.BatchSampleSender");
			Constructor<?> cons = clazz.getDeclaredConstructor(RemoteSampleListener.class);
			cons.setAccessible(true);
			decorate = (SampleSender) cons.newInstance(new Object[] { listener });
		} catch (Exception e) {
			throw e;
		}
		this.decoratedSender = decorate;
		this.listener = null;
		//
		InetAddress localHost=InetAddress.getLocalHost();
		master_host = localHost.getHostAddress();
		//
		log.info("Using MySampleSender for this run");		
	}

	@Override
	public void testEnded(String host) {
		log.info("Test Ended on {}", host);
		//
		SampleResultToServer.clear();
		//
		if (decoratedSender != null) {
			decoratedSender.testEnded(host);
		}
	}

	@Override
	public void sampleOccurred(SampleEvent event) {
		// Strip the response data before writing, but only for a successful request.
		SampleResult result = event.getResult();
		//
		SampleResultToServer.toService(result,master_host);
		//
		if (stripAlsoOnError || result.isSuccessful()) {
			// Compute bytes before stripping
			stripResponse(result);
			// see Bug 57449
			for (SampleResult subResult : result.getSubResults()) {
				stripResponse(subResult);
			}
		}
		if (decoratedSender == null) {
			try {
				listener.sampleOccurred(event);
			} catch (RemoteException e) {
				log.error("Error sending sample result over network", e);
			}
		} else {
			decoratedSender.sampleOccurred(event);
		}
	}

	/**
	 * Strip response but fill in bytes field.
	 * 
	 * @param result
	 *            {@link SampleResult}
	 */
	private void stripResponse(SampleResult result) {
		result.setBytes(result.getBytesAsLong());
		result.setResponseData(EMPTY_BA);
	}

	/**
	 * Processed by the RMI server code; acts as testStarted().
	 *
	 * @return this
	 * @throws ObjectStreamException
	 *             never
	 */
	private Object readResolve() throws ObjectStreamException {
		log.info("Using MySampleSender for this run with stripAlsoOnError: {}", stripAlsoOnError);
		return this;
	}
}
