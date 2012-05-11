package com.mmcmahon.skatetracker;

public class RowData
{
   long t;
   float values[] = new float[3];
   float vAcc;
   boolean isAccel;//True if this is acceleration data
   
   public RowData(long t, float values[], float vAcc, boolean isAccel)
   {
      this.t = t;
      System.arraycopy(values, 0, this.values, 0, 3);
      this.vAcc = vAcc;
      this.isAccel = isAccel;
   }
   
   public long getTime()
   {
      return t;
   }
   
   public float[] getValues()
   {
      return values;
   }
}