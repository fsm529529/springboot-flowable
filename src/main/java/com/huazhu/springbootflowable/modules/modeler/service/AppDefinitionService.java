package com.huazhu.springbootflowable.modules.modeler.service;

import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionSaveRepresentation;

public interface AppDefinitionService {

    AppDefinitionRepresentation updateAppDefinition(String modelId, AppDefinitionSaveRepresentation updatedModel);
}
