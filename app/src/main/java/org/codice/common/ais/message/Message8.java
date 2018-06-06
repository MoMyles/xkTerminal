/*    */ package org.codice.common.ais.message;
/*    */ 
/*    */ import java.util.Arrays;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Message8
/*    */   extends Message
/*    */ {
/*    */   private long designatedAreaCode;
/*    */   private long functionalId;
/*    */   private byte[] data;
/*    */   
/*    */   protected void parse(byte[] bitVector)
/*    */   {
/* 31 */     setMessageType(bin2dec(bitVector, 0, 5));
/* 32 */     setRepeatIndicator(bin2dec(bitVector, 6, 7));
/* 33 */     setMmsi((int)bin2dec(bitVector, 8, 37));
/*    */     
/*    */ 
/* 36 */     this.designatedAreaCode = bin2dec(bitVector, 40, 49);
/* 37 */     this.functionalId = bin2dec(bitVector, 50, 55);
/* 38 */     this.data = Arrays.copyOfRange(bitVector, 56, 920);
/*    */   }
/*    */   
/*    */   public long getDesignatedAreaCode() {
/* 42 */     return this.designatedAreaCode;
/*    */   }
/*    */   
/*    */   public void setDesignatedAreaCode(long designatedAreaCode) {
/* 46 */     this.designatedAreaCode = designatedAreaCode;
/*    */   }
/*    */   
/*    */   public long getFunctionalId() {
/* 50 */     return this.functionalId;
/*    */   }
/*    */   
/*    */   public void setFunctionalId(long functionalId) {
/* 54 */     this.functionalId = functionalId;
/*    */   }
/*    */   
/*    */   public byte[] getData() {
/* 58 */     return this.data;
/*    */   }
/*    */   
/*    */   public void setData(byte[] data) {
/* 62 */     this.data = data;
/*    */   }
/*    */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message8.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */