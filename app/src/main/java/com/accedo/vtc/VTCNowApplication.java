package com.accedo.vtc;

import android.app.Application;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.accedo.vtc.common.SharedPreferencesUtil;
import com.accedo.vtc.network.ApiService;
import com.accedo.vtc.network.ApiUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class VTCNowApplication extends Application {
    private static ApiService sApiService;
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("HotNews").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
            }
        });
        SharedPreferencesUtil.initialize(getApplicationContext(),MODE_PRIVATE);
        sApiService = ApiUtil.getApiService();
    }
    public static ApiService getApiService(){
        return sApiService;
    }
}
