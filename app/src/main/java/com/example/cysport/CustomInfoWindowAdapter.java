package com.example.cysport;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
    }



    @Override
    public View getInfoWindow(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);

        TextView title = view.findViewById(R.id.PlaceName);
        TextView desc = view.findViewById(R.id.PlaceDesc);
        TextView rating = view.findViewById(R.id.PlaceRating);
        ImageView image = view.findViewById(R.id.PlaceImg);


        Object tag = marker.getTag();
        CustomInfoWindowData data = null;
        if (tag instanceof CustomInfoWindowData) {
            data = (CustomInfoWindowData) tag;
        }

        if (data != null) {
            title.setText(data.getTitle());
            desc.setText(data.getDesc());
            rating.setText(String.format("%.1f", data.getRating()));
            Glide.with(context)
                    .load(data.getImage()) // URL изображения
                    .override(300, 300)                  // Оптимизация размеров
                    .into(image);
        }


        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}