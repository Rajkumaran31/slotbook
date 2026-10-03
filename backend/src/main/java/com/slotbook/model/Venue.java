package com.slotbook.model;

import jakarta.persistence.*;

@Entity
public class Venue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name, city, sport, emoji, about;
    public int basePrice;
    public Venue() {}
    public Venue(String name, String city, String sport, int basePrice, String emoji, String about) {
        this.name = name;
        this.city = city;
        this.sport = sport;
        this.basePrice = basePrice;
        this.emoji = emoji;
        this.about = about;
    }
}
