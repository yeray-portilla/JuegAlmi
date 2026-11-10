package com.example.appreto.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.appreto.R;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Tienda;

import java.util.List;

// Pantalla Galeria (menu lateral): por cada tienda, sus fotos en una fila que se desliza en horizontal
public class GaleriaFragment extends TiendasBaseFragment {

    // Mete la fila de fotos (o el aviso "No hay fotos") dentro de la tarjeta de la tienda
    @Override
    protected void anadirContenido(Tienda tienda, View tarjeta, LinearLayout contenido) {
        List<String> fotos = tienda.getFotos();

        if (fotos.isEmpty()) {
            mostrarSinFotos(contenido);
            return;
        }

        View filaFotos = LayoutInflater.from(requireContext()).inflate(R.layout.view_galeria_fotos, contenido, false);
        LinearLayout contenedorFotos = filaFotos.findViewById(R.id.ll_fotos_tienda);

        for (String foto : fotos) {
            ImageView imagen = crearImagen(contenedorFotos, foto, tienda.getNombre());
            contenedorFotos.addView(imagen);
        }

        contenido.addView(filaFotos);
    }

    // Crea una foto de la galeria y la descarga con Glide (si falla se ve el icono de respaldo)
    private ImageView crearImagen(LinearLayout contenedorFotos, String foto, String nombreTienda) {
        ImageView imagen = (ImageView) LayoutInflater.from(requireContext())
                .inflate(R.layout.item_foto_galeria, contenedorFotos, false);
        imagen.setContentDescription(nombreTienda);

        Glide.with(this)
                .load(RetrofitClient.construirUrlFoto(foto))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(imagen);

        return imagen;
    }

    // Texto que se ve cuando la tienda no tiene fotos
    private void mostrarSinFotos(LinearLayout contenido) {
        TextView textoSinFotos = new TextView(requireContext());
        textoSinFotos.setText("No hay fotos de esta tienda.");
        textoSinFotos.setTextColor(getResources().getColor(R.color.text_secondary, null));
        contenido.addView(textoSinFotos);
    }
}
