SparkFun driver for Android Things
=====================================

https://github.com/hcchoong79/androidthings/blob/master/sparkfun/SparkFunEdison.jpg
This driver provides easy access to the peripherals available on the [SparkFun for Android
Things][product]:
- OLEDBlock (GPIO) [oled]
- 9 Degrees of Freedom (Work in progress) [gyro]
- UART (Work in progress) [uart]

Work in the pipeline is to port some LPWAN code to Android Things platform through UART which cover
- Sigfox 
- LORA

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

How to use the driver
---------------------

### Gradle dependency

To use the `sparkfun` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [bintray][bintray].

root `build.gradle`
```
allprojects {
    repositories {
        maven { url  "http://dl.bintray.com/hcchoong79/contrib-driver-sparkfun" }
        ...
    }
}
```

module `build.gradle`
```
dependencies {
    compile 'com.google.android.things.contrib:sparkfun:<version>'
}
```

### Sample usage


```java
// import the Sparkfun driver
import com.google.android.things.contrib.driver.sparkfun.OLEDBlock;
// Font to be use for OLED
import com.google.android.things.contrib.driver.sparkfun.Fonts;
// SSD1306 for OLED display
import com.google.android.things.contrib.driver.sparkfun.SSD1306;
```

```java
// Draw a checker box
OLEDBlock sparkFunOLDEBlock = new OLEDBlock();
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
// Close the device when done.
sparkFunOLDEBlock.close();
```

```java
// Display a string on the segment display.
OLEDBlock sparkFunOLDEBlock = new OLEDBlock();
sparkFunOLDEBlock.getSsd1306().drawString(1,1,"testing 123", Fonts.Type.font5x5);
sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*1),"testing 123", Fonts.Type.fontAcme5Outlines);
sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*2),"testing 123", Fonts.Type.fontAztech);
sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*3),"testing 123", Fonts.Type.fontCrackers);
sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*4),"testing 123", Fonts.Type.fontSuperDig);
sparkFunOLDEBlock.getSsd1306().drawString(1,1+(Fonts.CHAR_HEIGHT*5),"testing 123", Fonts.Type.fontZxpix);
// Close the device when done.
sparkFunOLDEBlock.close();
```

```java
// Get key events - A, B, UP, DOWN, LEFT, RIGHT, SELECT
// Class is defined in com.google.android.things.contrib.driver.sparkfun.KeyEvent
// This wil also register the button driver
OLEDBlock sparkFunOLDEBlock = new OLEDBlock();

// In your Activity.
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ( keyCode == com.google.android.things.contrib.driver.sparkfun.KeyEvent.A) {
        // ...
    }
    return super.onKeyDown(keyCode, event);
}
@Override
public boolean onKeyUp(int keyCode, KeyEvent event) {
    if ( keyCode == com.google.android.things.contrib.driver.sparkfun.KeyEvent.A) {
        // ...
    }
    return super.onKeyUp(keyCode, event);
}
```

License
-------

Copyright 2016 Google Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

[bintray]: http://dl.bintray.com/hcchoong79/contrib-driver-sparkfun
[product]: https://www.sparkfun.com/categories/272
[oled]: https://www.sparkfun.com/products/13035
[gyro]: https://www.sparkfun.com/products/13033
[uart]: https://www.sparkfun.com/products/13040