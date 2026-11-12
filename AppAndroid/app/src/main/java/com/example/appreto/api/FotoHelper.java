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

public class FotoHelper {

    private static final int ANCHO_MAXIMO = 1280;

    private static final int CALIDAD_JPEG = 65;

    public static File obtenerArchivoFoto(Context context) {
        File carpeta = new File(context.getCacheDir(), "fotos");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return new File(carpeta, "foto_reparacion.jpg");
    }

    public static Uri crearUriParaFoto(Context context) {
        File archivo = obtenerArchivoFoto(context);
        String autoridad = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, autoridad, archivo);
    }

    public static Bitmap leerFotoReducida(File archivo) {
        Bitmap foto = leerSinGastarMuchaMemoria(archivo);
        if (foto == null) {
            return null;
        }
        Bitmap fotoDerecha = girarSegunExif(foto, archivo);
        return reducirAnchoMaximo(fotoDerecha);
    }

    public static String convertirABase64(Bitmap foto) {
        ByteArrayOutputStream bytesFoto = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, bytesFoto);
        return Base64.encodeToString(bytesFoto.toByteArray(), Base64.NO_WRAP);
    }

    private static Bitmap leerSinGastarMuchaMemoria(File archivo) {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);

        int ladoMasLargo = Math.max(opciones.outWidth, opciones.outHeight);
        int reduccion = 1;
        while (ladoMasLargo / (reduccion * 2) >= ANCHO_MAXIMO) {
            reduccion = reduccion * 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = reduccion;
        return BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);
    }

    private static Bitmap girarSegunExif(Bitmap foto, File archivo) {
        int grados = leerGradosDeGiro(archivo);
        if (grados == 0) {
            return foto;
        }
        Matrix giro = new Matrix();
        giro.postRotate(grados);
        return Bitmap.createBitmap(foto, 0, 0, foto.getWidth(), foto.getHeight(), giro, true);
    }

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
        }
        return 0;
    }

    private static Bitmap reducirAnchoMaximo(Bitmap foto) {
        if (foto.getWidth() <= ANCHO_MAXIMO) {
            return foto;
        }
        double proporcion = (double) ANCHO_MAXIMO / foto.getWidth();
        int altoNuevo = (int) Math.round(foto.getHeight() * proporcion);
        return Bitmap.createScaledBitmap(foto, ANCHO_MAXIMO, altoNuevo, true);
    }
}
