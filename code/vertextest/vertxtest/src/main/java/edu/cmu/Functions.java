package edu.cmu;

import org.json.JSONObject;


public class Functions {

    // Get the hash of the transaction
    public static String getHash(JSONObject tx, boolean isRewardTx) {
        String result = "";

        // Determine if the transaction is a reward transaction
        if (isRewardTx) {
            result = tx.getString("time") + "||" + tx.getLong("recv") + "|" + tx.getInt("amt") + "|";
        } else {
            result = tx.getString("time") + "|" + tx.getLong("send") + "|" + tx.getLong("recv") + "|"
                    + tx.getInt("amt") + "|" + tx.getInt("fee");
        }

        // Return the first 8 characters
        return get256Decoded(result).substring(0, 8);
    }

    public static String get256Decoded(String s) {
        String sha256Decoded = org.apache.commons.codec.digest.DigestUtils.sha256Hex(s);
        return sha256Decoded;
    }



    // Check wether the transaction is made by other account
    // By checking the tx has "sig" tag or not
    public static boolean isOtherAccount(JSONObject tx) {
        if(tx.isNull("sig")) {
            return false;
        }
        return true;
    }
   
    // Test
    public static void main(String[] args) {
        JSONObject object = new JSONObject(
                "{\n\"sig\": 160392705122,\n\"recv\": 658672873303,\n\"fee\": 3536,\n\"amt\": 34263741,\n"
                        + "\"time\": \"1582521636327155516\",\n\"send\": 831361201829,\n\"hash\": \"1fb48c71\"\n}");
        JSONObject object2 = new JSONObject(
                "{\n\"recv\": 1097844002039,\n\"amt\": 250000000,\n" + "\"time\": \"1582522203026667063\"\n}");
        System.out.println(Functions.getHash(object, false));
        System.out.println(Functions.getHash(object2, true));
    }

}
