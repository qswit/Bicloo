package fr.qsw.bicloo.bo;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;

/**
 * Created by Quentin on 27/06/2016.
 * Station de vélos, telle que décrite dans l'API JCDecaux.
 */
public class Station {

    private Integer number;
    private String contractName;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean banking;
    private Boolean bonus;
    private Boolean open;
    private Integer bikeStands;
    private Integer availableBikeStands;
    private Integer availableBikes;
    private Timestamp lastUpdate;
    private Float distance;

    public Station(
            int number,
            String contractName,
            String name,
            String address,
            double latitude,
            double longitude,
            boolean banking,
            boolean bonus){
        this.setNumber(number);
        this.setContractName(contractName);
        this.setName(name);
        this.setAddress(address);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setBanking(banking);
        this.setBonus(bonus);
    }

    public Station(
            int number,
            String contractName,
            String name,
            String address,
            double latitude,
            double longitude,
            boolean banking,
            boolean bonus,
            boolean open,
            int bikeStands,
            int availableBikeStands,
            int availableBikes,
            Timestamp lastUpdate){
        this.setNumber(number);
        this.setContractName(contractName);
        this.setName(name);
        this.setAddress(address);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setBanking(banking);
        this.setBonus(bonus);
        this.setOpen(open);
        this.setBikeStands(bikeStands);
        this.setAvailableBikeStands(availableBikeStands);
        this.setAvailableBikes(availableBikes);
        this.setLastUpdate(lastUpdate);
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getBanking() {
        return banking;
    }

    public void setBanking(Boolean banking) {
        this.banking = banking;
    }

    public Boolean getBonus() {
        return bonus;
    }

    public void setBonus(Boolean bonus) {
        this.bonus = bonus;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Integer getBikeStands() {
        return bikeStands;
    }

    public void setBikeStands(Integer bikeStands) {
        this.bikeStands = bikeStands;
    }

    public Integer getAvailableBikeStands() {
        return availableBikeStands;
    }

    public void setAvailableBikeStands(Integer availableBikeStands) {
        this.availableBikeStands = availableBikeStands;
    }

    public Integer getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(Integer availableBikes) {
        this.availableBikes = availableBikes;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public void setDistance(LatLng currentLocation){
        this.distance = distanceTo(currentLocation);
    }

    public float distanceTo(LatLng latLng){
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(this.getLatitude()-latLng.latitude);
        double lngDiff = Math.toRadians(this.getLongitude()-latLng.longitude);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) + Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(latLng.latitude)) * Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;
        int meterConversion = 1609;
        return new Float(distance * meterConversion).floatValue();
    }
}
