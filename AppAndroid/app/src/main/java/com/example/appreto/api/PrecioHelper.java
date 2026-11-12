package com.example.appreto.api;

import java.util.Locale;

public class PrecioHelper {

    public static String formatearPrecio(double precio) {
        if (precio == Math.floor(precio)) {
            return ((long) precio) + " €";
        }
        return String.format(new Locale("es", "ES"), "%.2f €", precio);
    }
}
