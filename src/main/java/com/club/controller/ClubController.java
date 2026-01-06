package com.club.controller;

import com.club.dto.*;
import com.club.entity.Club;
import com.club.service.ClubService;
import com.club.common.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class ClubController {

    private final ClubService clubService;

    /**
     * 创建社团
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Club>> createClub(@Valid @ModelAttribute ClubDTO clubDTO) {
        Club club = clubService.createClub(clubDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("创建成功", club));
    }

    /**
     * 更新社团
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Club>> updateClub(
            @PathVariable Long id,
            @Valid @ModelAttribute ClubDTO clubDTO) {
        Club club = clubService.updateClub(id, clubDTO);
        return ResponseEntity.ok(ApiResponse.success("更新成功", club));
    }

    /**
     * 删除社团
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }

    /**
     * 获取社团详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Club>> getClub(@PathVariable Long id) {
        Club club = clubService.getClubById(id);
        return ResponseEntity.ok(ApiResponse.success(club));
    }

    /**
     * 获取社团列表（带分页和筛选）
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<Page<Club>>> getClubs(@RequestBody ClubQueryDTO queryDTO) {
        Page<Club> page = clubService.getClubs(queryDTO);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 快速搜索
     */
    @GetMapping("/quick-search")
    public ResponseEntity<ApiResponse<List<Club>>> quickSearch(@ModelAttribute QuickSearchDTO searchDTO) {
        List<Club> clubs = clubService.quickSearch(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }

    /**
     * 检查社团名称是否可用
     */
    @GetMapping("/check-name")
    public ResponseEntity<ApiResponse<Boolean>> checkClubName(@RequestParam String name) {
        boolean available = clubService.checkClubName(name);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    /**
     * 批量导入社团
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Integer>> importClubs(@RequestParam("file") MultipartFile file) {
        int count = clubService.importClubs(file);
        return ResponseEntity.ok(ApiResponse.success("成功导入 " + count + " 条数据", count));
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"club_import_template.xlsx\"");

            byte[] template = generateImportTemplate();
            response.getOutputStream().write(template);

        } catch (Exception e) {
            throw new RuntimeException("下载模板失败", e);
        }
    }

    /**
     * 导出社团数据
     */
    @PostMapping("/export")
    public void exportClubs(@RequestBody ClubExportDTO exportDTO, HttpServletResponse response) {
        try {
            byte[] excelBytes = clubService.exportClubs(exportDTO);

            String filename = "社团列表_" + System.currentTimeMillis() + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(excelBytes.length);

            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    /**
     * 批量操作
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchOperation(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> clubIds = (List<Long>) request.get("clubIds");
        String action = (String) request.get("action");

        clubService.batchOperation(clubIds, action);
        return ResponseEntity.ok(ApiResponse.success("操作成功"));
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> statistics = clubService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取热门搜索标签
     */
    @GetMapping("/hot-search-tags")
    public ResponseEntity<ApiResponse<List<String>>> getHotSearchTags() {
        List<String> tags = clubService.getHotSearchTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 获取所有类别
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = clubService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 获取所有校区
     */
    @GetMapping("/campuses")
    public ResponseEntity<ApiResponse<List<String>>> getCampuses() {
        List<String> campuses = clubService.getCampuses();
        return ResponseEntity.ok(ApiResponse.success(campuses));
    }

    /**
     * 获取状态选项
     */
    @GetMapping("/status-options")
    public ResponseEntity<ApiResponse<List<String>>> getStatusOptions() {
        List<String> statusOptions = List.of("active", "inactive", "closed");
        return ResponseEntity.ok(ApiResponse.success(statusOptions));
    }

    /**
     * 获取成员范围选项
     */
    @GetMapping("/member-range-options")
    public ResponseEntity<ApiResponse<List<String>>> getMemberRangeOptions() {
        List<String> options = List.of("0-50", "50-100", "100-200", "200+");
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    /**
     * 获取排序选项
     */
    @GetMapping("/sort-options")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSortOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("name", "按名称排序");
        options.put("members", "按成员数排序");
        options.put("date", "按成立时间排序");
        options.put("activities", "按活动数量排序");
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    /**
     * 获取负责人列表（用于搜索建议）
     */
    @GetMapping("/presidents")
    public ResponseEntity<ApiResponse<List<String>>> getPresidents(@RequestParam(required = false) String keyword) {
        List<String> presidents = List.of("张三", "李四", "王五", "赵六", "孙七");
        return ResponseEntity.ok(ApiResponse.success(presidents));
    }

    /**
     * 获取班级列表
     */
    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<String>>> getClasses(@RequestParam(required = false) String grade) {
        List<String> classes = List.of("计算机1班", "计算机2班", "软件工程1班");
        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    private byte[] generateImportTemplate() throws Exception {
        // 创建简单的Excel模板
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             var outputStream = new java.io.ByteArrayOutputStream()) {

            var sheet = workbook.createSheet("模板");

            // 创建标题行
            var headerRow = sheet.createRow(0);
            String[] headers = {"社团名称*", "类别", "描述", "负责人*", "联系方式", "校区", "成立日期(YYYY-MM-DD)"};
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 添加示例数据
            var exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("计算机协会");
            exampleRow.createCell(1).setCellValue("academic");
            exampleRow.createCell(2).setCellValue("致力于计算机技术学习与交流");
            exampleRow.createCell(3).setCellValue("张三");
            exampleRow.createCell(4).setCellValue("computer@example.com");
            exampleRow.createCell(5).setCellValue("校本部");
            exampleRow.createCell(6).setCellValue("2020-09-01");

            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}