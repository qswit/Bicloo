package fr.qsw.bicloo.listener;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import fr.qsw.bicloo.bo.Station;

/**
 * Created by Quentin on 27/06/2016.
 * Singleton dédié à l'accès aux données des stations via l'API JCDecaux ou un fichier json stocké et mis à jour à chaque accès à l'API réussi.
 */
public class StationService {
    private static StationService instance = new StationService();
    public static StationService getInstance() {
        return instance;
    }
    private StationService() {
    }

    private static final String STATIONS_FILENAME = "stations.json";

    private static final String API_ADDRESS = "https://api.jcdecaux.com/vls/v1/stations?contract=Nantes&apiKey=5dc69b6d9123fb82eb410addb1084b86299a9cd6";
    private static final String JSON_KEY_NUMBER = "number";
    private static final String JSON_KEY_CONTRACTNAME = "contract_name";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_ADDRESS = "address";
    private static final String JSON_KEY_POSITION = "position";
    private static final String JSON_KEY_POSITION_LAT = "lat";
    private static final String JSON_KEY_POSITION_LNG = "lng";
    private static final String JSON_KEY_BANKING = "banking";
    private static final String JSON_KEY_BONUS = "bonus";
    private static final String JSON_KEY_STATUS = "status";
    private static final String JSON_KEY_STATUS_OPEN = "OPEN";
    private static final String JSON_KEY_BIKESTANDS = "bike_stands";
    private static final String JSON_KEY_AVAILABLEBIKESTANDS = "available_bike_stands";
    private static final String JSON_KEY_AVAILABLEBIKES = "available_bikes";
    private static final String JSON_KEY_LASTUPDATE = "last_update";

    private static SyncHttpClient client = new SyncHttpClient();

    /**
     * Listener des évènements envoyés par StationService
     */
    public interface StationServiceListener {
        void onStationsFound(Set<Station> stations);
        void onStationsRetrieveFromFile(Set<Station> stations);
        void onStationsSearchFailure();
    }
    private Set<StationServiceListener> listeners = new HashSet<>();
    public void addListener(StationServiceListener listener){listeners.add(listener);}
    public void removeListener(StationServiceListener listener){listeners.remove(listener);}

