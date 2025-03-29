package com.example.rentalTool_BackEnd.tool.model;

import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import com.example.rentalTool_BackEnd.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tools")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String description;
    private double pricePerDay;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String address; // adres tekstowy do wyświetlania
    private Double latitude; // szerokość geograficzna
    private Double longitude; // długość geograficzna

//    private String images;
    private Instant createdAt;
    private Instant updatedAt;

    public Tool(String name, String description, double pricePerDay, Category category, User owner, String address, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.category = category;
        this.owner = owner;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}
