package fr.qsw.bicloo;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import fr.qsw.bicloo.listener.LocationService;
import fr.qsw.bicloo.listener.StationService;
import fr.qsw.bicloo.bo.Station;

/**
 * Created by Quentin on 27/06/2016.
 * Service de gestion des données globales de l'application.
 * Il écoute LocationService et StationService et transmet aux activities.
 */
public class BiclooService extends Service implements LocationService.LocationServiceListener,StationService.StationServiceListener{

    /**
     * Fréquence de tentative d'accès à l'API JCDecaux.
     */
    private static final Long STATIONS_UPDATE_FREQUENCY = 30000l;

    private LocationService locationService;
    private StationService stationService;

    /**
     * Map des données des stations à jour, avec en clé un identifiant composé de id+contractName.
     */
    private Map<String,Station> stations = new HashMap<>();

    /**
     * Timer d'accès récurrent à l'API JCDecaux.
     */
    private Timer stationUpdateTimer;

    /**
     * Listener des évènements envoyés par BiclooService
     */
    public interface BiclooServiceListener{
        void onLocationChanged(Location location);
        void onStationsUpdated();
        void onStationsUpdateFailure();
    }
    private Set<BiclooServiceListener> listeners = new HashSet<>();
    public void addListener(BiclooServiceListener listener){listeners.add(listener);}
    public void removeListener(BiclooServiceListener listener){listeners.remove(listener);}

    @Override
    public void onCreate() {
        super.onCreate();
        locationService = LocationService.getInstance();
        stationService = StationService.getInstance();
        locationService.startLocation(getApplicationContext());
        locationService.addListener(this);
        stationService.addListener(this);
    }

    @Override
    public void onDestroy(){
        locationService.stopLocation();
        locationService.removeListener(this);
        stationService.removeListener(this);
    }

    private final IBinder binder = new BiclooServiceBinder();
    public class BiclooServiceBinder extends Binder{
        public BiclooService getService(){
            return BiclooService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        locationService.removeListener(this);
        locationService.stopLocation();
        return true;
    }

    @Override
    public void onStationsFound(Set<Station> stations) {
        for(Station station : stations){
            updateStation(station.getName()+station.getContractName(),station);
        }
        for(BiclooServiceListener listener : listeners) {
            listener.onStationsUpdated();
        }
    }

    @Override
    public void onStationsRetrieveFromFile(Set<Station> stations) {
        for(Station station : stations){
            station.setOpen(null);
            station.setBikeStands(null);
            station.setAvailableBikeStands(null);
            station.setAvailableBikes(null);
            updateStation(station.getName()+station.getContractName(),station);
        }
        for(BiclooServiceListener listener : listeners) {
            listener.onStationsUpdated();
        }
    }

    @Override
    public void onStationsSearchFailure() {

    }

    @Override
    public void onLocationChanged(Location location) {
        for(Station station : stations.values()){
            station.setDistance(new LatLng(location.getLatitude(),location.getLongitude()));
        }
        for(BiclooServiceListener listener : listeners){
            listener.onLocationChanged(location);
        }
    }

    public boolean isGpsEnabled(){
        return locationService.isGpsEnabled();
    }

    public Map<String,Station> getStations() {
        return stations;
    }

    public void updateStation(String idStation, Station station){
        stations.put(idStation,station);
    }

    public Station getStationById(String stationId){
        return stations.get(stationId);
    }

    /**
     * Retourne un liste triée par distance avec l'utilisateur (la plus proche en première) ou par nom si aucune donnée de localisation
     * et filtrée via différents paramètres.
     * @param containingName Filtre les stations ne contenant pas la chaine de caractères (inapplicable si vide)
     * @param filterClosed Filtre les stations fermées
     * @param filterEmpty Filtre les stations vides (acunu vélo disponible)
     * @param filterFull Filtre les stations pleines (tous les emplacements sont occupés)
     * @return Liste de stations triée et filtrée
     */
    public List<Station> getFilteredStations(String containingName,boolean filterClosed,boolean filterEmpty,boolean filterFull){
        List<Station> filteredStations = new LinkedList<>();
        for(Station station : stations.values()){
            if((containingName.equals("") || station.getName().toLowerCase().contains(containingName.toLowerCase()))
                    && (filterClosed?station.getOpen():true)
                    && ((station.getAvailableBikes()!=null && filterEmpty)?station.getAvailableBikes()>0:true)
                    && ((station.getAvailableBikeStands()!=null && filterFull)?station.getAvailableBikeStands()>0:true)
                    ){
                filteredStations.add(station);
            }
        }
        Location lastKnownLocation = locationService.getLastKnownLocation();
        if(lastKnownLocation!=null){
            Collections.sort(filteredStations,new StationService.StationByDistanceComparator(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude())));
        }else{
            Collections.sort(filteredStations,new StationService.StationByNameComparator());
        }
        return filteredStations;
    }

    public void startUpdateStationsTimer(){
        stationUpdateTimer = new Timer();
        stationUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStations();
            }
        },STATIONS_UPDATE_FREQUENCY,STATIONS_UPDATE_FREQUENCY);
    }

    public void stopUpdateStationsTimer(){
        if(stationUpdateTimer!=null){
            stationUpdateTimer.cancel();
        }
    }

    public void updateStations(){
        stationService.findStations(getApplicationContext());
    }




}
