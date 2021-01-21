package com.laioffer.jupiter.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true) // Meet some data that we do not know
@JsonInclude(JsonInclude.Include.NON_NULL) // DO NOT convert NULL
@JsonDeserialize(builder = Game.Builder.class) // String -> Game
public class Game {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    private Game(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("box_art_url")
        private String boxArtUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder boxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }
//    @JsonProperty("name")
//    public String name;
//
//    @JsonProperty("developer")
//    public String developer;
//
//    @JsonProperty("release_time")
//    public String releaseTime;
//
//    @JsonProperty("website")
//    public String website;
//
//    @JsonProperty("price")
//    public double price;

//    public Game(String name, String developer, String releaseTime, String website, double price) {
//        this.name = name;
//        this.developer = developer;
//        this.releaseTime = releaseTime;
//        this.website = website;
//        this.price = price;
//    }
}

