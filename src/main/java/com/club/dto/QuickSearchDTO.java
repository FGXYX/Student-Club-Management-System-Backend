package com.club.dto;

import lombok.Data;

@Data
public class QuickSearchDTO {
    private String keyword;
    private String category;
    private String status;
    private String sort = "name";
    private Integer limit = 6;
}