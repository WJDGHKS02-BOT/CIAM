package com.samsung.ciam.repositories;

import com.samsung.ciam.models.UserOnboardHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOnboardHistoryRepository extends JpaRepository<UserOnboardHistory, Long> {

    // search all
    public List<UserOnboardHistory> findAll();

}
