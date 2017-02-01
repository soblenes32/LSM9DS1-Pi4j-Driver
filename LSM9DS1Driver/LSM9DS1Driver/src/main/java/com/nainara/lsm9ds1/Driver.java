package com.nainara.lsm9ds1;

import java.nio.ByteBuffer;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;


/***************************************************************************************
 * 
 * Driver class for the LSM9DS1 IMU chip that uses pi4j to communicate over the I2C bus.
 * For details about operation of the LSM9DS1, see the vendor datasheet at: 
 * 
 * @see https://cdn.sparkfun.com/assets/learn_tutorials/3/7/3/LSM9DS1_Datasheet.pdf
 * 
 * @author Samuel O'Blenes 01/31/2017
 * 
 ***************************************************************************************/
public class Driver {
	
	/******************************************************************
	 * Configuration variables
	 ******************************************************************/
	//This is a pi4j handle to the device itself
	private I2CDevice lsm9ds1 = null;
		
	/* (00: ±2g; 01: ±16 g; 10: ±4 g; 11: ±8 g) */
	private AccelerometerScale accelerometerScale = AccelerometerScale.SCALE_PLUS_MINUS_16G;
	
	/* (00: 245 dps; 01: 500 dps; 10: Not Available; 11: 2000 dps) */
	private int gyroscopeScale = 245;
	
	/**
	 * FS1 FS0 Full scale
	 * 00 ± 4 gauss
	 * 01 ± 8 gauss
	 * 10 ± 12 gauss
	 * 11 ± 16 gauss
	 */
	private int magnometerScale = 4;
	
	/** 
	 * The frequency at which the accelerometer and gyroscope refresh 
	 */
	private DataRate datarate = DataRate.FREQ_14_9_HZ;
	
	/**
	 *  The address of the IMU on the I2C bus. If no bridges have been soldered shut, it will be 
	 *  set to the manufacturer default at 0x6b
	 */
	private int i2cDeviceAddress = 0x6b;
	
	
	/**
	 *  Flag to indicate whether to use passthrough or use the FIFO buffer memory
	 */
	private boolean isUseFifoBuffer = false;
	
	/******************************************************************
	 * Sensor reading variables from the last polling
	 ******************************************************************/
	
	//Gyroscope
	private float gyrPitchX;
	private float gyrRollY;
	private float gyrYawZ; 

	//Accelerometer
	private float accX;
	private float accY;
	private float accZ;

	//Magnometer
	private float magX;
	private float magY;
	private float magZ;

	//Thermometer
	private float temperature;
	
	//Stringbuilder for toString method
	StringBuilder sb = new StringBuilder();
	
	/* Structures for conversion */
	private ByteBuffer bb;
	private byte[] byteArr = new byte[2];
	byte[] readbuffer = new byte[12];
	
	/***********************************************************************************************
	 * Sample usage of FIFO mode
	 ***********************************************************************************************/
	
//	public static void main(String[] args){
//		LSM9DS1Main prog = new LSM9DS1Main();
//		
//		DataRate imuFreq = DataRate.FREQ_476_HZ;
//		prog.setAccelerometerScale(AccelerometerScale.SCALE_PLUS_MINUS_2G);
//		prog.setDatarate(imuFreq);
//		prog.setUseFifoBuffer(true);
//		prog.initialize();
//		long start, duration = 0, iterations = 10;
//
//		int sleepTimeMs = imuFreq.getSleepDuration();
//		int availReads = 0;
//		
//		try{
//			System.out.print("Waking up " + iterations + " times at " + sleepTimeMs + " ms intervals. Hoping to have 31 readings ready each time.");
//			for(int interval=0; interval < iterations; interval++){
//				availReads = prog.pollAvailableReads();
//				System.out.println("Waking up! Available reads before polling: " + availReads);
//				start = System.nanoTime();
//				for(int reads=availReads; reads > 0; reads--){
//					prog.pollIMU();
//					//Do something with the data here
//					//System.out.print(prog.toString());
//				}
//				duration += System.nanoTime() - start;
//				prog.resetFifoBuffer();
//				System.out.println("Going back to sleep for " + sleepTimeMs + " ms. Good night.");
//				Thread.sleep(sleepTimeMs);
//			}
//			System.out.print("Total time spent in I/O: " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) +" ms");
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}

	/***********************************************************************************************
	 * Sample usage of passthrough mode
	 ***********************************************************************************************/
	
