package com.slotbook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    @Column(unique = true) public String email;
    @JsonIgnore public String password;
    public User() {}
    public User(String name, String email, String password) { this.name = name; this.email = email; this.password = password; }
}
