# LSM9DS1 Pi4j Driver

This package contains a java driver to poll the Sparkfun LSM9DS1 IMU breakout board. The LSM9DS1 is an 9-DOF IMU chipset that is available on a breakout board manufactured by Sparkfun. This project is a driver to be used in conjunction with the pi4j framework, and is designed to provide an efficient higher-level interface to read raw and interpreted IMU polling data from the chip via the I2C bus.

## Getting Started

As this driver is relatively lightweight with few dependencies, it may be included in your project in one of two ways:
* Directly copy the source java files into your project
* Execute the gradle build script via the command line and include the compiled jars at build/lib/*.jar into your project dependencies 

### Prerequisites

The LSM9DS1 Pi4j Driver has a dependency on the Pi4j core libraries and apache commons collections. These libraries must be included in your project classpath.

If you are including the project java source files directly and using your own gradle build script, the following dependencies should be present:

```
compile group: 'commons-collections', name: 'commons-collections', version: '3.2.2'
compile group: 'com.pi4j', name: 'pi4j-core', version: '1.1'
```

### Installation

#### Step 1: Physical device hookup

Sparkfun has an excellent overview of the physical hookup of the breakout board which is located on their [website](https://learn.sparkfun.com/tutorials/lsm9ds1-breakout-hookup-guide). The most basic configuration requires attachment of 4 contacts to the board with the Raspberry PI's 3v, ground, I2C SDA (data), and I2C SDL (clock). On the Raspberry PI 3 model B, The SDA and SDL pins are located at GPIO #8, and 9 respectively using the [Pi4j numbering scheme](http://pi4j.com/pins/model-3b-rev1.html).

#### Step 2: Raspbian I2C setup

After the physical wiring of the breakout board has been completed, follow the instructions in the [Sparkfun I2C guide](https://learn.sparkfun.com/tutorials/raspberry-pi-spi-and-i2c-tutorial#i2c-on-pi) to enable I2C bus using the raspi-config interface, and test bus connectivity using the i2c-tools utility. 

#### Step 3: Deployment & validation

Compile the project using the gradle build wrapper.

```
gradlew build
```

Copy the resultant artifacts from the "build/lib/*.jar" directory into a workspace on your raspberry pi. Place the dependencies into a subdirectory like "lib". From the deployment directory, execute the AsyncPollingHelperTest main class to conduct an end-to-end test.

```
sudo java -cp "LSM9DS1-0.1.0.jar:lib/*" com.nainara.lsm9ds1.AsyncPollingHelperTest > out.txt
```

Access the stdout text file "out.txt" to validate that the IMU is polling accelerometer and gyroscope data.

Note that the validation program uses the default I2C bus address used by the vendor. If you have soldered any of the address jumpers shut on the bottom of the breakout board, you will need to modify and recompile the test program with the appropriate address.

### Configuration & API usage

This driver is designed to support two of the IMU's four operating modes: pass-through, and FIFO queue. Each operating mode has its own advantages and drawbacks as detailed below. 

#### Pass-through mode

At the device-level, pass-through mode simply overwrites sensor data on a single set of registers. Accessing the IMU in pass-through mode will produce the most current polling data. However, as the thread timing accuracy of most JVM implementations (and their underlying operating systems) are only accurate to a granularity of ~9-13 ms, a typical java thread will be unable to keep up with the LSM9DS1's higher two frequency settings, and polling data will be "dropped" in an unpredictable way. Pass-through mode is best used for applications that employ a low refresh frequency and only require infrequent sensor data pollings. If used to poll at high frequencies, the java thread may thrash and add non-trivial overhead to the Raspberry Pi's computational load.

#### FIFO buffer mode

Engaging the FIFO buffer allows the device to accumulate up to 32 sensor readings in on-board memory before the queue begins to overwrite itself. The AsyncPollingHelper class is designed to wait for the LSM9DS1's buffer to fill before exporting its data, reducing the program's computational and I/O overhead. However, this means that the program will receive telemetry data that is aged up to 32x the IMU's clock frequency. For example, at the lowest frequency (~15hz), the IMU on-board buffer can hold about 2 seconds worth of data. At highest frequency (476hz), the IMU can hold about 61ms of polling data. The first reading in the buffer will be proportinately aged.

When fifo buffer mode is engaged, multiple read calls must be executed to pull down consecutive entries. The size of the active buffer may be read via the "pollAvailableReads" method. After reading is complete, the buffer may be emptied via the "resetFifoBuffer" method.

#### Driver Configuration

Driver initialization
```
Driver driver = new Driver();
//...
//Configure the driver
//...
driver.initialize();
```

The datarate determines how often the device will write data from its sensors to the accessible registers. Available frequencies are [14.9hz, 59.5hz, 119hz, 238hz, 476hz]. Higher frequencies consume more power.
```
//Set the datarate to 14.9 sensor readings-per-second
driver.setDatarate(Driver.DataRate.FREQ_14_9_HZ);
```

The accelerometer scale determines the range of forces that the accelerometer can read, and the level of precision that it reports. Plus or minus 2G results in the highest precision readings, while 16G produces lower precision readings.
```
//Set the range of the accelerometer from -2G to +2G
driver.setAccelerometerScale(Driver.AccelerometerScale.SCALE_PLUS_MINUS_2G);
```

The useFifoBuffer flag signals whether the device should use pass-through mode or FIFO buffer mode.
```
//Turn on the FIFO buffer
driver.setUseFifoBuffer(true);
```

The setI2cDeviceAddress method allows the user to configure the device from alternative I2C bus addresses.
```
//Set the address of the IMU to 0x6b
driver.setI2cDeviceAddress(0x6b);
```

#### Pass-through mode sample program
```
public static void main(String[] args){
	LSM9DS1Main driver = new LSM9DS1Main();
	driver.setAccelerometerScale(Driver.AccelerometerScale.SCALE_PLUS_MINUS_2G);
	driver.setDatarate(Driver.DataRate.FREQ_14_9_HZ);
	driver.setUseFifoBuffer(false);
	driver.initialize();
	long start, duration = 0, iterations = 10;
	try{
		System.out.print("Waking " + iterations + " times at 1 second intervals");
		for(int interval=0; interval < iterations; interval++){
			start = System.nanoTime();
			driver.pollIMU();
			Do something with the data here
			duration += System.nanoTime() - start;
			System.out.print(driver.toString());
			Thread.sleep(1000);
		}
		System.out.print("Finished polling. Mean poll time: " + TimeUnit.MILLISECONDS.convert((duration / iterations), TimeUnit.NANOSECONDS) +" ms");
	}catch(Exception e){
		e.printStackTrace();
	}
}
```


#### FIFO mode sample program
```
public static void main(String[] args){
	LSM9DS1Main driver = new LSM9DS1Main();

	Driver.DataRate imuFreq = Driver.DataRate.FREQ_476_HZ;
	driver.setAccelerometerScale(Driver.AccelerometerScale.SCALE_PLUS_MINUS_2G);
	driver.setDatarate(imuFreq);
	driver.setUseFifoBuffer(true);
	driver.initialize();
	long start, duration = 0, iterations = 10;

	//How long I need to sleep until the fifo buffer is almost full
	int sleepTimeMs = imuFreq.getSleepDuration();
	int availReads = 0;

	try{
		System.out.print("Waking up " + iterations + " times at " + sleepTimeMs + " ms intervals. Hoping to have 31 readings ready each time.");
		for(int interval=0; interval < iterations; interval++){
			availReads = driver.pollAvailableReads();
			System.out.println("Waking up! Available reads before polling: " + availReads);
			start = System.nanoTime();
			for(int reads=availReads; reads > 0; reads--){
				driver.pollIMU();
				 Do something with the data here
				 System.out.print(driver.toString());
			}
			duration += System.nanoTime() - start;
			driver.resetFifoBuffer();
			System.out.println("Going back to sleep for " + sleepTimeMs + " ms. Good night.");
			Thread.sleep(sleepTimeMs);
		}
		System.out.print("Total time spent in I/O: " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) +" ms");

	}catch(Exception e){
		e.printStackTrace();
	}
}
```



## Authors

* **Samuel O'Blenes**

## License

This project is licensed under the MIT License - see the LICENSE.md file for details

