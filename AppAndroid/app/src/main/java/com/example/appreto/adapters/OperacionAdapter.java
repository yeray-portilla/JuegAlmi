package com.example.appreto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.appreto.R;
import com.example.appreto.api.PrecioHelper;
import com.example.appreto.api.RetrofitClient;
import com.example.appreto.models.Operacion;
import java.util.List;

/**
 * Tarjetas de "Mis operaciones". Sirve para las tres pestanas:
 *  - Compras:        "Compra #N", productos, fecha y total.
 *  - Alquileres:     "Alquiler #N", juegos, fecha, estado, total y boton Cancelar (pendiente) o Devolver (activo).
 *  - Mis solicitudes: aparato, descripcion, fecha, estado, motivo, foto, precio y boton Cancelar (pendiente).
 * Lo que hace cada boton lo decide MisOperacionesFragment (interfaz AccionesOperacion).
 */
public class OperacionAdapter extends RecyclerView.Adapter<OperacionAdapter.OperacionViewHolder> {

    // Lo que puede hacer el usuario desde una tarjeta
    public interface AccionesOperacion {
        void cancelarSolicitud(Operacion solicitud);
        // Devuelve un alquiler activo o cancela uno pendiente (la API usa la misma ruta para los dos)
        void devolverAlquiler(Operacion alquiler);
    }

    private final List<Operacion> operaciones;
    private final AccionesOperacion acciones;

    public OperacionAdapter(List<Operacion> operaciones, AccionesOperacion acciones) {
        this.operaciones = operaciones;
        this.acciones = acciones;
    }

