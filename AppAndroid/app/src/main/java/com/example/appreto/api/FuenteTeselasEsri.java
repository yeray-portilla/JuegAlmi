package com.example.appreto.api;

import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

/**
 * Fuente de teselas (los "cuadraditos" de imagen que forman el mapa) de Esri World Street Map, para osmdroid.
 *
 * Por que una clase propia: osmdroid construye la URL como zoom/x/y (el orden de OpenStreetMap),
 * pero Esri la quiere como zoom/y/x (primero la fila y, luego la columna x) y sin extension (.png).
 * Por eso sobrescribimos getTileURLString() y montamos la URL nosotros.
 *
 * Ejemplo de URL: https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/16/25000/32000
 */
public class FuenteTeselasEsri extends XYTileSource {

    private static final String NOMBRE = "EsriWorldStreetMap";
    private static final String URL_BASE =
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/";
    private static final int ZOOM_MINIMO = 0;
    private static final int ZOOM_MAXIMO = 19;
    private static final int TAMANO_TESELA = 256; // pixeles de cada tesela
    private static final String SIN_EXTENSION = ""; // Esri no lleva ".png" al final

    // Crea la fuente con sus datos: nombre, zoom minimo y maximo, tamano, extension y servidor
    public FuenteTeselasEsri() {
        super(NOMBRE, ZOOM_MINIMO, ZOOM_MAXIMO, TAMANO_TESELA, SIN_EXTENSION, new String[]{URL_BASE});
    }

    // URL de una tesela en el orden de Esri: base + zoom + "/" + y + "/" + x
    // (pMapTileIndex es un numero en el que osmdroid guarda juntos el zoom, la x y la y de la tesela)
    @Override
    public String getTileURLString(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        return getBaseUrl() + zoom + "/" + y + "/" + x;
    }
}
