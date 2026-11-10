package com.example.appreto.fragments;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.appreto.R;
import com.example.appreto.models.Tienda;

// Pantalla Historia (menu lateral): por cada tienda, su historia
public class HistoriaFragment extends TiendasBaseFragment {

    // Pone el texto de la historia dentro de la tarjeta de la tienda
    @Override
    protected void anadirContenido(Tienda tienda, View tarjeta, LinearLayout contenido) {
        TextView textoHistoria = new TextView(requireContext());
        textoHistoria.setText(obtenerTextoHistoria(tienda));
        textoHistoria.setTextColor(getResources().getColor(R.color.text_primary, null));
        textoHistoria.setTextSize(15);
        textoHistoria.setLineSpacing(0, 1.3f);
        contenido.addView(textoHistoria);
    }

    // La historia de la tienda, o un aviso si todavia no tiene
    private String obtenerTextoHistoria(Tienda tienda) {
        String historia = tienda.getHistoria();
        if (historia == null || historia.trim().isEmpty()) {
            return "Esta tienda todavía no tiene historia.";
        }
        return historia;
    }
}
