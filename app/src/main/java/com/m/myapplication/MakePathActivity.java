package com.m.myapplication;

import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MakePathActivity extends AppCompatActivity implements SensorEventListener {
    Button btn_start,btn_finish;
    TextView txt_steps;
    Float totalSteps;
    private ImageView img_compass;
    List<PointF> points = new ArrayList<>();
    String lastdirection = "straight";
    String currentDirection = "straight";
    String lastAction = "straight";

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float customAzimuth = 0f;
    private float exactDirection = 0f;
    private float initialdegree = 0f;
    private float currectAzimuth = 0f;
    private float currecCustomtAzimuth = 0f;
    private SensorManager mSensorManager;
    boolean running = false;
    float currentX =300, currentY =900;
    float multiplier = 0.5f;
    float currentSteps,previosuStreps;
    int previousDegree=0;
    private LineView mLineView ;
    ScrollView scrollView;
    boolean isFinished = false;
    boolean isStarted = false;
    private boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_path);
        btn_start = findViewById(R.id.start);
        btn_finish = findViewById(R.id.finish);
        txt_steps = findViewById(R.id.steps);
        mLineView = (LineView) findViewById(R.id.lineView);
        scrollView = findViewById(R.id.scrollView);
        img_compass = (ImageView)findViewById(R.id.compass);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialdegree = azimuth;
                isStarted = true;
                btn_start.setVisibility(View.GONE);
                btn_finish.setVisibility(View.VISIBLE);
                PointF pointF = new PointF(currentX,currentY);
                points.add(pointF);
                resetSteps();
//                setupDirections(currentDirection);
            }
        });
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStarted = false;
                isFinished = true;
            }
        });

    }
    private void resetSteps(){
        txt_steps.setText("0");
        previosuStreps = 0;
        storeStepsCount(totalSteps);
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
            if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
            {
                if (running){
                    totalSteps = sensorEvent.values[0];
                    float oldSteps = getStepsCount();

                    if(oldSteps != 0.0){
                        currentSteps = totalSteps-oldSteps;
                    }
                    else{
                        currentSteps = totalSteps;
                    }
                    if(isFirstTime){
                        txt_steps.setText("0");
                        isFirstTime = false;
                    }
                    else{
                        txt_steps.setText(currentSteps+"");

                    }

                    if(isStarted) {
                        if (currentSteps >= previosuStreps) {
                            if(customAzimuth > 337.5 || customAzimuth < 22.5 )
                            {
                                rotateView(previousDegree,0);
                                currentY =  currentY -  ((int) currentSteps * multiplier);
                                lastdirection = currentDirection;
                                currentDirection = "straight";
                                previousDegree=0;
                            }
                            if(customAzimuth > 22.5 && customAzimuth < 67.5 )
                            {
                                lastdirection = currentDirection;
                                currentDirection = "upright";
                                currentX =  currentX +  ((int) currentSteps * multiplier);
                                currentY =  currentY -  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,-45);
                                previousDegree=-45;
                            }
                            if(customAzimuth > 67.5 && customAzimuth < 112.5 )
                            {
                                lastdirection = currentDirection;
                                currentDirection = "right";
                                currentX =  currentX +  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,-90);
                                previousDegree=-90;
                            }
                            if(customAzimuth > 112.5 && customAzimuth < 157.5 )
                            {


                                lastdirection = currentDirection;
                                currentDirection = "downright";
                                currentX =  currentX +  ((int) currentSteps * multiplier);
                                currentY =  currentY +  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,-135);
                                previousDegree=-135;
                            }
                            if(customAzimuth > 157.5 && customAzimuth < 202.5 )
                            {

                                lastdirection = currentDirection;
                                currentDirection = "down";
//                    currentX =  currentX +  ((int) currentSteps * multiplier);
                                currentY =  currentY +  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,180);
                                previousDegree=180;
                            }
                            if(customAzimuth > 202.5 && customAzimuth < 247.5 )
                            {

                                lastdirection = currentDirection;
                                currentDirection = "downleft";
                                currentX =  currentX -  ((int) currentSteps * multiplier);
                                currentY =  currentY +  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,135);
                                previousDegree=135;

                            }
                            if(customAzimuth > 247.5 && customAzimuth < 292.5 )
                            {

                                lastdirection = currentDirection;
                                currentDirection = "left";
                                currentX =  currentX -  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,90);
                                previousDegree=90;

                            }
                            if(customAzimuth > 292.5 && customAzimuth < 337.5 )
                            {

                                lastdirection = currentDirection;
                                currentDirection = "upleft";
                                currentX =  currentX -  ((int) currentSteps * multiplier);
                                currentY =  currentY -  ((int) currentSteps * multiplier);
                                rotateView(previousDegree,45);
                                previousDegree=45;
                            }
                            dropPoints();
                            previosuStreps = currentSteps;
                        }
                        if(!currentDirection.equals(lastdirection)){
                            resetSteps();
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
                Animation anim = new RotateAnimation(-currectAzimuth,-azimuth, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
                currectAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);
                img_compass.startAnimation(anim);
            }



        }

    }


