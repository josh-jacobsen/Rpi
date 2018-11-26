package com.example.android.ledbutton;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final boolean VERBOSE = true;
    private static final String TAG = "MainActivity";

    private static final String GREEN_LED_PIN = "BCM19";
    private static final String BUTTON_PIN = "BCM21";

    private Gpio ledBus;
    private Gpio buttonBus;

    private Handler ledToggleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERBOSE) Log.v(TAG, "+++ ON CREATE +++");

        PeripheralManager ledService = PeripheralManager.getInstance();
        PeripheralManager buttonService = PeripheralManager.getInstance();

        try {
            ledBus = ledService.openGpio(GREEN_LED_PIN);
            buttonBus = buttonService.openGpio(BUTTON_PIN);
        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " ledBus and/or buttonBus cannot be opened.", e);
        }

        try {
            ledBus.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            ledBus.setActiveType(Gpio.ACTIVE_HIGH);

            buttonBus.setDirection(Gpio.DIRECTION_IN);
            buttonBus.setActiveType(Gpio.ACTIVE_LOW);

        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " ledBus and/or buttonBus cannot be opened.", e);
        }

    }

    @Override
    protected void onStart(){
        if (VERBOSE) Log.v(TAG, "++ onStart ++" );
        super.onStart();
        try {
            buttonBus.setEdgeTriggerType(Gpio.EDGE_BOTH);
            buttonBus.registerGpioCallback(touchButtonACallback);
        } catch (IOException e) {
            throw new IllegalStateException(BUTTON_PIN + " cannot be monitored", e);
        }
    }

    @Override
    protected void onStop(){
        if (VERBOSE) Log.v(TAG, "++ onStop ++" );
        buttonBus.unregisterGpioCallback(touchButtonACallback);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        if (VERBOSE) Log.v(TAG, "++ onDestroy ++" );
        try {
            ledBus.close();
            buttonBus.close();
        } catch (IOException e) {
            Log.e("MainActivity", GREEN_LED_PIN + " ledBus and/or buttonBus cannot be closed. This may cause errors at next launch");
        }
        super.onDestroy();
    }

    private final GpioCallback touchButtonACallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio buttonBus) {
            try {
                if (buttonBus.getValue()) {
                    Log.v(TAG, "Button pressed; LED on");
                    ledBus.setValue(true);
                }
                else {
                    Log.v(TAG, "Button released; LED off");
                    ledBus.setValue(false);
                }
            } catch (IOException e) {
                throw new IllegalStateException(BUTTON_PIN + " cannot be read", e);
            }
            return true;
        }
    };
}