    // Crea la tarjeta de una operacion (item_operacion.xml)
    @NonNull
    @Override
    public OperacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_operacion, parent, false);
        return new OperacionViewHolder(view);
    }

    // Rellena la tarjeta segun el tipo de operacion
    @Override
    public void onBindViewHolder(@NonNull OperacionViewHolder holder, int position) {
        Operacion operacion = operaciones.get(position);

        if (esSolicitud(operacion)) {
            rellenarSolicitud(holder, operacion);
        } else {
            rellenarCompraOAlquiler(holder, operacion);
        }

        mostrarEstado(holder.textoEstado, operacion);
        mostrarTotal(holder.textoTotal, operacion);
        cargarFoto(holder.imagenFoto, operacion);
        mostrarBotonAccion(holder.botonAccion, operacion);
    }

    // true si la operacion es una solicitud de reparacion
    private boolean esSolicitud(Operacion operacion) {
        return "reparacion".equals(operacion.getTipo());
    }

    // true si la operacion es un alquiler
    private boolean esAlquiler(Operacion operacion) {
        return "alquiler".equals(operacion.getTipo());
    }

    // Compra o alquiler: "Compra #N" / "Alquiler #N", lo que incluye y la fecha
    private void rellenarCompraOAlquiler(OperacionViewHolder holder, Operacion operacion) {
        String titulo;
        String fecha;
        if (esAlquiler(operacion)) {
            titulo = "Alquiler #" + operacion.getId();
            fecha = "Desde el " + operacion.getFecha() + " · ";
        } else {
            titulo = "Compra #" + operacion.getId();
            fecha = "El " + operacion.getFecha() + " · ";
        }
        holder.textoTitulo.setText(titulo);
        holder.textoFecha.setText(fecha);
        holder.textoDetalle.setText(crearTextoDetalle(operacion));
        holder.textoMotivo.setVisibility(View.GONE);
    }

    // Solicitud: aparato, descripcion, fecha y, si se rechazo, el motivo
    private void rellenarSolicitud(OperacionViewHolder holder, Operacion solicitud) {
        holder.textoTitulo.setText(solicitud.getDispositivo());
        holder.textoDetalle.setText(solicitud.getDescripcion());
        holder.textoFecha.setText(solicitud.getFecha() + " · ");

        boolean rechazada = "rechazada".equals(solicitud.getEstado());
        if (rechazada && solicitud.getMotivoRechazo() != null) {
            holder.textoMotivo.setText("Motivo: " + solicitud.getMotivoRechazo());
            holder.textoMotivo.setVisibility(View.VISIBLE);
        } else {
            holder.textoMotivo.setVisibility(View.GONE);
        }
    }

    // Lo que incluye la compra o el alquiler: "Juego A x2, Juego B"
    private String crearTextoDetalle(Operacion operacion) {
        StringBuilder texto = new StringBuilder();
        if (operacion.getDetalle() == null) {
            return "";
        }
        for (Operacion.Detalle linea : operacion.getDetalle()) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(linea.getNombre());
            if (linea.getCantidad() != null && linea.getCantidad() > 1) {
                texto.append(" x").append(linea.getCantidad());
            }
        }
        return texto.toString();
    }

    // Estado con su texto y su color (como .estado-op en Ionic)
    private void mostrarEstado(TextView texto, Operacion operacion) {
        String estado = operacion.getEstado();
        if (estado == null) {
            estado = "";
        }
        texto.setText(obtenerTextoEstado(operacion, estado));
        texto.setTextColor(obtenerColorEstado(texto.getContext(), estado));
    }

    // Texto del estado. En las solicitudes es una frase; en compras y alquileres, la palabra con mayuscula.
    private String obtenerTextoEstado(Operacion operacion, String estado) {
        if (esSolicitud(operacion)) {
            if (estado.equals("pendiente")) {
                return "Pendiente de revisar";
            }
            if (estado.equals("rechazada")) {
                return "Rechazada";
            }
            if (estado.equals("en_proceso")) {
                return "En reparación";
            }
            if (estado.equals("completada")) {
                return "Reparada";
            }
        }
        return ponerMayuscula(estado);
    }

    // "activo" -> "Activo"
    private String ponerMayuscula(String texto) {
        if (texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    // Verde = terminado bien, ambar = en marcha, rojo = rechazado, gris = finalizado u otro
    private int obtenerColorEstado(Context contexto, String estado) {
        int color;
        if (estado.equals("activo") || estado.equals("completada")) {
            color = R.color.estado_completada;
        } else if (estado.equals("pendiente") || estado.equals("en_proceso")) {
            color = R.color.colorAccent;
        } else if (estado.equals("rechazada")) {
            color = R.color.estado_rechazada;
        } else {
            color = R.color.text_secondary;
        }
        return contexto.getResources().getColor(color, null);
    }

    // Total en ambar. Las solicitudes pendientes o rechazadas aun no tienen precio (total = null).
    private void mostrarTotal(TextView texto, Operacion operacion) {
        if (operacion.getTotal() == null) {
            texto.setVisibility(View.GONE);
            return;
        }
        texto.setText(PrecioHelper.formatearPrecio(operacion.getTotal()));
        texto.setVisibility(View.VISIBLE);
    }

    // Foto de la operacion (url_foto) con Glide. Si no tiene foto, se oculta la imagen.
    private void cargarFoto(ImageView imagen, Operacion operacion) {
        String urlFoto = RetrofitClient.construirUrlFoto(operacion.getUrlFoto());
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

    // Boton de la tarjeta:
    //  - solicitud pendiente -> "Cancelar" (DELETE soporte/solicitudes/{id})
    //  - alquiler pendiente  -> "Cancelar" (PUT operaciones/alquileres/{id}/devolver)
    //  - alquiler activo     -> "Devolver" (la misma ruta)
    //  - el resto            -> sin boton
    private void mostrarBotonAccion(Button boton, Operacion operacion) {
        boolean pendiente = "pendiente".equals(operacion.getEstado());
        boolean activo = "activo".equals(operacion.getEstado());

        if (esSolicitud(operacion) && pendiente) {
            boton.setText("Cancelar");
            boton.setOnClickListener(v -> acciones.cancelarSolicitud(operacion));
            boton.setVisibility(View.VISIBLE);
        } else if (esAlquiler(operacion) && pendiente) {
            boton.setText("Cancelar");
            boton.setOnClickListener(v -> acciones.devolverAlquiler(operacion));
            boton.setVisibility(View.VISIBLE);
        } else if (esAlquiler(operacion) && activo) {
            boton.setText("Devolver");
            boton.setOnClickListener(v -> acciones.devolverAlquiler(operacion));
            boton.setVisibility(View.VISIBLE);
        } else {
            boton.setOnClickListener(null);
            boton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return operaciones.size();
    }

    static class OperacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenFoto;
        TextView textoTitulo;
        TextView textoDetalle;
        TextView textoFecha;
        TextView textoEstado;
        TextView textoMotivo;
        TextView textoTotal;
        Button botonAccion;

        OperacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenFoto = itemView.findViewById(R.id.iv_foto_operacion);
            textoTitulo = itemView.findViewById(R.id.tv_titulo_operacion);
            textoDetalle = itemView.findViewById(R.id.tv_detalle_operacion);
            textoFecha = itemView.findViewById(R.id.tv_fecha_operacion);
            textoEstado = itemView.findViewById(R.id.tv_estado_operacion);
            textoMotivo = itemView.findViewById(R.id.tv_motivo_operacion);
            textoTotal = itemView.findViewById(R.id.tv_total_operacion);
            botonAccion = itemView.findViewById(R.id.btn_accion_operacion);
        }
    }
}
