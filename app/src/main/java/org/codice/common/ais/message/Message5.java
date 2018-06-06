/*     */ package org.codice.common.ais.message;
/*     */ 
/*     */ 
/*     */ public class Message5
/*     */   extends Message
/*     */ {
/*     */   private long aisVersion;
/*     */   
/*     */   private long imoNumber;
/*     */   
/*     */   private String callSign;
/*     */   
/*     */   private String vesselName;
/*     */   
/*     */   private long vesselTypeInt;
/*     */   
/*     */   private long length;
/*     */   
/*     */   private long width;
/*     */   
/*     */   private long typeOfEpfd;
/*     */   
/*     */   private long etaMonth;
/*     */   
/*     */   private long etaDay;
/*     */   
/*     */   private long etaHour;
/*     */   
/*     */   private long etaMinute;
/*     */   
/*     */   private long draught;
/*     */   
/*     */   private String destination;
/*     */   
/*     */   private boolean dte;
/*     */   
/*     */ 
/*     */   protected void parse(byte[] bitVector)
/*     */   {
/*  40 */     setMessageType(bin2dec(bitVector, 0, 5));
/*  41 */     setRepeatIndicator(bin2dec(bitVector, 6, 7));
/*  42 */     setMmsi((int)bin2dec(bitVector, 8, 37));
/*     */     
/*  44 */     this.aisVersion = bin2dec(bitVector, 38, 39);
/*  45 */     this.imoNumber = bin2dec(bitVector, 40, 69);
/*  46 */     this.callSign = stripAisAsciiGarbage(bin2SixBitAISAscii(bitVector, 70, 111));
/*  47 */     this.vesselName = stripAisAsciiGarbage(bin2SixBitAISAscii(bitVector, 112, 231));
/*  48 */     this.vesselTypeInt = bin2dec(bitVector, 232, 239);
/*  49 */     this.length = (bin2dec(bitVector, 240, 248) + bin2dec(bitVector, 249, 257));
/*  50 */     this.width = (bin2dec(bitVector, 258, 263) + bin2dec(bitVector, 264, 269));
/*  51 */     this.typeOfEpfd = bin2dec(bitVector, 270, 273);
/*  52 */     this.etaMonth = bin2dec(bitVector, 274, 277);
/*  53 */     this.etaDay = bin2dec(bitVector, 278, 282);
/*  54 */     this.etaHour = bin2dec(bitVector, 283, 287);
/*  55 */     this.etaMinute = bin2dec(bitVector, 288, 293);
/*  56 */     this.draught = bin2dec(bitVector, 294, 301);
/*  57 */     this.destination = stripAisAsciiGarbage(bin2SixBitAISAscii(bitVector, 302, 421));
/*  58 */     this.dte = bin2bool(bitVector, 422);
/*     */   }
/*     */   
/*     */   public long getAisVersion() {
/*  62 */     return this.aisVersion;
/*     */   }
/*     */   
/*     */   public void setAisVersion(long aisVersion) {
/*  66 */     this.aisVersion = aisVersion;
/*     */   }
/*     */   
/*     */   public long getImoNumber() {
/*  70 */     return this.imoNumber;
/*     */   }
/*     */   
/*     */   public void setImoNumber(long imoNumber) {
/*  74 */     this.imoNumber = imoNumber;
/*     */   }
/*     */   
/*     */   public String getCallSign() {
/*  78 */     return this.callSign;
/*     */   }
/*     */   
/*     */   public void setCallSign(String callSign) {
/*  82 */     this.callSign = callSign;
/*     */   }
/*     */   
/*     */   public String getVesselName() {
/*  86 */     return this.vesselName;
/*     */   }
/*     */   
/*     */   public void setVesselName(String vesselName) {
/*  90 */     this.vesselName = vesselName;
/*     */   }
/*     */   
/*     */   public long getVesselTypeInt() {
/*  94 */     return this.vesselTypeInt;
/*     */   }
/*     */   
/*     */   public void setVesselTypeInt(long vesselTypeInt) {
/*  98 */     this.vesselTypeInt = vesselTypeInt;
/*     */   }
/*     */   
/*     */   public long getLength() {
/* 102 */     return this.length;
/*     */   }
/*     */   
/*     */   public void setLength(long length) {
/* 106 */     this.length = length;
/*     */   }
/*     */   
/*     */   public long getWidth() {
/* 110 */     return this.width;
/*     */   }
/*     */   
/*     */   public void setWidth(long width) {
/* 114 */     this.width = width;
/*     */   }
/*     */   
/*     */   public long getTypeOfEpfd() {
/* 118 */     return this.typeOfEpfd;
/*     */   }
/*     */   
/*     */   public void setTypeOfEpfd(long typeOfEpfd) {
/* 122 */     this.typeOfEpfd = typeOfEpfd;
/*     */   }
/*     */   
/*     */   public long getEtaMonth() {
/* 126 */     return this.etaMonth;
/*     */   }
/*     */   
/*     */   public void setEtaMonth(long etaMonth) {
/* 130 */     this.etaMonth = etaMonth;
/*     */   }
/*     */   
/*     */   public long getEtaDay() {
/* 134 */     return this.etaDay;
/*     */   }
/*     */   
/*     */   public void setEtaDay(long etaDay) {
/* 138 */     this.etaDay = etaDay;
/*     */   }
/*     */   
/*     */   public long getEtaHour() {
/* 142 */     return this.etaHour;
/*     */   }
/*     */   
/*     */   public void setEtaHour(long etaHour) {
/* 146 */     this.etaHour = etaHour;
/*     */   }
/*     */   
/*     */   public long getEtaMinute() {
/* 150 */     return this.etaMinute;
/*     */   }
/*     */   
/*     */   public void setEtaMinute(long etaMinute) {
/* 154 */     this.etaMinute = etaMinute;
/*     */   }
/*     */   
/*     */   public long getDraught() {
/* 158 */     return this.draught;
/*     */   }
/*     */   
/*     */   public void setDraught(long draught) {
/* 162 */     this.draught = draught;
/*     */   }
/*     */   
/*     */   public String getDestination() {
/* 166 */     return this.destination;
/*     */   }
/*     */   
/*     */   public void setDestination(String destination) {
/* 170 */     this.destination = destination;
/*     */   }
/*     */   
/*     */   public boolean isDte() {
/* 174 */     return this.dte;
/*     */   }
/*     */   
/*     */   public void setDte(boolean dte) {
/* 178 */     this.dte = dte;
/*     */   }
/*     */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message5.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */