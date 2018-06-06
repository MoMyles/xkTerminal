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
/*    */ public class Message6
/*    */   extends Message
/*    */ {
/*    */   private long sequenceNumber;
/*    */   private long destinationMmsi;
/*    */   private boolean retransmitFlag;
/*    */   private long designatedAreaCode;
/*    */   private long functionalId;
/*    */   private byte[] data;
/*    */   
/*    */   protected void parse(byte[] bitVector)
/*    */   {
/* 32 */     setMessageType(bin2dec(bitVector, 0, 5));
/* 33 */     setRepeatIndicator(bin2dec(bitVector, 6, 7));
/* 34 */     setMmsi((int)bin2dec(bitVector, 8, 37));
/*    */     
/* 36 */     this.sequenceNumber = bin2dec(bitVector, 38, 39);
/* 37 */     this.destinationMmsi = bin2dec(bitVector, 40, 69);
/* 38 */     this.retransmitFlag = bin2bool(bitVector, 70);
/* 39 */     this.designatedAreaCode = bin2dec(bitVector, 72, 81);
/* 40 */     this.functionalId = bin2dec(bitVector, 82, 87);
/* 41 */     this.data = Arrays.copyOfRange(bitVector, 88, 920);
/*    */   }
/*    */   
/*    */   public long getSequenceNumber()
/*    */   {
/* 46 */     return this.sequenceNumber;
/*    */   }
/*    */   
/*    */   public void setSequenceNumber(long sequenceNumber) {
/* 50 */     this.sequenceNumber = sequenceNumber;
/*    */   }
/*    */   
/*    */   public long getDestinationMmsi() {
/* 54 */     return this.destinationMmsi;
/*    */   }
/*    */   
/*    */   public void setDestinationMmsi(long destinationMmsi) {
/* 58 */     this.destinationMmsi = destinationMmsi;
/*    */   }
/*    */   
/*    */   public boolean isRetransmitFlag() {
/* 62 */     return this.retransmitFlag;
/*    */   }
/*    */   
/*    */   public void setRetransmitFlag(boolean retransmitFlag) {
/* 66 */     this.retransmitFlag = retransmitFlag;
/*    */   }
/*    */   
/*    */   public long getDesignatedAreaCode() {
/* 70 */     return this.designatedAreaCode;
/*    */   }
/*    */   
/*    */   public void setDesignatedAreaCode(long designatedAreaCode) {
/* 74 */     this.designatedAreaCode = designatedAreaCode;
/*    */   }
/*    */   
/*    */   public long getFunctionalId() {
/* 78 */     return this.functionalId;
/*    */   }
/*    */   
/*    */   public void setFunctionalId(long functionalId) {
/* 82 */     this.functionalId = functionalId;
/*    */   }
/*    */   
/*    */   public byte[] getData() {
/* 86 */     return this.data;
/*    */   }
/*    */   
/*    */   public void setData(byte[] data) {
/* 90 */     this.data = data;
/*    */   }
/*    */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\message\Message6.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */