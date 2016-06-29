package fr.qsw.bicloo;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.text.DecimalFormat;

import fr.qsw.bicloo.bo.Station;

/**
 * Created by Quentin on 28/06/2016.
 * Composant d'affichage d'une station.
 */
public class StationCardView extends CardView {

    private Station station;

    private TextView nameTextView;
    private TextView addressTextView;
    private TextView distanceTextView;
    private TextView bikesAvailibilityTextView;

    public StationCardView(Context context){
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardview_station,this);
        nameTextView = (TextView)findViewById(R.id.stationCardViewName);
        addressTextView = (TextView)findViewById(R.id.stationCardViewAddress);
        distanceTextView = (TextView)findViewById(R.id.stationCardViewDistance);
        bikesAvailibilityTextView = (TextView)findViewById(R.id.stationCardViewBikesAvailibility);
    }

    public StationCardView(Context context, AttributeSet attr){
        super(context,attr);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardview_station,this);
        nameTextView = (TextView)findViewById(R.id.stationCardViewName);
        addressTextView = (TextView)findViewById(R.id.stationCardViewAddress);
        distanceTextView = (TextView)findViewById(R.id.stationCardViewDistance);
        bikesAvailibilityTextView = (TextView)findViewById(R.id.stationCardViewBikesAvailibility);
    }

    public StationCardView(Context context, AttributeSet attr, int defStyle){
        super(context,attr,defStyle);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardview_station,this);
        nameTextView = (TextView)findViewById(R.id.stationCardViewName);
        addressTextView = (TextView)findViewById(R.id.stationCardViewAddress);
        distanceTextView = (TextView)findViewById(R.id.stationCardViewDistance);
        bikesAvailibilityTextView = (TextView)findViewById(R.id.stationCardViewBikesAvailibility);
    }

    public StationCardView(Context context, Station station) {
        super(context);
        this.station = station;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardview_station,this);
        nameTextView = (TextView)findViewById(R.id.stationCardViewName);
        addressTextView = (TextView)findViewById(R.id.stationCardViewAddress);
        distanceTextView = (TextView)findViewById(R.id.stationCardViewDistance);
        bikesAvailibilityTextView = (TextView)findViewById(R.id.stationCardViewBikesAvailibility);
        updateCardView(station);
    }

    public void updateCardView(Station station){
        this.station = station;
        if(station.getOpen()!=null?station.getOpen():true){
            nameTextView.setText(concatFirstPart(station.getName()));
        }else{
            nameTextView.setText(concatFirstPart(station.getName())+" ("+getResources().getString(R.string.closedUpperCase)+")");
        }
        addressTextView.setText(concatFirstPart(station.getAddress()));
        if(station.getAvailableBikes()!=null && station.getBikeStands()!=null){
            bikesAvailibilityTextView.setText(station.getAvailableBikes()+"/"+station.getBikeStands()+" "+getResources().getString(R.string.bikesAvailable));
        }else{
            bikesAvailibilityTextView.setText(getResources().getString(R.string.unknownBikeAvailibility));
        }
        if(station.getDistance()!=null){
            distanceTextView.setText(formatDistance(station.getDistance()));
        }
    }

    public Station getStation(){
        return this.station;
    }

    public String concatFirstPart(String original){
        String[] tab = original.split("-");
        if(tab.length>1){
            String formated = "";
            for(int i=1; i<tab.length; i++){
                formated+=tab[i]+" - ";
            }
            return formated.substring(0,formated.length()-2);
        }else{
            return original;
        }
    }

    public String formatDistance(Float distance){
        if(distance>=1000){
            return String.valueOf(new DecimalFormat("#.#").format(distance/1000))+" km";
        }else{
            return String.valueOf(distance.intValue())+" m";
        }
    }
}
