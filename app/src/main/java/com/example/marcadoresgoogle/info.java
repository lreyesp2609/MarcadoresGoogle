package com.example.marcadoresgoogle;

import android.content.Context;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class info extends AppCompatActivity implements GoogleMap.InfoWindowAdapter {
    private Context context;

    public info(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View infoView = LayoutInflater.from(context).inflate(R.layout.info, null);

        TextView titleTextView = infoView.findViewById(R.id.titleTextView);
        TextView snippetTextView = infoView.findViewById(R.id.snippetTextView);
        ImageView Imagen = infoView.findViewById(R.id.imageView2);

        Bitmap foto = (Bitmap) marker.getTag();
        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());
        Imagen.setImageBitmap(foto);

        return infoView;
    }
}
