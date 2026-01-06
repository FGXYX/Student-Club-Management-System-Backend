package com.club.service;

import com.club.dto.*;
import com.club.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface ClubService {

    // 创建社团
    Club createClub(ClubDTO clubDTO);

    // 更新社团
    Club updateClub(Long id, ClubDTO clubDTO);

    // 删除社团
    void deleteClub(Long id);

    // 根据ID获取社团
    Club getClubById(Long id);

    // 获取所有社团（带分页和筛选）
    Page<Club> getClubs(ClubQueryDTO queryDTO);

    // 快速搜索
    List<Club> quickSearch(QuickSearchDTO searchDTO);

    // 检查社团名称是否可用
    boolean checkClubName(String name);

    // 批量导入社团
    int importClubs(MultipartFile file);

    // 导出社团
    byte[] exportClubs(ClubExportDTO exportDTO);

    // 批量操作
    void batchOperation(List<Long> clubIds, String action);

    // 获取统计数据
    Map<String, Object> getStatistics();

    // 获取热门搜索标签
    List<String> getHotSearchTags();

    // 获取类别列表
    List<String> getCategories();

    // 获取校区列表
    List<String> getCampuses();
}