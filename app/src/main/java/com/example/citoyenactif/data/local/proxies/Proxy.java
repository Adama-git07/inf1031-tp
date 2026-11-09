package com.example.citoyenactif.data.local.proxies;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.GsonBuilder;

public interface Proxy {

    ExecutorService connexionPool = Executors.newSingleThreadExecutor();

    static <T extends Proxy> T build(Class<T> proxy) {
        T p = new Retrofit.Builder()
                .baseUrl("https://seneparkingauth-6c9716e70ab8.herokuapp.com/")
                // .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().create()))
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(proxy);
        return p;
    }
}