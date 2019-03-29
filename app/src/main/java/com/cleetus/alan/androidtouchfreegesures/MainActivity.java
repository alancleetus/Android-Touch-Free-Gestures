package com.cleetus.alan.androidtouchfreegesures;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String Log_tag = "Log ";

    private SensorManager sensorManager;

    //proximity gesture variables
    private Sensor      proximitySensor;
    private boolean     isTapGestureAvailable   = false;
    private final int   tapGestureDuration      = 2000;
    private final int   numOfTapsNeeded         = 2;
    private long        lastTapGestureTime      = 0;
    private float       lastProximitySensorValue= 0;
    private int         numOfTapGesturesDetected= 0;

    //accelerometer gesture variables
    private Sensor      accelerometerSensor;
    private boolean     isShakeGestureAvailable     = false;
    private final int   shakeGestureDuration        = 500;
    private final int   shakeGestureThreshold       = 500;
    private final int   detectShakeEvery            = 1000;
    private long        lastShakeGestureTime        = 0;
    private long        lastShakeGestureDetectedTime= 0;
    private float []    currentAccelerometerValues  = {0,0,0};
    private float []    prevAccelerometerValues     = {0,0,0};

    //rotation vector gesture variables
    private Sensor      rotationVectorSensor;
    private Boolean[]   isRotationGestureAvailable  = {false, false, false, false}; //{left, right, forward, backward}
    private int[]       rotationTriggerValue        = {-45, 45, -45, 70}; //{left, right, forward, backward}
    private long        lastRotationDetected        = 0;
    private long        detectRotateEvery           = 1000;
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
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final Switch shakeGestureSwitch = (Switch) findViewById(R.id.shakeGestureSwitch);
        shakeGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Shake Switch: "+ (shakeGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isShakeGestureAvailable = shakeGestureSwitch.isChecked();
            }
        });
        /*ACCELEROMETER END*/



        /*ROTATION VECTOR START*/
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        /*rotate left switch*/
        final Switch rotateLeftGestureSwitch = (Switch) findViewById(R.id.rotateLeftGestureSwitch);
        rotateLeftGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Rotate Left Switch: "+ (rotateLeftGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isRotationGestureAvailable[0] = rotateLeftGestureSwitch.isChecked();
            }
        });

        /*rotate right switch*/
        final Switch rotateRightGestureSwitch = (Switch) findViewById(R.id.rotateRightGestureSwitch);
        rotateRightGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Rotate Right Switch: "+ (rotateRightGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isRotationGestureAvailable[1] = rotateRightGestureSwitch.isChecked();
            }
        });

        /*rotate forward switch*/
        final Switch rotateForwardGestureSwitch = (Switch) findViewById(R.id.rotateForwardGestureSwitch);
        rotateForwardGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Rotate Forward Switch: "+ (rotateForwardGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isRotationGestureAvailable[2] = rotateForwardGestureSwitch.isChecked();
            }
        });

        /*rotate backward switch*/
        final Switch rotateBackGestureSwitch = (Switch) findViewById(R.id.rotateBackGestureSwitch);
        rotateBackGestureSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Log_tag, "Rotate Backward Switch: "+ (rotateBackGestureSwitch.isChecked()?"Enabled": "Disabled"));
                isRotationGestureAvailable[3] = rotateBackGestureSwitch.isChecked();
            }
        });
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
        * So to double air tap just bring your hand close to prox sensor, take it away, and repeat once more.
        * */
        if(sensor.getType() == Sensor.TYPE_PROXIMITY && isTapGestureAvailable){

            Log.d(Log_tag, "Proximity Sensor: "+event.values[0]);

            //after tapGestureDuration reset variables
            if( (System.currentTimeMillis() - lastTapGestureTime) >= tapGestureDuration  )
            {
                lastTapGestureTime = System.currentTimeMillis();
                numOfTapGesturesDetected = 0;
                lastProximitySensorValue = event.values[0];

                //Log.d(Log_tag, "Reset Tap Gesture");
            }
            else if((event.values[0] != lastProximitySensorValue ) ) {

                numOfTapGesturesDetected++;

                Log.d(Log_tag, "Tap Detected");
                if (numOfTapGesturesDetected == numOfTapsNeeded) {
                    lastProximitySensorValue = System.currentTimeMillis();
                    numOfTapGesturesDetected = 0;

                    Log.d(Log_tag, "Double Tap Detected ");
                    Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
                }
            }
            /**Proximity Sensor Code End**/
        }
        /*
        * Accelerometer
        * Detects one phone shake every 1 second.
        * */
        else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER  && isShakeGestureAvailable) {

            long currentTime = System.currentTimeMillis();
            //reduce polling rate to 2 per second
            if ((currentTime - lastShakeGestureTime) > shakeGestureDuration) {

                long shakeTime = (currentTime - lastShakeGestureTime);
                lastShakeGestureTime = currentTime;

                currentAccelerometerValues[0] = event.values[0];
                currentAccelerometerValues[1] = event.values[1];
                currentAccelerometerValues[2] = event.values[2];

                float speed = Math.abs(currentAccelerometerValues[0]+ currentAccelerometerValues[1]+  currentAccelerometerValues[2] - prevAccelerometerValues[0] - prevAccelerometerValues[1] - prevAccelerometerValues[2]) / shakeTime * 10000;

                Log.d(Log_tag, "Speed: " + speed );
                if (speed > shakeGestureThreshold && (currentTime-lastShakeGestureDetectedTime)>detectShakeEvery) {

                    Log.d(Log_tag, "Shake detected w/ speed: " + speed );
                    Toast.makeText(this, "Shake", Toast.LENGTH_SHORT).show();
                    lastShakeGestureDetectedTime = currentTime;
                }

                prevAccelerometerValues[0] = currentAccelerometerValues[0];
                prevAccelerometerValues[1] = currentAccelerometerValues[1];
                prevAccelerometerValues[2] = currentAccelerometerValues[2];
            }
            /**Accelerometer Code End**/
        }
        /**
         * Rotation Vector
         * Wrist gestures. Based on the angle the phone is rotated using only your wrist,
         * do different actions.
         */
        else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR &&
                (isRotationGestureAvailable[0] ||
                        isRotationGestureAvailable[1] ||
                        isRotationGestureAvailable[2] ||
                        isRotationGestureAvailable[3])) {

            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, event.values);

            // Remap coordinate system
            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedRotationMatrix);

            // Convert to orientations
            float[] orientations = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientations);

            for (int i = 0; i < 3; i++) {
                orientations[i] = (float) (Math.toDegrees(orientations[i]));
            }

            long currentTime = System.currentTimeMillis();
            if((currentTime -lastRotationDetected)>detectRotateEvery){
                /**Y-axis**/
                //Rotate Left

                //Log.d(Log_tag, "Rotate Y"+ orientations[2]);
                if (isRotationGestureAvailable[0] && orientations[2] < rotationTriggerValue[0]) {

                   Log.d(Log_tag, "Rotate Left\t" + orientations[2]+" "+ rotationTriggerValue[0]);
                   Toast.makeText(this, "Rotate Left", Toast.LENGTH_SHORT).show();
                   lastRotationDetected = currentTime;
                }
                //Rotate Right
                else if (isRotationGestureAvailable[1] && orientations[2] > rotationTriggerValue[1]) {

                    Log.d(Log_tag, "Rotate Right\t"+ orientations[2]);
                    Toast.makeText(this, "Rotate Right", Toast.LENGTH_SHORT).show();
                    lastRotationDetected = currentTime;
                }

                /**Z-axis**/
                //Rotate Forward
                //Log.d(Log_tag, "Rotate Y"+ orientations[1]);
                if (isRotationGestureAvailable[2] && orientations[1] < rotationTriggerValue[2]) {

                    Log.d(Log_tag, "Rotate Forward\t"+ orientations[1]);
                    Toast.makeText(this, "Rotate Forward", Toast.LENGTH_SHORT).show();
                    lastRotationDetected = currentTime;
                }
                //Rotate Back
                else if (isRotationGestureAvailable[3] && orientations[1] > rotationTriggerValue[3]) {

                    Log.d(Log_tag, "Rotate Backward\t"+ orientations[1]);
                    Toast.makeText(this, "Rotate Backward", Toast.LENGTH_SHORT).show();
                    lastRotationDetected = currentTime;
                }
            }
            /* Rotation Vector Code End**/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){

        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
