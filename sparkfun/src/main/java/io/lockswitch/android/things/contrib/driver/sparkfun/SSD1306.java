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

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.Closeable;
import java.io.IOException;

/**
 * SSD1306 for Sparkfun OLED Block
 * Port mostly taken from
 * https://github.com/sparkfun/SparkFun_Micro_OLED_Arduino_Library/blob/V_1.0.0/src/SFE_MicroOLED.cpp
 *
 */
public class SSD1306 implements Closeable {
    private static final String TAG = "SSD1306";

    private SpiDevice spiDevice;
    private Gpio DC_PIN;
    private Gpio RST_PIN;

    // Screen configuration constants.
    private static final int LCDWIDTH = 64;
    private static final int LCDHEIGHT = 48;

    // Protocol constants
    //private static final int DATA_OFFSET = 1;
    private static final int DATA_OFFSET = 0;

    private static final int COMMAND_ACTIVATE_SCROLL = 0x2F;
    private static final int COMMAND_DEACTIVATE_SCROLL = 0x2E;
    private static final int COMMAND_RIGHT_HORIZONTAL_SCROLL = 0x26;
    private static final int COMMAND_LEFT_HORIZONTAL_SCROLL = 0x27;
    private static final int COMMAND_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
    private static final int COMMAND_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL = 0x2A;
    private static final int COMMAND_DISPLAY_ON = 0xAF;
    private static final int COMMAND_DISPLAY_OFF = 0xAE;
    private static final int COMMAND_START_LINE = 0x40;
    private static final int INIT_CHARGE_PUMP = 0x8D;
    private static final int INIT_CLK_DIV = 0xD5;
    private static final int INIT_COMSCAN_DEC = 0xC8;
    private static final int INIT_DISPLAY_NO_OFFSET = 0x0;
    private static final int INIT_DISPLAY_OFFSET = 0xD3;
    private static final int INIT_DUTY_CYCLE_1_64 = 0x3F;
    private static final int INIT_MEMORY_ADDRESSING_HORIZ = 0x0;
    private static final int INIT_RESISTER_RATIO = 0x80;
    private static final int INIT_SEGREMAP = 0xA1;
    private static final int INIT_SET_MEMORY_ADDRESSING_MODE = 0x20;

    private static final byte SSD1306_DISPLAY_WRITE = (byte) 0xA4;

    private static final int SETCONTRAST 		= 0x81;
    private static final int DISPLAYALLONRESUME = 0xA4;
    private static final int DISPLAYALLON 		= 0xA5;
    private static final int NORMALDISPLAY 		= 0xA6;
    private static final int INVERTDISPLAY 		= 0xA7;
    private static final int DISPLAYOFF 		= 0xAE;
    private static final int DISPLAYON 			= 0xAF;
    private static final int SETDISPLAYOFFSET 	= 0xD3;
    private static final int SETCOMPINS 		= 0xDA;
    private static final int SETVCOMDESELECT	= 0xDB;
    private static final int SETDISPLAYCLOCKDIV = 0xD5;
    private static final int SETPRECHARGE 		= 0xD9;
    private static final int SETMULTIPLEX 		= 0xA8;
    private static final int SETLOWCOLUMN 		= 0x00;
    private static final int SETHIGHCOLUMN 		= 0x10;
    private static final int SETSTARTLINE 		= 0x40;
    private static final int MEMORYMODE 		= 0x20;
    private static final int COMSCANINC 		= 0xC0;
    private static final int COMSCANDEC 		= 0xC8;
    private static final int SEGREMAP 			= 0xA0;
    private static final int CHARGEPUMP 		= 0x8D;
    private static final int EXTERNALVCC 		= 0x01;
    private static final int SWITCHCAPVCC 		= 0x02;

    // Scroll
    private static final int ACTIVATESCROLL 				= 0x2F;
    private static final int DEACTIVATESCROLL 				= 0x2E;
    private static final int SETVERTICALSCROLLAREA 			= 0xA3;
    private static final int RIGHTHORIZONTALSCROLL 			= 0x26;
    private static final int LEFT_HORIZONTALSCROLL 			= 0x27;
    private static final int VERTICALRIGHTHORIZONTALSCROLL	= 0x29;
    private static final int VERTICALLEFTHORIZONTALSCROLL	= 0x2A;

