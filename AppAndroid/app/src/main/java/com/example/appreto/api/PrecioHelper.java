package com.example.appreto.api;

import java.util.Locale;

// Formato de los precios que se ven en la app (catalogo, operaciones, administracion)
public class PrecioHelper {

    // 449.0 -> "449 €"   55.5 -> "55,50 €"   (sin decimales si es un numero entero, como en la app Ionic)
    public static String formatearPrecio(double precio) {
        if (precio == Math.floor(precio)) {
            return ((long) precio) + " €";
        }
        return String.format(new Locale("es", "ES"), "%.2f €", precio);
    }
}
