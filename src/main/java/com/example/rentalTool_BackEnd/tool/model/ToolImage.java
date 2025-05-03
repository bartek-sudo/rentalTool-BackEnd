package com.example.rentalTool_BackEnd.tool.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tool_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToolImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String url;
    private String filename;
    private String contentType;
    private boolean isMain;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
