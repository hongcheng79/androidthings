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

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Created by chc on 03/02/2017.
 */

public class LSM9DS0 implements Closeable {
    // https://github.com/sparkfun/SparkFun_9DOF_Block_for_Edison_CPP_Library

    private static final String TAG = LSM9DS0.class.getSimpleName();

    private I2cDevice gyro;
    private I2cDevice xm;

    // We'll store the gyro, accel, and magnetometer readings in a series of
    // public class variables. Each sensor gets three variables -- one for each
    // axis. Call readGyro(), readAccel(), and readMag() first, before using
    // these variables!
    public int gx=0, gy=0, gz=0; // x, y, and z axis readings of the gyroscope
    public int ax=0, ay=0, az=0; // x, y, and z axis readings of the accelerometer
    public int mx=0, my=0, mz=0; // x, y, and z axis readings of the magnetometer
    public int temperature=0;

    // gScale, aScale, and mScale store the current scale range for each
    // sensor. Should be updated whenever that value changes.
    private LSM9DS0Constants.gyro_scale gScale = LSM9DS0Constants.gyro_scale.G_SCALE_245DPS;
    private LSM9DS0Constants.accel_scale aScale = LSM9DS0Constants.accel_scale.A_SCALE_4G;
    private LSM9DS0Constants.mag_scale mScale = LSM9DS0Constants.mag_scale.M_SCALE_2GS;

    // gRes, aRes, and mRes store the current resolution for each sensor.
    // Units of these values would be DPS (or g's or Gs's) per ADC tick.
    // This value is calculated as (sensor scale) / (2^15).
    private float gRes=0.0f, aRes=0.0f, mRes=0.0f;

    public LSM9DS0(int gyroAdddress, int xmAddress) throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

        // Make sure there is available I2C
        List<String> i2cList = peripheralManagerService.getI2cBusList();
        if (i2cList.isEmpty()) {
            Log.i(TAG, "No I2C available on this device.");
            throw new IOException("I2C interface is require");
        } else {
            Log.i(TAG, "List of available I2C : " + i2cList);
        }

