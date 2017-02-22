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

    private int buttons[] = { KeyEvent.SELECT, KeyEvent.A, KeyEvent.B , KeyEvent.UP, KeyEvent.DOWN , KeyEvent.LEFT , KeyEvent.RIGHT };
    private String GPIOButtons[] = { "GP48", "GP49", "GP46", "GP47", "GP44", "GP165" , "GP45" };

    private List<ButtonInputDriver> buttonInputDriverList = new ArrayList<ButtonInputDriver>();

    private SSD1306 ssd1306;

    public OLEDBlock() throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();

        // Make sure GPIO is available for buttons
        List<String> gpioList = peripheralManagerService.getGpioList();
        if (gpioList.isEmpty()) {
            Log.i(TAG, "No GPIO available on this device.");
            throw new IOException("GPIO interface is require");
        } else {
            Log.i(TAG, "List of available GPIO : " + gpioList);
            for ( int i = 0; i < GPIOButtons.length; i ++ ) {
                if ( gpioList.contains(GPIOButtons[i]) == false )
                    Log.i(TAG, "GPIO "+GPIOButtons[i]+" is not available on this device.");
            }
            // SSD1306
            if (gpioList.contains("GP14")== false )
                Log.i(TAG, "GPIO GP14 is not available on this device.");
            if (gpioList.contains("GP15")== false )
                Log.i(TAG, "GPIO GP15 is not available on this device.");
        }

        // SPI will be use by SSD1306 OLED display
        List<String> spiList = peripheralManagerService.getSpiBusList();
        if (spiList.isEmpty()) {
            Log.i(TAG, "No SPI available on this device.");
            throw new IOException("SPI interface is require");
        } else {
            Log.i(TAG, "List of available SPI : " + spiList);
        }

        // Setup button
        for ( int i = 0 ; i < GPIOButtons.length; i++ ) {
            ButtonInputDriver buttonInputDriver = new ButtonInputDriver(GPIOButtons[i],
                    Button.LogicState.PRESSED_WHEN_LOW,
                    buttons[i] // the keycode to send
            );
            buttonInputDriver.register();
            buttonInputDriverList.add(buttonInputDriver);
        }

        try {
            ssd1306 = new SSD1306(spiList.get(0));
        } catch (InterruptedException e) {
            Log.e(TAG,"error",e);
            throw new IOException("Unable to init SSD1306");
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
