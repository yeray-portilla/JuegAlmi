package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appreto.R;
import com.example.appreto.api.PrecioHelper;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.SolicitudAdmin;

import java.util.List;

// Lista de solicitudes de reparacion de la pantalla de Administracion.
// Solo pinta las tarjetas; lo que hace cada boton lo decide AdminFragment (interfaz AccionesSolicitud).
public class SolicitudAdminAdapter extends RecyclerView.Adapter<SolicitudAdminAdapter.SolicitudViewHolder> {

    // Lo que puede hacer el administrador con una solicitud
    public interface AccionesSolicitud {
        void aceptar(SolicitudAdmin solicitud);
        void rechazar(SolicitudAdmin solicitud);
        void completar(SolicitudAdmin solicitud);
        void verUbicacion(SolicitudAdmin solicitud);
    }

    private final List<SolicitudAdmin> solicitudes;
    private final AccionesSolicitud acciones;

    public SolicitudAdminAdapter(List<SolicitudAdmin> solicitudes, AccionesSolicitud acciones) {
        this.solicitudes = solicitudes;
        this.acciones = acciones;
    }

    // Crea la tarjeta de una solicitud (item_solicitud_admin.xml)
    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_admin, parent, false);
        return new SolicitudViewHolder(vista);
    }

    // Rellena la tarjeta con los datos de la solicitud
    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudAdmin solicitud = solicitudes.get(position);

        holder.textoAparato.setText(solicitud.getDispositivo());
        holder.textoTipoTienda.setText(solicitud.getTipo() + " · " + solicitud.getTienda());
        holder.textoCliente.setText("Cliente: " + solicitud.getUsuario() + " · " + solicitud.getFecha());
        holder.textoDescripcion.setText(solicitud.getDescripcion());

        cargarFoto(holder.imagenFoto, solicitud);
        mostrarUbicacion(holder.textoUbicacion, solicitud);
        mostrarMotivoRechazo(holder.textoMotivo, solicitud);
        mostrarDatosReparacion(holder, solicitud);
        mostrarBotones(holder, solicitud);
    }

    // Foto del aparato con Glide. Si la solicitud no tiene foto, no se ve la imagen.
    private void cargarFoto(ImageView imagen, SolicitudAdmin solicitud) {
        String urlFoto = RetrofitClient.construirUrlFoto(solicitud.getUrlFoto());
        if (urlFoto == null) {
            Glide.with(imagen).clear(imagen); // por si esta tarjeta se reutiliza y tenia la foto de otra
            imagen.setVisibility(View.GONE);
            return;
        }
        imagen.setVisibility(View.VISIBLE);
        Glide.with(imagen)
                .load(urlFoto)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imagen);
    }

    // "Ubicación: lat, lon" en ambar y pulsable (abre el mapa), o "no compartida" si el cliente no dio permiso
    private void mostrarUbicacion(TextView texto, SolicitudAdmin solicitud) {
        if (solicitud.tieneUbicacion()) {
            texto.setText("Ubicación: " + solicitud.getLatitud() + ", " + solicitud.getLongitud());
            texto.setTextColor(texto.getContext().getResources().getColor(R.color.colorAccent, null));
            texto.setOnClickListener(v -> acciones.verUbicacion(solicitud));
        } else {
            texto.setText("Ubicación: no compartida");
            texto.setTextColor(texto.getContext().getResources().getColor(R.color.text_secondary, null));
            texto.setOnClickListener(null);
        }
    }

    // Motivo del rechazo (solo en las rechazadas que lo tienen)
    private void mostrarMotivoRechazo(TextView texto, SolicitudAdmin solicitud) {
        boolean rechazada = "rechazada".equals(solicitud.getEstado());
        if (rechazada && solicitud.getMotivoRechazo() != null) {
            texto.setText("Motivo: " + solicitud.getMotivoRechazo());
            texto.setVisibility(View.VISIBLE);
        } else {
            texto.setVisibility(View.GONE);
        }
    }

    // En las aceptadas: "Reparación #N · estimado X €" y, si ya esta terminada, "Reparada · X €"
    private void mostrarDatosReparacion(SolicitudViewHolder holder, SolicitudAdmin solicitud) {
        if (!tieneReparacion(solicitud)) {
            holder.textoReparacion.setVisibility(View.GONE);
            holder.textoCompletada.setVisibility(View.GONE);
            return;
        }

        String textoReparacion = "Reparación #" + solicitud.getReparacionId();
        if (solicitud.getPrecioEstimado() != null) {
            textoReparacion = textoReparacion + " · estimado " + PrecioHelper.formatearPrecio(solicitud.getPrecioEstimado());
        }
        holder.textoReparacion.setText(textoReparacion);
        holder.textoReparacion.setVisibility(View.VISIBLE);

        if (solicitud.estaCompletada()) {
            String textoCompletada = "Reparada";
            if (solicitud.getPrecioFinal() != null) {
                textoCompletada = textoCompletada + " · " + PrecioHelper.formatearPrecio(solicitud.getPrecioFinal());
            }
            holder.textoCompletada.setText(textoCompletada);
            holder.textoCompletada.setVisibility(View.VISIBLE);
        } else {
            holder.textoCompletada.setVisibility(View.GONE);
        }
    }

    // Pendiente: Aceptar y Rechazar. Aceptada sin terminar: Marcar completada. El resto: sin botones.
    private void mostrarBotones(SolicitudViewHolder holder, SolicitudAdmin solicitud) {
        boolean pendiente = "pendiente".equals(solicitud.getEstado());
        boolean sePuedeCompletar = tieneReparacion(solicitud) && !solicitud.estaCompletada();

        mostrarSiOcultar(holder.botonAceptar, pendiente);
        mostrarSiOcultar(holder.botonRechazar, pendiente);
        mostrarSiOcultar(holder.botonCompletar, sePuedeCompletar);

        holder.botonAceptar.setOnClickListener(v -> acciones.aceptar(solicitud));
        holder.botonRechazar.setOnClickListener(v -> acciones.rechazar(solicitud));
        holder.botonCompletar.setOnClickListener(v -> acciones.completar(solicitud));
    }

    // true si la solicitud esta aceptada y ya tiene su reparacion creada
    private boolean tieneReparacion(SolicitudAdmin solicitud) {
        return "aceptada".equals(solicitud.getEstado()) && solicitud.getReparacionId() != null;
    }

    // Muestra la vista si "mostrar" es true y la oculta si es false
    private void mostrarSiOcultar(View vista, boolean mostrar) {
        if (mostrar) {
            vista.setVisibility(View.VISIBLE);
        } else {
            vista.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenFoto;
        TextView textoAparato;
        TextView textoTipoTienda;
        TextView textoCliente;
        TextView textoDescripcion;
        TextView textoUbicacion;
        TextView textoMotivo;
        TextView textoReparacion;
        TextView textoCompletada;
        View botonAceptar;
        View botonRechazar;
        View botonCompletar;

        SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenFoto = itemView.findViewById(R.id.iv_foto_solicitud);
            textoAparato = itemView.findViewById(R.id.tv_aparato_solicitud);
            textoTipoTienda = itemView.findViewById(R.id.tv_tipo_tienda_solicitud);
            textoCliente = itemView.findViewById(R.id.tv_cliente_solicitud);
            textoDescripcion = itemView.findViewById(R.id.tv_descripcion_solicitud);
            textoUbicacion = itemView.findViewById(R.id.tv_ubicacion_solicitud);
            textoMotivo = itemView.findViewById(R.id.tv_motivo_solicitud);
            textoReparacion = itemView.findViewById(R.id.tv_reparacion_solicitud);
            textoCompletada = itemView.findViewById(R.id.tv_completada_solicitud);
            botonAceptar = itemView.findViewById(R.id.btn_aceptar_solicitud);
            botonRechazar = itemView.findViewById(R.id.btn_rechazar_solicitud);
            botonCompletar = itemView.findViewById(R.id.btn_completar_solicitud);
        }
    }
}
