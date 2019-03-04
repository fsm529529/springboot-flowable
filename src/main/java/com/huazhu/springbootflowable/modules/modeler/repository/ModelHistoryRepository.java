package com.huazhu.springbootflowable.modules.modeler.repository;

import com.huazhu.springbootflowable.modules.modeler.entity.domain.ModelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelHistoryRepository extends JpaRepository<ModelHistory,String> {
}
