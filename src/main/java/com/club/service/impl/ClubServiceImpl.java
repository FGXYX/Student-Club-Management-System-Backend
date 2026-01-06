package com.club.service.impl;

import com.club.dto.*;
import com.club.entity.Club;
import com.club.repository.ClubRepository;
import com.club.service.ClubService;
import com.club.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final FileService fileService;

    @Override
    public Club createClub(ClubDTO clubDTO) {
        // 检查名称是否重复
        if (clubRepository.existsByName(clubDTO.getName())) {
            throw new RuntimeException("社团名称已存在");
        }

        Club club = new Club();
        copyDtoToEntity(clubDTO, club);
        club.setCurrentMembers(0);
        club.setActivitiesCount(0);

        // 处理Logo上传
        if (clubDTO.getLogo() != null && !clubDTO.getLogo().isEmpty()) {
            String logoUrl = fileService.uploadFile(clubDTO.getLogo(), "club_logos");
            club.setLogoUrl(logoUrl);
        }

        // 处理标签
        if (clubDTO.getTags() != null && !clubDTO.getTags().isEmpty()) {
            club.setTags(String.join(",", clubDTO.getTags()));
        }

        return clubRepository.save(club);
    }

    @Override
    public Club updateClub(Long id, ClubDTO clubDTO) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("社团不存在"));

        // 检查名称是否重复（排除自己）
        if (!club.getName().equals(clubDTO.getName()) &&
                clubRepository.existsByName(clubDTO.getName())) {
            throw new RuntimeException("社团名称已存在");
        }

        copyDtoToEntity(clubDTO, club);

        // 处理Logo上传
        if (clubDTO.getLogo() != null && !clubDTO.getLogo().isEmpty()) {
            String logoUrl = fileService.uploadFile(clubDTO.getLogo(), "club_logos");
            club.setLogoUrl(logoUrl);
        }

        // 处理标签
        if (clubDTO.getTags() != null) {
            club.setTags(String.join(",", clubDTO.getTags()));
        }

        return clubRepository.save(club);
    }

    @Override
    public void deleteClub(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("社团不存在"));
        clubRepository.delete(club);
    }

    @Override
    public Club getClubById(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("社团不存在"));
    }

    @Override
    public Page<Club> getClubs(ClubQueryDTO queryDTO) {
        Specification<Club> spec = buildSpecification(queryDTO);
        Pageable pageable = buildPageable(queryDTO);

        return clubRepository.findAll(spec, pageable);
    }

    @Override
    public List<Club> quickSearch(QuickSearchDTO searchDTO) {
        Specification<Club> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索
            if (StringUtils.isNotBlank(searchDTO.getKeyword())) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                Predicate presidentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("president")), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                predicates.add(criteriaBuilder.or(namePredicate, presidentPredicate, descriptionPredicate));
            }

            // 类别筛选
            if (StringUtils.isNotBlank(searchDTO.getCategory())) {
                predicates.add(criteriaBuilder.equal(root.get("category"), searchDTO.getCategory()));
            }

            // 状态筛选
            if (StringUtils.isNotBlank(searchDTO.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDTO.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 排序
        Sort sort = buildSort(searchDTO.getSort());

        List<Club> clubs = clubRepository.findAll(spec, sort);

        // 限制数量
        int limit = searchDTO.getLimit() != null ? searchDTO.getLimit() : 6;
        if (clubs.size() > limit) {
            clubs = clubs.subList(0, limit);
        }

        return clubs;
    }

    @Override
    public boolean checkClubName(String name) {
        return !clubRepository.existsByName(name);
    }

    @Override
    public int importClubs(MultipartFile file) {
        int count = 0;
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Club club = parseRowToClub(row);
                if (club != null) {
                    clubRepository.save(club);
                    count++;
                }
            }

        } catch (Exception e) {
            log.error("导入社团失败", e);
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
        return count;
    }

    @Override
    public byte[] exportClubs(ClubExportDTO exportDTO) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("社团列表");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"社团名称", "类别", "负责人", "当前成员数", "成立日期", "状态", "校区", "联系方式"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 查询数据
            ClubQueryDTO queryDTO = new ClubQueryDTO();
            queryDTO.setKeyword(exportDTO.getKeyword());
            // ... 设置其他查询条件

            Page<Club> clubs = getClubs(queryDTO);

            // 填充数据
            int rowNum = 1;
            for (Club club : clubs.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(club.getName());
                row.createCell(1).setCellValue(club.getCategory());
                row.createCell(2).setCellValue(club.getPresident());
                row.createCell(3).setCellValue(club.getCurrentMembers());
                row.createCell(4).setCellValue(club.getEstablishedDate().toString());
                row.createCell(5).setCellValue(club.getStatus());
                row.createCell(6).setCellValue(club.getCampus());
                row.createCell(7).setCellValue(club.getContact());
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("导出社团失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void batchOperation(List<Long> clubIds, String action) {
        List<Club> clubs = clubRepository.findAllById(clubIds);

        switch (action.toLowerCase()) {
            case "activate":
                clubs.forEach(club -> club.setStatus("active"));
                clubRepository.saveAll(clubs);
                break;
            case "deactivate":
                clubs.forEach(club -> club.setStatus("inactive"));
                clubRepository.saveAll(clubs);
                break;
            case "delete":
                // 使用自定义的删除方法
                clubRepository.deleteByIds(clubIds);
                break;
            default:
                throw new RuntimeException("不支持的操作: " + action);
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 社团总数
        long totalClubs = clubRepository.count();
        stats.put("totalClubs", totalClubs);

        // 活跃社团数
        long activeClubs = clubRepository.countByStatus("active");
        stats.put("activeClubs", activeClubs);

        // 总成员数
        Long totalMembers = clubRepository.sumCurrentMembers();
        stats.put("totalMembers", totalMembers != null ? totalMembers : 0);

        // 按类别统计
        List<Object[]> categoryStats = clubRepository.countByCategory();
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] row : categoryStats) {
            categoryMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("categoryStats", categoryMap);

        // 成员规模分布
        Map<String, Long> sizeDistribution = new HashMap<>();
        sizeDistribution.put("0-50", clubRepository.countByMemberRange(0, 50));
        sizeDistribution.put("50-100", clubRepository.countByMemberRange(50, 100));
        sizeDistribution.put("100-200", clubRepository.countByMemberRange(100, 200));
        sizeDistribution.put("200+", clubRepository.countByMemberRange(200, null));
        stats.put("sizeDistribution", sizeDistribution);

        return stats;
    }

    @Override
    public List<String> getHotSearchTags() {
        // 这里可以返回固定的热门搜索词，或者从数据库中统计
        return Arrays.asList("计算机协会", "篮球社", "志愿者协会", "音乐社",
                "摄影协会", "学术科技", "体育竞技", "文化艺术");
    }

    @Override
    public List<String> getCategories() {
        return Arrays.asList("academic", "art", "sports", "volunteer",
                "interest", "innovation");
    }

    @Override
    public List<String> getCampuses() {
        return clubRepository.findDistinctCampuses();
    }

    // 辅助方法
    private void copyDtoToEntity(ClubDTO dto, Club entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setEstablishedDate(dto.getEstablishedDate());
        entity.setMaxMembers(dto.getMaxMembers());
        entity.setPresident(dto.getPresident());
        entity.setContact(dto.getContact());
        entity.setCampus(dto.getCampus());
        entity.setStatus(dto.getStatus());
        entity.setWechatGroup(dto.getWechatGroup());
        entity.setQqGroup(dto.getQqGroup());
    }

    private Specification<Club> buildSpecification(ClubQueryDTO queryDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索
            if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
                String keyword = "%" + queryDTO.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                Predicate presidentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("president")), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                predicates.add(criteriaBuilder.or(namePredicate, presidentPredicate, descriptionPredicate));
            }

            // 类别筛选
            if (StringUtils.isNotBlank(queryDTO.getCategory())) {
                predicates.add(criteriaBuilder.equal(root.get("category"), queryDTO.getCategory()));
            }

            // 状态筛选
            if (StringUtils.isNotBlank(queryDTO.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }

            // 日期范围筛选
            if (queryDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("establishedDate"), queryDTO.getStartDate()));
            }
            if (queryDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("establishedDate"), queryDTO.getEndDate()));
            }

            // 成员数量筛选
            if (queryDTO.getMinMembers() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("currentMembers"), queryDTO.getMinMembers()));
            }
            if (queryDTO.getMaxMembers() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("currentMembers"), queryDTO.getMaxMembers()));
            }

            // 负责人筛选
            if (StringUtils.isNotBlank(queryDTO.getPresident())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("president")),
                        "%" + queryDTO.getPresident().toLowerCase() + "%"));
            }

            // 仅显示活跃社团
            if (Boolean.TRUE.equals(queryDTO.getOnlyActive())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), "active"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(ClubQueryDTO queryDTO) {
        Sort sort = Sort.by(Sort.Direction.fromString(queryDTO.getSortOrder()), queryDTO.getSortField());
        return PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize(), sort);
    }

    private Sort buildSort(String sortOption) {
        String field;
        Sort.Direction direction;

        switch (sortOption) {
            case "members":
                field = "currentMembers";
                direction = Sort.Direction.DESC;
                break;
            case "date":
                field = "establishedDate";
                direction = Sort.Direction.DESC;
                break;
            case "activities":
                field = "activitiesCount";
                direction = Sort.Direction.DESC;
                break;
            case "name":
            default:
                field = "name";
                direction = Sort.Direction.ASC;
                break;
        }

        return Sort.by(direction, field);
    }

    private Club parseRowToClub(Row row) {
        try {
            Club club = new Club();

            // 读取Excel行数据并设置到Club对象
            club.setName(getCellValue(row.getCell(0)));
            club.setCategory(getCellValue(row.getCell(1)));
            club.setDescription(getCellValue(row.getCell(2)));
            club.setPresident(getCellValue(row.getCell(3)));
            club.setContact(getCellValue(row.getCell(4)));
            club.setCampus(getCellValue(row.getCell(5)));

            // 解析日期
            Cell dateCell = row.getCell(6);
            if (dateCell != null) {
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    club.setEstablishedDate(dateCell.getLocalDateTimeCellValue().toLocalDate());
                } else {
                    club.setEstablishedDate(LocalDate.parse(getCellValue(dateCell)));
                }
            }

            // 设置默认值
            club.setCurrentMembers(0);
            club.setMaxMembers(100);
            club.setStatus("active");
            club.setActivitiesCount(0);
            club.setCreatedAt(LocalDateTime.now());
            club.setUpdatedAt(LocalDateTime.now());

            return club;
        } catch (Exception e) {
            log.warn("解析行数据失败", e);
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}