package edu.cmu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

import org.json.JSONObject;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    HttpServer server = vertx.createHttpServer();

    server.requestHandler(request -> {
      // Get the encoded part
      String info = request.getParam("cc");
      
      String result = decode(info);

      // Create response
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      JSONObject original = new JSONObject(result);
      original.getJSONArray("chain").put(Functions.getNewBlock(result));
      original.remove("new_target");
      original.remove("new_tx");     

      Deflater compresser = new Deflater();
      int compressedDataLength = 0;
      compresser.setInput(original.toString().getBytes());
      compresser.finish();
      ByteArrayOutputStream o = new ByteArrayOutputStream(original.toString().getBytes().length);
      byte[] compress_result_bytes = new byte[1024];

      while (!compresser.finished()) {
        compressedDataLength = compresser.deflate(compress_result_bytes);
        o.write(compress_result_bytes, 0, compressedDataLength);
      }

      compresser.end();
      byte[] compress_result = o.toByteArray();
      String url_encoded = new String(Base64.getUrlEncoder().encode(compress_result));

      response.end(String.format("%s,%s%n%s", "HUNTERs", "752696750380", url_encoded));
    });

    // Set the port to listen
    server.listen(8888, "localhost");
  }

  public static String decode(String info){
     // Decode using URL-SAFE Base64
     Base64.Decoder d = Base64.getUrlDecoder();
     byte[] decodedBytes = d.decode(info);

     // Inflate zlib compress
     Inflater decompresser = new Inflater();
     decompresser.setInput(decodedBytes);
     ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decodedBytes.length);
     byte[] buffer = new byte[1024];
     while (!decompresser.finished()) {
       int count;
       try {
         count = decompresser.inflate(buffer);
         outputStream.write(buffer, 0, count);
       } catch (DataFormatException e) {
         e.printStackTrace();
       }
     }
     try {
       outputStream.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
     byte[] output = outputStream.toByteArray();

     // Get the decoded message, in String
     String result = new String(output);
     return result;
  }
}
