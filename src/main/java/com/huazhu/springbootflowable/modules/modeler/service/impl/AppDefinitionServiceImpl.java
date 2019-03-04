package com.huazhu.springbootflowable.modules.modeler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionSaveRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AbstractModel;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AppDefinition;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import com.huazhu.springbootflowable.modules.task.service.AppDefinitionPublishService;
import com.huazhu.springbootflowable.modules.modeler.service.AppDefinitionService;
import com.huazhu.springbootflowable.modules.modeler.service.ModelService;
import com.huazhu.springbootflowable.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AppDefinitionServiceImpl implements AppDefinitionService {

    @Autowired
    private ModelService modelService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppDefinitionPublishService appDefinitionPublishService;


    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public AppDefinitionRepresentation updateAppDefinition(String modelId, AppDefinitionSaveRepresentation updatedModel) {
        User user = SecurityUtils.getCurrentUserObject();

        Model model = modelService.getModel(modelId);

        model.setName(updatedModel.getAppDefinition().getName());
        model.setKey(updatedModel.getAppDefinition().getKey());
        model.setDescription(updatedModel.getAppDefinition().getDescription());
        String editorJson;
        try {
            editorJson = objectMapper.writeValueAsString(updatedModel.getAppDefinition().getDefinition());
        } catch (Exception e) {
            log.error("Error while processing app definition json {}", modelId, e);
            throw new InternalServerErrorException("App definition could not be saved " + modelId);
        }

        model = modelService.saveModel(model, editorJson, null, false, null, user);

        AppDefinitionRepresentation appDefinition = createAppDefinitionRepresentation(model);
        if (updatedModel.isPublish()) {
            appDefinitionPublishService.publishAppDefinition(null, model, user);
        } else {
            appDefinition.setDefinition(updatedModel.getAppDefinition().getDefinition());
        }
        return appDefinition;
    }

    private AppDefinitionRepresentation createAppDefinitionRepresentation(AbstractModel model) {
        AppDefinition appDefinition = null;
        try {
            appDefinition = objectMapper.readValue(model.getModelEditorJson(), AppDefinition.class);
        } catch (Exception e) {
            log.error("Error deserializing app {}", model.getId(), e);
            throw new InternalServerErrorException("Could not deserialize app definition");
        }
        AppDefinitionRepresentation result = new AppDefinitionRepresentation(model);
        result.setDefinition(appDefinition);
        return result;
    }
}
