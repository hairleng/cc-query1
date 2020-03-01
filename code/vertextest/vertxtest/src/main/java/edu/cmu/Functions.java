package edu.cmu;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.math.BigInteger;

public class Functions {

    // Get the hash of the transaction
    public static String getHash(JSONObject tx, boolean isRewardTx) {
        String result = "";

        // Determine if the transaction is a reward transaction
        if (isRewardTx) {
            result = tx.getString("time") + "||" + tx.getLong("recv") + "|" + tx.getInt("amt") + "|";
        } else {
            result = tx.getString("time") + "|" + tx.getLong("send") + "|" + tx.getLong("recv") + "|" + tx.getInt("amt")
                    + "|" + tx.getInt("fee");
        }

        // Return the first 8 characters
        return get256Decoded(result).substring(0, 8);
    }

    // Test
    public static void main(String[] args) {
        JSONObject object = new JSONObject(
                "{\n\"sig\": 160392705122,\n\"recv\": 658672873303,\n\"fee\": 3536,\n\"amt\": 34263741,\n"
                        + "\"time\": \"1582521636327155516\",\n\"send\": 831361201829,\n\"hash\": \"1fb48c71\"\n}");
        JSONObject object2 = new JSONObject(
                "{\n\"recv\": 1097844002039,\n\"amt\": 250000000,\n" + "\"time\": \"1582522203026667063\"\n}");
        // System.out.println(Functions.getHash(object, false));
        // System.out.println(Functions.getHash(object2, true));
    }

    /**
     *
     * @param num integer value of message
     * @param e   exponential
     * @param n   mod
     * @return the signature of the message
     */
    public static Long getSig(String a, String e, String n) {
        // create a BigInteger exponent
        BigInteger num = new BigInteger(String.valueOf(Long.parseLong(a.toUpperCase(), 16)));
        BigInteger exponent = new BigInteger(e);
        BigInteger mod = new BigInteger(n);
        // perform modPow operation on num using exp and mod
        return Long.parseLong(num.modPow(exponent, mod).toString());
    }

    public static long getHashFromSig(String a, String e, String n) {
        // create a BigInteger exponent
        BigInteger num = new BigInteger(a);
        BigInteger exponent = new BigInteger(e);
        BigInteger mod = new BigInteger(n);
        // perform modPow operation on num using exp and mod
        return Long.parseLong(num.modPow(exponent, mod).toString());
    }

    public static JSONObject getNewBlock(String request) {
        JSONObject result = new JSONObject();
        result.put("all_tx", updateNewTx(request));

        JSONObject requestJson = new JSONObject(request);
        JSONObject last_All_tx = getLastData(requestJson, "chain");
        String target = getSting(requestJson, "new_target");

        String[] re = getPowAndHash(getNewBlockString(request), target);

        result.put("id", last_All_tx.getInt("id") + 1);
        result.put("id", last_All_tx.getInt("id") + 1);
        result.put("pow", re[0]);
        result.put("hash", re[1]);
        result.put("target", target);

        return result;
    }