    public enum ColorCode  {
        WHITE, BLACK, INVERSE
    };

    // Init sequence for 64x48 OLED module
    private static final byte[] INIT_PAYLOAD = new byte[]{
            (byte) DISPLAYOFF,          // 0xAE

            (byte) SETDISPLAYCLOCKDIV,  // 0xD5
            (byte) 0x80,                // the suggested ratio 0x80

            (byte) SETMULTIPLEX,		// 0xA8
            (byte) 0x2F,

            (byte) SETDISPLAYOFFSET,	// 0xD3
            (byte) 0x0,					// no offset

            (byte)(SETSTARTLINE | 0x0),	// line #0

            (byte) CHARGEPUMP,			// enable charge pump
            (byte) 0x14,

            (byte) NORMALDISPLAY,		// 0xA6
            (byte) DISPLAYALLONRESUME,	// 0xA4

            (byte)(SEGREMAP | 0x1),
            (byte) COMSCANDEC,

            (byte) SETCOMPINS,			// 0xDA
            (byte) 0x12,

            (byte) SETCONTRAST,			// 0x81
            (byte) 0x8F,

            (byte) SETPRECHARGE,		// 0xd9
            (byte) 0xF1,

            (byte) SETVCOMDESELECT,		// 0xDB
            (byte) 0x40,

            (byte) DISPLAYON			//--turn on oled panel
    };

    private final byte[] mBuffer = new byte[((LCDWIDTH * LCDHEIGHT) / 8)];

    /**
     * Contructor to setup SSD1306 OLED
     * @param spiName
     * @throws IOException
     * @throws InterruptedException
     */
    public SSD1306(String spiName) throws IOException, InterruptedException {
        DC_PIN = new PeripheralManagerService().openGpio("GP14");
        RST_PIN = new PeripheralManagerService().openGpio("GP15");
        spiDevice = new PeripheralManagerService().openSpiDevice(spiName);

        spiDevice.setMode(SpiDevice.MODE0);
        spiDevice.setFrequency(10000000);
        spiDevice.setBitsPerWord(8);
        spiDevice.setBitJustification(false);

        DC_PIN.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        RST_PIN.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        RST_PIN.setActiveType(Gpio.ACTIVE_HIGH);
        RST_PIN.setValue(true);
        Thread.sleep(5000); // VDD (3.3V) goes high at start, lets just chill for 5 ms
        RST_PIN.setActiveType(Gpio.ACTIVE_LOW);
        RST_PIN.setValue(true);
        Thread.sleep(10000); // wait 10ms
        RST_PIN.setActiveType(Gpio.ACTIVE_HIGH);
        RST_PIN.setValue(true);

        BitmapHelper.bmpToBytes(mBuffer, DATA_OFFSET,
                Bitmap.createBitmap(LCDWIDTH, LCDHEIGHT, Bitmap.Config.ARGB_8888),
                false);

        for ( byte c : INIT_PAYLOAD ) {
            command(c);
        }
    }

    /**
     * Draw pixel in SSD1306
     * @param x
     * @param y
     * @param color WHITE | BLACK | INVERSE
     */
    public void setPixel(int x, int y, ColorCode color)  {
        if (x < 0 || y < 0 || x >= LCDWIDTH || y >= LCDHEIGHT) {
            // Ignore the out of bound at this point
            return;
        }

        switch (color) {
            case WHITE :
                mBuffer[DATA_OFFSET + x + ((y / 8) * LCDWIDTH)] |= (1 << y % 8);
                break;
            case BLACK:
                mBuffer[DATA_OFFSET + x + ((y / 8) * LCDWIDTH)] &= ~(1 << y % 8);
                break;
            case INVERSE:
                mBuffer[DATA_OFFSET + x + ((y / 8) * LCDWIDTH)] ^= (1 << y % 8);
                break;
        }
    }

