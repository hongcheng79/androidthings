/*
 * Copyright 2016 Google Inc.
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

package com.google.android.things.contrib.driver.sparkfun;

import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SparkFun OLED Block that control SSD1306 OLED and GPIO input button
 *
 */
public class OLEDBlock implements Closeable {
    private static final String TAG = OLEDBlock.class.getSimpleName();

    private int buttons[] = { KeyEvent.UP, KeyEvent.DOWN, KeyEvent.LEFT, KeyEvent.RIGHT, KeyEvent.SELECT, KeyEvent.A, KeyEvent.B };
    private String GPIOButtons[] = { "GP47", "GP44", "GP165", "GP45", "GP48", "GP49", "GP46" };

    private List<ButtonInputDriver> buttonInputDriverList = new ArrayList<ButtonInputDriver>();
    private SSD1306 ssd1306;

    public OLEDBlock() {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

        List<String> gpioList = peripheralManagerService.getGpioList();
        if (gpioList.isEmpty()) {
            Log.i(TAG, "No GPIO available on this device.");
        } else {
            Log.i(TAG, "List of available GPIO : " + gpioList);
        }

        List<String> i2cList = peripheralManagerService.getI2cBusList();
        if (i2cList.isEmpty()) {
            Log.i(TAG, "No I2C available on this device.");
        } else {
            Log.i(TAG, "List of available I2C : " + i2cList);
        }

        List<String> pwmList = peripheralManagerService.getPwmList();
        if (pwmList.isEmpty()) {
            Log.i(TAG, "No PWM available on this device.");
        } else {
            Log.i(TAG, "List of available PWM : " + pwmList);
        }

        List<String> spiList = peripheralManagerService.getSpiBusList();
        if (spiList.isEmpty()) {
            Log.i(TAG, "No SPI available on this device.");
        } else {
            Log.i(TAG, "List of available SPI : " + spiList);
        }

        List<String> uartList = peripheralManagerService.getUartDeviceList();
        if (uartList.isEmpty()) {
            Log.i(TAG, "No UART available on this device.");
        } else {
            Log.i(TAG, "List of available UART : " + uartList);
        }

        // Setup button
        try {
            for ( int i = 0 ; i < GPIOButtons.length; i++ ) {
                ButtonInputDriver buttonInputDriver = new ButtonInputDriver(GPIOButtons[i],
                        Button.LogicState.PRESSED_WHEN_LOW,
                        buttons[i] // the keycode to send
                );
                buttonInputDriver.register();
                buttonInputDriverList.add(buttonInputDriver);
            }
        } catch (IOException e) {
            // error configuring button...
            Log.e(TAG,"Unable to configure button",e);
        }

        // Test OLED
        try {
            ssd1306 = new SSD1306("SPI2");
            //dev13035 = new SSD1306("I2C1");
        } catch (IOException e) {
            // couldn't configure the display...
            Log.e(TAG,"Unable to configure display",e);
        } catch (InterruptedException e) {
            Log.e(TAG,"Unable to configure display",e);
        }

        Log.i(TAG, "Completed init");
    }

    /**
     * Method to return SSD1306
     * @return SSD1306
     */
    public SSD1306 getSsd1306() {
        return ssd1306;
    }

    @Override
    public void close() throws IOException {
        for ( ButtonInputDriver buttonInputDriver : buttonInputDriverList ) {
            buttonInputDriver.unregister();
            try {
                buttonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG,"Unable to close",e);
            }
        }
        try {
            ssd1306.close();
        } catch (IOException e) {
            Log.e(TAG,"Unable to close",e);
        }
    }
}
