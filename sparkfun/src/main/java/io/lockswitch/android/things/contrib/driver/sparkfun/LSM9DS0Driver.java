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

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by chc on 10/02/2017.
 */

public class LSM9DS0Driver implements Closeable {
    private static final String TAG = LSM9DS0Driver.class.getSimpleName();

    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    private static final String DRIVER_VENDOR = "Sparkfun";
    private static final String DRIVER_NAME = "LSM9DS0";
    //private static final int DRIVER_MIN_DELAY_US = Math.round(1000000.f / Bmx280.MAX_FREQ_HZ);
    //private static final int DRIVER_MAX_DELAY_US = Math.round(1000000.f / Bmx280.MIN_FREQ_HZ);

    // TODO : Need to figure out the correct value for this
    private static final float DRIVER_MAX_RANGE = 4 * SensorManager.GRAVITY_EARTH;
    private static final int DRIVER_MIN_DELAY_US = 0;
    private static final int DRIVER_MAX_DELAY_US = 10;

    private static final int DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";

    private LSM9DS0 lsm9DS0;
    private UserSensor userSensor;

    public LSM9DS0Driver() throws IOException {
        // https://github.com/androidthings/contrib-drivers/blob/master/mma7660fc/src/main/java/com/google/android/things/contrib/driver/mma7660fc/Mma7660FcAccelerometerDriver.java
        // https://github.com/androidthings/contrib-drivers/blob/master/bmx280/src/main/java/com/google/android/things/contrib/driver/bmx280/Bmx280SensorDriver.java

        lsm9DS0 = new LSM9DS0(0x6B, 0x1D);

        // begin() -- Initialize the gyro, accelerometer, and magnetometer.
        // This will set up the scale and output rate of each sensor. It'll also
        // "turn on" every sensor and every axis of every sensor.
        // Input:
        //	- gScl = The scale of the gyroscope. This should be a gyro_scale value.
        //	- aScl = The scale of the accelerometer. Should be a accel_scale value.
        //	- mScl = The scale of the magnetometer. Should be a mag_scale value.
        //	- gODR = Output data rate of the gyroscope. gyro_odr value.
        //	- aODR = Output data rate of the accelerometer. accel_odr value.
        //	- mODR = Output data rate of the magnetometer. mag_odr value.
        // Output: The function will return an unsigned 16-bit value. The most-sig
        //		bytes of the output are the WHO_AM_I reading of the accel. The
        //		least significant two bytes are the WHO_AM_I reading of the gyro.
        // All parameters have a defaulted value, so you can call just "begin()".
        // Default values are FSR's of:  245DPS, 2g, 2Gs; ODRs of 95 Hz for
        // gyro, 100 Hz for accelerometer, 100 Hz for magnetometer.
        // Use the return value of this function to verify communication.
        lsm9DS0.begin(LSM9DS0Constants.gyro_scale.G_SCALE_245DPS, LSM9DS0Constants.accel_scale.A_SCALE_2G, LSM9DS0Constants.mag_scale.M_SCALE_2GS,
                LSM9DS0Constants.gyro_odr.G_ODR_95_BW_25, LSM9DS0Constants.accel_odr.A_ODR_25, LSM9DS0Constants.mag_odr.M_ODR_50);
    }

    @Override
    public void close() throws IOException {
        unregister();
        if ( lsm9DS0 != null ) {
            try {
                lsm9DS0.close();
            } finally {
                lsm9DS0 = null;
            }
        }
    }

    /**
     * Register the driver in the framework.
     * @see #unregister()
     */
    public void register() {
        if (lsm9DS0 == null) {
            throw new IllegalStateException("cannot registered closed driver");
        }
        if (userSensor == null) {
            userSensor = build(lsm9DS0);
            UserDriverManager.getManager().registerSensor(userSensor);
        }
    }

    /**
     * Unregister the driver from the framework.
     */
    public void unregister() {
        if  ( userSensor != null ) {
            UserDriverManager.getManager().unregisterSensor(userSensor);
            userSensor = null;
        }
    }

    static UserSensor build(final LSM9DS0 lsm9DS0) {
        return UserSensor.builder()
                .setType(Sensor.TYPE_ACCELEROMETER)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)

                .setMaxRange(DRIVER_MAX_RANGE)
                .setMinDelay(DRIVER_MIN_DELAY_US)
                .setMaxDelay(DRIVER_MAX_DELAY_US)

                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        lsm9DS0.readAccel();
                        lsm9DS0.readMag();
                        lsm9DS0.readMag();
                        lsm9DS0.readTemp();

                        //Log.i(TAG,"Read sensor info");
                        float[] sample = new float[] { 0.0f, 0.0f, 0.0f };

                        return new UserSensorReading(
                                sample,
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH); // 120Hz
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            //mma7660fc.setMode(Mma7660Fc.MODE_ACTIVE);
                        } else {
                            //mma7660fc.setMode(Mma7660Fc.MODE_STANDBY);
                        }
                    }
                })
                .build();
    }
}
