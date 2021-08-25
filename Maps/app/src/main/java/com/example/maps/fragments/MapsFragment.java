package com.example.maps.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.maps.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, View.OnClickListener {

    private View rootView;
    private FloatingActionButton fab_gps;

    private GoogleMap googleMap;
    private MapView mv_map;
    private Geocoder geocoder;
    private MarkerOptions markerOptions;

    private List<Address> addresses;
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean clickeable = true;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mv_map = rootView.findViewById(R.id.mapView_map);
        fab_gps = rootView.findViewById(R.id.fab_gps);

        if (mv_map != null) {
            mv_map.onCreate(null);
            mv_map.onResume();
            mv_map.getMapAsync(this);
        }

        fab_gps.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng place = new LatLng(-31.425886115448456, -64.18734661424236);

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        markerOptions = new MarkerOptions();
        markerOptions.position(place);
        markerOptions.title("Mi marcador");
        markerOptions.draggable(true);
        markerOptions.snippet("Esto es una linea de texto donde se modifican los datos");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on));

        this.googleMap.addMarker(markerOptions);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        this.googleMap.animateCamera(zoom);

        this.googleMap.setOnMarkerDragListener(this);

        geocoder = new Geocoder(getContext(), Locale.getDefault());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();

        marker.setSnippet(address);
        marker.showInfoWindow();

/*        Toast.makeText(getContext(), "address: "+address+"\n"+
                        "city: "+city+"\n"+
                        "state: "+state+"\n"+
                        "country: "+country+"\n"+
                        "postalCode: "+postalCode+"\n"
                , Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onClick(View v) {
        this.checkIfGPSIsEnable();
    }

    private void checkIfGPSIsEnable() {
        try {
            int gpsSignal = Settings.Secure
                    .getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0)
                showInfoAlert();
            else if (clickeable) {
                Toast.makeText(getContext(), "GPS activado!", Toast.LENGTH_SHORT).show();
                clickeable = false;
                handler.postDelayed(() -> clickeable = true, 2500);
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showInfoAlert() {
        new AlertDialog.Builder(getContext())
                .setTitle("GPS Signal")
                .setMessage("¿Desea activar el GPS?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }
}