//    private void setupDirections(String direction){
//        if(direction.equals("straight")) {
//            lastAction = "straight";
//            currentDirection = "straight";
//            lastdirection = "straight";
//        }
//        else if(direction.equals("right")){
//            lastAction = "right";
//            if(currentDirection.equals("straight")){
//                lastdirection = "straight";
//                currentDirection="right";
//            }
//            else if(currentDirection.equals("upright")){
//                currentDirection = "downright";
//                lastdirection = "upright";
//            }
//
//            else if(currentDirection.equals("right")){
//                currentDirection = "down";
//                lastdirection = "right";
//
//            }
//            else if(currentDirection.equals("downright")){
//                currentDirection = "downleft";
//                lastdirection = "downright";
//            }
//            else if(currentDirection.equals("down")){
//                currentDirection = "left";
//                lastdirection = "down";
//            }
//            else if(currentDirection.equals("downleft")){
//                currentDirection = "upleft";
//                lastdirection = "downleft";
//            }
//            else if(currentDirection.equals("left")){
//                currentDirection = "straight";
//                lastdirection = "left";
//            }
//            else if(currentDirection.equals("upleft")){
//                currentDirection = "upright";
//                lastdirection = "upleft";
//            }
//        }
//        else if(direction.equals("left")){
//            lastAction = "left";
//            if(currentDirection.equals("straight")){
//                lastdirection = "straight";
//                currentDirection = "left";
//            }
//            else if(currentDirection.equals("upright")){
//                currentDirection = "upleft";
//                lastdirection = "upright";
//            }
//
//            else if(currentDirection.equals("right")){
//                currentDirection = "straight";
//                lastdirection = "right";
//            }
//            else if(currentDirection.equals("downright")){
//                currentDirection = "upright";
//                lastdirection = "downright";
//            }
//            else if(currentDirection.equals("down")){
//                currentDirection = "right";
//                lastdirection = "down";
//            }
//            else if(currentDirection.equals("downleft")){
//                currentDirection = "downright";
//                lastdirection = "downleft";
//            }
//            else if(currentDirection.equals("left")){
//                currentDirection = "down";
//                lastdirection = "left";
//            }
//            else if(currentDirection.equals("upleft")){
//                currentDirection = "downleft";
//                lastdirection = "upleft";
//            }
//        }
//        else if(direction.equals("upright")){
//            lastAction = "upright";
//            if(currentDirection.equals("straight")){
//                currentDirection = "upright";
//                lastdirection = "straight";
//            }
//            else if(currentDirection.equals("upright")){
//                currentDirection = "right";
//                lastdirection = "upright";
//            }
//
//            else if(currentDirection.equals("right")){
//                currentDirection = "downright";
//                lastdirection = "right";
//            }
//
//            else if(currentDirection.equals("downright")){
//                currentDirection = "down";
//                lastdirection = "downright";
//            }
//            else if(currentDirection.equals("down")){
//                currentDirection = "downleft";
//                lastdirection = "down";
//            }
//            else if(currentDirection.equals("downleft")){
//                currentDirection = "left";
//                lastdirection = "down";
//            }
//            else if(currentDirection.equals("left")){
//                currentDirection = "upleft";
//                lastdirection = "left";
//            }
//            else if(currentDirection.equals("upleft")){
//                currentDirection = "straight";
//                lastdirection = "upleft";
//            }
//        }
//        else if(direction.equals("upleft")){
//            lastAction = "upleft";
//            if(currentDirection.equals("straight")){
//                currentDirection = "upleft";
//                lastdirection = "straight";
//            }
//            else if(currentDirection.equals("upright")){
//                currentDirection = "straight";
//                lastdirection = "upright";
//            }
//
//            else if(currentDirection.equals("right")){
//                currentDirection = "upright";
//                lastdirection = "right";
//            }
//            else if(currentDirection.equals("downright")){
//                currentDirection = "right";
//                lastdirection = "downright";
//            }
//            else if(currentDirection.equals("down")){
//                currentDirection = "downright";
//                lastdirection = "down";
//            }
//            else if(currentDirection.equals("downleft")){
//                currentDirection = "down";
//                lastdirection = "downleft";
//            }
//            else if(currentDirection.equals("left")){
//                currentDirection = "downleft";
//                lastdirection = "left";
//            }
//            else if(currentDirection.equals("upleft")){
//                currentDirection = "left";
//                lastdirection = "upleft";
//            }
//        }
//    }

    private void rotateView(int fromdeg, int todeg) {
        if (!lastdirection.equals(currentDirection))
        {
            Animation anim = new RotateAnimation(fromdeg, todeg, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            currectAzimuth = azimuth;
            anim.setDuration(1000);
            anim.setRepeatCount(0);
            anim.setFillAfter(true);
            mLineView.startAnimation(anim);
         }
    }

    private void dropPoints(){

//        if(direction.equals("straight")){
//            currentY =  currentY -  ((int) currentSteps * multiplier);
//        }
//
//        if(direction.equals("right")){
//           if(lastdirection.equals("straight")){
//               currentX =  currentX +  ((int) currentSteps * multiplier);
//           }
//           else if(lastdirection.equals("upright")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//
//           else if(lastdirection.equals("right")){
//               currentY =  currentY +  ((int) currentSteps * multiplier);
//
//           }
//            else if(lastdirection.equals("downright")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//           else if(lastdirection.equals("down")){
//               currentX =  currentX -  ((int) currentSteps * multiplier);
//           }
//            else if(lastdirection.equals("downleft")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//           else if(lastdirection.equals("left")){
//               currentY =  currentY -  ((int) currentSteps * multiplier);
//           }
//           else if(lastdirection.equals("upleft")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }

//        }
//        else if(direction.equals("left")){
//            if(lastdirection.equals("straight")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//            }
//           else if(lastdirection.equals("upright")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//
//            else if(lastdirection.equals("right")){
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("downright")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("down")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("downleft")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("left")){
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("upleft")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//        }
//        else if(direction.equals("upright")){
//            if(lastdirection.equals("straight")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("upright")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//            }
//
//            else if(lastdirection.equals("right")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//
//            else if(lastdirection.equals("downright")){
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("down")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("downleft")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("left")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("upleft")){
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//        }
//        else if(direction.equals("upleft")){
//            if(lastdirection.equals("straight")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("upright")){
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//
//            else if(lastdirection.equals("right")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY -  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("downright")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("down")){
//                currentX =  currentX +  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("downleft")){
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("left")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//                currentY =  currentY +  ((int) currentSteps * multiplier);
//            }
//            else if(lastdirection.equals("upleft")){
//                currentX =  currentX -  ((int) currentSteps * multiplier);
//            }
//        }

        Log.e("point","x="+currentX+",y="+currentY);
        PointF pointF = new PointF(currentX,currentY);
        points.add(pointF);
        mLineView.setPoints(pointF);
        mLineView.draw();
        //Toast.makeText(this, currentX+","+currentY, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private float getStepsCount() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        float steps = prefs.getFloat("steps", 0.0f);
        return steps;
    }

    private void storeStepsCount(Float count){
        if(count!=null) {
            SharedPreferences.Editor editor = getSharedPreferences("session", MODE_PRIVATE).edit();
            editor.putFloat("steps", count);
            editor.apply();
        }
    }
}
