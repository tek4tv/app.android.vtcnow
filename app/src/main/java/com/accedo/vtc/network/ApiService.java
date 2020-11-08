package com.accedo.vtc.network;

import com.accedo.vtc.model.ReponseCommon;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface ApiService {
    @POST(ApiUtil.URL_INSERT)
    Observable<ResponseBody> getDailyForecast(@QueryMap Map<String,String> params);
}
