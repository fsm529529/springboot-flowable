package com.huazhu.springbootflowable.modules.modeler.repository;

import com.huazhu.springbootflowable.modules.modeler.entity.domain.ModelRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRelationRepository extends JpaRepository<ModelRelation,String> {

    List<ModelRelation> findByParentModelIdAndType(String id, String relationshipType);
}
