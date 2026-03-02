package com.example.rentalTool_BackEnd.tool.model;

import com.example.rentalTool_BackEnd.tool.category.model.Category;
import com.example.rentalTool_BackEnd.tool.model.enums.ModerationStatus;
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
    @Column(length = 2000)
    private String description;
    private double pricePerDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private long ownerId;

    private String address; // adres tekstowy do wyświetlania
    private Double latitude; // szerokość geograficzna
    private Double longitude; // długość geograficzna

    @Column(nullable = false)
    private Long termsId;

    private String mainImageUrl;

    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToolImage> images = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationStatus moderationStatus;

    private Long moderatorId; // ID moderatora który przejrzał narzędzie
    private Instant moderatedAt;
    private String moderationComment;

    public Tool(String name, String description, double pricePerDay, Category category, long owner, String address, Double latitude, Double longitude, Long termsId) {
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.category = category;
        this.ownerId = owner;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.termsId = termsId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.isActive = true;
        this.moderationStatus = ModerationStatus.PENDING;
    }

    public void addImage(ToolImage image) {
        images.add(image);
        image.setTool(this);
    }

    public void removeImage(ToolImage image) {
        boolean wasMain = image.isMain();

        images.remove(image);
        image.setTool(null);

        // Jeśli usuwamy główne zdjęcie i są jeszcze inne zdjęcia
        if (wasMain && !images.isEmpty()) {
            // Ustaw pierwsze dostępne zdjęcie jako główne
            ToolImage newMainImage = images.get(0);
            newMainImage.setMain(true);
            this.mainImageUrl = newMainImage.getUrl();
        } else if (images.isEmpty()) {
            // Jeśli nie ma więcej zdjęć, wyczyść URL głównego zdjęcia
            this.mainImageUrl = null;
        }
    }

    public void setMainImage(ToolImage newMainImage) {
        // Sprawdź czy zdjęcie należy do tego narzędzia
        if (!images.contains(newMainImage)) {
            throw new IllegalArgumentException("Zdjęcie nie należy do tego narzędzia");
        }

        if (newMainImage.isMain()) {
            return;
        }

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

    // ===== METODY DO MODERACJI =====

    /**
     * Zatwierdza narzędzie przez moderatora
     */
    public void approve(Long moderatorId, String comment) {
        this.moderationStatus = ModerationStatus.APPROVED;
        this.moderatorId = moderatorId;
        this.moderatedAt = Instant.now();
        this.moderationComment = comment;
        this.isActive = true; // zatwierdzone narzędzie jest automatycznie aktywne
    }

    /**
     * Odrzuca narzędzie przez moderatora
     */
    public void reject(Long moderatorId, String comment) {
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderatedAt = Instant.now();
        this.moderationComment = comment;
        this.isActive = false; // odrzucone narzędzie jest nieaktywne
    }

    /**
     * Sprawdza czy narzędzie jest widoczne publicznie
     * (zatwierdzone przez moderatora i aktywne)
     */
    public boolean isPubliclyVisible() {
        return moderationStatus == ModerationStatus.APPROVED && isActive;
    }

}
