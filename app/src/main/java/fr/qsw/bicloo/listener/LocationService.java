package fr.qsw.bicloo.listener;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Quentin on 27/06/2016.
 * Singleton dédié à la gestion du GPS et le récupération des données de localisation.
 */
public class LocationService {
    private static LocationService instance = new LocationService();
    public static LocationService getInstance() {
        return instance;
    }
    private LocationService() {
    }

    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean gpsEnabled = false;
    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    /**
     * Listener des évènements envoyés par LocationService.
     */
    public interface LocationServiceListener{
        void onLocationChanged(Location location);
    }
    private Set<LocationServiceListener> listeners = new HashSet<>();
    public void addListener(LocationServiceListener listener){listeners.add(listener);}
    public void removeListener(LocationServiceListener listener){listeners.remove(listener);}

    public void startLocation(Context context) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                for(LocationServiceListener listener : listeners){
                    listener.onLocationChanged(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    gpsEnabled = true;
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    gpsEnabled = false;
                }
            }
        };
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }catch(SecurityException e){
        }
    }

    public void stopLocation() {
        if (locationListener != null && locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            }catch(SecurityException e){
            }
        }
    }

    public Location getLastKnownLocation() {
        if(locationManager!=null){
            try{
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }catch (SecurityException e){
                return null;
            }
        }else{
            return null;
        }
    }

}
