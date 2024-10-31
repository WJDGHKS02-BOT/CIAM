package com.samsung.ciam.repositories;

import com.samsung.ciam.models.MenuAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuAccessControlRepository extends JpaRepository<MenuAccessControl, Long> {

    @Query(value = "SELECT * FROM menu_access_control m " +
            "WHERE ((:role = 'CIAM Admin' AND m.ciam_admin_yn = 'Y') OR " +
            "(:role = 'Channel Admin' AND m.channel_admin_yn = 'Y') OR " +
            "(:role = 'Channel biz Admin' AND m.channel_biz_admin_yn = 'Y') OR " +
            "(:role = 'Partner Admin' AND m.partner_admin_yn = 'Y') OR " +
            "(:role = 'Temp Approver' AND m.temp_approver_yn = 'Y') OR " +
            "(:role = 'General User' AND m.general_user_yn = 'Y')) " +
            "AND m.channel = :channel " +
            "ORDER BY display_seq asc",
            nativeQuery = true)
    List<MenuAccessControl> selectByRole(@Param("role") String role, @Param("channel") String channel);

    @Query(value = "SELECT COUNT(*) > 0 FROM menu_access_control m WHERE m.menu_id = :menuId",
            nativeQuery = true)
    boolean existsByMenuId(@Param("menuId") String menuId);

    @Query(value = "SELECT * FROM menu_access_control m ORDER BY display_seq asc",
            nativeQuery = true)
    List<MenuAccessControl> selectMenuList(@Param("role") String role);
}