package com.mmcmahon.skatetracker;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class EventAnalyzer 
{
   private float delta[] = {0, 0, 0};
   private float accVals[] = {0, 0, 0};
   private float magVals[] = {0, 0, 0};
   float orientation[] = {0, 0, 0};

   /**
    * Records a new accelerometer reading and returns the change in value.
    * 
    * @param event Encapsulates a new set of acceleration values.
    * @return An array of size 3, containing the change in values on the x, y,
    *    and z axes, in that order.
    */
   public float[] readAccEvent(SensorEvent event)
   {
      float prevX = accVals[0];
      float prevY = accVals[1];
      float prevZ = accVals[2];
      System.arraycopy(event.values, 0, accVals, 0, 3);//Copy new values
      
      delta[0] = accVals[0] - prevX;
      delta[1] = accVals[1] - prevY;
      delta[2] = accVals[2] - prevZ;
            
      return delta;
   }
   
   public float[] readMagEvent(SensorEvent event)
   {
      float rotation[] = new float[9];//The rotation matrix, R
      System.arraycopy(event.values, 0, magVals, 0, 3);

      if(!SensorManager.getRotationMatrix(rotation, null, accVals, magVals))
      {
         //Calculation of rotation matrix failed!
         orientation[0] = 0;
         orientation[2] = 0;
         orientation[1] = 0;         
         return orientation;
      }
      
      SensorManager.getOrientation(rotation, orientation);
      
      //Convert radians to degrees
      orientation[0] = (float) Math.toDegrees(orientation[0]);
      orientation[1] = (float) Math.toDegrees(orientation[1]);
      orientation[2] = (float) Math.toDegrees(orientation[2]);

      return orientation;
   }
   
   /**
    * Computes the euclidean magnitude of the last change in acceleration.
    * @return The magnitude of change in acceleration.
    */
   public float euclideanDeltaAcc()
   {
      float squaredSum = (delta[0] * delta[0]) + (delta[1] * delta[1]) +
            (delta[2] * delta[2]);
      return (float)Math.sqrt(squaredSum);
   }
   
   /**
    * Computes the euclidean magnitude of the last acceleration.
    * @return The magnitude of acceleration.
    */
   public float euclideanAcc()
   {
      float squaredSum = (accVals[0] * accVals[0]) + (accVals[1] * accVals[1]) + 
            (accVals[2] * accVals[2]);
      return (float)Math.sqrt(squaredSum);
   }
   
   /**
    * Computes the device's acceleration away from the ground.
    * @return The magnitude of accerlation away from the ground.
    */
   public float verticalAcc()
   {
      //Calculate the vertical component of the 3 acceleration vectors
      float x = (float) (accVals[0] * Math.sin(orientation[2]));//Xacc * sin(roll)
      float y = (float) (accVals[1] * Math.sin(orientation[1]));//Yacc * sin(pitch)
      float z = (float) (accVals[2] * Math.cos(orientation[1]));//Zacc * cos(pitch)
      
      return x + y + z;
   }
   
   public float verticalDeltaAcc()
   {
      //Calculate the vertical component of the 3 acceleration vectors
      float x = (float) (delta[0] * Math.sin(orientation[2]));//Xacc * sin(roll)
      float y = (float) (delta[1] * Math.sin(orientation[1]));//Yacc * sin(pitch)
      float z = (float) (delta[2] * Math.cos(orientation[1]) * 
            Math.cos(orientation[2]));//Zacc * -cos(pitch) * cos(roll)
      
      return x + y + z;
   }
}
