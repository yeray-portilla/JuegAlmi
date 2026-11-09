package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.models.Operacion;
import java.util.List;

public class OperacionAdapter extends RecyclerView.Adapter<OperacionAdapter.OperacionViewHolder> {

    private List<Operacion> operaciones;
    private OnReparacionActionListener listener;

    public interface OnReparacionActionListener {
        void onEdit(Operacion op);
        void onCancel(Operacion op);
    }

    public OperacionAdapter(List<Operacion> operaciones) {
        this.operaciones = operaciones;
    }

    public OperacionAdapter(List<Operacion> operaciones, OnReparacionActionListener listener) {
        this.operaciones = operaciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OperacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_operacion, parent, false);
        return new OperacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OperacionViewHolder holder, int position) {
        Operacion op = operaciones.get(position);
        holder.tvTipo.setText(op.getTipo() != null ? op.getTipo().toUpperCase() : "");
        holder.tvFecha.setText(op.getFecha());
        
        if (op.getEstado() != null) {
            holder.tvEstado.setText(op.getEstado().toUpperCase());
        } else {
            holder.tvEstado.setText("");
        }

        StringBuilder detalleText = new StringBuilder();
        if ("reparacion".equals(op.getTipo())) {
            detalleText.append(op.getDispositivo() != null ? op.getDispositivo() : "").append("\n");
            detalleText.append(op.getDescripcion() != null ? op.getDescripcion() : "");
            if ("rechazada".equals(op.getEstado()) && op.getMotivoRechazo() != null) {
                detalleText.append("\nMotivo: ").append(op.getMotivoRechazo());
            }
        } else if (op.getDetalle() != null) {
            for (Operacion.Detalle d : op.getDetalle()) {
                detalleText.append("• ").append(d.getNombre());
                if (d.getCantidad() != null) {
                    detalleText.append(" x").append(d.getCantidad());
                }
                detalleText.append("\n");
            }
        }
        holder.tvDetalle.setText(detalleText.toString().trim());

        if (op.getTotal() != null) {
            holder.tvTotal.setText("Total: " + op.getTotal() + " €");
            holder.tvTotal.setVisibility(View.VISIBLE);
        } else {
            holder.tvTotal.setVisibility(View.GONE);
        }

        if ("reparacion".equals(op.getTipo()) && "pendiente".equals(op.getEstado())) {
            holder.llReparacionAcciones.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(op);
            });
            holder.btnCancelar.setOnClickListener(v -> {
                if (listener != null) listener.onCancel(op);
            });
        } else {
            holder.llReparacionAcciones.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return operaciones != null ? operaciones.size() : 0;
    }

    static class OperacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo;
        TextView tvFecha;
        TextView tvEstado;
        TextView tvDetalle;
        TextView tvTotal;
        View llReparacionAcciones;
        View btnEditar;
        View btnCancelar;

        public OperacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tv_tipo);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvDetalle = itemView.findViewById(R.id.tv_detalle);
            tvTotal = itemView.findViewById(R.id.tv_total);
            llReparacionAcciones = itemView.findViewById(R.id.ll_reparacion_acciones);
            btnEditar = itemView.findViewById(R.id.btn_editar_reparacion);
            btnCancelar = itemView.findViewById(R.id.btn_cancelar_reparacion);
        }
    }
}
