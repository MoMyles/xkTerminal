/*     */ package org.codice.common.ais.message;
/*     */ 
/*     */ 
/*     */ public class Message1
/*     */   extends Message
/*     */ {
/*     */   private long navStatus;
/*     */   
/*     */   private double rot;
/*     */   
/*     */   private double sog;
/*     */   
/*     */   private boolean positionAccuracy;
/*     */   
/*     */   private double cog;
/*     */   
/*     */   private long trueHeading;
/*     */   
/*     */   private long timestamp;
/*     */   
/*     */   private long maneuverIndicator;
/*     */   
/*     */   private boolean raimFlag;
/*     */   
/*     */   private long radioStatus;
/*     */   
/*     */ 
/*     */   protected void parse(byte[] bitVector)
/*     */   {
/*  30 */     double rot = bin2dec(bitVector, 42, 49);
/*  31 */     setMessageType(bin2dec(bitVector, 0, 5));
/*  32 */     setRepeatIndicator(bin2dec(bitVector, 6, 7));
/*  33 */     setMmsi((int)bin2dec(bitVector, 8, 37));
/*     */     
/*  35 */     setNavStatus(bin2dec(bitVector, 38, 41));
/*  36 */     setRot(sign(rot) * (rot / 4.733D) * (rot / 4.733D));
/*  37 */     setSog(bin2dec(bitVector, 50, 59) / 10.0D);
/*  38 */     setPositionAccuracy(bin2bool(bitVector, 60));
/*  39 */     setLon(bin2dec(bitVector, 61, 88, true) / 600000.0D);
/*  40 */     setLat(bin2dec(bitVector, 89, 115, true) / 600000.0D);
/*  41 */     setCog(bin2dec(bitVector, 116, 127) / 10.0D);
/*  42 */     setTrueHeading(bin2dec(bitVector, 128, 136));
/*  43 */     setTimestamp(bin2dec(bitVector, 137, 142));
/*  44 */     setManeuverIndicator(bin2dec(bitVector, 143, 144));
/*  45 */     setRaimFlag(bin2bool(bitVector, 148));
/*  46 */     setRadioStatus(bin2dec(bitVector, 149, 167));
/*     */   }
/*     */   
/*     */ 
/*     */   public boolean hasLocationData()
/*     */   {
/*  52 */     return true;
/*     */   }
/*     */   
/*     */   public long getNavStatus() {
/*  56 */     return this.navStatus;
/*     */   }
/*     */   
/*     */   public void setNavStatus(long navStatus) {
/*  60 */     this.navStatus = navStatus;
/*     */   }
/*     */   
/*     */   public double getRot() {
/*  64 */     return this.rot;
/*     */   }
/*     */   
/*     */   public void setRot(double rot) {
/*  68 */     this.rot = rot;
/*     */   }
/*     */   
/*     */   public double getSog() {
/*  72 */     return this.sog;
/*     */   }
/*     */   
/*     */   public void setSog(double sog) {
/*  76 */     this.sog = sog;
/*     */   }
/*     */   
/*     */   public boolean getPositionAccuracy() {
/*  80 */     return this.positionAccuracy;
/*     */   }
/*     */   
/*     */   public void setPositionAccuracy(boolean positionAccuracy) {
/*  84 */     this.positionAccuracy = positionAccuracy;
/*     */   }
/*     */   
/*     */   public double getCog() {
/*  88 */     return this.cog;
/*     */   }
/*     */   
/*     */   public void setCog(double cog) {
/*  92 */     this.cog = cog;
/*     */   }
/*     */   
/*     */   public long getTrueHeading() {
/*  96 */     return this.trueHeading;
/*     */   }
/*     */   
/*     */   public void setTrueHeading(long trueHeading) {
/* 100 */     this.trueHeading = trueHeading;
/*     */   }
/*     */   
/*     */   public long getTimestamp() {
/* 104 */     return this.timestamp;
/*     */   }
/*     */   
/*     */   public void setTimestamp(long timestamp) {
/* 108 */     this.timestamp = timestamp;
/*     */   }
/*     */   
/*     */   public long getManeuverIndicator() {
/* 112 */     return this.maneuverIndicator;
/*     */   }
/*     */   
/*     */   public void setManeuverIndicator(long maneuverIndicator) {
/* 116 */     this.maneuverIndicator = maneuverIndicator;
/*     */   }
/*     */   
/*     */   public boolean isRaimFlag() {
/* 120 */     return this.raimFlag;
/*     */   }
/*     */   
/*     */   public void setRaimFlag(boolean raimFlag) {
/* 124 */     this.raimFlag = raimFlag;
/*     */   }
/*     */   
/*     */   public long getRadioStatus() {
/* 128 */     return this.radioStatus;
/*     */   }
/*     */   
/*     */   public void setRadioStatus(long radioStatus) {
/* 132 */     this.radioStatus = radioStatus;
/*     */   }
/*     */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message1.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */