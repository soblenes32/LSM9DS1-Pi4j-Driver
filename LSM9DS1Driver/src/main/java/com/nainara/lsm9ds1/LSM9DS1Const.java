package com.nainara.lsm9ds1;

public class LSM9DS1Const {
	
/***************************************************************************************
 * Register addresses on the LSM9DS1 IMU, per the device datasheet.
 * 
 * @see https://cdn.sparkfun.com/assets/learn_tutorials/3/7/3/LSM9DS1_Datasheet.pdf
 * 
 * @author Samuel O'Blenes 01/31/2017
 ***************************************************************************************/
	
	public static final byte ACT_THS 			= 0x04; //r/w Activity threshold register.
	public static final byte ACT_DUR 			= 0x05; // r/w Inactivity duration register.
	public static final byte byte_GEN_CFG_XL 	= 0x06; // r/w Linear acceleration sensor byteerrupt generator configuration register. 
	public static final byte byte_GEN_THS_X_XL 	= 0x07; // r/w Linear acceleration sensor byteerrupt threshold register. 
	public static final byte byte_GEN_THS_Y_XL 	= 0x08; // r/w Linear acceleration sensor byteerrupt threshold register. 
	public static final byte byte_GEN_THS_Z_XL 	= 0x09; // r/w Linear acceleration sensor byteerrupt threshold register. 
	public static final byte byte_GEN_DUR_XL 	= 0x0A; // r/w Linear acceleration sensor byteerrupt duration register.
	public static final byte REFERENCE_G 		= 0x0B; // r/w Angular rate sensor reference value register for digital high-pass filter (r/w).
	public static final byte byte1_CTRL 		= 0x0C; // r/w byte1_A/G pin control register
	public static final byte byte2_CTRL 		= 0x0D; // r/w byte2_A/G pin control register.
	public static final byte WHO_AM_I 			= 0x0F; // r Who_AM_I register.
	public static final byte CTRL_REG1_G 		= 0x10; // r/w Angular rate sensor Control Register 1.
	public static final byte CTRL_REG2_G 		= 0x11; // r/w Angular rate sensor Control Register 2.
	public static final byte CTRL_REG3_G 		= 0x12; // r/w Angular rate sensor Control Register 3.
	public static final byte ORIENT_CFG_G 		= 0x13; // r/w Angular rate sensor sign and orientation register.
	public static final byte byte_GEN_SRC_G 	= 0x14; // r Angular rate sensor byteerrupt source register. 
	public static final byte OUT_TEMP_L 		= 0x15; // r Temperature data output register. L and H registers together express a 16-bit word in two’s complement right-justified.
	public static final byte OUT_TEMP_H 		= 0x16; // r
	public static final byte STATUS_REG1 		= 0x17; // r Status register.
	public static final byte OUT_X_L_G 			= 0x18; // r Angular rate sensor pitch axis (X) angular rate output register. The value is expressed as a 16-bit word in two’s complement.
	public static final byte OUT_X_H_G 			= 0x19; // r 
	public static final byte OUT_Y_L_G 			= 0x1A; // r Angular rate sensor roll axis (Y) angular rate output register. The value is expressed as a 16-bit word in two’s complement.
	public static final byte OUT_Y_H_G 			= 0x1B; // r
	public static final byte OUT_Z_L_G 			= 0x1C; // r Angular rate sensor yaw axis (Z) angular rate output register. The value is expressed as a 16-bit word in two’s complement.
	public static final byte OUT_Z_H_G 			= 0x1D; // r
	public static final byte CTRL_REG4 			= 0x1E; // r/w Control register 4.
	public static final byte CTRL_REG5_XL 		= 0x1F; // r/w Linear acceleration sensor Control Register 5.
	public static final byte CTRL_REG6_XL 		= 0x20; // r/w Linear acceleration sensor Control Register 6.
	public static final byte CTRL_REG7_XL 		= 0x21; // r/w Linear acceleration sensor Control Register 7.
	public static final byte CTRL_REG8  		= 0x22; // r/w
	public static final byte CTRL_REG9 			= 0x23; // r/w 
	public static final byte CTRL_REG10 		= 0x24; // r/w 
	public static final byte byte_GEN_SRC_XL 	= 0x26; // r 
	public static final byte STATUS_REG2 		= 0x27; // r 
	public static final byte OUT_X_L_XL 		= 0x28; // r //Linear acceleration X
	public static final byte OUT_X_H_XL 		= 0x29; // r 
	public static final byte OUT_Y_L_XL 		= 0x2A; // r //Linear acceleration Y
	public static final byte OUT_Y_H_XL 		= 0x2B; // r 
	public static final byte OUT_Z_L_XL 		= 0x2C; // r //Linear acceleration Z
	public static final byte OUT_Z_H_XL 		= 0x2D; // r 
	public static final byte FIFO_CTRL 			= 0x2E; // r/w 
	public static final byte FIFO_SRC 			= 0x2F; // r 
	public static final byte byte_GEN_CFG_G 	= 0x30; // r/w 
	public static final byte byte_GEN_THS_XH_G 	= 0x31; // r/w 
	public static final byte byte_GEN_THS_XL_G 	= 0x32; // r/w 
	public static final byte byte_GEN_THS_YH_G 	= 0x33; // r/w 
	public static final byte byte_GEN_THS_YL_G 	= 0x34; // r/w 
	public static final byte byte_GEN_THS_ZH_G 	= 0x35; // r/w 
	public static final byte byte_GEN_THS_ZL_G 	= 0x36; // r/w 
	public static final byte byte_GEN_DUR_G 	= 0x37; // r/w 
	public static final byte OFFSET_X_REG_L_M 	= 0x05; // r/w 
	public static final byte OFFSET_X_REG_H_M 	= 0x06; // r/w 
	public static final byte OFFSET_Y_REG_L_M 	= 0x07; // r/w 
	public static final byte OFFSET_Y_REG_H_M 	= 0x08; // r/w 
	public static final byte OFFSET_Z_REG_L_M 	= 0x09; // r/w 
	public static final byte OFFSET_Z_REG_H_M 	= 0x0A; // r/w 
	public static final byte WHO_AM_I_M 		= 0x0F; // r 
	public static final byte CTRL_REG1_M 		= 0x20; // r/w 
	public static final byte CTRL_REG2_M 		= 0x21; // r/w 
	public static final byte CTRL_REG3_M 		= 0x22; // r/w 
	public static final byte CTRL_REG4_M 		= 0x23; // r/w 
	public static final byte CTRL_REG5_M 		= 0x24; // r/w 
	public static final byte STATUS_REG_M 		= 0x27; // r 
	public static final byte OUT_X_L_M 			= 0x28; // r 
	public static final byte OUT_X_H_M 			= 0x29; // r 
	public static final byte OUT_Y_L_M 			= 0x2A; // r 
	public static final byte OUT_Y_H_M 			= 0x2B; // r 
	public static final byte OUT_Z_L_M 			= 0x2C; // r 
	public static final byte OUT_Z_H_M 			= 0x2D; // r 
	public static final byte byte_CFG_M 		= 0x30; // r/w 
	public static final byte byte_SRC_M 		= 0x31; // r Magnetic byteerrupt generator status register
	public static final byte byte_THS_L_M 		= 0x32; // r Magnetic byteerrupt generator
	public static final byte byte_THS_H_M 		= 0x33; // r threshold
}
