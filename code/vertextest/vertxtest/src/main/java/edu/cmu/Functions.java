package edu.cmu;

import io.vertx.core.json.JsonObject;

public class Functions {

    // Get the hash of the transaction
    public static String getHash(JsonObject tx, boolean isRewardTx) {
        String result = "";

        // Determine if the transaction is a reward transaction
        if (isRewardTx) {
            result = tx.getString("time") + "||" + tx.getLong("recv") + "|" + tx.getInteger("amt") + "|";
        } else {
            result = tx.getString("time") + "|" + tx.getLong("send") + "|" + tx.getLong("recv") + "|"
                    + tx.getInteger("amt") + "|" + tx.getInteger("fee");
        }

        // Return the first 8 characters
        return get256Decoded(result).substring(0, 8);
    }

    private static String get256Decoded(String s) {
        String sha256Decoded = org.apache.commons.codec.digest.DigestUtils.sha256Hex(s);
        return sha256Decoded;
    }

    // Check wether the transaction is made by other account
    // By checking the tx has "sig" tag or not
    public static boolean isOtherAccount(JsonObject tx) {
        if(tx.getLong("sig") != null) {
            return true;
        }
        return false;
    }
   
    // Test
    public static void main(String[] args) {
        JsonObject object = new JsonObject(
                "{\n\"sig\": 160392705122,\n\"recv\": 658672873303,\n\"fee\": 3536,\n\"amt\": 34263741,\n"
                        + "\"time\": \"1582521636327155516\",\n\"send\": 831361201829,\n\"hash\": \"1fb48c71\"\n}");
        JsonObject object2 = new JsonObject(
                "{\n\"recv\": 1097844002039,\n\"amt\": 250000000,\n" + "\"time\": \"1582522203026667063\"\n}");
        System.out.println(Functions.getHash(object, false));
        System.out.println(Functions.getHash(object2, true));
    }

}