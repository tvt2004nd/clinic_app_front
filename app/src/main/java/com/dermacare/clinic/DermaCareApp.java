package com.dermacare.clinic;

import android.app.Application;

import java.util.Locale;

public class DermaCareApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Locale.setDefault(new Locale("vi", "VN"));
    }
}
