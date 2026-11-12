package com.example.appreto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.api.ImagenHelper;
import com.example.appreto.api.PrecioHelper;
import com.example.appreto.api.RetrofitClient;
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
        Product producto = mobiles.get(position);
        holder.title.setText(producto.getTitle());
        holder.subtitle.setText(crearSubtitulo(producto));

        cargarFoto(holder.image, producto);
        mostrarPrecioYBotones(holder, producto);

        holder.btnBuy.setOnClickListener(v -> pulsarComprar(producto));
        holder.btnRent.setOnClickListener(v -> pulsarAlquilar(producto));
    }

    private void cargarFoto(ImageView imagen, Product producto) {
        String urlFoto = RetrofitClient.construirUrlFoto(producto.getFotoUrl());
        ImagenHelper.cargar(imagen, urlFoto, R.drawable.ic_placeholder);
    }

    private void mostrarPrecioYBotones(MobileViewHolder holder, Product producto) {
        String precio = PrecioHelper.formatearPrecio(producto.getPrice());
        holder.btnBuy.setVisibility(View.VISIBLE);
        activarBoton(holder.btnBuy, producto.hayStock());

        if (esAlquilable(producto)) {
            String alquiler = PrecioHelper.formatearPrecio(producto.getPrecioDiario());
            holder.price.setText(precio + " · alquiler " + alquiler + "/día");
            holder.btnRent.setVisibility(View.VISIBLE);
            activarBoton(holder.btnRent, producto.hayCopiasParaAlquilar());
        } else {
            holder.price.setText(precio);
            holder.btnRent.setVisibility(View.GONE);
        }
    }

    private void activarBoton(View boton, boolean activo) {
        boton.setEnabled(activo);
        if (activo) {
            boton.setAlpha(1f);
        } else {
            boton.setAlpha(0.6f);
        }
    }

    private String crearSubtitulo(Product producto) {
        StringBuilder subtitulo = new StringBuilder();
        anadirParte(subtitulo, producto.getMarca());

        String categoria = producto.getCategory();
        if (categoria.equals("Smartphones")) {
            anadirParte(subtitulo, "Smartphone");
        } else if (categoria.equals("Tablets")) {
            anadirParte(subtitulo, "Tablet");
        }

        anadirParte(subtitulo, producto.getAlmacenamiento());
        return subtitulo.toString();
    }

    private void anadirParte(StringBuilder texto, String parte) {
        if (parte == null || parte.isEmpty()) {
            return;
        }
        if (texto.length() > 0) {
            texto.append(" · ");
        }
        texto.append(parte);
    }

    public static boolean esAlquilable(Product producto) {
        return "Videojuegos".equals(producto.getCategory());
    }

    private void pulsarComprar(Product producto) {
        if (listener != null) {
            listener.onBuyClick(producto);
        }
    }

    private void pulsarAlquilar(Product producto) {
        if (listener != null) {
            listener.onRentClick(producto);
        }
    }

    @Override
    public int getItemCount() {
        if (mobiles == null) {
            return 0;
        }
        return mobiles.size();
    }

    static class MobileViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;
        TextView price;
        View btnBuy;
        View btnRent;

        public MobileViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            title = itemView.findViewById(R.id.item_title);
            subtitle = itemView.findViewById(R.id.item_subtitle);
            price = itemView.findViewById(R.id.item_price);
            btnBuy = itemView.findViewById(R.id.btn_buy);
            btnRent = itemView.findViewById(R.id.btn_rent);
        }
    }
}
