package com.huazhu.springbootflowable.modules.modeler.service;

import com.huazhu.springbootflowable.modules.modeler.entity.ModelKeyRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AbstractModel;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.ModelRepresentation;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.idm.api.User;

import java.util.Map;
import java.util.Set;

public interface ModelService {


    Model getModel(String modelId);

    ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key);

    String createModelJson(ModelRepresentation model);

    Model createModel(ModelRepresentation model, String editorJson, User createdBy);

    Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, User updatedBy);

    Model saveModel(String modelId, String name, String key, String description, String editorJson,
                    boolean newVersion, String newVersionComment, User updatedBy);

    Set<ModelRepresentation> getModels(String filter, String sort, Integer modelType);

    Model createNewModelVersion(Model modelObject, String comment, User updatedBy);

    BpmnModel getBpmnModel(AbstractModel model, Map<String, Model> formMap, Map<String, Model> decisionTableMap);

    byte[] getBpmnXML(BpmnModel bpmnMode);
}
