package com.example.appreto.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Prepara la foto de la camara para mandarla a la API (Soporte -> pedir reparacion).
 *
 * Como se usa:
 *  1. crearUriParaFoto(): la camara guarda ahi la foto a tamano completo (ActivityResultContracts.TakePicture).
 *  2. leerFotoReducida(): lee esa foto, la gira si hace falta y la reduce a 1280 px de ancho como mucho.
 *  3. convertirABase64(): la comprime a JPEG calidad 65 y la pasa a texto base64 (campo "foto_base64").
 */
public class FotoHelper {

    // Ancho maximo de la foto que se manda a la API
    private static final int ANCHO_MAXIMO = 1280;

    // Calidad del JPEG (0-100). Con 65 la foto pesa poco y se sigue viendo bien.
    private static final int CALIDAD_JPEG = 65;

    // Devuelve el archivo donde se guarda la foto (siempre el mismo: cada foto nueva sustituye a la anterior)
    public static File obtenerArchivoFoto(Context context) {
        File carpeta = new File(context.getCacheDir(), "fotos"); // la misma carpeta que res/xml/rutas_fotos.xml
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return new File(carpeta, "foto_reparacion.jpg");
    }

    // Uri "content://" del archivo de la foto. Se la damos a la camara para que guarde ahi la foto.
    // FileProvider esta declarado en el AndroidManifest con authorities = paquete + ".fileprovider".
    public static Uri crearUriParaFoto(Context context) {
        File archivo = obtenerArchivoFoto(context);
        String autoridad = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, autoridad, archivo);
    }

    // Lee la foto del archivo, la pone derecha y la reduce a 1280 px de ancho como mucho. Devuelve null si falla.
    public static Bitmap leerFotoReducida(File archivo) {
        Bitmap foto = leerSinGastarMuchaMemoria(archivo);
        if (foto == null) {
            return null;
        }
        Bitmap fotoDerecha = girarSegunExif(foto, archivo);
        return reducirAnchoMaximo(fotoDerecha);
    }

    // Comprime la foto a JPEG (calidad 65) y la convierte a texto base64 para mandarla en un JSON
    public static String convertirABase64(Bitmap foto) {
        ByteArrayOutputStream bytesFoto = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, bytesFoto);
        return Base64.encodeToString(bytesFoto.toByteArray(), Base64.NO_WRAP);
    }

    // Una foto de la camara puede tener 4000 px o mas. Si la cargamos entera, la app se puede quedar sin memoria.
    // Por eso primero miramos su tamano y la leemos ya reducida a la mitad, a la cuarta parte... (inSampleSize).
    private static Bitmap leerSinGastarMuchaMemoria(File archivo) {
        // 1) Solo leer el tamano, sin cargar la imagen
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);

        // 2) Leer la imagen dividiendo su tamano entre 2 mientras siga siendo mas del doble de lo que necesitamos.
        // Usamos el lado mas largo porque la foto puede estar girada (se pone derecha despues).
        int ladoMasLargo = Math.max(opciones.outWidth, opciones.outHeight);
        int reduccion = 1;
        while (ladoMasLargo / (reduccion * 2) >= ANCHO_MAXIMO) {
            reduccion = reduccion * 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = reduccion;
        return BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);
    }

    // Muchas camaras guardan la foto "tumbada" y apuntan en los datos EXIF cuanto hay que girarla
    private static Bitmap girarSegunExif(Bitmap foto, File archivo) {
        int grados = leerGradosDeGiro(archivo);
        if (grados == 0) {
            return foto;
        }
        Matrix giro = new Matrix();
        giro.postRotate(grados);
        return Bitmap.createBitmap(foto, 0, 0, foto.getWidth(), foto.getHeight(), giro, true);
    }

    // Lee del EXIF cuantos grados hay que girar la foto (0, 90, 180 o 270)
    private static int leerGradosDeGiro(File archivo) {
        try {
            ExifInterface exif = new ExifInterface(archivo.getAbsolutePath());
            int orientacion = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientacion == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            }
            if (orientacion == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            }
            if (orientacion == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
        } catch (IOException e) {
            // Sin datos EXIF: dejamos la foto como esta
        }
        return 0;
    }

    // Si la foto mide mas de 1280 px de ancho, la reduce manteniendo la proporcion
    private static Bitmap reducirAnchoMaximo(Bitmap foto) {
        if (foto.getWidth() <= ANCHO_MAXIMO) {
            return foto;
        }
        double proporcion = (double) ANCHO_MAXIMO / foto.getWidth();
        int altoNuevo = (int) Math.round(foto.getHeight() * proporcion);
        return Bitmap.createScaledBitmap(foto, ANCHO_MAXIMO, altoNuevo, true);
    }
}
