package com.nilhcem.uart;

import android.app.Activity;
import android.os.Bundle;

class MainActivity extends Activity implements UARTHelper.KeyReceivedListener {

    private final UARTHelper uartHelper = new UARTHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uartHelper.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uartHelper.close();
    }

    @Override
    public void onKeyReceived(int key) {
        // TODO
    }
}
