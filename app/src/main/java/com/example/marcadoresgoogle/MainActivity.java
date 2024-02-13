package com.example.marcadoresgoogle;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;

import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener {

    GoogleMap Mapa;

    private RequestQueue requestQueue;
    private static final String API_KEY = "AIzaSyCx2klhltS0foiCjqvuxh27SV67y_VyZ_w";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Mapa = googleMap;
        Mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        Mapa.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate camUpd1 =
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(-1.012529191970843, -79.46946021380661), 19);
        Mapa.moveCamera(camUpd1);
        LatLng madrid = new LatLng(-1.012529191970843, -79.46946021380661);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(45)
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);



        info infoWindowAdapter = new  info (this);
        Mapa.setInfoWindowAdapter(infoWindowAdapter);
        Mapa.animateCamera(camUpd3);
        Mapa.setOnMapClickListener(this);

    }
    double latitud, longitud;
    @Override
    public void onMapClick(LatLng latLng) {

        LatLng punto = new LatLng(latLng.latitude, latLng.longitude);
        Mapa.addMarker(new MarkerOptions().position(punto));

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?fields=name,types&location=" + latLng.latitude + "," + latLng.longitude + "&radius=1500&type=bar&key=" + API_KEY;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject place = results.getJSONObject(i);
                                String name = place.getString("name");
                                JSONArray typesArray = place.optJSONArray("types");
                                String placeType = getPlaceType(typesArray);
                                String placeId = place.getString("place_id");
                                JSONObject openingHours = place.optJSONObject("opening_hours");
                                String openingHoursStr;
                                if (openingHours != null) {
                                    JSONArray weekdayText = openingHours.optJSONArray("weekday_text");
                                    if (weekdayText != null) {
                                        StringBuilder sb = new StringBuilder();
                                        for (int j = 0; j < weekdayText.length(); j++) {
                                            sb.append(weekdayText.getString(j)).append("\n");
                                        }
                                        openingHoursStr = sb.toString();
                                    } else {
                                        openingHoursStr = "Horarios no disponibles";
                                    }
                                } else {
                                    openingHoursStr = "Horarios no disponibles";
                                }
                                String Ubicacion = place.getString("vicinity");
                                JSONObject geometry = place.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double placeLat = location.getDouble("lat");
                                double placeLng = location.getDouble("lng");

                                JSONArray photos = place.optJSONArray("photos");

                                if (photos != null && photos.length() > 0) {
                                    JSONObject firstPhoto = photos.getJSONObject(0);
                                    String photoReference = firstPhoto.getString("photo_reference");
                                    String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?" +
                                            "fields=name%2Crating%2Cformatted_phone_number" +
                                            "&place_id=" + placeId +
                                            "&key=" + API_KEY;

                                    JsonObjectRequest detailsRequest = new JsonObjectRequest(Request.Method.GET, detailsUrl, null,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        String rating = response.getJSONObject("result").getString("rating");
                                                        String phoneNumber = response.getJSONObject("result").getString("formatted_phone_number");

                                                        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                                                                "maxwidth=200" +
                                                                "&photo_reference=" + photoReference +
                                                                "&key=" + API_KEY;

                                                        ImageRequest imageRequest = new ImageRequest(photoUrl,
                                                                new Response.Listener<Bitmap>() {
                                                                    @Override
                                                                    public void onResponse(Bitmap response) {
                                                                        LatLng placeLatLng = new LatLng(placeLat, placeLng);
                                                                        MarkerOptions markerOptions = new MarkerOptions()
                                                                                .position(placeLatLng)
                                                                                .title(name)
                                                                                .snippet("Tipo: " + placeType + "\nCalificacion: " + rating + "\nTelefono: " + phoneNumber + "\nUbicacion: " +
                                                                                        Ubicacion + "\nHorarios: " + openingHoursStr);
                                                                        Marker marker = Mapa.addMarker(markerOptions);
                                                                        marker.setTag(response);
                                                                    }
                                                                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                                                                new Response.ErrorListener() {
                                                                    @Override
                                                                    public void onErrorResponse(VolleyError error) {
                                                                        error.printStackTrace();
                                                                    }
                                                                });

                                                        requestQueue.add(imageRequest);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                }
                                            });
                                    requestQueue.add(detailsRequest);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        requestQueue.add(request);
    }

    private String getPlaceType(JSONArray typesArray) {
        if (typesArray != null) {
            try {
                for (int i = 0; i < typesArray.length(); i++) {
                    String type = typesArray.getString(i);
                    if (type.equals("store")) {
                        return "Tienda";
                    } else if (type.equals("lodging")) {
                        return "Hotel";
                    } else if (type.equals("restaurant")) {
                        return "Restaurante";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "Desconocido";
    }


}