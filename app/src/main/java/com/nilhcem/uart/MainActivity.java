package com.nilhcem.uart;

import android.app.Activity;
import android.os.Bundle;

class MainActivity extends Activity implements UARTHelper.KeyReceivedListener {

    private final UARTHelper uartHelper = new UARTHelper();
    private final RainbowHatHelper rainbowhatHelper = new RainbowHatHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uartHelper.init(this);
        rainbowhatHelper.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rainbowhatHelper.close();
        uartHelper.close();
    }

    @Override
    public void onKeyReceived(int key) {
        rainbowhatHelper.onKeyReceived(key);
    }
}
