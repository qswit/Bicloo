package fr.qsw.bicloo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

import fr.qsw.bicloo.R;
import fr.qsw.bicloo.StationCardView;
import fr.qsw.bicloo.bo.Station;

/**
 * Created by Quentin on 28/06/2016.
 * Permet l'affichage d'une liste de CardView décrivant une station (Station CardView)
 */
public class StationCardsAdapter extends RecyclerView.Adapter<StationCardsAdapter.ViewHolder>  {

    private List<Station> stations = new LinkedList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public StationCardView cardView;
        public ViewHolder(StationCardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
        public void bindStation(Station station){
            this.cardView.updateCardView(station);
        }
    }

    public StationCardsAdapter(List<Station> stations) {
        this.stations = stations;
    }

    @Override
    public StationCardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stationcardview, parent, false);
        ViewHolder viewHolder = new ViewHolder((StationCardView)view.findViewById(R.id.stationCardView));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindStation(stations.get(position));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public void setStations(List<Station> stations){
        this.stations = stations;
        notifyDataSetChanged();
    }

}
