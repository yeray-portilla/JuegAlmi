package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.models.Mobile;
import java.util.List;

public class MobileAdapter extends RecyclerView.Adapter<MobileAdapter.MobileViewHolder> {

    private List<Mobile> mobiles;

    public MobileAdapter(List<Mobile> mobiles) {
        this.mobiles = mobiles;
    }

    @NonNull
    @Override
    public MobileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mobile, parent, false);
        return new MobileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MobileViewHolder holder, int position) {
        Mobile mobile = mobiles.get(position);
        holder.title.setText(mobile.getTitle());
        holder.subtitle.setText(mobile.getCategory());
        // Formato para que sea como "449 €" pero usando Locale si es necesario
        // Aquí lo simplificamos a entero si no tiene decimales para que coincida con la UI
        int priceInt = (int) mobile.getPrice();
        if (mobile.getCategory().equals("Videojuegos")) {
            holder.price.setText(priceInt + " € · alquiler 4 €/día");
            holder.btnRent.setVisibility(View.VISIBLE);
        } else if (mobile.getCategory().equals("Consolas")) {
            holder.price.setText(priceInt + " € · alquiler 15 €/día");
            holder.btnRent.setVisibility(View.VISIBLE);
        } else {
            holder.price.setText(priceInt + " €");
            holder.btnRent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mobiles != null ? mobiles.size() : 0;
    }

    static class MobileViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        TextView price;
        View btnRent;

        public MobileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            subtitle = itemView.findViewById(R.id.item_subtitle);
            price = itemView.findViewById(R.id.item_price);
            btnRent = itemView.findViewById(R.id.btn_rent);
        }
    }
}
