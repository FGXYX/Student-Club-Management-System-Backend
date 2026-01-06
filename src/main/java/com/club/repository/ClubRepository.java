package com.club.repository;

import com.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {

    boolean existsByName(String name);

    Long countByStatus(String status);

    @Query("SELECT SUM(c.currentMembers) FROM Club c")
    Long sumCurrentMembers();

    @Query("SELECT c.category, COUNT(c) FROM Club c GROUP BY c.category")
    List<Object[]> countByCategory();

    @Query("SELECT COUNT(c) FROM Club c WHERE " +
            "(:min IS NULL OR c.currentMembers >= :min) AND " +
            "(:max IS NULL OR c.currentMembers <= :max)")
    Long countByMemberRange(@Param("min") Integer min, @Param("max") Integer max);

    @Query("SELECT DISTINCT c.campus FROM Club c WHERE c.campus IS NOT NULL")
    List<String> findDistinctCampuses();

    // 删除有问题的 deleteAll(List<Long> ids) 方法
    // 使用 @Query 注解的自定义删除方法
    @Modifying
    @Transactional
    @Query("DELETE FROM Club c WHERE c.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}