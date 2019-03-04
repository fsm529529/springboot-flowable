package com.huazhu.springbootflowable.modules.modeler.mapper;

import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ModelMapper {

    List<Model> selectModelByParameters(Map<String, Object> parameters);

    List<Model> selectModelByParentModelId(String id);
}
