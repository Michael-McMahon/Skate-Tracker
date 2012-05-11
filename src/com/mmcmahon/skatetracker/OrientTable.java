package com.mmcmahon.skatetracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class OrientTable extends Activity implements SensorEventListener
{  
   private TextView xText, yText, zText;

   
   private SensorManager sensorMan;//Allows this activity to listen to accelerometer
   private Sensor accelerometer;
   
   private String eTag = "#" + this.getClass().getName() + "#";
   
   public void onCreate(Bundle sis)
   {
      super.onCreate(sis);
      setContentView(R.layout.orientation);
      setTitle("Orientation");
      
      configureButton();
      xText = (TextView)findViewById(R.id.tv_orientationX);
      yText = (TextView)findViewById(R.id.tv_orientationY);
      zText = (TextView)findViewById(R.id.tv_orientationZ);
      
      sensorMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
      accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ORIENTATION);
   }
   
   public void onResume()
   {
      super.onResume();
      //Register listener
      sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
   }
   
   public void onPause()
   {
      super.onPause();
      //Disable battery consuming listener
      sensorMan.unregisterListener(this);
   }
   
   public void onAccuracyChanged(Sensor arg0, int arg1)
   {
      // TODO Auto-generated method stub
      
   }

   public void onSensorChanged(SensorEvent event)
   {      
      xText.setText(Float.toString(event.values[0]));
      yText.setText(Float.toString(event.values[1]));
      zText.setText(Float.toString(event.values[2]));
   }
  
  private void configureButton()
  {
     Button b = (Button)findViewById(R.id.btn_orientationFlip);
     
     b.setText("View Acceleration");
     
     b.setOnClickListener(
           new OnClickListener()
           {
            public void onClick(View arg0)
            {
               startActivity(new Intent(OrientTable.this, AccelTable.class));
            }
           });
  }
}