    /**
     * Draw bitmap font using text which only cover alphanumeric at this point
     * @param x
     * @param y
     * @param text
     * @param type Font.Type
     */
    public void drawString(int x, int y, String text, Fonts.Type type ) {
        byte[] characters = text.getBytes();

        for ( byte character : characters ) {
            int code = (int)character;
            int index = code - 32;

            if ( index >= 0 && index < 96 ) {
                char[] pixel = Fonts.font5x5[index];

                if ( type == Fonts.Type.font5x5 )
                    pixel = Fonts.font5x5[index];
                else if ( type == Fonts.Type.fontAcme5Outlines )
                    pixel = Fonts.fontAcme5Outlines[index];
                else if ( type == Fonts.Type.fontAztech )
                    pixel = Fonts.fontAztech[index];
                else if ( type == Fonts.Type.fontCrackers )
                    pixel = Fonts.fontCrackers[index];
                else if ( type == Fonts.Type.fontSuperDig )
                    pixel = Fonts.fontSuperDig[index];
                else if ( type == Fonts.Type.fontZxpix )
                    pixel = Fonts.fontZxpix[index];

                // Draw pixels
                for (int j=0; j < Fonts.CHAR_WIDTH; j++) {
                    String binary = Integer.toBinaryString(pixel[j]);
                    while (binary.length() < Fonts.CHAR_HEIGHT)
                    {
                        binary = "0" + binary;
                    }

                    // Reverse it
                    binary = new StringBuilder(binary).reverse().toString();

                    for (int i = 0; i < Fonts.CHAR_HEIGHT; i++) {
                        if ( binary.substring(i,i+1).equals("1") )
                            setPixel((x+j),(y+i),ColorCode.WHITE);
                        else
                            setPixel((x+j),(y+i),ColorCode.BLACK);
                    }
                }

                x = x +Fonts.CHAR_WIDTH;
            }
        }
    }

    /**
     * LCD Width
     * @return int
     */
    public int getLcdWidth() {
        return LCDWIDTH;
    }

    /**
     * LCD Height
     * @return int
     */
    public int getLcdHeight() {
        return LCDHEIGHT;
    }

    @Override
    public void close() throws IOException {
        if ( spiDevice != null ) {
            try {
                spiDevice.close();
                spiDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close SPI device", e);
            }
        }
        if ( DC_PIN != null ) {
            try {
                DC_PIN.close();
                DC_PIN = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close DC_PIN", e);
            }
        }
        if ( RST_PIN != null ) {
            try {
                RST_PIN.close();
                RST_PIN = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close RST_PIN", e);
            }
        }
    }

    /**
     * Clear bitmap on OLED
     * @throws IOException
     */
    private void clearMemory() throws IOException {
        for (int i=0;i<8; i++) {
            setPageAddress(i);
            setColumnAddress(0);

            for ( int j = 0 ; j < 48; j++ ) {
                data((byte) 0);
            }
        }
    }

    /**
     * Draw bitmap on OLED
     * @throws IOException
     */
    public void show() throws IOException {
        for (int i=0;i<6; i++) {
            setPageAddress(i);
            setColumnAddress(0);

            for ( int j = 0 ; j < 0x40; j++ ) {
                data(mBuffer[i*0x40+j]);
            }
        }
    }

    /**
     * SSD1306 set page address
     * @param i
     * @throws IOException
     */
    private void setPageAddress(int i) throws IOException {
        i = 0xb0|i;
        command((byte)i);
    }

    /**
     * SSD1306 set column address
     * @param i
     * @throws IOException
     */
    private void setColumnAddress(int i) throws IOException {
        command((byte) ((0x10|(i>>4))+0x02) );
        command((byte) (0x0f&i) );
    }

    /**
     * SSD1306 send command
     * @param c
     * @throws IOException
     */
    private void command(byte c) throws IOException {
        DC_PIN.setActiveType(Gpio.ACTIVE_LOW);
        DC_PIN.setValue(true);
        spiDevice.write(new byte[]{c},1);
    }

    /**
     * SSD1306 send data
     * @param c
     * @throws IOException
     */
    private void data(byte c) throws IOException {
        DC_PIN.setActiveType(Gpio.ACTIVE_HIGH);
        DC_PIN.setValue(true);
        spiDevice.write(new byte[]{c},1);
    }

    /**
     * Convert byte to hex string
     * @param bytes
     * @return string
     */
    private String byteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
