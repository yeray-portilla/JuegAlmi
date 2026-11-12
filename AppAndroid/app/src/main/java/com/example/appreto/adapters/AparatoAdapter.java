package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appreto.R;
import com.example.appreto.models.Aparato;

import java.util.List;

public class AparatoAdapter extends RecyclerView.Adapter<AparatoAdapter.AparatoViewHolder> {

    public interface AlPedirReparacion {
        void pedirReparacion(Aparato aparato);
    }

    private final List<Aparato> aparatos;
    private final AlPedirReparacion alPedirReparacion;

    public AparatoAdapter(List<Aparato> aparatos, AlPedirReparacion alPedirReparacion) {
        this.aparatos = aparatos;
        this.alPedirReparacion = alPedirReparacion;
    }

    @NonNull
    @Override
    public AparatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aparato, parent, false);
        return new AparatoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AparatoViewHolder holder, int position) {
        Aparato aparato = aparatos.get(position);
        holder.textoNombre.setText(aparato.getNombre());
        holder.textoTipo.setText(aparato.getDepartamento());
        holder.botonReparar.setOnClickListener(v -> alPedirReparacion.pedirReparacion(aparato));
    }

    @Override
    public int getItemCount() {
        return aparatos.size();
    }

    static class AparatoViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombre;
        TextView textoTipo;
        View botonReparar;

        AparatoViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNombre = itemView.findViewById(R.id.tv_nombre_aparato);
            textoTipo = itemView.findViewById(R.id.tv_tipo_aparato);
            botonReparar = itemView.findViewById(R.id.btn_reparar_aparato);
        }
    }
}
