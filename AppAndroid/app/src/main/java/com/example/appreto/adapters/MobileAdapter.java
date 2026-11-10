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
import com.example.appreto.models.Product;
import java.util.List;

// Lista de productos del catalogo (Dispositivos, Videojuegos y Consolas)
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

    // Crea la tarjeta de un producto (item_mobile.xml)
    @NonNull
    @Override
    public MobileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mobile, parent, false);
        return new MobileViewHolder(view);
    }

    // Rellena la tarjeta con los datos del producto
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

    // Descarga la foto del producto (foto_url) con Glide. Si no tiene o falla, se ve el icono de respaldo.
    private void cargarFoto(ImageView imagen, Product producto) {
        Glide.with(imagen)
                .load(RetrofitClient.construirUrlFoto(producto.getFotoUrl()))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imagen);
    }

    // Solo los videojuegos se alquilan: llevan el precio de alquiler y el boton "Alquilar".
    // Las consolas y los moviles/tablets solo se compran (igual que en la app Ionic).
    // Sin stock no se puede comprar, y sin copias no se puede alquilar (el boton se ve apagado, como en Ionic).
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

    // Activa o desactiva un boton. Desactivado se ve al 60 % para que el texto se siga leyendo (como en Ionic).
    private void activarBoton(View boton, boolean activo) {
        boton.setEnabled(activo);
        if (activo) {
            boton.setAlpha(1f);
        } else {
            boton.setAlpha(0.6f);
        }
    }

    // Linea gris debajo del nombre, como en Ionic:
    //  moviles/tablets "Samsung · Smartphone · 128 GB", consolas "Sony · 825 GB", videojuegos "Nintendo"
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

    // Anade un trozo al subtitulo separado por " · " (si el trozo esta vacio, no hace nada)
    private void anadirParte(StringBuilder texto, String parte) {
        if (parte == null || parte.isEmpty()) {
            return;
        }
        if (texto.length() > 0) {
            texto.append(" · ");
        }
        texto.append(parte);
    }

    // true si el producto se puede alquilar (solo los videojuegos). Lo usa tambien MobilesFragment.
    public static boolean esAlquilable(Product producto) {
        return "Videojuegos".equals(producto.getCategory());
    }

    // Avisa al Fragment de que se ha pulsado "Comprar"
    private void pulsarComprar(Product producto) {
        if (listener != null) {
            listener.onBuyClick(producto);
        }
    }

    // Avisa al Fragment de que se ha pulsado "Alquilar"
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