    /**
     * Appel à l'API JCDecaux pour la récupération des données des stations.
     * En cas de succès direct, le contenu du json est stocké dans un fichier.
     * En cas d'échec, les dernières données sont récupérées via le fichier (s'il existe).
     * @param context
     */
    public void findStations(final Context context){
        client.get(context, API_ADDRESS, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    File stationsFile = context.getExternalFilesDir(STATIONS_FILENAME);
                    if (stationsFile.exists()) {
                        stationsFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(stationsFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    osw.write(response.toString());
                    osw.flush();
                    osw.close();
                    fos.close();
                    for(StationServiceListener listener : listeners) {
                        listener.onStationsFound(parseJsonStations(response));
                    }
                } catch (JSONException e) {
                    for(StationServiceListener listener : listeners) {
                        listener.onStationsSearchFailure();
                    }
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse){
                if(context.getExternalFilesDir(STATIONS_FILENAME).exists()){
                    try {
                        FileReader fr = new FileReader(context.getExternalFilesDir(STATIONS_FILENAME));
                        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                        org.json.simple.JSONArray array = (org.json.simple.JSONArray)parser.parse(fr);
                        for(StationServiceListener listener : listeners) {
                            listener.onStationsRetrieveFromFile(parseJsonStations(array));
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        for(StationServiceListener listener : listeners) {
                            listener.onStationsSearchFailure();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        for(StationServiceListener listener : listeners) {
                            listener.onStationsSearchFailure();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        for(StationServiceListener listener : listeners) {
                            listener.onStationsSearchFailure();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        for(StationServiceListener listener : listeners) {
                            listener.onStationsSearchFailure();
                        }
                    }
                }else {
                    for(StationServiceListener listener : listeners) {
                        listener.onStationsSearchFailure();
                    }
                }
            }
        });
    }

    private static Set<Station> parseJsonStations(JSONArray jsonStations) throws JSONException {
        Set<Station> stations = new HashSet<>();
        for(int i=0 ; i<jsonStations.length()-1 ; i++){
            JSONObject jsonStation = (JSONObject)jsonStations.get(i);
            stations.add(new Station(
                    jsonStation.getInt(JSON_KEY_NUMBER),
                    jsonStation.getString(JSON_KEY_CONTRACTNAME),
                    jsonStation.getString(JSON_KEY_NAME),
                    jsonStation.getString(JSON_KEY_ADDRESS),
                    jsonStation.getJSONObject(JSON_KEY_POSITION).getDouble(JSON_KEY_POSITION_LAT),
                    jsonStation.getJSONObject(JSON_KEY_POSITION).getDouble(JSON_KEY_POSITION_LNG),
                    jsonStation.getBoolean(JSON_KEY_BANKING),
                    jsonStation.getBoolean(JSON_KEY_BONUS),
                    jsonStation.getString(JSON_KEY_STATUS).equals(JSON_KEY_STATUS_OPEN),
                    jsonStation.getInt(JSON_KEY_BIKESTANDS),
                    jsonStation.getInt(JSON_KEY_AVAILABLEBIKESTANDS),
                    jsonStation.getInt(JSON_KEY_AVAILABLEBIKES),
                    new Timestamp(jsonStation.getLong(JSON_KEY_LASTUPDATE))
            ));
        }
        return stations;
    }

    private static Set<Station> parseJsonStations(org.json.simple.JSONArray jsonStations) throws JSONException {
        Set<Station> stations = new HashSet<>();
        for(int i=0 ; i<jsonStations.size()-1 ; i++){
            org.json.simple.JSONObject jsonStation = (org.json.simple.JSONObject)jsonStations.get(i);
            stations.add(new Station(
                    ((Long)jsonStation.get(JSON_KEY_NUMBER)).intValue(),
                    (String)jsonStation.get(JSON_KEY_CONTRACTNAME),
                    (String)jsonStation.get(JSON_KEY_NAME),
                    (String)jsonStation.get(JSON_KEY_ADDRESS),
                    (Double)((org.json.simple.JSONObject)jsonStation.get(JSON_KEY_POSITION)).get(JSON_KEY_POSITION_LAT),
                    (Double)((org.json.simple.JSONObject)jsonStation.get(JSON_KEY_POSITION)).get(JSON_KEY_POSITION_LNG),
                    (Boolean)jsonStation.get(JSON_KEY_BANKING),
                    (Boolean)jsonStation.get(JSON_KEY_BONUS),
                    jsonStation.get(JSON_KEY_STATUS).equals(JSON_KEY_STATUS_OPEN),
                    ((Long)jsonStation.get(JSON_KEY_BIKESTANDS)).intValue(),
                    ((Long)jsonStation.get(JSON_KEY_AVAILABLEBIKESTANDS)).intValue(),
                    ((Long)jsonStation.get(JSON_KEY_AVAILABLEBIKES)).intValue(),
                    new Timestamp((Long)jsonStation.get(JSON_KEY_LASTUPDATE))
            ));
        }
        return stations;
    }

    public static class StationByDistanceComparator implements Comparator<Station>{
        private LatLng latLng;
        public StationByDistanceComparator(LatLng latLng){
            this.latLng = latLng;
        }
        @Override
        public int compare(Station a, Station b) {
            return a.distanceTo(latLng)<b.distanceTo(latLng)?-1:a.distanceTo(latLng)==b.distanceTo(latLng)?0:1;
        }
    }

    public static class StationByNameComparator implements Comparator<Station>{
        public StationByNameComparator(){}
        @Override
        public int compare(Station a, Station b) {
            return a.getName().compareTo(b.getName());
        }
    }
}
