package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.models.Product;
import java.util.List;

public class MobileAdapter extends RecyclerView.Adapter<MobileAdapter.MobileViewHolder> {

    private List<Product> mobiles;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onBuyClick(Product product);
        void onRentClick(Product product);
    }

    public MobileAdapter(List<Product> mobiles, OnProductClickListener listener) {
        this.mobiles = mobiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MobileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mobile, parent, false);
        return new MobileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MobileViewHolder holder, int position) {
        Product mobile = mobiles.get(position);
        holder.title.setText(mobile.getTitle());
        holder.subtitle.setText(mobile.getCategory());
        // Formato para que sea como "449 €" pero usando Locale si es necesario
        // Aquí lo simplificamos a entero si no tiene decimales para que coincida con la UI
        int priceInt = (int) mobile.getPrice();
        if ("Videojuegos".equals(mobile.getCategory())) {
            double alquiler = mobile.getPrecioDiario() > 0 ? mobile.getPrecioDiario() : 4.0;
            holder.price.setText(priceInt + " € · alquiler " + alquiler + " €/día");
            holder.btnBuy.setVisibility(View.VISIBLE);
            holder.btnRent.setVisibility(View.VISIBLE);
        } else if ("Consolas".equals(mobile.getCategory())) {
            double alquiler = mobile.getPrecioDiario() > 0 ? mobile.getPrecioDiario() : 15.0;
            holder.price.setText(priceInt + " € · alquiler " + alquiler + " €/día");
            holder.btnBuy.setVisibility(View.VISIBLE);
            holder.btnRent.setVisibility(View.VISIBLE);
        } else {
            holder.price.setText(priceInt + " €");
            holder.btnBuy.setVisibility(View.VISIBLE); // Dispositivos solo se compran
            holder.btnRent.setVisibility(View.GONE);
        }

        holder.btnBuy.setOnClickListener(v -> {
            if (listener != null) listener.onBuyClick(mobile);
        });

        holder.btnRent.setOnClickListener(v -> {
            if (listener != null) listener.onRentClick(mobile);
        });
    }

    @Override
    public int getItemCount() {
        return mobiles != null ? mobiles.size() : 0;
    }

    static class MobileViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        TextView price;
        View btnBuy;
        View btnRent;

        public MobileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            subtitle = itemView.findViewById(R.id.item_subtitle);
            price = itemView.findViewById(R.id.item_price);
            btnBuy = itemView.findViewById(R.id.btn_buy);
            btnRent = itemView.findViewById(R.id.btn_rent);
        }
    }
}
