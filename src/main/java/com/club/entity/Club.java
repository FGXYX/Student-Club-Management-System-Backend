package com.club.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "club")
@Data
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @Column(name = "current_members")
    private Integer currentMembers = 0;

    @Column(name = "max_members")
    private Integer maxMembers = 100;

    @Column(name = "president")
    private String president;

    @Column(name = "contact")
    private String contact;

    @Column(name = "campus")
    private String campus;

    @Column(name = "status")
    private String status = "active"; // active, inactive, closed

    @Column(name = "activities_count")
    private Integer activitiesCount = 0;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "wechat_group")
    private String wechatGroup;

    @Column(name = "qq_group")
    private String qqGroup;

    @Column(name = "tags")
    private String tags; // 用逗号分隔的标签

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