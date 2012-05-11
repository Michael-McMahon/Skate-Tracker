package com.mmcmahon.skatetracker;

import java.text.SimpleDateFormat;
import java.util.Vector;
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

public class AccelTable extends Activity implements SensorEventListener
{
   private final float NOISE = (float)2;//Adjusts sensitivity to motion
   private final int MAX_ROWS = 50;//Maximum rows allowed in table
   
   private TextView tvYaw, tvPitch, tvRoll, tvVertAcc;
   private ScrollView scroller;
   private TableLayout table;
   private int rowCount;
   private Vector<RowData> data;
   private SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss:SS");
   private boolean showAccel = true;

   
   private SensorManager sensorMan;//Allows this activity to listen to accelerometer
   private Sensor accelerometer, geomagnometer;
   private EventAnalyzer analyzer;//Analyzes motion events
   
   private float orientation[] = {0, 0, 0};
   
   private String eTag = "#" + this.getClass().getName() + "#";
   
   public void onCreate(Bundle sis)
   {
      super.onCreate(sis);
      setContentView(R.layout.record);
      setTitle("Accerlation");
      
      tvYaw = (TextView)findViewById(R.id.tv_recordYaw);
      tvPitch = (TextView)findViewById(R.id.tv_recordPitch);
      tvRoll = (TextView)findViewById(R.id.tv_recordRoll);
      tvVertAcc = (TextView)findViewById(R.id.tv_recordVertAcc);
      
      scroller = (ScrollView)findViewById(R.id.sv_recordRoot);
      table = (TableLayout)findViewById(R.id.tl_recordTable);
      rowCount = 0;
      data = new Vector<RowData>();
      restoreTable();
      
      configureButton();
      
      analyzer = new EventAnalyzer();
      sensorMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
      accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      geomagnometer = sensorMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
   }
   
   
   public void onResume()
   {
      super.onResume();
      //Register listener
      sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
      sensorMan.registerListener(this, geomagnometer, SensorManager.SENSOR_DELAY_NORMAL);
   }
   
   public void onPause()
   {
      super.onPause();
      //Disable battery consuming listener
      sensorMan.unregisterListener(this);
   }
   
   public Object onRetainNonConfigurationInstance()
   {
      return data;
   }
   
   public void onAccuracyChanged(Sensor arg0, int arg1)
   {
      // TODO Auto-generated method stub
      
   }

   public void onSensorChanged(SensorEvent event)
   {
      long timeMs = System.currentTimeMillis();
      float delta[];
      float vAcc;
      
      switch(event.sensor.getType())
      {
         case Sensor.TYPE_ACCELEROMETER:
            delta = analyzer.readAccEvent(event);
            vAcc = analyzer.verticalDeltaAcc();
            
            //Determine if acceleration is negligible
            if(analyzer.euclideanDeltaAcc() > NOISE)
            {
               //Update vertical acceleration display
               updateVerticalAcc(vAcc);
               addData(timeMs, delta, vAcc, true);//Add acceleration entry to table
               addData(timeMs, orientation, vAcc, false);//Add an entry for the current orientation
            }
            return;
         
         case Sensor.TYPE_MAGNETIC_FIELD:
            orientation = analyzer.readMagEvent(event);
            //Update orientation display
            updateOrientation();
            return;
         
         default:
            return;
      }
   }
   
   private void updateOrientation()
   {
      tvYaw.setText(Float.toString(orientation[0]));
      tvPitch.setText(Float.toString(orientation[1]));
      tvRoll.setText(Float.toString(orientation[2]));
   }
   
   /**
    * Updates the vertical acceleration display.
    * @param vAcc The value to display.
    */
   private void updateVerticalAcc(float vAcc)
   {
      tvVertAcc.setText(Float.toString(vAcc));
   }
   
   public void addData(long time, float delta[], float vAcc, boolean isAccel)
   {
      data.add(new RowData(time, delta, vAcc, isAccel));

      if(showAccel == isAccel)
      {
         addRow(time, delta, vAcc);
      }
   }
      /**
    * Adds a row to the table.
    * @param time The time value for the row
    * @param delta The x, y, and z values for the row
    */
  public void addRow(long time, float delta[], float vAcc)
  {
     TextView tCol, xCol, yCol, zCol, vAccCol;
     TableRow row = (TableRow)View.inflate(this, R.layout.record_row, null);

     //Every other row is a lighter color
     if(0 == (rowCount % 2))
     {
        row.setBackgroundColor(0xFF202020);
     }
     rowCount++;
     
     tCol = (TextView)row.findViewById(R.id.tv_rrTime);
     xCol = (TextView)row.findViewById(R.id.tv_rrX);
     yCol = (TextView)row.findViewById(R.id.tv_rrY);
     zCol = (TextView)row.findViewById(R.id.tv_rrZ);
     vAccCol = (TextView)row.findViewById(R.id.tv_rrVacc);
     
     tCol.setText(formatter.format(new Date(time)));//Print time in a formatted string
     xCol.setText(String.format("%.3f", delta[0]));
     yCol.setText(String.format("%.3f", delta[1]));
     zCol.setText(String.format("%.3f", delta[2]));
     vAccCol.setText(String.format("%.3f", vAcc));
     
     if(rowCount > MAX_ROWS)//Rows removed in FIFO order
     {
        table.removeViewAt(0);
        data.remove(0);//Remove acceleration and orientation pair
        data.remove(0);
     }
     
     table.addView(row);
     scroller.post(
        new Runnable()
        {
            public void run()
            {
               scroller.fullScroll(View.FOCUS_DOWN);
            }  
        });     
  }
  
  private void restoreTable()
  {
     final Vector<RowData> oldData = 
           (Vector<RowData>)getLastNonConfigurationInstance();

     data = new Vector<RowData>();
     
     if(oldData == null)
     {
        return;
     }
     
     for(RowData d : oldData)
     {
        addData(d.t, d.values, d.vAcc, d.isAccel);
     }    
  }
  
  private void switchTables()
  {
     showAccel = !showAccel;
     table.removeAllViews();
     rowCount = 0;
     
     for(RowData d : data)
     {
        if(d.isAccel == showAccel)
        {
           addRow(d.t, d.values, d.vAcc);
        }
     }       
  }
  
  private void configureButton()
  {
     final Button b = (Button)findViewById(R.id.btn_recordFlip);
     
     b.setText("View Orientation");
     
     b.setOnClickListener(
           new OnClickListener()
           {
            public void onClick(View arg0)
            {
               if(showAccel)
               {
                  b.setText("View Acceleration");
               }
               else
               {
                  b.setText("View Orientation");
               }

               switchTables();
               //startActivity(new Intent(AccelTable.this, OrientTable.class));
            }
           });
  }
}
