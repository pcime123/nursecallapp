package com.sscctv.nursecallapp.pbx;

import com.google.gson.Gson;
import com.sscctv.nursecallapp.ui.utils.NurseCallUtils;

public class PbxManager {
    private static final String PROTOCOL = "http://";
    private static final String TAG = "PbxManager";
    private boolean status;

    public void postLogin(String username, String password, String addr, String port) {
        OpenApi mApi = new OpenApi();
        OpenApi.LoginApi mLoginApi = mApi.new LoginApi();
        try {
            mLoginApi.username = username;
            mLoginApi.password = NurseCallUtils.getEncMD5(password);
            mLoginApi.port = "";
            mLoginApi.version = "";
            mLoginApi.url = "";
            mLoginApi.urltag = "0";
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String url = PROTOCOL + addr + ":" + port + EndPoint.LOGIN;
        String json = new Gson().toJson(mLoginApi, OpenApi.LoginApi.class);
        new PbxHttpClient().postSync(url, json);


    }
//
//    public OpenApi.DeviceInfo queryPbxInfo() {
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_PBX_INFO + "?token=" + PbxData.token;
////        System.out.println(url);
//
//        String response = new PbxHttpClient().postSync(url, "");
//        if (response == null) {
//            return null;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//
//        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
//            JsonElement result = element.getAsJsonObject().get("deviceinfo");
////            System.out.println(result.getAsString());   // Exception in thread "JavaFX Application Thread" java.lang.UnsupportedOperationException: JsonObject Error �쑙 ????
////            System.out.println(result);
//
//            OpenApi.DeviceInfo mDeviceInfo = new Gson().fromJson(result, OpenApi.DeviceInfo.class);
//
//            return mDeviceInfo;
//        } else {
//            return null;
//        }
//    }
//
//    public OpenApi.ExtList[] queryExtList() {
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_EXTENSION_LIST + "?token=" + PbxData.token;
////        System.out.println(url);
//
//        String response = new PbxHttpClient().postSync(url, "");
//        if (response == null) {
//            return null;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//
//        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
//            JsonElement result = element.getAsJsonObject().get("extlist");
//            OpenApi.ExtList[] mExtListArray = new Gson().fromJson(result, OpenApi.ExtList[].class);
//
//            return mExtListArray;
//        } else {
//            return null;
//        }
//    }

//    public OpenApi.ExtInfos[] queryExtensionSettings(String extid) {  // extid : "6300", "6300,6301", ""(Query all Extensions)
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_EXTENSION_SETTINGS + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        String json = "";
//        Gson mGson = new Gson();
//
//        if (extid != "") {
//            JsonObject object = new JsonObject();
//            object.addProperty("extid", extid);
//            json = mGson.toJson(object);
//        }
//
//        String response = new PbxHttpClient().postSync5(url, json);
//        if (response == null) {
//            return null;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
//            JsonElement result = element.getAsJsonObject().get("extinfos");
//            OpenApi.ExtInfos[] mExtInfos = mGson.fromJson(result, OpenApi.ExtInfos[].class);
//
//            return mExtInfos;
//        } else {
//            return null;
//        }
//    }

//    public boolean modifyExtension(String extid, OpenApi.ExtInfos modifyExtInfos) {
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.MODIFY_EXTENSION + "?token="
//                + PbxData.token;
////        System.out.println(url);
//        Gson mGson = new Gson();
//        String modify = mGson.toJson(modifyExtInfos, OpenApi.ExtInfos.class);
////        System.out.println(modify);
//
//        JsonElement element = new JsonParser().parse(modify);
//        JsonObject object = element.getAsJsonObject();
//
//        object.addProperty("extid", extid);
//        object.remove("status");
//        object.remove("type");
//        object.remove("port");
//        //object.addProperty("selectoutroute", "");
//        String json = mGson.toJson(object);
//
//        System.out.println(json);
//        String response = new PbxHttpClient().postSync1(url, json);
//        if (response == null) {
//            return false;
//        }
//
//        element = JsonParser.parseString(response);
//
//        return element.getAsJsonObject().get("status").getAsString().equals("Success"); // if "Success"  return true, else false
//    }


//    public OpenApi.PagingGroupList[] queryPagingGroupList() {
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_PAGINGGROUPLIST + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        String response = new PbxHttpClient().postSync(url, "");
//        if (response == null) {
//            return null;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//
//        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
//            JsonElement result = element.getAsJsonObject().get("paginggrouplist");
//            OpenApi.PagingGroupList[] mPagingArray = new Gson().fromJson(result, OpenApi.PagingGroupList[].class);
//
//            return mPagingArray;
//        } else {
//            return null;
//        }
//    }
//
//    public OpenApi.PagingGroup[] queryPagingGroupSettings(String number) {  // number "all" or "6300", "6300,6301"
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.QUERY_PAGING_GROUP_SETTINGS + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        Gson mGson = new Gson();
//        JsonObject object = new JsonObject();
//        object.addProperty("number", number);
//        String json = mGson.toJson(object);
//
//        String response = new PbxHttpClient().postSync(url, json);
//        if (response == null) {
//            return null;
//        }
//
////        JsonElement element = JsonParser.parseString(response);
//
////        if (element.getAsJsonObject().get("status").getAsString().equals("Success")) {
////            JsonElement result = element.getAsJsonObject().get("paginggroup");
////            OpenApi.PagingGroup[] mPagingArray = mGson.fromJson(result, OpenApi.PagingGroup[].class);
////
////            return mPagingArray;
////        } else {
//        return null;
////        }
//    }
//
//    public boolean addPagingGroup(OpenApi.PagingGroupAdd paginggroup) {
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.ADD_PAGING_GROUP + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        String json = new Gson().toJson(paginggroup, OpenApi.PagingGroupAdd.class);
//        System.out.println(json);
//
//        String response = new PbxHttpClient().postSync(url, json);
//        if (response == null) {
//            return false;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//
//        return element.getAsJsonObject().get("status").getAsString().equals("Success");   // if "Success"  return true, else false
//    }
//
//    public boolean editPagingGroup(OpenApi.PagingGroup paginggroup) {
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.EDIT_PAGING_GROUP + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        String json = new Gson().toJson(paginggroup, OpenApi.PagingGroup.class);
//        System.out.println(json);
//
//        String response = new PbxHttpClient().postSync(url, json);
//        if (response == null) {
//            return false;
//        }
//
//        JsonElement element = new JsonParser().parse(response);
//
//        return element.getAsJsonObject().get("status").getAsString().equals("Success");   // if "Success"  return true, else false
//    }

//    public boolean deletePagingGroup(String number) {  // number "all" or "6300", "6300,6301"
//
//        String url = PROTOCOL + PbxData.ipaddress + ":" + PbxData.port + EndPoint.DELETE_PAGING_GROUP + "?token="
//                + PbxData.token;
////        System.out.println(url);
//
//        Gson mGson = new Gson();
//        JsonObject object = new JsonObject();
//        object.addProperty("number", number);
//        String json = mGson.toJson(object);
//
//        String response = new PbxHttpClient().postSync5(url, json);
//        if (response == null) {
//            return false;
//        }
//
//        JsonElement element = JsonParser.parseString(response);
//
//        return element.getAsJsonObject().get("status").getAsString().equals("Success");   // if "Success"  return true, else false
//    }
}
