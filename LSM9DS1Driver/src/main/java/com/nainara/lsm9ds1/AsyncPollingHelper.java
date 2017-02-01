package com.nainara.lsm9ds1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

/*************************************************************************************************
 * 
 * PollingHelper spawns a new thread that pulls IMU gyro and accelerometer data down at regular 
 * intervals. Polled data is stored for consumption by other processes in a circular fifo buffer. 
 * 
 * This class is designed to minimize processing overhead by waiting until the device buffer is 
 * almost full, and then polling all available data at once using the burst cache. However, the 
 * tradeoff is that the IMU data in the FIFO buffer is not as "fresh" as it would be using a 
 * continuous polling method.
 * 
 * The frequency that this class pulls down IMU data from the buffer depends on the frequency set
 * in the IMU. For example, at the lowest frequency (~15hz), the IMU on-board buffer can hold about 
 * 2 seconds worth of data. At highest frequency (476hz), the IMU can hold about 61ms of polling 
 * data.
 * 
 * Sample usage:
 * 
 * Driver driver = new Driver();
 * AsyncPollingHelper helper = new AsyncPollingHelper(driver);
 * helper.beginPolling();
 * Thread.sleep(1000);
 * CircularFifoBuffer buffer = helper.getFifo();
 * helper.endPolling();
 * 
 *************************************************************************************************/
public class AsyncPollingHelper {
	
	//Buffer where polling results are stored
	private Buffer fifo = null;
	
	//Thread executor for starting & stopping the async polling 
	protected ExecutorService executor = Executors.newSingleThreadExecutor();
	
	//Flag to signal when the thread should finish up
	protected volatile boolean isContinuePolling = false;
	
	//Handle to the LMS9DS1 Driver instance
	protected Driver driver = null;	
	
	/***********************************************************************************************
	 * @param driver Handle to the driver class that will do the polling
	 ***********************************************************************************************/
	public AsyncPollingHelper(Driver driver) {
		this.driver = driver;
		
		//Default the max buffer size to 10 seconds worth of imu data
		int maxSize = Math.round(driver.getDatarate().getHz() * 10);
		fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxSize));
	}
	
	/****************************************************************************************
	 * Signals the helper to start polling the IMU for data. It will continue until the
	 * endPolling() method is called.
	 ****************************************************************************************/
	public void beginPolling(){
		Driver.DataRate imuFreq = driver.getDatarate();
		//The driver must be setup to use the fifo buffer
		driver.setUseFifoBuffer(true);
		driver.initialize();
		isContinuePolling = true;
		int sleepTimeMs = imuFreq.getSleepDuration();
		
		executor.submit(() -> {
			int availReads = 0;
			try{
				while(isContinuePolling){
					availReads = driver.pollAvailableReads();
					for(int reads=availReads; reads > 0; reads--){
						driver.pollIMU();
						Polling polling = new Polling();
						polling.pollDriver(driver);
						fifo.add(polling);
					}
					driver.resetFifoBuffer();
					Thread.sleep(sleepTimeMs);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		});
	}
	
	/****************************************************************************************
	 * Signals the helper to stop polling the IMU for data and terminates the thread
	 ****************************************************************************************/
	public void endPolling(){
		isContinuePolling = false;
		executor.shutdown();
	    try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Buffer getFifo() {
		return fifo;
	}
	
	/************************************************************************************************
	 * @param size sets the maximum size of the circular fifo buffer
	 ************************************************************************************************/
	public void setBufferSize(int size){
		fifo = (CircularFifoBuffer) BufferUtils.synchronizedBuffer(new CircularFifoBuffer(size));
	}
}
