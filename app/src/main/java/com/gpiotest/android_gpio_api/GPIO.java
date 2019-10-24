package com.gpiotest.android_gpio_api;

public class GPIO {
    // JNI
    public static native int nativeWriteGpio( int value);
    public static native int nativeReadGpio();

    public static int writeGpioValue(int value)
    {
        return nativeWriteGpio(value);
    }

    public static int readGpio()
    {
        return nativeReadGpio();
    }

    static
    {
        System.loadLibrary("GPIO");
    }
}
