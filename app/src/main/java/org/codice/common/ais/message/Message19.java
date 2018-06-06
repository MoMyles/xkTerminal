/*     */ package org.codice.common.ais.message;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Message19
/*     */   extends Message
/*     */ {
/*     */   private double sog;
/*     */   
/*     */ 
/*     */   private double cog;
/*     */   
/*     */ 
/*     */   private long trueHeading;
/*     */   
/*     */ 
/*     */   private String vesselName;
/*     */   
/*     */ 
/*     */   private long vesselTypeInt;
/*     */   
/*     */ 
/*     */   private long length;
/*     */   
/*     */ 
/*     */   private long width;
/*     */   
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
/*  40 */     this.vesselName = bin2SixBitAISAscii(bitVector, 143, 262);
/*  41 */     this.vesselTypeInt = bin2dec(bitVector, 263, 270);
/*  42 */     this.length = (bin2dec(bitVector, 271, 279) + bin2dec(bitVector, 280, 288));
/*  43 */     this.width = (bin2dec(bitVector, 289, 294) + bin2dec(bitVector, 295, 300));
/*     */   }
/*     */   
/*     */   public boolean hasLocationData()
/*     */   {
/*  48 */     return true;
/*     */   }
/*     */   
/*     */   public double getSog()
/*     */   {
/*  53 */     return this.sog;
/*     */   }
/*     */   
/*     */   public void setSog(double sog) {
/*  57 */     this.sog = sog;
/*     */   }
/*     */   
/*     */   public double getCog() {
/*  61 */     return this.cog;
/*     */   }
/*     */   
/*     */   public void setCog(double cog) {
/*  65 */     this.cog = cog;
/*     */   }
/*     */   
/*     */   public long getTrueHeading() {
/*  69 */     return this.trueHeading;
/*     */   }
/*     */   
/*     */   public void setTrueHeading(long trueHeading) {
/*  73 */     this.trueHeading = trueHeading;
/*     */   }
/*     */   
/*     */   public String getVesselName() {
/*  77 */     return this.vesselName;
/*     */   }
/*     */   
/*     */   public void setVesselName(String vesselName) {
/*  81 */     this.vesselName = vesselName;
/*     */   }
/*     */   
/*     */   public long getVesselTypeInt() {
/*  85 */     return this.vesselTypeInt;
/*     */   }
/*     */   
/*     */   public void setVesselTypeInt(long vesselTypeInt) {
/*  89 */     this.vesselTypeInt = vesselTypeInt;
/*     */   }
/*     */   
/*     */   public long getLength() {
/*  93 */     return this.length;
/*     */   }
/*     */   
/*     */   public void setLength(long length) {
/*  97 */     this.length = length;
/*     */   }
/*     */   
/*     */   public long getWidth() {
/* 101 */     return this.width;
/*     */   }
/*     */   
/*     */   public void setWidth(long width) {
/* 105 */     this.width = width;
/*     */   }
/*     */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message19.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */