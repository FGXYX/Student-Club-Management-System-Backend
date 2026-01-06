package com.club.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClubExportDTO {
    private String keyword;
    private List<String> categories;
    private List<String> columns = List.of("name", "category", "president", "currentMembers", "establishedDate");
}