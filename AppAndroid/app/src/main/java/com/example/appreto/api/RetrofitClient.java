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
    // Cambia esto a la IP de tu servidor local (ej. 10.0.2.2 para emulador) o dominio.
    private static final String BASE_URL = "https://juegalmi-shop.duckdns.org/api/";

    // Las fotos que la API manda como ruta ("/uploads/...") cuelgan del dominio del servidor
    private static final String DOMINIO_SERVIDOR = "https://juegalmi-shop.duckdns.org";

    private static Retrofit retrofit;

    // URL completa de una foto. Si la API manda una URL completa la usamos tal cual; si manda solo la ruta,
    // le ponemos el dominio delante. Devuelve null si no hay foto (Glide pone entonces el icono de respaldo).
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

    // Crea Retrofit una sola vez, con el interceptor que pone el token y trata el 401
    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            // Contexto de la aplicacion: asi no nos quedamos con una Activity o un Fragment antiguo
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

    // Devuelve el ApiService para hacer las peticiones
    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }

    // Lee el mensaje de error de la API ({"error": "mensaje"}). Si no se puede leer, devuelve el texto por defecto.
    // Se escribe retrofit2.Response entero porque en este archivo "Response" es la de okhttp3 (la del interceptor).
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
            // El cuerpo no era JSON: usamos el mensaje por defecto
        }
        return porDefecto;
    }

    /**
     * Se ejecuta en cada peticion a la API:
     *  1. Anade la cabecera "Authorization: Bearer <token>" si hay sesion.
     *  2. Si la respuesta es 401 (token caducado o invalido), cierra la sesion y vuelve al login.
     */
    private static class InterceptorToken implements Interceptor {
        private final Context appContext;

        InterceptorToken(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            String token = SharedPrefManager.getInstance(appContext).getToken();
            Request peticion = anadirToken(chain.request(), token);
            Response respuesta = chain.proceed(peticion);

            if (esSesionCaducada(peticion, respuesta, token)) {
                cerrarSesionYVolverAlLogin();
            }
            return respuesta;
        }

        // Copia la peticion anadiendo el token (si hay)
        private Request anadirToken(Request peticionOriginal, String token) {
            if (token == null || token.isEmpty()) {
                return peticionOriginal;
            }
            return peticionOriginal.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        // true si la API ha dicho 401 a una peticion que llevaba token.
        // En /auth/* el 401 significa "credenciales incorrectas" y lo trata la pantalla de login.
        // Si no habia token (ya se cerro la sesion) no hacemos nada, para no repetirlo con varias respuestas a la vez.
        private boolean esSesionCaducada(Request peticion, Response respuesta, String token) {
            if (respuesta.code() != 401 || token == null) {
                return false;
            }
            String ruta = peticion.url().encodedPath();
            boolean esRutaDeLogin = ruta.contains("/api/auth/");
            return !esRutaDeLogin;
        }

        // Borra la sesion y vuelve a abrir MainActivity (sin token, MainActivity muestra el login)
        private void cerrarSesionYVolverAlLogin() {
            SharedPrefManager.getInstance(appContext).clear();

            // El interceptor corre en un hilo de fondo: lo que se ve en pantalla hay que hacerlo en el hilo principal
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
}
