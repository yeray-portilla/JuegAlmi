package com.example.appreto.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.appreto.MainActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    static final String BASE_URL = "https://juegalmi-shop.duckdns.org/api/";

    private static final String DOMINIO_SERVIDOR = "https://juegalmi-shop.duckdns.org";

    private static Retrofit retrofit;

    public static String construirUrlFoto(String foto) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        if (foto.startsWith("http")) {
            return foto;
        }
        if (foto.startsWith("/")) {
            return DOMINIO_SERVIDOR + foto;
        }
        return DOMINIO_SERVIDOR + "/" + foto;
    }

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new InterceptorToken(appContext))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }

    public static String leerMensajeError(retrofit2.Response<?> respuesta, String porDefecto) {
        try {
            if (respuesta.errorBody() == null) {
                return porDefecto;
            }
            String cuerpo = respuesta.errorBody().string();
            JsonObject json = JsonParser.parseString(cuerpo).getAsJsonObject();
            if (json.has("error")) {
                return json.get("error").getAsString();
            }
        } catch (Exception e) {
        }
        return porDefecto;
    }

    private static class InterceptorToken implements Interceptor {
        private final Context appContext;

        InterceptorToken(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            if (!esRutaDeLaApi(chain.request())) {
                return chain.proceed(chain.request());
            }

            String token = SharedPrefManager.getInstance(appContext).getToken();
            Request peticion = anadirToken(chain.request(), token);
            Response respuesta = chain.proceed(peticion);

            if (esSesionCaducada(peticion, respuesta, token)) {
                cerrarSesionYVolverAlLogin(appContext);
            }
            return respuesta;
        }

        private boolean esRutaDeLaApi(Request peticion) {
            return peticion.url().toString().startsWith(BASE_URL);
        }

        private Request anadirToken(Request peticionOriginal, String token) {
            if (token == null || token.isEmpty()) {
                return peticionOriginal;
            }
            return peticionOriginal.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        private boolean esSesionCaducada(Request peticion, Response respuesta, String token) {
            if (respuesta.code() != 401 || token == null) {
                return false;
            }
            String ruta = peticion.url().encodedPath();
            boolean esRutaDeLogin = ruta.contains("/api/auth/");
            return !esRutaDeLogin;
        }
    }

    public static void cerrarSesionYVolverAlLogin(Context context) {
        final Context appContext = context.getApplicationContext();
        SharedPrefManager.getInstance(appContext).clear();

        Handler hiloPrincipal = new Handler(Looper.getMainLooper());
        hiloPrincipal.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appContext, "Tu sesión ha caducado. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(appContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            }
        });
    }
}
