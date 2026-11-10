package com.example.appreto.models;

import com.google.gson.annotations.SerializedName;

// Una solicitud de reparacion tal como la devuelve GET admin/solicitudes (solo administrador)
public class SolicitudAdmin {
    @SerializedName("id")
    private int id;

    @SerializedName("fecha")
    private String fecha;

    // pendiente, aceptada o rechazada
    @SerializedName("estado")
    private String estado;

    // Nombre del cliente que pidio la reparacion
    @SerializedName("usuario")
    private String usuario;

    // Aparato, ej. "Sony PlayStation 5"
    @SerializedName("dispositivo")
    private String dispositivo;

    // Departamento del aparato: Consolas, Moviles o Tablets
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("tienda")
    private String tienda;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("url_foto")
    private String urlFoto;

    // 0 y 0 = el cliente no compartio su ubicacion
    @SerializedName("latitud")
    private double latitud;

    @SerializedName("longitud")
    private double longitud;

    @SerializedName("motivo_rechazo")
    private String motivoRechazo;

    // Datos de la reparacion. Son null mientras la solicitud no se acepta.
    // Se usan Integer y Double (no int y double) para poder guardar null.
    @SerializedName("reparacion_id")
    private Integer reparacionId;

    // en_proceso o completada
    @SerializedName("reparacion_estado")
    private String reparacionEstado;

    @SerializedName("precio_estimado")
    private Double precioEstimado;

    @SerializedName("precio_final")
    private Double precioFinal;

    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getUsuario() { return usuario; }
    public String getDispositivo() { return dispositivo; }
    public String getTipo() { return tipo; }
    public String getTienda() { return tienda; }
    public String getDescripcion() { return descripcion; }
    public String getUrlFoto() { return urlFoto; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public Integer getReparacionId() { return reparacionId; }
    public String getReparacionEstado() { return reparacionEstado; }
    public Double getPrecioEstimado() { return precioEstimado; }
    public Double getPrecioFinal() { return precioFinal; }

    // true si el cliente compartio su ubicacion (0, 0 = sin permiso de GPS)
    public boolean tieneUbicacion() {
        return latitud != 0 || longitud != 0;
    }

    // true si la solicitud esta aceptada y su reparacion ya esta terminada
    public boolean estaCompletada() {
        return "completada".equals(reparacionEstado);
    }
}
