package com.cleetus.alan.androidtouchfreegesures;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String Log_tag = "Log ";

    private SensorManager sensorManager;

    //proximity sensor variables
    private Sensor      proximitySensor;
    private boolean     isTapGestureAvailable = false;
    private final int   tapGestureDuration = 2000;
    private final int   numOfTapsNeeded = 2;
    private long        lastTapGestureTime = 0;
    private float       lastProximitySensorValue;
    private int         numOfTapGesturesDetected = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /*PROXIMITY SENSOR START*/
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        final Switch tapGestureSwitch = (Switch) findViewById(R.id.tapGestureSwitch);
        tapGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Tap Switch: "+ (tapGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isTapGestureAvailable = tapGestureSwitch.isChecked();
            }
        });

        /*PROXIMITY SENSOR END*/


        /*ACCELEROMETER START*/

        /*ACCELEROMETER END*/



        /*ROTATION VECTOR START*/

        /*ROTATION VECTOR END*/


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        /*
        * Proximity Sensor Gesture
        * When there is an object close to the proximity sensor the value becomes 0
        * otherwise the value is ~5.  We can use the switching of the values when an
        * object is brought near by to detect an "air tap".
        *
        * So to double airtap just bring your hand close to prox sensor, take it away, and repeat once more.
        * */
        if(sensor.getType() == Sensor.TYPE_PROXIMITY && isTapGestureAvailable){

            Log.d(Log_tag, "Proximity Sensor: "+event.values[0]);

            //after tapGestureDuration reset variables
            if( (System.currentTimeMillis() - lastTapGestureTime) >= tapGestureDuration  )
            {
                lastTapGestureTime = System.currentTimeMillis();
                numOfTapGesturesDetected = 0;
                lastProximitySensorValue = event.values[0];

                Log.d(Log_tag, "Reset Tap Gesture");
            }
            else if((event.values[0] != lastProximitySensorValue ) ) {

                numOfTapGesturesDetected++;

                Log.d(Log_tag, "Tap Detected");
                if (numOfTapGesturesDetected == numOfTapsNeeded) {
                    lastProximitySensorValue = System.currentTimeMillis();
                    numOfTapGesturesDetected = 0;

                    Log.d(Log_tag, "Double Tap Detected ");
                    Toast.makeText(this, "double tap", Toast.LENGTH_SHORT).show();
                }
            }
            /**Proximity Sensor Code End**/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){

        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
