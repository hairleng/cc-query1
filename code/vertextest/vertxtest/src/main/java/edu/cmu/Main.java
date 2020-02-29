package edu.cmu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;

import java.math.BigInteger;
import org.json.*;

public class Main {
//  public static void main(String[] args) {
//    Vertx vertx = Vertx.vertx();

//    HttpServer server = vertx.createHttpServer();

//    server.requestHandler(request -> {
//      // Get the encoded part
//      String info = request.getParam("cc");

//      // Decode using URL-SAFE Base64
//      Base64.Decoder d = Base64.getUrlDecoder();
//      byte[] decodedBytes = d.decode(info);

//      // Inflate zlib compress
//      Inflater decompresser = new Inflater();
//      decompresser.setInput(decodedBytes);
//      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decodedBytes.length);
//      byte[] buffer = new byte[1024];
//      while (!decompresser.finished()) {
//        int count;
//        try {
//          count = decompresser.inflate(buffer);
//          outputStream.write(buffer, 0, count);
//        } catch (DataFormatException e) {
//          e.printStackTrace();
//        }
//      }
//      try {
//        outputStream.close();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//      byte[] output = outputStream.toByteArray();

//      // Get the decoded message, in String
//      String result = new String(output);

//      // Create response
//      HttpServerResponse response = request.response();
//      response.putHeader("content-type", "text/plain");
//      response.end(String.format("Got%s%n", result));
//    });

//    // Set the port to listen
//    server.listen(88, "localhost");
//  }



public static String GROUP_ID = "123";
public static String e = "5";
public static String n = "91";
public static void main(String[] args){
  System.out.println("result:" + getSig("23", e, n));
}

/**
*
* @param num integer value of message
* @param e exponential
* @param n
* @return
*/
public static Long getSig(String a, String e, String n) {
  // create a BigInteger exponent
  BigInteger num = new BigInteger(a);
  BigInteger exponent = new BigInteger(e);
  BigInteger mod = new BigInteger(n);

  // perform modPow operation on num using exp and mod

  return Long.parseLong(num.modPow(exponent, mod).toString());
}

public static JsonArray updateNewTx(String request){
  JSONArray ja = new JSONArray(request);
  for(int i = 0; i < ja.length(); i++){
    JSONObject jo = ja.getJSONObject(i);
    // if the JSONObject has signature, no need to complete
    if(hasSig(jo)){
      continue;
    }
    else{
      jo.append("send", GROUP_ID);
      jo.append("fee", 0);
      //need the integer value of the transaction s
      hash = getHash(jo, false);
      jo.append("hash", hash);
      //get the signature of the hash
      jo.append("sig", getSig(hash, e, n));
    }
    return ja;
  }
  


}
