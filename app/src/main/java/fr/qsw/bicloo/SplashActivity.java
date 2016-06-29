package fr.qsw.bicloo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Quentin on 27/06/2016.
 * SplashScreen de l'application. On en profite pour s'assurer que les autorisations d'accès
 * aux données de localisation ont bien été acceptées par l'utilisateur,
 * et on accède une première fois à l'API JCDecaux pour remplir la liste des stations.
 */
public class SplashActivity extends Activity implements BiclooService.BiclooServiceListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private BiclooService biclooService;
    private ServiceConnection serviceConnection;

    private Handler splashHandler;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_splash);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},0);
        }else{
            onPermissionsAccepted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsAccepted();
                } else {
                    SplashActivity.this.finish();
                }
            }
        }
    }

    public void onPermissionsAccepted(){
        splashHandler = new Handler();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                biclooService = ((BiclooService.BiclooServiceBinder)binder).getService();
                biclooService.addListener(SplashActivity.this);
                Thread updateStationsThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        biclooService.updateStations();
                    }
                });
                updateStationsThread.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(new Intent(this,BiclooService.class),serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        biclooService.removeListener(SplashActivity.this);
        if(serviceConnection!=null){
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStationsUpdated() {
        splashHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                SplashActivity.this.finish();
            }
        },1000);
    }

    @Override
    public void onStationsUpdateFailure() {
        splashHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                SplashActivity.this.finish();
            }
        },1000);
    }
}
