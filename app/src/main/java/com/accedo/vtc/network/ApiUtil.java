package com.accedo.vtc.network;

public class ApiUtil {
    private static final String BASE_URL = "http://61.28.235.91:8787/";
    static final String URL_INSERT = "/media/insertToken";

    public static ApiService getApiService() {
        return RetrofitClient.getClient(BASE_URL).create(ApiService.class);
    }



}
