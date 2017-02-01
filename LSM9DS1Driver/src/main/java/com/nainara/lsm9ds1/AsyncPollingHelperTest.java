package com.nainara.lsm9ds1;

import org.apache.commons.collections.Buffer;

public class AsyncPollingHelperTest {

	public static void main(String[] args) {
		Driver driver = new Driver();
		Driver.DataRate imuFreq = Driver.DataRate.FREQ_476_HZ;
		driver.setAccelerometerScale(Driver.AccelerometerScale.SCALE_PLUS_MINUS_2G);
		driver.setDatarate(imuFreq);
		driver.setUseFifoBuffer(true);
		
		AsyncPollingHelper helper = new AsyncPollingHelper(driver);
		
		try {
			helper.beginPolling();
			Thread.sleep(2500);
			System.out.println("***Fetching data first 2.5 seconds***");
			Buffer buffer = helper.getFifo();
			while(!buffer.isEmpty()){
				System.out.println(buffer.remove().toString());
			}
			
			Thread.sleep(2500);
			System.out.println("***Fetching data second 2.5 seconds***");
			while(!buffer.isEmpty()){
				System.out.println(buffer.remove().toString());
			}
			
			helper.endPolling();
			System.out.println("***Fetching final data***");
			while(!buffer.isEmpty()){
				System.out.println(buffer.remove().toString());
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
