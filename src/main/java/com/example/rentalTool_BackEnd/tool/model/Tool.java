package com.example.rentalTool_BackEnd.tool.model;

import com.example.rentalTool_BackEnd.tool.model.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tools")
@Getter
@Setter
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

    private long ownerId;

    private String address; // adres tekstowy do wyświetlania
    private Double latitude; // szerokość geograficzna
    private Double longitude; // długość geograficzna

    // Główne zdjęcie dla szybkiego dostępu
    private String mainImageUrl;

    // Relacja z obrazami
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToolImage> images = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    public Tool(String name, String description, double pricePerDay, Category category, long owner, String address, Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.category = category;
        this.ownerId = owner;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Pomocnicza metoda do dodawania zdjęcia
    public void addImage(ToolImage image) {
        images.add(image);
        image.setTool(this);

        // Jeśli to główne zdjęcie lub nie ma jeszcze głównego zdjęcia
        if (image.isMain() || mainImageUrl == null) {
            setMainImage(image);
        }
    }

    // Pomocnicza metoda do usuwania zdjęcia
    public void removeImage(ToolImage image) {
        images.remove(image);
        image.setTool(null);

        // Jeśli usuwamy główne zdjęcie, wybieramy nowe główne zdjęcie
        if (image.isMain() && !images.isEmpty()) {
            setMainImage(images.get(0));
        } else if (images.isEmpty()) {
            mainImageUrl = null;
        }
    }

    // Ustawianie głównego zdjęcia
    public void setMainImage(ToolImage newMainImage) {
        // Zresetowanie flagi głównego zdjęcia dla wszystkich zdjęć
        images.forEach(img -> img.setMain(false));

        // Ustawienie nowego głównego zdjęcia
        newMainImage.setMain(true);
        this.mainImageUrl = newMainImage.getUrl();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
