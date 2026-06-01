package com.dermacare.clinic;

import android.app.Application;
import android.content.Context;
import com.dermacare.clinic.util.LocaleHelper;

public class DermaCareApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        // Thiết lập ngôn ngữ mặc định là Tiếng Việt cho toàn bộ ứng dụng
        super.attachBaseContext(LocaleHelper.setLocale(base, "vi"));
    }
}
