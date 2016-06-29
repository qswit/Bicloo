package fr.qsw.bicloo;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import fr.qsw.bicloo.bo.Station;

/**
 * Created by Quentin on 27/06/2016.
 * Activity d'affichage de la carte d'ensemble. On y affiche un marqueur à chaque station, et un clic sur un marqueur
 * ouvre un bottom sheet contenant les détails de la station.
 * Un bouton flottant permet d'accéder à la recherche de stations (StationsActivity).
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, BiclooService.BiclooServiceListener {

    private GoogleMap gMap;
    private AlertDialog.Builder alertDialogBuilder;
    private BottomSheetLayout bottomSheetLayout;
    private StationCardView stationCardView;
    private FloatingActionButton searchFab;

    private BiclooService biclooService;
    private ServiceConnection serviceConnection;

    private Map<Marker,String> markers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bottomSheetLayout = (BottomSheetLayout)findViewById(R.id.mapBottomSheetLayout);
        searchFab = (FloatingActionButton)findViewById(R.id.searchFab);
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),StationsActivity.class));
            }
        });
        alertDialogBuilder = new AlertDialog.Builder(this);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                biclooService = ((BiclooService.BiclooServiceBinder)binder).getService();
                biclooService.addListener(MapsActivity.this);
                if (!biclooService.isGpsEnabled()) {
                    final AlertDialog gpsSettingsAlertDialog = alertDialogBuilder.create();
                    gpsSettingsAlertDialog.setTitle(getResources().getString(R.string.needForGPS));
                    gpsSettingsAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            gpsSettingsAlertDialog.dismiss();
                        }
                    });
                    gpsSettingsAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            gpsSettingsAlertDialog.dismiss();
                            Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(I);
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gpsSettingsAlertDialog.show();
                        }
                    });
                }
                refreshStationsMarkers(biclooService.getStations());
                biclooService.startUpdateStationsTimer();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(new Intent(this, BiclooService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        biclooService.stopUpdateStationsTimer();
        biclooService.removeListener(this);
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        try {
            gMap.setMyLocationEnabled(true);
        }catch(SecurityException e){
            e.printStackTrace();
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.21806,-1.55278),15));
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(markers.containsKey(marker)){
                    Station station = biclooService.getStationById(markers.get(marker));
                    stationCardView = new StationCardView(MapsActivity.this,station);
                    bottomSheetLayout.addOnSheetStateChangeListener(new BottomSheetLayout.OnSheetStateChangeListener() {
                        @Override
                        public void onSheetStateChanged(BottomSheetLayout.State state) {
                            switch(state){
                                case HIDDEN:
                                    searchFab.show();
                                    break;
                                case PREPARING:
                                    searchFab.hide();
                                    break;
                            }
                        }
                    });
                    bottomSheetLayout.showWithSheetView(stationCardView);
                }
                return false;
            }
        });
    }

    public void refreshStationsMarkers(final Map<String,Station> stations){
        if(gMap!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(String stationId : stations.keySet()){
                        Marker marker = gMap.addMarker(new MarkerOptions()
                                .position(new LatLng(stations.get(stationId).getLatitude(),stations.get(stationId).getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bicloo)));
                        markers.put(marker,stationId);
                    }
                }
            });
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        if(stationCardView!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stationCardView.updateCardView(biclooService.getStationById(stationCardView.getStation().getName()+stationCardView.getStation().getContractName()));
                }
            });
        }
    }

    @Override
    public void onStationsUpdated() {
        refreshStationsMarkers(biclooService.getStations());
        if(stationCardView!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stationCardView.updateCardView(biclooService.getStationById(stationCardView.getStation().getName()+stationCardView.getStation().getContractName()));
                }
            });
        }
    }

    @Override
    public void onStationsUpdateFailure() {}
}
