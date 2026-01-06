package com.club.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClubQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private String category;
    private String status;
    private String sortField = "name";
    private String sortOrder = "asc";
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minMembers;
    private Integer maxMembers;
    private String president;
    private String memberRange;
    private Boolean onlyActive = false;
}