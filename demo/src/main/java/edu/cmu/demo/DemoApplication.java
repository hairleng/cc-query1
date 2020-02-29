package edu.cmu.demo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@EnableAutoConfiguration
public class DemoApplication {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
	}
	
	/**
     * Map to "/profile", handling http requests.
     *
     * @param username username
     * @return JSON string of profile
     */
    @RequestMapping(value = "/cc={base64_encoded_request}", produces = "application/json", method = RequestMethod.GET)
	@ResponseBody
    public String getResult(HttpServletRequest request) {
		String url = request.getRequestURI();
		// String info_pre = request.getParameter("cc");
		String info = url.substring(4, url.length());
		// String result = info;
		String result = decode(info);

		// JSONObject original = new JSONObject(result);

		return result;
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
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}