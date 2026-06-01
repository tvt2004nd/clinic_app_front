package com.dermacare.clinic.data.api;
 
import android.content.Context;
 
import com.dermacare.clinic.util.SessionManager;
 
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
 
public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
 
    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            final SessionManager session = new SessionManager(context.getApplicationContext());
 
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
 
            // Interceptor to inject JWT Bearer Token dynamically
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                String token = session.getToken();
 
                if (token != null && !token.isEmpty()) {
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                    return chain.proceed(requestBuilder.build());
                }
                return chain.proceed(original);
            });
 
            // Logging Interceptor for HTTP Traffic inspection
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
 
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
 
    public static AuthService getAuthService(Context context) {
        return getClient(context).create(AuthService.class);
    }
 
    public static UserService getUserService(Context context) {
        return getClient(context).create(UserService.class);
    }


    public static MedicalRecordService getMedicalRecordService(Context context) {
        return getClient(context).create(MedicalRecordService.class);
    }

}
