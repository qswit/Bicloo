package fr.qsw.bicloo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import fr.qsw.bicloo.adapter.StationCardsAdapter;

/**
 * Created by Quentin on 28/06/2016.
 * Activity de recherche et d'affichage de stations. Par défaut toutes les stations sont affichées
 * dans un RecyclerView (triée par distance avec l'utilisateur), un champs textuel permet de n'afficher
 * que les stations dont le nom contient un texte donné.
 * Un bouton de paramètres permet d'ouvrir un volet proposant 3 autres options de filtre : ne pas afficher les stations
 * fermées, pleines ou vides.
 */
public class StationsActivity extends Activity implements BiclooService.BiclooServiceListener{

    private EditText stationNameEditText;
    private CheckBox stationClosedCheckBox;
    private CheckBox stationEmptyCheckBox;
    private CheckBox stationFullCheckBox;
    private ImageButton searchSettingsButton;
    private LinearLayout searchSettingsLayout;

    private RecyclerView stationsRecyclerView;
    private StationCardsAdapter stationsAdapter;
    private RecyclerView.LayoutManager stationsLayoutManager;

    private BiclooService biclooService;
    private ServiceConnection serviceConnection;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);
        stationNameEditText = (EditText)findViewById(R.id.stationNameEditText);
        stationClosedCheckBox = (CheckBox) findViewById(R.id.stationClosedCheckBox);
        stationEmptyCheckBox = (CheckBox) findViewById(R.id.stationEmptyCheckBox);
        stationFullCheckBox = (CheckBox) findViewById(R.id.stationFullCheckBox);
        searchSettingsButton = (ImageButton) findViewById(R.id.stationSearchSettingsButton);
        searchSettingsLayout = (LinearLayout) findViewById(R.id.stationSearchSettingsLayout);

        stationNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateStationsList();
            }
        });
        stationClosedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStationsList();
            }
        });
        stationEmptyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStationsList();
            }
        });
        stationFullCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStationsList();
            }
        });
        searchSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchSettingsLayout.getVisibility()==View.VISIBLE){
                    searchSettingsLayout.setVisibility(View.GONE);
                }else{
                    searchSettingsLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                biclooService = ((BiclooService.BiclooServiceBinder)binder).getService();
                biclooService.addListener(StationsActivity.this);
                stationsRecyclerView = (RecyclerView) findViewById(R.id.stationsRecyclerView);
                stationsLayoutManager = new LinearLayoutManager(StationsActivity.this);
                stationsRecyclerView.setLayoutManager(stationsLayoutManager);
                stationsAdapter = new StationCardsAdapter(biclooService.getFilteredStations(
                        String.valueOf(stationNameEditText.getText()),
                        stationClosedCheckBox.isChecked(),
                        stationEmptyCheckBox.isChecked(),
                        stationFullCheckBox.isChecked()));
                stationsRecyclerView.setAdapter(stationsAdapter);
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
        biclooService.removeListener(this);
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateStationsList();
    }

    @Override
    public void onStationsUpdated() {
        updateStationsList();
    }

    @Override
    public void onStationsUpdateFailure() {

    }

    public void updateStationsList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stationsAdapter.setStations(biclooService.getFilteredStations(
                        String.valueOf(stationNameEditText.getText()),
                        stationClosedCheckBox.isChecked(),
                        stationEmptyCheckBox.isChecked(),
                        stationFullCheckBox.isChecked()));
            }
        });
    }
}