    /**
     * 
     * @param request the original http response
     * @return jsonarray that fills in the missing field
     */
    public static JSONArray updateNewTx(String request) {
        JSONObject t_ob = new JSONObject(request);
        JSONArray ja = t_ob.getJSONArray("new_tx");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            // if the JSONObject has signature, no need to complete
            if (isOtherAccount(jo)) {
                continue;
            } else {
                jo.put("send", 1097844002039l);
                jo.put("fee", 0);
                // need the integer value of the transaction s
                String hash = getHash(jo, false);
                jo.put("hash", hash);
                // get the signature of the hash
                jo.put("sig", getSig(hash, "343710770439", "1561906343821"));
            }
        }
        ja.put(getRewardTx(request));
        // System.out.println(ja);
        return ja;
    }

    /**
     * 
     * @param request the original http response
     * @return the reward transaction by our own group
     */
    public static JSONObject getRewardTx(String request) {
        JSONObject last_All_block = getLastData(new JSONObject(request), "chain");
        int id = getInt(last_All_block, "id");
        int reward = getRewardAmt(id + 1);
        JSONObject result = new JSONObject();
        result.put("recv", 1097844002039l);
        result.put("amt", reward);
        JSONObject last_All_tx = getLastData(last_All_block, "all_tx");
        String lasttime = getSting(last_All_tx, "time");
        BigInteger newtime = new BigInteger(lasttime).add(new BigInteger(String.valueOf(1000000000l * 60l * 10l)));
        result.put("time", newtime.toString());
        result.put("hash", getHash(result, true));

        return result;
    }

    public static int getRewardAmt(int id) {
        int order = id / 2;
        int result = 500000000;
        for (int i = 0; i < order; i++) {
            result = result / 2;
        }
        return result;
    }

    public static String get256Decoded(String s) {
        String sha256Decoded = org.apache.commons.codec.digest.DigestUtils.sha256Hex(s);
        return sha256Decoded;
    }

    // Check wether the transaction is made by other account
    // By checking the tx has "sig" tag or not
    public static boolean isOtherAccount(JSONObject tx) {
        if (tx.isNull("sig")) {
            return false;
        }
        return true;
    }

    // get pow and block hash
    public static String[] getPowAndHash(String s, String target) {
        String sha256Decoded = org.apache.commons.codec.digest.DigestUtils.sha256Hex(s);
        // System.out.println(sha256Decoded);
        // String first_8_hex_characters_try = org.apache.commons.codec.digest.DigestUtils.sha256Hex(sha256Decoded + 12)
        //         .substring(0, 8);
        // System.out.println(first_8_hex_characters_try);
        // System.out.println(first_8_hex_characters_try.compareTo(target));
        String[] result = new String[2];
        for (int i = 0;; i++) {
            // get the first 8 hex characters of the SHA-256 result.
            String first_8_hex_characters = org.apache.commons.codec.digest.DigestUtils.sha256Hex(sha256Decoded + i)
                    .substring(0, 8);
            // compare the characters with target (lexicographically smaller)
            if (first_8_hex_characters.compareTo(target) < 0) {
                result[0] = i + ""; // get the pow
                result[1] = first_8_hex_characters; // get the new hash
                return result;
            }
        }
    }

    public static JSONObject getLastData(JSONObject jsonObject, String key) {
        try {
            // JSONObject jsonObject = new JSONObject(rawData);
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            JSONObject lastData = jsonArray.getJSONObject(jsonArray.length() - 1);
            // System.out.println("lastdata: " + lastData.toString());
            return lastData;
        } catch (JSONException e) {
            System.out.println();
        }
        return null;
    }

    public static int getInt(JSONObject jsonObject, String key) {
        int intInfo = jsonObject.getInt(key);
        // System.out.println(key + ": " + intInfo);
        return intInfo;
    }

    public static String getSting(JSONObject jsonObject, String key) {
        String stringInfo = jsonObject.getString(key);
        // System.out.println(key + ": " + stringInfo);
        return stringInfo;
    }

    public static String getNewBlockString(String request) {
        JSONObject jsonObject = new JSONObject(request);
        // get the last all_tx in chain
        JSONObject last_All_tx = getLastData(jsonObject, "chain");

        // get the last element of the last all_tx
        // JSONObject last_element = getLastData(last_All_tx, "all_tx");

        int id = getInt(last_All_tx, "id");
        String pre_block_hash = getSting(last_All_tx, "hash");

        JSONArray ja = updateNewTx(request);
        String res = String.valueOf(id + 1) + "|" + pre_block_hash;

        for (int i = 0; i < ja.length(); i++) {
            res += "|" + ja.getJSONObject(i).getString("hash");
        }

        // System.out.println("res: " + res);
        return res;
    }

}