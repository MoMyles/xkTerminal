/*     */ package org.codice.common.ais.message;
/*     */ 
/*     */ 
/*     */ public class Message18
/*     */   extends Message
/*     */ {
/*     */   private double sog;
/*     */   
/*     */   private double cog;
/*     */   
/*     */   private long trueHeading;
/*     */   
/*     */   private long timestamp;
/*     */   
/*     */   private boolean csUnit;
/*     */   
/*     */   private boolean displayFlag;
/*     */   
/*     */   private boolean dscFlag;
/*     */   
/*     */   private boolean bandFlag;
/*     */   
/*     */   private boolean message22Flag;
/*     */   
/*     */   private boolean assigned;
/*     */   private boolean raimFlag;
/*     */   private long radioStatus;
/*     */   
/*     */   protected void parse(byte[] bitVector)
/*     */   {
/*  31 */     setMessageType(bin2dec(bitVector, 0, 5));
/*  32 */     setRepeatIndicator(bin2dec(bitVector, 6, 7));
/*  33 */     setMmsi((int)bin2dec(bitVector, 8, 37));
/*     */     
/*  35 */     this.sog = (bin2dec(bitVector, 46, 55) / 10.0D);
/*  36 */     setLon(bin2dec(bitVector, 57, 84, true) / 600000.0D);
/*  37 */     setLat(bin2dec(bitVector, 85, 111, true) / 600000.0D);
/*  38 */     this.cog = (bin2dec(bitVector, 112, 123) / 10.0D);
/*  39 */     this.trueHeading = bin2dec(bitVector, 124, 132);
/*  40 */     this.timestamp = bin2dec(bitVector, 133, 138);
/*  41 */     this.csUnit = bin2bool(bitVector, 141);
/*  42 */     this.displayFlag = bin2bool(bitVector, 142);
/*  43 */     this.dscFlag = bin2bool(bitVector, 143);
/*  44 */     this.bandFlag = bin2bool(bitVector, 144);
/*  45 */     this.message22Flag = bin2bool(bitVector, 145);
/*  46 */     this.assigned = bin2bool(bitVector, 146);
/*  47 */     this.raimFlag = bin2bool(bitVector, 147);
/*  48 */     this.radioStatus = bin2dec(bitVector, 148, 167);
/*     */   }
/*     */   
/*     */   public boolean hasLocationData()
/*     */   {
/*  53 */     return true;
/*     */   }
/*     */   
/*     */   public double getSog() {
/*  57 */     return this.sog;
/*     */   }
/*     */   
/*     */   public void setSog(double sog) {
/*  61 */     this.sog = sog;
/*     */   }
/*     */   
/*     */   public double getCog() {
/*  65 */     return this.cog;
/*     */   }
/*     */   
/*     */   public void setCog(double cog) {
/*  69 */     this.cog = cog;
/*     */   }
/*     */   
/*     */   public long getTrueHeading() {
/*  73 */     return this.trueHeading;
/*     */   }
/*     */   
/*     */   public void setTrueHeading(long trueHeading) {
/*  77 */     this.trueHeading = trueHeading;
/*     */   }
/*     */   
/*     */   public long getTimestamp() {
/*  81 */     return this.timestamp;
/*     */   }
/*     */   
/*     */   public void setTimestamp(long timestamp) {
/*  85 */     this.timestamp = timestamp;
/*     */   }
/*     */   
/*     */   public boolean isCsUnit() {
/*  89 */     return this.csUnit;
/*     */   }
/*     */   
/*     */   public void setCsUnit(boolean csUnit) {
/*  93 */     this.csUnit = csUnit;
/*     */   }
/*     */   
/*     */   public boolean isDisplayFlag() {
/*  97 */     return this.displayFlag;
/*     */   }
/*     */   
/*     */   public void setDisplayFlag(boolean displayFlag) {
/* 101 */     this.displayFlag = displayFlag;
/*     */   }
/*     */   
/*     */   public boolean isDscFlag() {
/* 105 */     return this.dscFlag;
/*     */   }
/*     */   
/*     */   public void setDscFlag(boolean dscFlag) {
/* 109 */     this.dscFlag = dscFlag;
/*     */   }
/*     */   
/*     */   public boolean isBandFlag() {
/* 113 */     return this.bandFlag;
/*     */   }
/*     */   
/*     */   public void setBandFlag(boolean bandFlag) {
/* 117 */     this.bandFlag = bandFlag;
/*     */   }
/*     */   
/*     */   public boolean isMessage22Flag() {
/* 121 */     return this.message22Flag;
/*     */   }
/*     */   
/*     */   public void setMessage22Flag(boolean message22Flag) {
/* 125 */     this.message22Flag = message22Flag;
/*     */   }
/*     */   
/*     */   public boolean isAssigned() {
/* 129 */     return this.assigned;
/*     */   }
/*     */   
/*     */   public void setAssigned(boolean assigned) {
/* 133 */     this.assigned = assigned;
/*     */   }
/*     */   
/*     */   public boolean isRaimFlag() {
/* 137 */     return this.raimFlag;
/*     */   }
/*     */   
/*     */   public void setRaimFlag(boolean raimFlag) {
/* 141 */     this.raimFlag = raimFlag;
/*     */   }
/*     */   
/*     */   public long getRadioStatus() {
/* 145 */     return this.radioStatus;
/*     */   }
/*     */   
/*     */   public void setRadioStatus(long radioStatus) {
/* 149 */     this.radioStatus = radioStatus;
/*     */   }
/*     */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message18.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */