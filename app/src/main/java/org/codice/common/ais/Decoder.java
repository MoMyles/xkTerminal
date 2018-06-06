/*    */ package org.codice.common.ais;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.codice.common.ais.message.Message;
/*    */ import org.codice.common.ais.message.UnknownMessageException;
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
/*    */ public class Decoder
/*    */ {
/*    */   public List<Message> parseString(String sentence)
/*    */     throws IOException, UnknownMessageException
/*    */   {
/* 33 */     return parseInputStream(new ByteArrayInputStream(sentence.getBytes()));
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public List<Message> parseInputStream(InputStream inputStream)
/*    */     throws IOException, UnknownMessageException
/*    */   {
/* 44 */     List<Message> messages = new ArrayList();
/* 45 */     List aisMessages = new ArrayList();
/* 46 */     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
/*    */     String line;
/* 48 */     while ((line = reader.readLine()) != null)
/*    */     {
/* 50 */       String[] fields = line.substring(1).split(",");
/*    */       
/* 52 */       if ("AIVDM".equals(fields[0]) || "AIVDO".equals(fields[0]))
/*    */       {
/* 54 */         aisMessages.add(fields);
/* 55 */         if (fields[1].equals(fields[2]))
/*    */         {
/* 57 */           messages.add(processMessage(aisMessages));
/*    */           
/* 59 */           aisMessages = new ArrayList();
/*    */         }
/*    */       }
/*    */     }
/* 63 */     return messages;
/*    */   }
/*    */   
/*    */   private Message processMessage(List<String[]> aisMessages) throws UnknownMessageException
/*    */   {
/* 68 */     String fullPayload = "";
/*    */     
/* 70 */     for (String[] message : aisMessages) {
/* 71 */       fullPayload = fullPayload + message[5];
/*    */     }
/* 73 */     ArrayList<Byte> bitVector = new ArrayList();
/*    */     
/*    */ 
/* 76 */     for (byte bite : fullPayload.getBytes()) {
/* 77 */       byte byteChar = (byte)(bite - 48);
/* 78 */       byteChar = byteChar > 40 ? (byte)(byteChar - 8) : byteChar;
/* 79 */       bitVector.add(Byte.valueOf((byte)(byteChar >>> 5 & 0x1)));
/* 80 */       bitVector.add(Byte.valueOf((byte)(byteChar >>> 4 & 0x1)));
/* 81 */       bitVector.add(Byte.valueOf((byte)(byteChar >>> 3 & 0x1)));
/* 82 */       bitVector.add(Byte.valueOf((byte)(byteChar >>> 2 & 0x1)));
/* 83 */       bitVector.add(Byte.valueOf((byte)(byteChar >>> 1 & 0x1)));
/* 84 */       bitVector.add(Byte.valueOf((byte)(byteChar & 0x1)));
/*    */     }
/* 86 */     return Message.parseMessage(bitVector);
/*    */   }
/*    */ }


/* Location:              C:\Users\dell\Downloads\ais-parser-1.1.jar!\org\codice\common\ais\Decoder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */