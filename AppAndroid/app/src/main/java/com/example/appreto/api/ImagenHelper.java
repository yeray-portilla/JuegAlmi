package com.example.appreto.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImagenHelper {

    private static final int TAMANO_MAXIMO = 300;

    private static LruCache<String, Bitmap> cache;

    public static void cargar(ImageView vista, String url, int placeholder) {
        vista.setImageResource(placeholder);
        vista.setTag(url);

        if (url == null || url.isEmpty()) {
            return;
        }

        Bitmap guardada = getCache().get(url);
        if (guardada != null) {
            vista.setImageBitmap(guardada);
            return;
        }

        descargar(vista, url);
    }

    private static void descargar(final ImageView vista, final String url) {
        ApiService api = RetrofitClient.getApiService(vista.getContext());
        api.descargarImagen(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                Bitmap foto = leerFoto(response.body());
                if (foto == null) {
                    return;
                }
                getCache().put(url, foto);
                ponerSiSigueSiendoSuya(vista, url, foto);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private static void ponerSiSigueSiendoSuya(ImageView vista, String url, Bitmap foto) {
        if (url.equals(vista.getTag())) {
            vista.setImageBitmap(foto);
        }
    }

    private static Bitmap leerFoto(ResponseBody cuerpo) {
        try {
            byte[] bytes = cuerpo.bytes();

            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opciones);

            opciones.inSampleSize = calcularReduccion(opciones.outWidth, opciones.outHeight);
            opciones.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opciones);
        } catch (IOException e) {
            return null;
        }
    }

    private static int calcularReduccion(int ancho, int alto) {
        int reduccion = 1;
        while (ancho / (reduccion * 2) >= TAMANO_MAXIMO && alto / (reduccion * 2) >= TAMANO_MAXIMO) {
            reduccion = reduccion * 2;
        }
        return reduccion;
    }

    private static LruCache<String, Bitmap> getCache() {
        if (cache == null) {
            int memoriaMaximaKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int tamanoCacheKb = memoriaMaximaKb / 8;
            cache = new LruCache<String, Bitmap>(tamanoCacheKb) {
                @Override
                protected int sizeOf(String url, Bitmap foto) {
                    return foto.getByteCount() / 1024;
                }
            };
        }
        return cache;
    }
}
