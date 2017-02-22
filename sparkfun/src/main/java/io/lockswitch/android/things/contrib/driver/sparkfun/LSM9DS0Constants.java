/*
 * Copyright 2017 Choong Hong Cheng, Lockswitch Sdn Bhd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lockswitch.android.things.contrib.driver.sparkfun;

/**
 * Created by chc on 03/02/2017.
 */

public interface LSM9DS0Constants {
    ////////////////////////////
    // LSM9DS0 Gyro Registers //
    ////////////////////////////
    public static int WHO_AM_I_G			= 0x0F;
    public static int CTRL_REG1_G			= 0x20;
    public static int CTRL_REG2_G			= 0x21;
    public static int CTRL_REG3_G			= 0x22;
    public static int CTRL_REG4_G			= 0x23;
    public static int CTRL_REG5_G			= 0x24;
    public static int REFERENCE_G			= 0x25;
    public static int STATUS_REG_G		    = 0x27;
    public static int OUT_X_L_G			    = 0x28;
    public static int OUT_X_H_G			    = 0x29;
    public static int OUT_Y_L_G			    = 0x2A;
    public static int OUT_Y_H_G			    = 0x2B;
    public static int OUT_Z_L_G			    = 0x2C;
    public static int OUT_Z_H_G			    = 0x2D;
    public static int FIFO_CTRL_REG_G		= 0x2E;
    public static int FIFO_SRC_REG_G		= 0x2F;
    public static int INT1_CFG_G			= 0x30;
    public static int INT1_SRC_G			= 0x31;
    public static int INT1_THS_XH_G		    = 0x32;
    public static int INT1_THS_XL_G		    = 0x33;
    public static int INT1_THS_YH_G		    = 0x34;
    public static int INT1_THS_YL_G		    = 0x35;
    public static int INT1_THS_ZH_G		    = 0x36;
    public static int INT1_THS_ZL_G		    = 0x37;
    public static int INT1_DURATION_G		= 0x38;


    //////////////////////////////////////////
    // LSM9DS0 Accel/Magneto (XM) Registers //
    //////////////////////////////////////////
    public static int OUT_TEMP_L_XM		    = 0x05;
    public static int OUT_TEMP_H_XM		    = 0x06;
    public static int STATUS_REG_M		    = 0x07;
    public static int OUT_X_L_M			    = 0x08;
    public static int OUT_X_H_M			    = 0x09;
    public static int OUT_Y_L_M			    = 0x0A;
    public static int OUT_Y_H_M			    = 0x0B;
    public static int OUT_Z_L_M			    = 0x0C;
    public static int OUT_Z_H_M			    = 0x0D;
    public static int WHO_AM_I_XM		    = 0x0F;
    public static int INT_CTRL_REG_M	    = 0x12;
    public static int INT_SRC_REG_M		    = 0x13;
    public static int INT_THS_L_M		    = 0x14;
    public static int INT_THS_H_M		    = 0x15;
    public static int OFFSET_X_L_M		    = 0x16;
    public static int OFFSET_X_H_M		    = 0x17;
    public static int OFFSET_Y_L_M		    = 0x18;
    public static int OFFSET_Y_H_M		    = 0x19;
    public static int OFFSET_Z_L_M		    = 0x1A;
    public static int OFFSET_Z_H_M		    = 0x1B;
    public static int REFERENCE_X		    = 0x1C;
    public static int REFERENCE_Y		    = 0x1D;
    public static int REFERENCE_Z		    = 0x1E;
    public static int CTRL_REG0_XM		    = 0x1F;
    public static int CTRL_REG1_XM		    = 0x20;
    public static int CTRL_REG2_XM		    = 0x21;
    public static int CTRL_REG3_XM		    = 0x22;
    public static int CTRL_REG4_XM		    = 0x23;
    public static int CTRL_REG5_XM		    = 0x24;
    public static int CTRL_REG6_XM		    = 0x25;
    public static int CTRL_REG7_XM		    = 0x26;
    public static int STATUS_REG_A		    = 0x27;
    public static int OUT_X_L_A			    = 0x28;
    public static int OUT_X_H_A			    = 0x29;
    public static int OUT_Y_L_A			    = 0x2A;
    public static int OUT_Y_H_A			    = 0x2B;
    public static int OUT_Z_L_A			    = 0x2C;
    public static int OUT_Z_H_A			    = 0x2D;
    public static int FIFO_CTRL_REG		    = 0x2E;
    public static int FIFO_SRC_REG		    = 0x2F;
    public static int INT_GEN_1_REG		    = 0x30;
    public static int INT_GEN_1_SRC		    = 0x31;
    public static int INT_GEN_1_THS		    = 0x32;
    public static int INT_GEN_1_DURATION	= 0x33;
    public static int INT_GEN_2_REG		    = 0x34;
    public static int INT_GEN_2_SRC		    = 0x35;
    public static int INT_GEN_2_THS		    = 0x36;
    public static int INT_GEN_2_DURATION	= 0x37;
    public static int CLICK_CFG			    = 0x38;
    public static int CLICK_SRC			    = 0x39;
    public static int CLICK_THS			    = 0x3A;
    public static int TIME_LIMIT			= 0x3B;
    public static int TIME_LATENCY		    = 0x3C;
    public static int TIME_WINDOW			= 0x3D;
    public static int ACT_THS				= 0x3E;
    public static int ACT_DUR				= 0x3F;

