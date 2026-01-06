package com.club.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity")
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "club_id")
    private Long clubId;

    @Column(name = "club_name")
    private String clubName;

    @Column(name = "activity_time")
    private LocalDateTime activityTime;

    @Column(name = "location")
    private String location;

    @Column(name = "organizer")
    private String organizer;

    @Column(name = "current_participants")
    private Integer currentParticipants = 0;

    @Column(name = "max_participants")
    private Integer maxParticipants = 50;

    @Column(name = "activity_type")
    private String activityType; // academic, sports, volunteer, art, workshop

    @Column(name = "status")
    private String status; // preparing, upcoming, ongoing, completed

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}