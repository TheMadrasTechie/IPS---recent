package com.m.myapplication;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private ImageView img_compass,img_direction;


    TextView degree, txt_initialdegree, txt_sideFacing;

    Button buttonStart,btnReset;
    Float totalSteps;
    Float stepsToBeFinished;
    TextView txt_direction;
    TextView txt_total_steps;
    TextView txt_remaining_steps ;
    int currentPathIndex = 1;
    JSONObject currentPath;

    boolean running = false;


    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float customAzimuth = 0f;
    private float exactDirection = 0f;
    private float initialdegree = 0f;
    private float currectAzimuth = 0f;
    private float currecCustomtAzimuth = 0f;
    private SensorManager mSensorManager;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.directionLayout);
        buttonStart = (Button)findViewById(R.id.start);
        btnReset = (Button)findViewById(R.id.reset);
         txt_direction = findViewById(R.id.status);
         txt_total_steps = findViewById(R.id.totalSteps);
         txt_remaining_steps = findViewById(R.id.remainingSteps);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               resetSteps();
            }
        });
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialdegree = azimuth;
//
//                txt_initialdegree.setText(initialdegree + "");
//
//                storeAzimuth(azimuth);
                buttonStart.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                showDirections();

            }
        });

        img_compass = (ImageView)findViewById(R.id.compass);
        img_direction = (ImageView)findViewById(R.id.direction);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPostResume() {
        running = true;
        super.onPostResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        running = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha= 0.97f;
        synchronized (this){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mGravity[0] = alpha*mGravity[0] + (1-alpha)*sensorEvent.values[0];
                mGravity[1] = alpha*mGravity[1] + (1-alpha)*sensorEvent.values[1];
                mGravity[2] = alpha*mGravity[2] + (1-alpha)*sensorEvent.values[2];
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mGeomagnetic[0] = alpha*mGeomagnetic[0] + (1-alpha)*sensorEvent.values[0];
                mGeomagnetic[1] = alpha*mGeomagnetic[1] + (1-alpha)*sensorEvent.values[1];
                mGeomagnetic[2] = alpha*mGeomagnetic[2] + (1-alpha)*sensorEvent.values[2];
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                if (running){
                    totalSteps = sensorEvent.values[0];
                    float oldSteps = getStepsCount();
                    float currentSteps;
                    if(oldSteps != 0.0){
                        currentSteps = totalSteps-oldSteps;
                    }
                    else{
                        currentSteps = totalSteps;
                    }
                    txt_remaining_steps.setText(currentSteps+"");
                    if(stepsToBeFinished != null) {
                        if (currentSteps >= stepsToBeFinished) {
                            showDirections();
                        }
                    }
                }
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R,I,mGravity,mGeomagnetic);
            if(success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth+360)%360;
                customAzimuth = (azimuth+(360+(360-initialdegree)))%360;

                //
                Animation anim = new RotateAnimation(-currectAzimuth,-azimuth,
                        Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                currectAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                Animation anim1 = new RotateAnimation(-currecCustomtAzimuth,-(customAzimuth+(360-exactDirection)),
                        Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                currecCustomtAzimuth = (customAzimuth+(360-exactDirection));

                anim1.setDuration(500);
                anim1.setRepeatCount(0);
                anim1.setFillAfter(true);

                img_compass.startAnimation(anim);
                img_direction.startAnimation(anim1);

                try {
                    double fromDegree = currentPath.getDouble("fromDegree");
                    double toDegree = currentPath.getDouble("toDegree");
                    String direction = currentPath.getString("direction");
//                    if(direction.equals("straight") || direction.equals("left")) {
//                        if (customAzimuth > fromDegree || customAzimuth < toDegree) {
//                            txt_direction.setText("Go straight");
//                    }
//                        else{
//                            txt_direction.setText(currentPath.getString("direction"));
//                        }
//                    }
//                    else {
//                        if (customAzimuth >= fromDegree && customAzimuth <= toDegree) {
//                            txt_direction.setText("Go straight");
//                        }
//                        else{
//                            txt_direction.setText(currentPath.getString("direction"));
//                        }
//                    }
                   // txt_direction.setText(direction);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void showDirections(){
        try {
            JSONArray jsonArray = new JSONArray(getPath("godb"));
            for(int i=0 ; i < jsonArray.length(); i++){
                currentPath = jsonArray.getJSONObject(i);
                if(currentPathIndex == currentPath.getInt("path")){
                    stepsToBeFinished = Float.parseFloat(currentPath.getString("steps"));
                    txt_total_steps.setText(stepsToBeFinished+"");
                    //txt_direction.setText(currentPath.getString("direction"));
                    double degree = currentPath.getDouble("exactDegree");
                    exactDirection = (float)degree;
                    resetSteps();
                    currentPathIndex++;
                    return;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void resetSteps(){
        txt_remaining_steps.setText("0");
        storeStepsCount(totalSteps);
    }
    private void storeAzimuth(Float azimuth){
        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
        editor.putFloat("degree", azimuth);
        editor.apply();
    }
    private float getAzimuth() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        float azimuth = prefs.getFloat("degree", 0.0f);
        return azimuth;
    }
    private void storePath(String pathName, String json){
        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
        editor.putString(pathName, json);
        editor.apply();
    }
    private String getPath(String path) {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String json = prefs.getString(path, "[{\"path\":1,\"direction\":\"straight\",\"steps\":28,exactDegree:0.1,\"fromDegree\":337.5,\"toDegree\":22.5},{\"path\":2,\"direction\":\"right\",\"steps\":33,exactDegree:90,\"fromDegree\":67.5,\"toDegree\":112.5},{\"path\":3,\"direction\":\"left\",\"steps\":52,exactDegree:0.1,\"fromDegree\":337.5,\"toDegree\":22.5},{\"path\":4,\"direction\":\"left\",\"steps\":17,exactDegree:270,\"fromDegree\":247.5,\"toDegree\":292.5}]");
        return json;
    }
    private void storeStepsCount(Float count){
        SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
        editor.putFloat("steps", count);
        editor.apply();
    }
    private float getStepsCount()
    {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        float steps = prefs.getFloat("steps", 0.0f);
        return steps;
    }
}
