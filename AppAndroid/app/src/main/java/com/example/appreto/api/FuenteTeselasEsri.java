package com.example.appreto.api;

import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

public class FuenteTeselasEsri extends XYTileSource {

    private static final String NOMBRE = "EsriWorldStreetMap";
    private static final String URL_BASE =
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/";
    private static final int ZOOM_MINIMO = 0;
    private static final int ZOOM_MAXIMO = 19;
    private static final int TAMANO_TESELA = 256;
    private static final String SIN_EXTENSION = "";

    public FuenteTeselasEsri() {
        super(NOMBRE, ZOOM_MINIMO, ZOOM_MAXIMO, TAMANO_TESELA, SIN_EXTENSION, new String[]{URL_BASE});
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        return getBaseUrl() + zoom + "/" + y + "/" + x;
    }
}
