package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.*;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);

    
    @Query(value = "SELECT cas.channel , cas.approver_name , u.*  FROM public.users u JOIN public.channel_approval_statuses cas   ON u.cdc_uid = cas.login_uid " +
    "WHERE cas.channel = :channel AND cas.status = 'approved';" , nativeQuery = true)
    //List<Users> findByChannel(@Param("channel") String select_channel ); //partnerhub
    List<Map<String,Object>>  findByChannel(@Param("channel") String channel ); //partnerhub

}