        // Asssumption here to use the first available i2c
        gyro = peripheralManagerService.openI2cDevice(i2cList.get(0), gyroAdddress);
        xm = peripheralManagerService.openI2cDevice(i2cList.get(0), xmAddress);
    }

    public int begin(LSM9DS0Constants.gyro_scale gScl, LSM9DS0Constants.accel_scale aScl, LSM9DS0Constants.mag_scale mScl,
                     LSM9DS0Constants.gyro_odr gODR, LSM9DS0Constants.accel_odr aODR, LSM9DS0Constants.mag_odr mODR)
                    throws IOException {
        // Store the given scales in class variables. These scale variables
        // are used throughout to calculate the actual g's, DPS,and Gs's.
        gScale = gScl;
        aScale = aScl;
        mScale = mScl;

        // Once we have the scale values, we can calculate the resolution
        // of each sensor. That's what these functions are for. One for each sensor
        calcgRes(); // Calculate DPS / ADC tick, stored in gRes variable
        calcmRes(); // Calculate Gs / ADC tick, stored in mRes variable
        calcaRes(); // Calculate g / ADC tick, stored in aRes variable

        // To verify communication, we can read from the WHO_AM_I register of
        // each device. Store those in a variable so we can return them.
        int gTest = gReadByte(LSM9DS0Constants.WHO_AM_I_G);		// Read the gyro WHO_AM_I
        int xmTest = xmReadByte(LSM9DS0Constants.WHO_AM_I_XM);	// Read the accel/mag WHO_AM_I

        // Gyro initialization stuff:
        initGyro();	// This will "turn on" the gyro. Setting up interrupts, etc.
        setGyroODR(gODR); // Set the gyro output data rate and bandwidth.
        setGyroScale(gScale); // Set the gyro range

        // Accelerometer initialization stuff:
        initAccel(); // "Turn on" all axes of the accel. Set up interrupts, etc.
        setAccelODR(aODR); // Set the accel data rate.
        setAccelScale(aScale); // Set the accel range.

        // Magnetometer initialization stuff:
        initMag(); // "Turn on" all axes of the mag. Set up interrupts, etc.
        setMagODR(mODR); // Set the magnetometer output data rate.
        setMagScale(mScale); // Set the magnetometer's range.

        // Once everything is initialized, return the WHO_AM_I registers we read:
        return (xmTest << 8) | gTest;
    }

    private void initGyro() throws IOException {
        /* CTRL_REG1_G sets output data rate, bandwidth, power-down and enables
        Bits[7:0]: DR1 DR0 BW1 BW0 PD Zen Xen Yen
        DR[1:0] - Output data rate selection
            00=95Hz, 01=190Hz, 10=380Hz, 11=760Hz
        BW[1:0] - Bandwidth selection (sets cutoff frequency)
             Value depends on ODR. See datasheet table 21.
        PD - Power down enable (0=power down mode, 1=normal or sleep mode)
        Zen, Xen, Yen - Axis enable (o=disabled, 1=enabled)	*/
        gWriteByte(LSM9DS0Constants.CTRL_REG1_G, (byte)0x0F); // Normal mode, enable all axes

        /* CTRL_REG2_G sets up the HPF
        Bits[7:0]: 0 0 HPM1 HPM0 HPCF3 HPCF2 HPCF1 HPCF0
        HPM[1:0] - High pass filter mode selection
            00=normal (reset reading HP_RESET_FILTER, 01=ref signal for filtering,
            10=normal, 11=autoreset on interrupt
        HPCF[3:0] - High pass filter cutoff frequency
            Value depends on data rate. See datasheet table 26.
        */
        gWriteByte(LSM9DS0Constants.CTRL_REG2_G, (byte)0x00); // Normal mode, high cutoff frequency

        /* CTRL_REG3_G sets up interrupt and DRDY_G pins
        Bits[7:0]: I1_IINT1 I1_BOOT H_LACTIVE PP_OD I2_DRDY I2_WTM I2_ORUN I2_EMPTY
        I1_INT1 - Interrupt enable on INT_G pin (0=disable, 1=enable)
        I1_BOOT - Boot status available on INT_G (0=disable, 1=enable)
        H_LACTIVE - Interrupt active configuration on INT_G (0:high, 1:low)
        PP_OD - Push-pull/open-drain (0=push-pull, 1=open-drain)
        I2_DRDY - Data ready on DRDY_G (0=disable, 1=enable)
        I2_WTM - FIFO watermark interrupt on DRDY_G (0=disable 1=enable)
        I2_ORUN - FIFO overrun interrupt on DRDY_G (0=disable 1=enable)
        I2_EMPTY - FIFO empty interrupt on DRDY_G (0=disable 1=enable) */
        // Int1 enabled (pp, active low), data read on DRDY_G:
        gWriteByte(LSM9DS0Constants.CTRL_REG3_G, (byte)0x88);

        /* CTRL_REG4_G sets the scale, update mode
        Bits[7:0] - BDU BLE FS1 FS0 - ST1 ST0 SIM
        BDU - Block data update (0=continuous, 1=output not updated until read
        BLE - Big/little endian (0=data LSB @ lower address, 1=LSB @ higher add)
        FS[1:0] - Full-scale selection
            00=245dps, 01=500dps, 10=2000dps, 11=2000dps
        ST[1:0] - Self-test enable
            00=disabled, 01=st 0 (x+, y-, z-), 10=undefined, 11=st 1 (x-, y+, z+)
        SIM - SPI serial interface mode select
            0=4 wire, 1=3 wire */
        gWriteByte(LSM9DS0Constants.CTRL_REG4_G, (byte)0x00); // Set scale to 245 dps

        /* CTRL_REG5_G sets up the FIFO, HPF, and INT1
        Bits[7:0] - BOOT FIFO_EN - HPen INT1_Sel1 INT1_Sel0 Out_Sel1 Out_Sel0
        BOOT - Reboot memory content (0=normal, 1=reboot)
        FIFO_EN - FIFO enable (0=disable, 1=enable)
        HPen - HPF enable (0=disable, 1=enable)
        INT1_Sel[1:0] - Int 1 selection configuration
        Out_Sel[1:0] - Out selection configuration */
        gWriteByte(LSM9DS0Constants.CTRL_REG5_G, (byte)0x00);
    }

    private void initAccel() throws IOException {
        /* CTRL_REG0_XM (0x1F) (Default value: 0x00)
        Bits (7-0): BOOT FIFO_EN WTM_EN 0 0 HP_CLICK HPIS1 HPIS2
        BOOT - Reboot memory content (0: normal, 1: reboot)
        FIFO_EN - Fifo enable (0: disable, 1: enable)
        WTM_EN - FIFO watermark enable (0: disable, 1: enable)
        HP_CLICK - HPF enabled for click (0: filter bypassed, 1: enabled)
        HPIS1 - HPF enabled for interrupt generator 1 (0: bypassed, 1: enabled)
        HPIS2 - HPF enabled for interrupt generator 2 (0: bypassed, 1 enabled)   */
        xmWriteByte(LSM9DS0Constants.CTRL_REG0_XM, (byte)0x00);

        /* CTRL_REG1_XM (0x20) (Default value: 0x07)
        Bits (7-0): AODR3 AODR2 AODR1 AODR0 BDU AZEN AYEN AXEN
        AODR[3:0] - select the acceleration data rate:
            0000=power down, 0001=3.125Hz, 0010=6.25Hz, 0011=12.5Hz,
            0100=25Hz, 0101=50Hz, 0110=100Hz, 0111=200Hz, 1000=400Hz,
            1001=800Hz, 1010=1600Hz, (remaining combinations undefined).
        BDU - block data update for accel AND mag
            0: Continuous update
            1: Output registers aren't updated until MSB and LSB have been read.
        AZEN, AYEN, and AXEN - Acceleration x/y/z-axis enabled.
            0: Axis disabled, 1: Axis enabled									 */
        xmWriteByte(LSM9DS0Constants.CTRL_REG1_XM, (byte)0x57); // 100Hz data rate, x/y/z all enabled

        //Serial.println(xmReadByte(CTRL_REG1_XM));
        /* CTRL_REG2_XM (0x21) (Default value: 0x00)
        Bits (7-0): ABW1 ABW0 AFS2 AFS1 AFS0 AST1 AST0 SIM
        ABW[1:0] - Accelerometer anti-alias filter bandwidth
            00=773Hz, 01=194Hz, 10=362Hz, 11=50Hz
        AFS[2:0] - Accel full-scale selection
            000=+/-2g, 001=+/-4g, 010=+/-6g, 011=+/-8g, 100=+/-16g
        AST[1:0] - Accel self-test enable
            00=normal (no self-test), 01=positive st, 10=negative st, 11=not allowed
        SIM - SPI mode selection
            0=4-wire, 1=3-wire													 */
        xmWriteByte(LSM9DS0Constants.CTRL_REG2_XM, (byte)0x00); // Set scale to 2g

        /* CTRL_REG3_XM is used to set interrupt generators on INT1_XM
        Bits (7-0): P1_BOOT P1_TAP P1_INT1 P1_INT2 P1_INTM P1_DRDYA P1_DRDYM P1_EMPTY
        */
        // Accelerometer data ready on INT1_XM (0x04)
        xmWriteByte(LSM9DS0Constants.CTRL_REG3_XM, (byte)0x04);
    }

    private void initMag() throws IOException {
        /* CTRL_REG5_XM enables temp sensor, sets mag resolution and data rate
        Bits (7-0): TEMP_EN M_RES1 M_RES0 M_ODR2 M_ODR1 M_ODR0 LIR2 LIR1
        TEMP_EN - Enable temperature sensor (0=disabled, 1=enabled)
        M_RES[1:0] - Magnetometer resolution select (0=low, 3=high)
        M_ODR[2:0] - Magnetometer data rate select
            000=3.125Hz, 001=6.25Hz, 010=12.5Hz, 011=25Hz, 100=50Hz, 101=100Hz
        LIR2 - Latch interrupt request on INT2_SRC (cleared by reading INT2_SRC)
            0=interrupt request not latched, 1=interrupt request latched
        LIR1 - Latch interrupt request on INT1_SRC (cleared by readging INT1_SRC)
            0=irq not latched, 1=irq latched 									 */
        xmWriteByte(LSM9DS0Constants.CTRL_REG5_XM, (byte)0x94); // Mag data rate - 100 Hz, enable temperature sensor

        /* CTRL_REG6_XM sets the magnetometer full-scale
        Bits (7-0): 0 MFS1 MFS0 0 0 0 0 0
        MFS[1:0] - Magnetic full-scale selection
        00:+/-2Gauss, 01:+/-4Gs, 10:+/-8Gs, 11:+/-12Gs							 */
        xmWriteByte(LSM9DS0Constants.CTRL_REG6_XM, (byte)0x00); // Mag scale to +/- 2GS

        /* CTRL_REG7_XM sets magnetic sensor mode, low power mode, and filters
        AHPM1 AHPM0 AFDS 0 0 MLP MD1 MD0
        AHPM[1:0] - HPF mode selection
            00=normal (resets reference registers), 01=reference signal for filtering,
            10=normal, 11=autoreset on interrupt event
        AFDS - Filtered acceleration data selection
            0=internal filter bypassed, 1=data from internal filter sent to FIFO
        MLP - Magnetic data low-power mode
            0=data rate is set by M_ODR bits in CTRL_REG5
            1=data rate is set to 3.125Hz
        MD[1:0] - Magnetic sensor mode selection (default 10)
            00=continuous-conversion, 01=single-conversion, 10 and 11=power-down */
        xmWriteByte(LSM9DS0Constants.CTRL_REG7_XM, (byte)0x00); // Continuous conversion mode

        /* CTRL_REG4_XM is used to set interrupt generators on INT2_XM
        Bits (7-0): P2_TAP P2_INT1 P2_INT2 P2_INTM P2_DRDYA P2_DRDYM P2_Overrun P2_WTM
        */
        xmWriteByte(LSM9DS0Constants.CTRL_REG4_XM, (byte)0x04); // Magnetometer data ready on INT2_XM (0x08)

        /* INT_CTRL_REG_M to set push-pull/open drain, and active-low/high
        Bits[7:0] - XMIEN YMIEN ZMIEN PP_OD IEA IEL 4D MIEN
        XMIEN, YMIEN, ZMIEN - Enable interrupt recognition on axis for mag data
        PP_OD - Push-pull/open-drain interrupt configuration (0=push-pull, 1=od)
        IEA - Interrupt polarity for accel and magneto
            0=active-low, 1=active-high
        IEL - Latch interrupt request for accel and magneto
            0=irq not latched, 1=irq latched
        4D - 4D enable. 4D detection is enabled when 6D bit in INT_GEN1_REG is set
        MIEN - Enable interrupt generation for magnetic data
            0=disable, 1=enable) */
        xmWriteByte(LSM9DS0Constants.INT_CTRL_REG_M, (byte)0x09); // Enable interrupts for mag, active-low, push-pull
    }

    public void readAccel() throws IOException {
        byte temp[] = new byte[6]; // We'll read six bytes from the accelerometer into temp
        xmReadBytes(LSM9DS0Constants.OUT_X_L_A, temp, 6); // Read 6 bytes, beginning at OUT_X_L_A
        ax = (temp[1] << 8) | temp[0]; // Store x-axis values into ax
        ay = (temp[3] << 8) | temp[2]; // Store y-axis values into ay
        az = (temp[5] << 8) | temp[4]; // Store z-axis values into az
    }

    public void readMag() throws IOException {
        byte temp[] = new byte[6]; // We'll read six bytes from the mag into temp
        xmReadBytes(LSM9DS0Constants.OUT_X_L_M, temp, 6); // Read 6 bytes, beginning at OUT_X_L_M
        mx = (temp[1] << 8) | temp[0]; // Store x-axis values into mx
        my = (temp[3] << 8) | temp[2]; // Store y-axis values into my
        mz = (temp[5] << 8) | temp[4]; // Store z-axis values into mz
    }

    public void readTemp() throws IOException {
        byte temp[] = new byte[2]; // We'll read two bytes from the temperature sensor into temp
        xmReadBytes(LSM9DS0Constants.OUT_TEMP_L_XM, temp, 2); // Read 2 bytes, beginning at OUT_TEMP_L_M
        temperature =  temp[0] + (temp[1]<<8); // Temperature is a 12-bit signed integer
    }

    public void readGyro() throws IOException {
        byte temp[] = new byte[6]; // We'll read six bytes from the gyro into temp
        gReadBytes(LSM9DS0Constants.OUT_X_L_G, temp, 6); // Read 6 bytes, beginning at OUT_X_L_G
        gx = (temp[1] << 8) | temp[0]; // Store x-axis values into gx
        gy = (temp[3] << 8) | temp[2]; // Store y-axis values into gy
        gz = (temp[5] << 8) | temp[4]; // Store z-axis values into gz
    }

    private float calcGyro(int gyro) {
        // Return the gyro raw reading times our pre-calculated DPS / (ADC tick):
        return gRes * gyro;
    }

    private float calcAccel(int accel) {
        // Return the accel raw reading times our pre-calculated g's / (ADC tick):
        return aRes * accel;
    }

    private float calcMag(int mag) {
        // Return the mag raw reading times our pre-calculated Gs / (ADC tick):
        return mRes * mag;
    }

    private void setGyroScale(LSM9DS0Constants.gyro_scale gScl) throws IOException {
        // We need to preserve the other bytes in CTRL_REG4_G. So, first read it:
        int temp = gReadByte(LSM9DS0Constants.CTRL_REG4_G);
        // Then mask out the gyro scale bits:
        temp &= 0xFF^(0x3 << 4);
        // Then shift in our new scale bits:
        temp |= gScl.ordinal() << 4;
        // And write the new register value back into CTRL_REG4_G:
        gWriteByte(LSM9DS0Constants.CTRL_REG4_G, (byte)temp);

        // We've updated the sensor, but we also need to update our class variables
        // First update gScale:
        gScale = gScl;
        // Then calculate a new gRes, which relies on gScale being set correctly:
        calcgRes();
    }

    private void setAccelScale(LSM9DS0Constants.accel_scale aScl) throws IOException {
        // We need to preserve the other bytes in CTRL_REG2_XM. So, first read it:
        int temp = xmReadByte(LSM9DS0Constants.CTRL_REG2_XM);
        // Then mask out the accel scale bits:
        temp &= 0xFF^(0x3 << 3);
        // Then shift in our new scale bits:
        temp |= aScl.ordinal() << 3;
        // And write the new register value back into CTRL_REG2_XM:
        xmWriteByte(LSM9DS0Constants.CTRL_REG2_XM, (byte)temp);

        // We've updated the sensor, but we also need to update our class variables
        // First update aScale:
        aScale = aScl;
        // Then calculate a new aRes, which relies on aScale being set correctly:
        calcaRes();
    }

    private void setMagScale(LSM9DS0Constants.mag_scale mScl) throws IOException {
        // We need to preserve the other bytes in CTRL_REG6_XM. So, first read it:
        int temp = xmReadByte(LSM9DS0Constants.CTRL_REG6_XM);
        // Then mask out the mag scale bits:
        temp &= 0xFF^(0x3 << 5);
        // Then shift in our new scale bits:
        temp |= mScl.ordinal() << 5;
        // And write the new register value back into CTRL_REG6_XM:
        xmWriteByte(LSM9DS0Constants.CTRL_REG6_XM, (byte)temp);

        // We've updated the sensor, but we also need to update our class variables
        // First update mScale:
        mScale = mScl;
        // Then calculate a new mRes, which relies on mScale being set correctly:
        calcmRes();
    }

    private void setGyroODR(LSM9DS0Constants.gyro_odr gRate) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_G. So, first read it:
        int temp = gReadByte(LSM9DS0Constants.CTRL_REG1_G);
        // Then mask out the gyro ODR bits:
        temp &= 0xFF^(0xF << 4);
        // Then shift in our new ODR bits:
        temp |= (gRate.ordinal() << 4);
        // And write the new register value back into CTRL_REG1_G:
        gWriteByte(LSM9DS0Constants.CTRL_REG1_G, (byte)temp);
    }

    private void setAccelODR(LSM9DS0Constants.accel_odr aRate) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_XM. So, first read it:
        int temp = xmReadByte(LSM9DS0Constants.CTRL_REG1_XM);
        // Then mask out the accel ODR bits:
        temp &= 0xFF^(0xF << 4);
        // Then shift in our new ODR bits:
        temp |= (aRate.ordinal() << 4);
        // And write the new register value back into CTRL_REG1_XM:
        xmWriteByte(LSM9DS0Constants.CTRL_REG1_XM, (byte)temp);
    }

    private void setAccelABW(LSM9DS0Constants.accel_abw abwRate) throws IOException {
        // We need to preserve the other bytes in CTRL_REG2_XM. So, first read it:
        int temp = xmReadByte(LSM9DS0Constants.CTRL_REG2_XM);
        // Then mask out the accel ABW bits:
        temp &= 0xFF^(0x3 << 6);
        // Then shift in our new ODR bits:
        temp |= (abwRate.ordinal() << 6);
        // And write the new register value back into CTRL_REG2_XM:
        xmWriteByte(LSM9DS0Constants.CTRL_REG2_XM, (byte)temp);
    }

    private void setMagODR(LSM9DS0Constants.mag_odr mRate) throws IOException {
        // We need to preserve the other bytes in CTRL_REG5_XM. So, first read it:
        int temp = xmReadByte(LSM9DS0Constants.CTRL_REG5_XM);
        // Then mask out the mag ODR bits:
        temp &= 0xFF^(0x7 << 2);
        // Then shift in our new ODR bits:
        temp |= (mRate.ordinal() << 2);
        // And write the new register value back into CTRL_REG5_XM:
        xmWriteByte(LSM9DS0Constants.CTRL_REG5_XM, (byte)temp);
    }

    private void calcgRes() {
        // Possible gyro scales (and their register bit settings) are:
        // 245 DPS (00), 500 DPS (01), 2000 DPS (10). Here's a bit of an algorithm
        // to calculate DPS/(ADC tick) based on that 2-bit value:
        switch (gScale)
        {
            case G_SCALE_245DPS:
                gRes = 245.0f / 32768.0f;
                break;
            case G_SCALE_500DPS:
                gRes = 500.0f / 32768.0f;
                break;
            case G_SCALE_2000DPS:
                gRes = 2000.0f / 32768.0f;
                break;
        }
    }

    private void calcaRes() {
        // Possible accelerometer scales (and their register bit settings) are:
        // 2 g (000), 4g (001), 6g (010) 8g (011), 16g (100). Here's a bit of an
        // algorithm to calculate g/(ADC tick) based on that 3-bit value:
        aRes = (float) (aScale == LSM9DS0Constants.accel_scale.A_SCALE_16G ? 16.0 / 32768.0 :
                        (((float) aScale.ordinal() + 1.0) * 2.0) / 32768.0);
    }

    private void calcmRes() {
        // Possible magnetometer scales (and their register bit settings) are:
        // 2 Gs (00), 4 Gs (01), 8 Gs (10) 12 Gs (11). Here's a bit of an algorithm
        // to calculate Gs/(ADC tick) based on that 2-bit value:
        mRes = (float) (mScale == LSM9DS0Constants.mag_scale.M_SCALE_2GS ? 2.0 / 32768.0 :
                        (float) (mScale.ordinal() << 2) / 32768.0);
    }

    public boolean newXData() throws IOException {
        int dReadyMask = 0b00001000;
        byte statusRegVal = xmReadByte(LSM9DS0Constants.STATUS_REG_A);
        if ((dReadyMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    public boolean newMData() throws IOException {
        int dReadyMask = 0b00001000;
        byte statusRegVal = xmReadByte(LSM9DS0Constants.STATUS_REG_M);
        if ((dReadyMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    public boolean newGData() throws IOException {
        int dReadyMask = 0b00001000;
        byte statusRegVal = gReadByte(LSM9DS0Constants.STATUS_REG_G);
        if ((dReadyMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    public boolean xDataOverflow() throws IOException {
        int dOverflowMask = 0b10000000;
        byte statusRegVal = xmReadByte(LSM9DS0Constants.STATUS_REG_A);
        if ((dOverflowMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    public boolean gDataOverflow() throws IOException {
        int dOverflowMask = 0b10000000;
        byte statusRegVal = xmReadByte(LSM9DS0Constants.STATUS_REG_A);
        if ((dOverflowMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    public boolean mDataOverflow() throws IOException {
        int dOverflowMask = 0b10000000;
        byte statusRegVal = xmReadByte(LSM9DS0Constants.STATUS_REG_M);
        if ((dOverflowMask & statusRegVal) != 0)
        {
            return true;
        }
        return false;
    }

    private void gWriteByte(int subAddress, byte data) throws IOException {
        gyro.writeRegByte(subAddress, data);
    }

    private void xmWriteByte(int subAddress, byte data) throws IOException {
        xm.writeRegByte(subAddress, data);
    }

    private byte gReadByte(int subAddress) throws IOException {
        return gyro.readRegByte(subAddress);
    }

    private void gReadBytes(int subAddress, byte[] destination, int count) throws IOException {
        gyro.readRegBuffer((subAddress|0x80),destination,count);
    }

    private byte xmReadByte(int subAddress) throws IOException {
        return xm.readRegByte(subAddress);
    }

    public void xmReadBytes(int subAddress, byte[] destination, int count) throws IOException {
        xm.readRegBuffer((subAddress|0x80),destination,count);
    }

    @Override
    public void close() throws IOException {
        if (gyro != null) {
            try {
                gyro.close();
                gyro = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }

        if (xm != null) {
            try {
                xm.close();
                xm = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }
}
