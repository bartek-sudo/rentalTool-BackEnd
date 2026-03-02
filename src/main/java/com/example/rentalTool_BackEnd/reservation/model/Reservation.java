package com.example.rentalTool_BackEnd.reservation.model;

import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private long toolId;

    private long renterId;

    private LocalDate startDate;
    private LocalDate endDate;

    @Setter
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(nullable = true)
    private Long termsId;

    private Instant termsAcceptedAt;

    private Instant createdAt;
    private Instant updatedAt;

    public Reservation(long toolId, long renterId, LocalDate startDate, LocalDate endDate) {
        this.toolId = toolId;
        this.renterId = renterId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ReservationStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void acceptRegulations(Long termsId) {
        this.status = ReservationStatus.REGULATIONS_ACCEPTED;
        this.termsId = termsId;
        this.termsAcceptedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.updatedAt = Instant.now();
    }


}