//	public static void main(String[] args){
//		LSM9DS1Main prog = new LSM9DS1Main();
//		prog.setAccelerometerScale(AccelerometerScale.SCALE_PLUS_MINUS_2G);
//		prog.setDatarate(DataRate.FREQ_14_9_HZ);
//		prog.setUseFifoBuffer(false);
//		prog.initialize();
//		long start, duration = 0, iterations = 10;
//		try{
//			System.out.print("Waking " + iterations + " times at 1 second intervals");
//			for(int interval=0; interval < iterations; interval++){
//				start = System.nanoTime();
//				prog.pollIMU();
//				//Do something with the data here
//				duration += System.nanoTime() - start;
//				System.out.print(prog.toString());
//				Thread.sleep(1000);
//			}
//			System.out.print("Finished polling. Mean poll time: " + TimeUnit.MILLISECONDS.convert((duration / iterations), TimeUnit.NANOSECONDS) +" ms");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	public void initialize(){
		I2CBus bus;
		try {
			//Get a handle to the IMU
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			lsm9ds1 = bus.getDevice(i2cDeviceAddress);
			//Power-on the gyroscope and accelerometer
			byte ctrlReg1G = (byte) Integer.parseInt(datarate.getBits() + "00000", 2); 
			lsm9ds1.write(LSM9DS1Const.CTRL_REG1_G, ctrlReg1G);
			byte ctrlReg6XL = (byte) Integer.parseInt( "000" + accelerometerScale.getBits() + "000", 2);
			lsm9ds1.write(LSM9DS1Const.CTRL_REG6_XL, ctrlReg6XL);
			
			//Turn on FIFO mode if specified by user
			if(isUseFifoBuffer){
				//1) Set (7.27) CTRL_REG9 bits idx 6 to value (1). Enables FIFO memory
				byte ctrlReg9 = 0b00000010;
				lsm9ds1.write(LSM9DS1Const.CTRL_REG9, ctrlReg9);
				//2) Set (7.34) FIFO_CTRL bits idx 0-2 to value (011). Word: (11000000) - Sets the mode to FIFO overwrite
				byte fifoCtrl = -64; 
				lsm9ds1.write(LSM9DS1Const.FIFO_CTRL, fifoCtrl);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/***********************************************************************************************
	 * Fetch gyroscope and accelerometer register values
	 ***********************************************************************************************/
	public void pollIMU(){
		if(lsm9ds1 == null){
			System.err.println("Warning. Attempted to poll the IMU before it was initialized.");
			return;
		}
		try {
			lsm9ds1.read(LSM9DS1Const.OUT_X_L_G, readbuffer, 0, 12);
			gyrPitchX = toAngleDegrees(readbuffer[1], readbuffer[0]);
			gyrRollY = toAngleDegrees(readbuffer[3], readbuffer[2]);
			gyrYawZ = toAngleDegrees(readbuffer[5], readbuffer[4]); 
			accX = toGs(readbuffer[7], readbuffer[6]);
			accY = toGs(readbuffer[9], readbuffer[8]);
			accZ = toGs(readbuffer[11], readbuffer[10]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************************
	 * Fetch magnetometer register values
	 ***********************************************************************************************/
	public void pollMagnetometer(){
		if(lsm9ds1 == null){
			System.err.println("Warning. Attempted to poll the IMU before it was initialized.");
			return;
		}
		try {
			lsm9ds1.read(LSM9DS1Const.OUT_X_L_M, readbuffer, 0, 6);
			magX = toGauss(readbuffer[1], readbuffer[0]);
			magY = toGauss(readbuffer[3], readbuffer[2]);
			magZ = toGauss(readbuffer[5], readbuffer[4]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************************
	 * Fetch thermometer register values
	 ***********************************************************************************************/
	public void pollThermometer(){
		if(lsm9ds1 == null){
			System.err.println("Warning. Attempted to poll the IMU before it was initialized.");
			return;
		}
		try {
			lsm9ds1.read(LSM9DS1Const.OUT_TEMP_L, readbuffer, 0, 2);
			temperature = normalizeTemperature(readbuffer[1], readbuffer[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************************
	 * Fetch the number of accumulated unread values in chip FIFO buffer. 
	 * Only applicable if the chip has been initialized in FIFO mode.
	 ***********************************************************************************************/
	public int pollAvailableReads(){
		if(lsm9ds1 == null || !isUseFifoBuffer()){
			System.err.println("Warning. Attempted to poll the IMU before it was initialized or failed to set FIFO buffer flag.");
			return -1;
		}
		try {
			lsm9ds1.read(LSM9DS1Const.FIFO_SRC, readbuffer, 0, 1);
			//Reads are bits position 2-7, so reset the msd 
			readbuffer[0] &= ~(1 << 6);
			readbuffer[0] &= ~(1 << 7);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readbuffer[0];
	}
	
	/***********************************************************************************************
	 * Clears out the FIFO buffer by setting it to passthrough and back.
	 * Only applicable if the chip has been initialized in FIFO mode.
	 ***********************************************************************************************/
	public void resetFifoBuffer(){
		if(lsm9ds1 == null || !isUseFifoBuffer()){
			System.err.println("Warning. Attempted to poll the IMU before it was initialized or failed to set FIFO buffer flag.");
			return;
		}
		try {
			lsm9ds1.write(LSM9DS1Const.FIFO_CTRL, (byte) 0b00000000);
			lsm9ds1.write(LSM9DS1Const.FIFO_CTRL, (byte) -64);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/***********************************************************************************************************
	 * Methods to convert analog two's complement values to meaningful units
	 ***********************************************************************************************************/
	
	private float toGs(byte hb, byte lb){
		return normalizeToUnits(hb, lb, accelerometerScale.getScale());
	}
	private float toAngleDegrees(byte hb, byte lb){
		return normalizeToUnits(hb, lb, gyroscopeScale);
	}
	private float toGauss(byte hb, byte lb){
		return normalizeToUnits(hb, lb, magnometerScale);
	}
	private float normalizeToUnits(byte hb, byte lb, int scale) {
		byteArr[0] = hb;
		byteArr[1] = lb;
	    bb = ByteBuffer.wrap(byteArr);
	    return ((float) bb.getShort() / Short.MAX_VALUE) * scale;
	}
	//Temp scale is static -40 to +80c. Typical operating value is +25c
	private float normalizeTemperature(byte hb, byte lb) {
		byteArr[0] = hb;
		byteArr[1] = lb;
	    bb = ByteBuffer.wrap(byteArr);
	    return ((float) bb.getShort() / Short.MAX_VALUE) * (120 / 2) + 20;
	}
	
	/***********************************************************************************************************
	 * Helper methods
	 ***********************************************************************************************************/
	
	@Override
	public String toString() {
		sb.setLength(0);
		sb.append("Magnetometer: x, y, z (gauss) [")
			.append(magX).append(", ")
			.append(magY).append(", ")
			.append(magZ).append("]").append(System.getProperty("line.separator"));
		sb.append("Accelerometer: x, y, z (g) [")
			.append(accX).append(", ")
			.append(accY).append(", ")
			.append(accZ).append("]").append(System.getProperty("line.separator"));
		sb.append("Gyroscope: pitch, roll, yaw (degrees) [")
			.append(gyrPitchX).append(", ")
			.append(gyrRollY).append(", ")
			.append(gyrYawZ).append("]").append(System.getProperty("line.separator"));
		sb.append("Temperature: (degrees C) [")
			.append(temperature).append("]").append(System.getProperty("line.separator"));
		return sb.toString();
	}
	
	public enum DataRate{
		FREQ_14_9_HZ("001", 14.9f),
		FREQ_59_5_HZ("010", 59.5f),
		FREQ_119_HZ("011", 119f),
		FREQ_238_HZ("100", 238f),
		FREQ_476_HZ("101", 476f);
		
		private String bits;
		private float hz;
		DataRate(String bits, float hz) {
			this.bits = bits; 
			this.hz = hz; 
		}
	    public String getBits() {
	    	return bits; 
	    }
	    public float getHz() {
			return hz;
		}
		/**************************************************************************************
	     * @return number of milliseconds to sleep until the buffer is almost full. Note that
	     * the most jvms have a minimum resolution of ~13ms. The LSM9DS1 has memory with 32
	     * slots, so we want to wait until they're almost all full, but we dont want to drop
	     * any data.
	     **************************************************************************************/
	    public int getSleepDuration(){
	    	return Math.round(30 / hz * 1000);
	    }
	}
	
	/* (00: ±2g; 01: ±16 g; 10: ±4 g; 11: ±8 g) */
	public enum AccelerometerScale{
		SCALE_PLUS_MINUS_2G("00", 2),
		SCALE_PLUS_MINUS_16G("01", 16),
		SCALE_PLUS_MINUS_4G("10", 4),
		SCALE_PLUS_MINUS_8G("11", 8);
		
		private String bits;
		private int scale;
		AccelerometerScale(String bits, int scale) {
			this.bits = bits; 
			this.scale = scale; 
		}
	    public String getBits() {
	    	return bits; 
	    }
	    public int getScale(){
	    	return scale;
	    }
	}

	public I2CDevice getLsm9ds1() {
		return lsm9ds1;
	}

	public void setLsm9ds1(I2CDevice lsm9ds1) {
		this.lsm9ds1 = lsm9ds1;
	}

	public DataRate getDatarate() {
		return datarate;
	}

	public void setDatarate(DataRate datarate) {
		this.datarate = datarate;
	}

	public int getI2cDeviceAddress() {
		return i2cDeviceAddress;
	}

	public void setI2cDeviceAddress(int i2cDeviceAddress) {
		this.i2cDeviceAddress = i2cDeviceAddress;
	}

	public float getGyrPitchX() {
		return gyrPitchX;
	}

	public float getGyrRollY() {
		return gyrRollY;
	}

	public float getGyrYawZ() {
		return gyrYawZ;
	}

	public float getAccX() {
		return accX;
	}

	public float getAccY() {
		return accY;
	}

	public float getAccZ() {
		return accZ;
	}

	public float getMagX() {
		return magX;
	}

	public float getMagY() {
		return magY;
	}

	public float getMagZ() {
		return magZ;
	}

	public float getTemperature() {
		return temperature;
	}

	public boolean isUseFifoBuffer() {
		return isUseFifoBuffer;
	}

	public void setUseFifoBuffer(boolean isUseFifoBuffer) {
		this.isUseFifoBuffer = isUseFifoBuffer;
	}

	public AccelerometerScale getAccelerometerScale() {
		return accelerometerScale;
	}

	public void setAccelerometerScale(AccelerometerScale accelerometerScale) {
		this.accelerometerScale = accelerometerScale;
	}

}
