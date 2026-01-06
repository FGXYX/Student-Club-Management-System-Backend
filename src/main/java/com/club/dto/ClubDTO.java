package com.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@Data
public class ClubDTO {
    private Long id;

    @NotBlank(message = "社团名称不能为空")
    private String name;

    private String description;
    private String category;
    private LocalDate establishedDate;
    private Integer currentMembers = 0;
    private Integer maxMembers = 100;
    private String president;
    private String contact;
    private String campus;
    private String status = "active";
    private Integer activitiesCount = 0;
    private MultipartFile logo;
    private String wechatGroup;
    private String qqGroup;
    private List<String> tags;
    private String logoUrl;
}