    // gyro_scale defines the possible full-scale ranges of the gyroscope:
    public enum gyro_scale
    {
        G_SCALE_245DPS,		// 00:  245 degrees per second
        G_SCALE_500DPS,		// 01:  500 dps
        G_SCALE_2000DPS,	// 10:  2000 dps
    }

    // accel_scale defines all possible FSR's of the accelerometer:
    public enum accel_scale
    {
        A_SCALE_2G,	// 000:  2g
        A_SCALE_4G,	// 001:  4g
        A_SCALE_6G,	// 010:  6g
        A_SCALE_8G,	// 011:  8g
        A_SCALE_16G	// 100:  16g
    }

    // mag_scale defines all possible FSR's of the magnetometer:
    public enum mag_scale
    {
        M_SCALE_2GS,	// 00:  2Gs
        M_SCALE_4GS, 	// 01:  4Gs
        M_SCALE_8GS,	// 10:  8Gs
        M_SCALE_12GS,	// 11:  12Gs
    }

    // gyro_odr defines all possible data rate/bandwidth combos of the gyro:
    public enum gyro_odr
    {							// ODR (Hz) --- Cutoff
        G_ODR_95_BW_125  (0x0), //   95         12.5
        G_ODR_95_BW_25   (0x1), //   95          25
        // 0x2 and 0x3 define the same data rate and bandwidth
        G_ODR_190_BW_125 (0x4), //   190        12.5
        G_ODR_190_BW_25  (0x5), //   190         25
        G_ODR_190_BW_50  (0x6), //   190         50
        G_ODR_190_BW_70  (0x7), //   190         70
        G_ODR_380_BW_20  (0x8), //   380         20
        G_ODR_380_BW_25  (0x9), //   380         25
        G_ODR_380_BW_50  (0xA), //   380         50
        G_ODR_380_BW_100 (0xB), //   380         100
        G_ODR_760_BW_30  (0xC), //   760         30
        G_ODR_760_BW_35  (0xD), //   760         35
        G_ODR_760_BW_50  (0xE), //   760         50
        G_ODR_760_BW_100 (0xF); //   760         100

        private final int value;

        gyro_odr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // accel_oder defines all possible output data rates of the accelerometer:
    public enum accel_odr
    {
        A_POWER_DOWN, 	// Power-down mode (0x0)
        A_ODR_3125,		// 3.125 Hz	(0x1)
        A_ODR_625,		// 6.25 Hz (0x2)
        A_ODR_125,		// 12.5 Hz (0x3)
        A_ODR_25,		// 25 Hz (0x4)
        A_ODR_50,		// 50 Hz (0x5)
        A_ODR_100,		// 100 Hz (0x6)
        A_ODR_200,		// 200 Hz (0x7)
        A_ODR_400,		// 400 Hz (0x8)
        A_ODR_800,		// 800 Hz (9)
        A_ODR_1600		// 1600 Hz (0xA)
    }

    // accel_abw defines all possible anti-aliasing filter rates of the accelerometer:
    public enum accel_abw
    {
        A_ABW_773,		// 773 Hz (0x0)
        A_ABW_194,		// 194 Hz (0x1)
        A_ABW_362,		// 362 Hz (0x2)
        A_ABW_50,		//  50 Hz (0x3)
    }

    // mag_oder defines all possible output data rates of the magnetometer:
    public enum mag_odr
    {
        M_ODR_3125,	// 3.125 Hz (0x00)
        M_ODR_625,	// 6.25 Hz (0x01)
        M_ODR_125,	// 12.5 Hz (0x02)
        M_ODR_25,	// 25 Hz (0x03)
        M_ODR_50,	// 50 (0x04)
        M_ODR_100,	// 100 Hz (0x05)
    }
}
