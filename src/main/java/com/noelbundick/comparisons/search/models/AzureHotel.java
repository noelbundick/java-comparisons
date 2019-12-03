package com.noelbundick.comparisons.search.models;

import com.azure.search.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AzureHotel {
    @JsonProperty(value = "HotelId")
    private String hotelId;

    @JsonProperty(value = "HotelName")
    private String hotelName;

    @JsonProperty(value = "Description")
    private String description;

    @JsonProperty(value = "Description_fr")
    private String descriptionFr;

    @JsonProperty(value = "Category")
    private String category;

    @JsonProperty(value = "Tags")
    private List<String> tags;

    @JsonProperty(value = "ParkingIncluded")
    private Boolean parkingIncluded;

    @JsonProperty(value = "SmokingAllowed")
    private Boolean smokingAllowed;

    @JsonProperty(value = "LastRenovationDate")
    private Date lastRenovationDate;

    @JsonProperty(value = "Rating")
    private Integer rating;

    @JsonProperty(value = "Location")
    private GeoPoint location;

    @JsonProperty(value = "Address")
    private HotelAddress address;

    @JsonProperty(value = "Rooms")
    private List<HotelRoom> rooms;

    public AzureHotel() {
        this.tags = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public String hotelId() {
        return this.hotelId;
    }

    public AzureHotel hotelId(String hotelId) {
        this.hotelId = hotelId;
        return this;
    }

    public String hotelName() {
        return this.hotelName;
    }

    public AzureHotel hotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public String description() {
        return this.description;
    }

    public AzureHotel description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFr() {
        return this.descriptionFr;
    }

    public AzureHotel descriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
        return this;
    }

    public String category() {
        return this.category;
    }

    public AzureHotel category(String category) {
        this.category = category;
        return this;
    }

    public List<String> tags() {
        return this.tags;
    }

    public AzureHotel tags(List<String> tags) {
        this.tags = tags;
        return this;
    }


    public Boolean parkingIncluded() {
        return this.parkingIncluded;
    }

    public AzureHotel parkingIncluded(Boolean parkingIncluded) {
        this.parkingIncluded = parkingIncluded;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public AzureHotel smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public Date lastRenovationDate() {
        return this.lastRenovationDate;
    }

    public AzureHotel lastRenovationDate(Date lastRenovationDate) {
        this.lastRenovationDate = lastRenovationDate;
        return this;
    }

    public Integer rating() {
        return this.rating;
    }

    public AzureHotel rating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public GeoPoint location() {
        return this.location;
    }

    public AzureHotel location(GeoPoint location) {
        this.location = location;
        return this;
    }

    public HotelAddress address() {
        return this.address;
    }

    public AzureHotel address(HotelAddress address) {
        this.address = address;
        return this;
    }

    public List<HotelRoom> rooms() {
        return this.rooms;
    }

    public AzureHotel rooms(List<HotelRoom> rooms) {
        this.rooms = rooms;
        return this;
    }
}
