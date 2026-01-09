package com.example.rentalTool_BackEnd.tool.terms.model;

import com.example.rentalTool_BackEnd.shared.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Terms {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Category category; // null oznacza regulamin ogólny

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;
    private Instant updatedAt;

    public Terms(Category category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
