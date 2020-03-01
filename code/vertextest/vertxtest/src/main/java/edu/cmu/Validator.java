package edu.cmu;

import org.json.JSONObject;
import org.json.JSONArray;

public class Validator {
    static boolean flag = true;

    public static boolean checkTransationAll(JSONObject jsonObject, boolean isRewardTx, int block_order) {
        return checkSigValid(jsonObject) && checkTransactionNumberValid(jsonObject)
                && checkTransactionHashValid(jsonObject, isRewardTx)
                && checkRewardAmtValid(jsonObject, block_order, isRewardTx);
    }

    public static boolean checkSigValid(JSONObject jsonObject) {
        if (!jsonObject.isNull("send") && !jsonObject.isNull("sig") && !jsonObject.isNull("hash")) {
            // if (jsonObject.getLong("send") == 1097844002039l) {
            // long givenSig = jsonObject.getLong("sig");
            // long realSig = Functions.getSig(jsonObject.getString("hash"), "343710770439",
            // "1561906343821");
            // if (givenSig != realSig) {
            // System.out.println("========sig wrong=========");
            // return false;
            // }
            // } else {
            String givenSig = String.valueOf(jsonObject.getLong("sig"));
            String pubKey = String.valueOf(jsonObject.getLong("send"));
            Long realHash = Functions.getHashFromSig(givenSig, pubKey, "1561906343821");
            if (!realHash.equals(Long.parseLong(jsonObject.getString("hash").toUpperCase(), 16))) {
                // System.out.println("========sig wrong=========");
                // System.out.println(realHash);
                // System.out.println(jsonObject.getString("hash"));
                return false;
            }
        }
        // }
        return true;
    }

    public static boolean checkRewardAmtValid(JSONObject jsonObject, int order, boolean isReward) {
        if (isReward) {
            return jsonObject.getInt("amt") == Functions.getRewardAmt(order);
        }
        return true;
    }

    public static boolean checkTransactionHashValid(JSONObject jsonObject, boolean isRewardTx) {
        if (jsonObject.isNull("hash")) {
            return true;
        }
        String realHash = Functions.getHash(jsonObject, isRewardTx);
        String givenHash = jsonObject.getString("hash");
        if (realHash.equals(givenHash)) {
            return true;
        }
        // System.out.println("========tx hash wrong=========");
        return false;
    }

    public static boolean checkTransactionNumberValid(JSONObject jsonObject) {
        int amount = jsonObject.getInt("amt");
        if (!jsonObject.isNull("fee")) {
            int fee = jsonObject.getInt("fee");
            if (amount < 0 || fee < 0) {
                // System.out.println("========amt or fee < 0=========");
                return false;
            }
            return true;
        } else {
            return !(amount < 0);
        }
    }

    // Previous checkPowAndHashValid(JSONObject jsonObject, String key)
    public static String checkValid(JSONObject jsonObject) {
        flag = true;
        JSONArray newTxArray = jsonObject.getJSONArray("new_tx");
        checkNewTxValid(newTxArray);
        if (flag == false) {
            return "ERROR IN NEW_TX";
        }
        JSONArray jsonArray = jsonObject.getJSONArray("chain");
        String preHash = "";
        String code;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String allHash = getAllhash(object, i);
            if (flag == false) {
                return "ERROR IN block" + i;
            }

            if (i == 0) {// 0|00000000|4b277860
                preHash = "00000000";
                code = i + "|" + preHash + allHash;
            } else {
                code = i + "|" + preHash + allHash;
            }
            // System.out.println(code);
            // System.out.println(object.toString());
            preHash = object.get("hash").toString();

            // check
            String target = object.getString("target");
            String pow = Functions.getPowAndHash(code, target)[0];
            String hash = Functions.getPowAndHash(code, target)[1];

            if (!pow.equals(object.getString("pow")) || !hash.equals(object.getString("hash"))) {
                // System.out.println("NOT Equal");
                return "ERROR IN HASH OR POW";
            }
        }
        return "0";
    }

    public static void checkNewTxValid(JSONArray jsonArray) {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tx = jsonArray.getJSONObject(i);
            if (!Functions.isOtherAccount(tx)) {
                continue;
            } else {
                if (!checkTransationAll(tx, false, 0)) {
                    flag = false;
                    // System.out.println("ERROR IN NEW" + i);
                    break;
                }
            }
        }

    }

    public static String getAllhash(JSONObject jsonObject, int order) {
        JSONArray jsonArray = jsonObject.getJSONArray("all_tx");
        String allHash = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            boolean isRewardTx = false;
            if (i == jsonArray.length() - 1) {
                isRewardTx = true;
            }
            if (!checkTransationAll(jsonArray.getJSONObject(i), isRewardTx, order)) {
                // System.out.println("========error in tx " + i);
                flag = false;
                break;
            }
            allHash = allHash + "|" + jsonArray.getJSONObject(i).get("hash");
        }
        return allHash;
    }

    public static Boolean checkTimeValid(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("chain");
        String currentTime = "";
        String preTime = "0";
        for (int i = 0; i < jsonArray.length(); i++) {
            for (int j = 0; j < jsonArray.getJSONObject(i).getJSONArray("all_tx").length(); j++) {
                currentTime = jsonArray.getJSONObject(i).getJSONArray("all_tx").getJSONObject(j).getString("time");
                if (currentTime.compareTo(preTime) <= 0) {
                    return false;
                }
                preTime = currentTime;
            }
        }

        JSONArray jsonArray1 = jsonObject.getJSONArray("new_tx");
        for (int i = 0; i < jsonArray1.length(); i++) {
            currentTime = jsonArray1.getJSONObject(i).getString("time");
            if (currentTime.compareTo(preTime) <= 0) {

                return false;
            }
            preTime = currentTime;
        }
        return true;
    }

}