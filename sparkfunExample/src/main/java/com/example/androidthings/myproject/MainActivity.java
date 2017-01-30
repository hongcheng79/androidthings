/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.androidthings.myproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.sparkfun.Fonts;
import com.google.android.things.contrib.driver.sparkfun.OLEDBlock;
import com.google.android.things.contrib.driver.sparkfun.SSD1306;

import java.io.IOException;

/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private OLEDBlock sparkFunOLDEBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        sparkFunOLDEBlock = new OLEDBlock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            sparkFunOLDEBlock.close();
        } catch (IOException e) {
            Log.e(TAG,"Unable to close",e);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "GPIO changed, button pressed : "+keyCode);

        if ( keyCode == com.google.android.things.contrib.driver.sparkfun.KeyEvent.A) {
            for (int i = 0; i < sparkFunOLDEBlock.getSsd1306().getLcdWidth(); i++ ) {
                for (int j = 0; j < sparkFunOLDEBlock.getSsd1306().getLcdHeight(); j++ ) {
                    // checkerboard
                    if ( (i % 2) == (j % 2) )
                        sparkFunOLDEBlock.getSsd1306().setPixel(i, j, SSD1306.ColorCode.WHITE);
                    else
                        sparkFunOLDEBlock.getSsd1306().setPixel(i, j, SSD1306.ColorCode.BLACK);
                }
            }

            try {
                sparkFunOLDEBlock.getSsd1306().show(); // render the pixel data
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ( keyCode == com.google.android.things.contrib.driver.sparkfun.KeyEvent.B ) {
            sparkFunOLDEBlock.getSsd1306().drawString(1,1,"testing 123", Fonts.Type.font5x5);
            sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*1),"testing 123", Fonts.Type.fontAcme5Outlines);
            sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*2),"testing 123", Fonts.Type.fontAztech);
            sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*3),"testing 123", Fonts.Type.fontCrackers);
            sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*4),"testing 123", Fonts.Type.fontSuperDig);
            sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*5),"testing 123", Fonts.Type.fontZxpix);

            try {
                sparkFunOLDEBlock.getSsd1306().show(); // render the pixel data
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
