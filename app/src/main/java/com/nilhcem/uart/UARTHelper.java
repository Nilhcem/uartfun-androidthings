package com.nilhcem.uart;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

public class UARTHelper implements UartDeviceCallback {

    private static final String TAG = UARTHelper.class.getSimpleName();
    private static final String UART_NAME = "UART0";
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 512;

    interface KeyReceivedListener {
        void onKeyReceived(int key);
    }

    private HandlerThread inputThread;
    private UartDevice uartDevice;
    private KeyReceivedListener listener;

    public void init(KeyReceivedListener listener) {
        this.listener = listener;

        inputThread = new HandlerThread("UARTThread");
        inputThread.start();

        try {
            uartDevice = PeripheralManager.getInstance().openUartDevice(UART_NAME);
            uartDevice.setBaudrate(BAUD_RATE);
            uartDevice.setDataSize(DATA_BITS);
            uartDevice.setParity(UartDevice.PARITY_NONE);
            uartDevice.setStopBits(STOP_BITS);
            uartDevice.registerUartDeviceCallback(new Handler(inputThread.getLooper()), this);

            String ready = "Ready. Have fun!\r\n";
            uartDevice.write(ready.getBytes(), ready.length());
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);
        }
    }

    public void close() {
        listener = null;

        try {
            uartDevice.unregisterUartDeviceCallback(this);
            uartDevice.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }

        inputThread.quitSafely();
    }

    @Override
    public boolean onUartDeviceDataAvailable(UartDevice uart) {
        try {
            byte[] buffer = new byte[CHUNK_SIZE];
            while (uartDevice.read(buffer, buffer.length) > 0) {
                int key = (int) buffer[0];
                listener.onKeyReceived(key);
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to transfer data over UART", e);
        }
        return true;
    }

    @Override
    public void onUartDeviceError(UartDevice uart, int error) {
        Log.w(TAG, uart + ": Error event " + error);
    }
}
