package com.example.appreto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appreto.R;
import com.example.appreto.adapters.MobileAdapter;
import com.example.appreto.models.Mobile;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MobilesFragment extends Fragment {

    private String currentCategory;

    public MobilesFragment() {
        this.currentCategory = "Móviles";
    }

    public MobilesFragment(String category) {
        this.currentCategory = category;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobiles, container, false);
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        
        final List<Mobile> dummyDataList = new ArrayList<>();
        if (currentCategory.equals("Dispositivos")) {
            dummyDataList.add(new Mobile("Smartphone X", "Smartphones", 449));
            dummyDataList.add(new Mobile("iPhone 14", "Smartphones", 799));
            dummyDataList.add(new Mobile("Tablet Y", "Tablets", 329));
            dummyDataList.add(new Mobile("Samsung Galaxy S23", "Smartphones", 850));
            dummyDataList.add(new Mobile("iPad Air", "Tablets", 650));
            dummyDataList.add(new Mobile("Xiaomi 13", "Smartphones", 400));
        } else if (currentCategory.equals("Videojuegos")) {
            dummyDataList.add(new Mobile("The Legend of Zelda: Tears of the Kingdom", "Videojuegos", 69));
            dummyDataList.add(new Mobile("Super Mario Bros. Wonder", "Videojuegos", 59));
            dummyDataList.add(new Mobile("Marvel's Spider-Man 2", "Videojuegos", 79));
            dummyDataList.add(new Mobile("Elden Ring", "Videojuegos", 49));
        } else if (currentCategory.equals("Consolas")) {
            dummyDataList.add(new Mobile("PlayStation 5", "Consolas", 499));
            dummyDataList.add(new Mobile("Nintendo Switch OLED", "Consolas", 349));
            dummyDataList.add(new Mobile("Xbox Series X", "Consolas", 499));
            dummyDataList.add(new Mobile("Steam Deck OLED", "Consolas", 569));
        }

        MobileAdapter adapter = new MobileAdapter(dummyDataList);
        recyclerView.setAdapter(adapter);

        if (currentCategory.equals("Dispositivos")) {
            tabLayout.setVisibility(View.VISIBLE);
            
            // Lógica básica para filtrar (ejemplo)
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    List<Mobile> filteredList = new ArrayList<>();
                    if (tab.getPosition() == 0) { // Todos
                        filteredList.addAll(dummyDataList);
                    } else if (tab.getPosition() == 1) { // Smartphones
                        for (Mobile m : dummyDataList) {
                            if (m.getCategory().equals("Smartphones")) {
                                filteredList.add(m);
                            }
                        }
                    } else if (tab.getPosition() == 2) { // Tablets
                        for (Mobile m : dummyDataList) {
                            if (m.getCategory().equals("Tablets")) {
                                filteredList.add(m);
                            }
                        }
                    }
                    recyclerView.setAdapter(new MobileAdapter(filteredList));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
            
        } else {
            tabLayout.setVisibility(View.GONE);
        }
        
        return view;
    }
}
