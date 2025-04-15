package com.example.rentalTool_BackEnd.reservation.model;

import com.example.rentalTool_BackEnd.reservation.model.enums.ReservationStatus;
import com.example.rentalTool_BackEnd.tool.model.Tool;
import com.example.rentalTool_BackEnd.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    public Reservation(Tool tool, User renter, LocalDate startDate, LocalDate endDate) {
        this.tool = tool;
        this.renter = renter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ReservationStatus.PENDING;

        long days =  ChronoUnit.DAYS.between(startDate, endDate)+1;
        this.totalPrice = tool.getPricePerDay() * days;

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void pay() {
        this.status = ReservationStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void finish() {
        this.status = ReservationStatus.FINISHED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.updatedAt = Instant.now();
    }


}
