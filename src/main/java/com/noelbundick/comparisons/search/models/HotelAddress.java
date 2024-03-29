package com.noelbundick.comparisons.search.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HotelAddress {
    @JsonProperty(value = "StreetAddress")
    private String streetAddress;

    @JsonProperty(value = "City")
    private String city;

    @JsonProperty(value = "StateProvince")
    private String stateProvince;

    @JsonProperty(value = "Country")
    private String country;

    @JsonProperty(value = "PostalCode")
    private String postalCode;


    public String streetAddress() {
        return this.streetAddress;
    }

    public HotelAddress streetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        return this;
    }

    public String city() {
        return this.city;
    }

    public HotelAddress city(String city) {
        this.city = city;
        return this;
    }

    public String stateProvince() {
        return this.stateProvince;
    }

    public HotelAddress stateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
        return this;
    }

    public String country() {
        return this.country;
    }

    public HotelAddress country(String country) {
        this.country = country;
        return this;
    }

    public String postalCode() {
        return this.postalCode;
    }

    public HotelAddress postalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }
}
