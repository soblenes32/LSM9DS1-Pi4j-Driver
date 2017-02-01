package com.nainara.lsm9ds1;

public class Polling {
	//Gyroscope
	private float gyrPitchX;
	private float gyrRollY;
	private float gyrYawZ; 

	//Accelerometer
	private float accX;
	private float accY;
	private float accZ;
	
	public void pollDriver(Driver driver){
		this.gyrPitchX = driver.getGyrPitchX();
		this.gyrRollY = driver.getGyrRollY();
		this.gyrYawZ = driver.getGyrYawZ();
		this.accX = driver.getAccX();
		this.accY = driver.getAccY();
		this.accZ = driver.getAccZ();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Accelerometer: x, y, z (g) [")
			.append(accX).append(", ")
			.append(accY).append(", ")
			.append(accZ).append("]").append(System.getProperty("line.separator"));
		sb.append("Gyroscope: pitch, roll, yaw (degrees) [")
			.append(gyrPitchX).append(", ")
			.append(gyrRollY).append(", ")
			.append(gyrYawZ).append("]").append(System.getProperty("line.separator"));
		return sb.toString();
	}
	
	public float getGyrPitchX() {
		return gyrPitchX;
	}
	public void setGyrPitchX(float gyrPitchX) {
		this.gyrPitchX = gyrPitchX;
	}
	public float getGyrRollY() {
		return gyrRollY;
	}
	public void setGyrRollY(float gyrRollY) {
		this.gyrRollY = gyrRollY;
	}
	public float getGyrYawZ() {
		return gyrYawZ;
	}
	public void setGyrYawZ(float gyrYawZ) {
		this.gyrYawZ = gyrYawZ;
	}
	public float getAccX() {
		return accX;
	}
	public void setAccX(float accX) {
		this.accX = accX;
	}
	public float getAccY() {
		return accY;
	}
	public void setAccY(float accY) {
		this.accY = accY;
	}
	public float getAccZ() {
		return accZ;
	}
	public void setAccZ(float accZ) {
		this.accZ = accZ;
	}
}
