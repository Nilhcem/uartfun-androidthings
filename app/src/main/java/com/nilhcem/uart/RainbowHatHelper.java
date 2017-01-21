package com.nilhcem.uart;

import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.Random;

public class RainbowHatHelper implements Button.OnButtonEventListener {

    private static final String TAG = RainbowHatHelper.class.getSimpleName();
    private static final int LEDS_BUTTONS_NB = 3;
    private static final int HANDLER_MSG_STOP = 0;
    private static final int HANDLER_MSG_PLAY = 1;

    private AlphanumericDisplay display;
    private Speaker buzzer;
    private Apa102 ledstrip;
    private final Gpio[] leds = new Gpio[LEDS_BUTTONS_NB];
    private final Button[] buttons = new Button[LEDS_BUTTONS_NB];

    private boolean textMode = true;
    private String textBuffer;
    private final Random random = new Random();
    private int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];

    private final HandlerThread inputThread = new HandlerThread("BuzzerThread");
    private Handler handler;

    public void init() {
        inputThread.start();
        handler = new Handler(inputThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    buzzer.stop();
                    if (msg.what == HANDLER_MSG_PLAY) {
                        buzzer.play(msg.arg1);
                        handler.sendEmptyMessageDelayed(HANDLER_MSG_STOP, 800);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Buzzer error", e);
                }
            }
        };

        try {
            display = RainbowHat.openDisplay();
            display.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            display.display(textBuffer);
            display.setEnabled(true);
            resetDisplay();

            String[] ledPins = new String[]{RainbowHat.LED_RED, RainbowHat.LED_GREEN, RainbowHat.LED_BLUE};
            String[] buttonPins = new String[]{RainbowHat.BUTTON_A, RainbowHat.BUTTON_B, RainbowHat.BUTTON_C};
            for (int i = 0; i < LEDS_BUTTONS_NB; i++) {
                leds[i] = RainbowHat.openLed(ledPins[i]);
                buttons[i] = RainbowHat.openButton(buttonPins[i]);
                buttons[i].setOnButtonEventListener(this);
            }

            buzzer = RainbowHat.openPiezo();

            ledstrip = RainbowHat.openLedStrip();
            ledstrip.setBrightness(8);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(255, new float[]{i * 360.f / rainbow.length, 1.0f, 1.0f});
            }
        } catch (IOException e) {
            Log.e(TAG, "Error initializing Rainbow HAT", e);
        }
    }

    public void close() {
        try {
            inputThread.quitSafely();

            display.close();
            buzzer.close();
            ledstrip.close();

            for (int i = 0; i < LEDS_BUTTONS_NB; i++) {
                leds[i].close();
                buttons[i].setOnButtonEventListener(null);
                buttons[i].close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing Rainbow HAT", e);
        }
    }

    public void onKeyReceived(int key) {
        Log.i(TAG, "onKeyReceived: " + key);

        try {
            if (textMode) {
                onTextKeyReceived(key);
            } else {
                onMusicKeyReceived(key);
            }

            moveRainbow();
        } catch (IOException e) {
            Log.e(TAG, "Error interacting with Rainbow HAT onKeyReceived: " + key, e);
        }
    }

    @Override
    public void onButtonEvent(Button button, boolean pressed) {
        if (pressed) {
            resetDisplay();
            showLeds(false, false, false);

            if (button == buttons[0]) {
                Log.i(TAG, "Text mode");
                textMode = true;
            } else if (button == buttons[2]) {
                Log.i(TAG, "Music mode");
                textMode = false;
            } else {
                Log.i(TAG, "Toggle mode");
                textMode = !textMode;
            }
        }
    }

    private void onTextKeyReceived(int key) {
        // Backspace
        if (key == 127) {
            textBuffer = " " + textBuffer.substring(0, textBuffer.length() - 1);
        } else {
            textBuffer = textBuffer.substring(1) + Character.toString((char) key).toUpperCase();
        }

        displayMessage(textBuffer);
        showLeds(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
    }

    private void onMusicKeyReceived(int key) {
        char c = Character.toUpperCase((char) key);

        switch (c) {
            case 'F':
                playNote(370);
                displayMessage("FA#");
                showLeds(false, true, true);
                break;
            case 'G':
                playNote(415);
                displayMessage("SOL#");
                showLeds(true, false, false);
                break;
            case 'H':
                playNote(440);
                displayMessage("LA");
                showLeds(true, true, true);
                break;
            case 'J':
                playNote(494);
                displayMessage("SI");
                showLeds(false, true, false);
                break;
            case 'K':
                playNote(554);
                displayMessage("DO#");
                showLeds(true, false, true);
                break;
            case 'L':
                playNote(587);
                displayMessage("RE");
                showLeds(true, true, false);
                break;
            default:
                playNote(0);
                displayMessage("");
                showLeds(false, false, false);
                break;
        }
    }

    private void showLeds(boolean red, boolean green, boolean blue) {
        try {
            leds[0].setValue(red);
            leds[1].setValue(green);
            leds[2].setValue(blue);
        } catch (IOException e) {
            Log.e(TAG, "Error settings leds", e);
        }
    }

    private void playNote(int frequency) {
        handler.removeMessages(HANDLER_MSG_STOP);

        Message msg = new Message();
        msg.what = frequency == 0 ? HANDLER_MSG_STOP : HANDLER_MSG_PLAY;
        msg.arg1 = frequency;
        handler.sendMessage(msg);
    }

    private void moveRainbow() throws IOException {
        int firstColor = rainbow[0];
        System.arraycopy(rainbow, 1, rainbow, 0, RainbowHat.LEDSTRIP_LENGTH - 1);
        rainbow[RainbowHat.LEDSTRIP_LENGTH - 1] = firstColor;
        ledstrip.write(rainbow);
    }

    private void resetDisplay() {
        textBuffer = "    ";
        displayMessage(textBuffer);
    }

    private void displayMessage(String message) {
        char[] toDisplay = new char[]{' ', ' ', ' ', ' '};

        try {
            for (int i = 0; i < 4 && i < message.length(); i++) {
                toDisplay[i] = message.charAt(i);
            }
            display.display(new String(toDisplay));
        } catch (IOException e) {
            Log.e(TAG, "Error displaying message", e);
        }
    }
}
