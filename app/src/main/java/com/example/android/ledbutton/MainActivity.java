package com.example.android.ledbutton;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private static final boolean VERBOSE = true;
    private static final String TAG = "MainActivity";

    private static final String GREEN_LED_PIN = "BCM19";

    private Gpio bus;
    private Handler ledToggleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERBOSE) Log.v(TAG, "+++ ON CREATE +++");

        PeripheralManager service = PeripheralManager.getInstance();

        try {
            bus = service.openGpio(GREEN_LED_PIN);
        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " bus cannot be opened.", e);
        }

        try {
            bus.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            bus.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " bus cannot be opened.", e);
        }

        ledToggleHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onStart(){
        if (VERBOSE) Log.v(TAG, "++ onStart ++" );
        super.onStart();
        ledToggleHandler.post(toggleLed);
    }

    @Override
    protected void onDestroy(){
        if (VERBOSE) Log.v(TAG, "++ onDestroy ++" );
        try {
            bus.close();
        } catch (IOException e) {
            Log.e("MainActivity", GREEN_LED_PIN + " bus cannot be closed. This may cause errors at next launch");
        }
        super.onDestroy();
    }

    private final Runnable toggleLed = new Runnable() {
        @Override
        public void run() {
            if (VERBOSE) Log.v(TAG, "++ Runnable ++" );
            boolean isOn;

            try {
                isOn = bus.getValue();
            } catch (IOException e){
                throw new IllegalStateException(GREEN_LED_PIN + " cannot be read.", e);
            }

            try {
                if (isOn) {
                    if (VERBOSE) Log.v(TAG, "Turning LED off" );
                    bus.setValue(false);
                } else {
                    if (VERBOSE) Log.v(TAG, "Turning LED on" );
                    bus.setValue(true);
                }
            } catch (IOException e){
                throw new IllegalStateException(GREEN_LED_PIN + " cannot be written", e);
            }

            ledToggleHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
        }
    };
}
