package com.huazhu.springbootflowable.modules.modeler.repository;

import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model,String> {

    List<Model> findByKeyAndModelType(String key, Integer modelType);
}
