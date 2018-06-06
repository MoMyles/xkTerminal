/*     */ package org.codice.common.ais.message;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public abstract class Message
/*     */ {
/*     */   private long messageType;
/*     */   private long repeatIndicator;
/*     */   private long mmsi;
/*     */   private double lon;
/*     */   private double lat;
/*     */   
/*     */   public static Message parseMessage(List<Byte> bitList)
/*     */     throws UnknownMessageException
/*     */   {
/*  29 */     byte[] bitVector = ByteListTobyteArray(bitList);
/*  30 */     int messageType = (int)bin2Dec(bitVector, 0);
/*     */     
/*  32 */     Message message = null;
/*  33 */     switch (messageType)
/*     */     {
/*     */     case 1: 
/*     */     case 2: 
/*     */     case 3: 
/*  38 */       message = new Message1();
/*  39 */       break;
/*     */     case 4: 
/*     */       break;
/*     */     
/*     */     case 5: 
/*  44 */       message = new Message5();
/*  45 */       break;
/*     */     case 6: 
/*  47 */       message = new Message6();
/*  48 */       break;
/*     */     case 8: 
/*  50 */       message = new Message8();
/*     */       
/*  52 */       break;
/*     */     
/*     */ 
/*     */     case 18: 
/*  56 */       message = new Message18();
/*     */       
/*  58 */       break;
/*     */     case 19: 
/*  60 */       message = new Message19();
/*     */       
/*     */ 
/*     */ 
/*  64 */       break;
/*     */     case 7: case 9: case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: default: 
/*  66 */       throw new UnknownMessageException("Message of type " + messageType + " is currently unsupported.");
/*     */     }
/*  68 */     message.parse(bitVector);
/*  69 */     return message;
/*     */   }
/*     */   
/*     */ 
/*     */   protected abstract void parse(byte[] paramArrayOfByte);
/*     */   
/*     */ 
/*     */   static int sign(double v)
/*     */   {
/*  78 */     return v < 0.0D ? -1 : 1;
/*     */   }
/*     */   
/*     */   static byte[] ByteListTobyteArray(List<Byte> bigBytes) {
/*  82 */     byte[] bytes = new byte[bigBytes.size()];
/*  83 */     for (int ii = 0; ii < bytes.length; ii++) {
/*  84 */       bytes[ii] = ((Byte)bigBytes.get(ii)).byteValue();
/*     */     }
/*  86 */     return bytes;
/*     */   }
/*     */   
/*     */   static double bin2Dec(byte[] bitVector, int offset)
/*     */   {
/*  91 */     double result = 0.0D;
/*     */     
/*  93 */     for (int ii = offset; ii <= offset + 5; ii++) {
/*  94 */       result = result * 2.0D + bitVector[ii];
/*     */     }
/*  96 */     return result;
/*     */   }
/*     */   
/*     */   static boolean bin2bool(byte[] bitVector, int offset)
/*     */   {
/* 101 */     return bitVector[offset] > 0;
/*     */   }
/*     */   
/*     */   static long bin2dec(byte[] bitVector, int startBit, int endBit) {
/* 105 */     return bin2dec(bitVector, startBit, endBit, false);
/*     */   }
/*     */   
/*     */   static long bin2dec(byte[] bitVector, int startBit, int endBit, boolean isSigned)
/*     */   {
/* 110 */     long result = 0L;
/*     */     
/* 112 */     if (isSigned)
/*     */     {
/* 114 */       boolean firstTrueFlag = false;
/* 115 */       byte[] binTC = new byte[endBit - startBit];
/*     */       
/* 117 */       if (bitVector[startBit] > 0)
/*     */       {
/* 119 */         int TCind = binTC.length - 1;
/*     */         
/* 121 */         for (int p = endBit; p > startBit; p--) {
/* 122 */           if (firstTrueFlag)
/*     */           {
/* 124 */             binTC[TCind] = (byte) (bitVector[p] > 0 ? 0 : 1);
/*     */           }
/*     */           else
/*     */           {
/* 128 */             binTC[TCind] = bitVector[p];
/* 129 */             if (bitVector[p] >= 0) firstTrueFlag = true;
/*     */           }
/* 131 */           TCind--;
/*     */         }
/*     */         
/* 134 */         for (int ii = 0; ii <= binTC.length - 1; ii++) {
/* 135 */           result = result * 2L + binTC[ii];
/*     */         }
/* 137 */         result = -result;
/*     */       }
/*     */       else
/*     */       {
/* 141 */         for (int ii = startBit + 1; ii <= endBit; ii++) {
/* 142 */           result = result * 2L + bitVector[ii];
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 148 */       for (int ii = startBit; ii <= endBit; ii++) {
/* 149 */         result = result * 2L + bitVector[ii];
/*     */       }
/*     */     }
/*     */     
/* 153 */     return result;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   static String stripAisAsciiGarbage(Object inputValue)
/*     */   {
/* 160 */     return inputValue.toString().replaceAll("@.*", "").trim();
/*     */   }
/*     */   
/*     */ 
/*     */   static String bin2SixBitAISAscii(byte[] bitVector, int startBit, int endBit)
/*     */   {
/* 166 */     String result = "";
/* 167 */     int length = (endBit - startBit) / 6;
/* 168 */     int startBitIdx = startBit;
/* 169 */     for (int ii = 0; ii < length; ii++) {
/* 170 */       int v = (int)bin2dec(bitVector, startBitIdx, startBitIdx + 5);
/* 171 */       switch (v)
/*     */       {
/*     */       case 0: 
/* 174 */         result = result + "@";
/* 175 */         break;
/*     */       case 1: 
/*     */       case 2: 
/*     */       case 3: 
/*     */       case 4: 
/*     */       case 5: 
/*     */       case 6: 
/*     */       case 7: 
/*     */       case 8: 
/*     */       case 9: 
/*     */       case 10: 
/*     */       case 11: 
/*     */       case 12: 
/*     */       case 13: 
/*     */       case 14: 
/*     */       case 15: 
/*     */       case 16: 
/*     */       case 17: 
/*     */       case 18: 
/*     */       case 19: 
/*     */       case 20: 
/*     */       case 21: 
/*     */       case 22: 
/*     */       case 23: 
/*     */       case 24: 
/*     */       case 25: 
/*     */       case 26: 
/* 202 */         result = result + String.valueOf((char)(65 + v - 1));
/* 203 */         break;
/*     */       case 48: 
/*     */       case 49: 
/*     */       case 50: 
/*     */       case 51: 
/*     */       case 52: 
/*     */       case 53: 
/*     */       case 54: 
/*     */       case 55: 
/*     */       case 56: 
/* 213 */         result = result + String.valueOf((char)(48 + v - 48));
/* 214 */         break;
/*     */       case 27: 
/* 216 */         result = result + "[";
/* 217 */         break;
/*     */       case 28: 
/* 219 */         result = result + "\\";
/* 220 */         break;
/*     */       case 29: 
/* 222 */         result = result + "]";
/* 223 */         break;
/*     */       case 30: 
/* 225 */         result = result + "\\^";
/* 226 */         break;
/*     */       case 31: 
/* 228 */         result = result + "\\_";
/* 229 */         break;
/*     */       case 32: 
/* 231 */         result = result + " ";
/* 232 */         break;
/*     */       case 33: 
/* 234 */         result = result + "!";
/* 235 */         break;
/*     */       case 34: 
/* 237 */         result = result + "\"";
/* 238 */         break;
/*     */       case 35: 
/* 240 */         result = result + "\\#";
/* 241 */         break;
/*     */       case 36: 
/* 243 */         result = result + "$";
/* 244 */         break;
/*     */       case 37: 
/* 246 */         result = result + "%";
/* 247 */         break;
/*     */       case 38: 
/* 249 */         result = result + "&";
/* 250 */         break;
/*     */       case 39: 
/* 252 */         result = result + "\\";
/* 253 */         break;
/*     */       case 40: 
/* 255 */         result = result + "(";
/* 256 */         break;
/*     */       case 41: 
/* 258 */         result = result + ")";
/* 259 */         break;
/*     */       case 42: 
/* 261 */         result = result + "\\*";
/* 262 */         break;
/*     */       case 43: 
/* 264 */         result = result + "\\+";
/* 265 */         break;
/*     */       case 44: 
/* 267 */         result = result + ",";
/* 268 */         break;
/*     */       case 45: 
/* 270 */         result = result + "-";
/* 271 */         break;
/*     */       case 46: 
/* 273 */         result = result + ".";
/* 274 */         break;
/*     */       case 47: 
/* 276 */         result = result + "/";
/*     */       }
/*     */       
/* 279 */       startBitIdx += 6;
/*     */     }
/*     */     
/*     */ 
/* 283 */     return result;
/*     */   }
/*     */   
/*     */   public boolean hasLocationData() {
/* 287 */     return false;
/*     */   }
/*     */   
/*     */   public long getMessageType()
/*     */   {
/* 292 */     return this.messageType;
/*     */   }
/*     */   
/*     */   public void setMessageType(long messageType) {
/* 296 */     this.messageType = messageType;
/*     */   }
/*     */   
/*     */   public long getRepeatIndicator() {
/* 300 */     return this.repeatIndicator;
/*     */   }
/*     */   
/*     */   public void setRepeatIndicator(long repeatIndicator) {
/* 304 */     this.repeatIndicator = repeatIndicator;
/*     */   }
/*     */   
/*     */   public long getMmsi() {
/* 308 */     return this.mmsi;
/*     */   }
/*     */   
/*     */   public void setMmsi(long mmsi) {
/* 312 */     this.mmsi = mmsi;
/*     */   }
/*     */   
/*     */   public double getLon() {
/* 316 */     return this.lon;
/*     */   }
/*     */   
/*     */   public void setLon(double lon) {
/* 320 */     this.lon = lon;
/*     */   }
/*     */   
/*     */   public double getLat() {
/* 324 */     return this.lat;
/*     */   }
/*     */   
/*     */   public void setLat(double lat) {
/* 328 */     this.lat = lat;
/*     */   }
/*     */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */