package com.huazhu.springbootflowable.modules.modeler.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.huazhu.basejarservice.UUIDUtils;
import com.huazhu.springbootflowable.exception.NotFoundException;
import com.huazhu.springbootflowable.modules.modeler.decisiontable.DecisionTableDefinitionRepresentation;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionListModelRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.ModelKeyRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.*;
import com.huazhu.springbootflowable.modules.modeler.mapper.ModelMapper;
import com.huazhu.springbootflowable.modules.modeler.repository.ModelHistoryRepository;
import com.huazhu.springbootflowable.modules.modeler.repository.ModelRelationRepository;
import com.huazhu.springbootflowable.modules.modeler.repository.ModelRepository;
import com.huazhu.springbootflowable.modules.modeler.service.ModelService;
import com.huazhu.springbootflowable.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.editor.language.json.converter.util.JsonConverterUtil;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private ModelRelationRepository modelRelationRepository;
    @Autowired
    private ModelHistoryRepository modelHistoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    private BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    private BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

    @Override
    public Model getModel(String modelId) {
        Model model = modelRepository.getOne(modelId);

        if (model == null) {
            NotFoundException modelNotFound = new NotFoundException("No model found with the given id: " + modelId);
            String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";
            modelNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
            throw modelNotFound;
        }

        return model;
    }

    @Override
    public ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key) {
        ModelKeyRepresentation modelKeyResponse = new ModelKeyRepresentation();
        modelKeyResponse.setKey(key);

        List<Model> models = modelRepository.findByKeyAndModelType(key, modelType);
        for (Model modelInfo : models) {
            if (model == null || !modelInfo.getId().equals(model.getId())) {
                modelKeyResponse.setKeyAlreadyExists(true);
                modelKeyResponse.setId(modelInfo.getId());
                modelKeyResponse.setName(modelInfo.getName());
                break;
            }
        }

        return modelKeyResponse;
    }

    @Override
    public String createModelJson(ModelRepresentation model) {
        String json = null;
        if (Integer.valueOf(AbstractModel.MODEL_TYPE_FORM).equals(model.getModelType())) {
            try {
                json = objectMapper.writeValueAsString(new SimpleFormModel());
            } catch (Exception e) {
                log.error("Error creating form model", e);
                throw new InternalServerErrorException("Error creating form");
            }

        } else if (Integer.valueOf(AbstractModel.MODEL_TYPE_DECISION_TABLE).equals(model.getModelType())) {
            try {
                DecisionTableDefinitionRepresentation decisionTableDefinition = new DecisionTableDefinitionRepresentation();

                String decisionTableDefinitionKey = model.getName().replaceAll(" ", "");
                decisionTableDefinition.setKey(decisionTableDefinitionKey);

                json = objectMapper.writeValueAsString(decisionTableDefinition);
            } catch (Exception e) {
                log.error("Error creating decision table model", e);
                throw new InternalServerErrorException("Error creating decision table");
            }

        } else if (Integer.valueOf(AbstractModel.MODEL_TYPE_APP).equals(model.getModelType())) {
            try {
                json = objectMapper.writeValueAsString(new AppDefinition());
            } catch (Exception e) {
                log.error("Error creating app definition", e);
                throw new InternalServerErrorException("Error creating app definition");
            }

        } else if (Integer.valueOf(AbstractModel.MODEL_TYPE_CMMN).equals(model.getModelType())) {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/cmmn1.1#");
            editorNode.set("stencilset", stencilSetNode);
            ObjectNode propertiesNode = objectMapper.createObjectNode();
            propertiesNode.put("case_id", model.getKey());
            propertiesNode.put("name", model.getName());
            if (StringUtils.isNotEmpty(model.getDescription())) {
                propertiesNode.put("documentation", model.getDescription());
            }
            editorNode.set("properties", propertiesNode);

            ArrayNode childShapeArray = objectMapper.createArrayNode();
            editorNode.set("childShapes", childShapeArray);
            ObjectNode childNode = objectMapper.createObjectNode();
            childShapeArray.add(childNode);
            ObjectNode boundsNode = objectMapper.createObjectNode();
            childNode.set("bounds", boundsNode);
            ObjectNode lowerRightNode = objectMapper.createObjectNode();
            boundsNode.set("lowerRight", lowerRightNode);
            lowerRightNode.put("x", 758);
            lowerRightNode.put("y", 754);
            ObjectNode upperLeftNode = objectMapper.createObjectNode();
            boundsNode.set("upperLeft", upperLeftNode);
            upperLeftNode.put("x", 40);
            upperLeftNode.put("y", 40);
            childNode.set("childShapes", objectMapper.createArrayNode());
            childNode.set("dockers", objectMapper.createArrayNode());
            childNode.set("outgoing", objectMapper.createArrayNode());
            childNode.put("resourceId", "casePlanModel");
            ObjectNode stencilNode = objectMapper.createObjectNode();
            childNode.set("stencil", stencilNode);
            stencilNode.put("id", "CasePlanModel");
            json = editorNode.toString();

        } else {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.set("stencilset", stencilSetNode);
            ObjectNode propertiesNode = objectMapper.createObjectNode();
            propertiesNode.put("process_id", model.getKey());
            propertiesNode.put("name", model.getName());
            if (StringUtils.isNotEmpty(model.getDescription())) {
                propertiesNode.put("documentation", model.getDescription());
            }
            editorNode.set("properties", propertiesNode);

            ArrayNode childShapeArray = objectMapper.createArrayNode();
            editorNode.set("childShapes", childShapeArray);
            ObjectNode childNode = objectMapper.createObjectNode();
            childShapeArray.add(childNode);
            ObjectNode boundsNode = objectMapper.createObjectNode();
            childNode.set("bounds", boundsNode);
            ObjectNode lowerRightNode = objectMapper.createObjectNode();
            boundsNode.set("lowerRight", lowerRightNode);
            lowerRightNode.put("x", 130);
            lowerRightNode.put("y", 193);
            ObjectNode upperLeftNode = objectMapper.createObjectNode();
            boundsNode.set("upperLeft", upperLeftNode);
            upperLeftNode.put("x", 100);
            upperLeftNode.put("y", 163);
            childNode.set("childShapes", objectMapper.createArrayNode());
            childNode.set("dockers", objectMapper.createArrayNode());
            childNode.set("outgoing", objectMapper.createArrayNode());
            childNode.put("resourceId", "startEvent1");
            ObjectNode stencilNode = objectMapper.createObjectNode();
            childNode.set("stencil", stencilNode);
            stencilNode.put("id", "StartNoneEvent");
            json = editorNode.toString();
        }

        return json;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Model createModel(ModelRepresentation model, String editorJson, User createdBy) {
        Model newModel = new Model();
        newModel.setId(UUIDUtils.getId());
        newModel.setVersion(1);
        newModel.setName(model.getName());
        newModel.setKey(model.getKey());
        newModel.setModelType(model.getModelType());
        newModel.setCreated(Calendar.getInstance().getTime());
        newModel.setCreatedBy(createdBy.getId());
        newModel.setDescription(model.getDescription());
        newModel.setModelEditorJson(editorJson);
        newModel.setLastUpdated(Calendar.getInstance().getTime());
        newModel.setLastUpdatedBy(createdBy.getId());
        newModel.setTenantId(createdBy.getTenantId());

        persistModel(newModel);
        return newModel;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, User updatedBy) {
        return internalSave(modelObject.getName(), modelObject.getKey(), modelObject.getDescription(), editorJson, newVersion,
                newVersionComment, imageBytes, updatedBy, modelObject);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Model saveModel(String modelId, String name, String key, String description, String editorJson, boolean newVersion, String newVersionComment, User updatedBy) {
        Model modelObject = modelRepository.getOne(modelId);
        return internalSave(name, key, description, editorJson, newVersion, newVersionComment, null, updatedBy, modelObject);
    }

    @Override
    public Set<ModelRepresentation> getModels(String filter, String sort, Integer modelType) {

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("modelType", modelType);
        params.put("filter", makeValidFilterText(filter));
        params.put("sort", sort);
        params.put("tenantId", SecurityUtils.getCurrentTenantId());
        List<Model> models = modelMapper.selectModelByParameters(params);
        return models.stream().map(this::createModelRepresentation).collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Model createNewModelVersion(Model modelObject, String comment, User updatedBy) {
        return (Model) internalCreateNewModelVersion(modelObject, comment, updatedBy, false);
    }

    @Override
    public BpmnModel getBpmnModel(AbstractModel model, Map<String, Model> formMap, Map<String, Model> decisionTableMap) {
        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
            Map<String, String> formKeyMap = new HashMap<>();
            for (Model formModel : formMap.values()) {
                formKeyMap.put(formModel.getId(), formModel.getKey());
            }

            Map<String, String> decisionTableKeyMap = new HashMap<>();
            for (Model decisionTableModel : decisionTableMap.values()) {
                decisionTableKeyMap.put(decisionTableModel.getId(), decisionTableModel.getKey());
            }

            return bpmnJsonConverter.convertToBpmnModel(editorJsonNode, formKeyMap, decisionTableKeyMap);

        } catch (Exception e) {
            log.error("Could not generate BPMN 2.0 model for {}", model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
        }
    }

    @Override
    public byte[] getBpmnXML(BpmnModel bpmnModel) {
        for (Process process : bpmnModel.getProcesses()) {
            if (StringUtils.isNotEmpty(process.getId())) {
                char firstCharacter = process.getId().charAt(0);
                // no digit is allowed as first character
                if (Character.isDigit(firstCharacter)) {
                    process.setId("a" + process.getId());
                }
            }
        }
        return bpmnXMLConverter.convertToXML(bpmnModel);
    }

    private Model persistModel(Model model) {

        if (StringUtils.isNotEmpty(model.getModelEditorJson())) {

            // Parse json to java
            ObjectNode jsonNode = null;
            try {
                jsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
            } catch (Exception e) {
                log.error("Could not deserialize json model", e);
                throw new InternalServerErrorException("Could not deserialize json model");
            }

            if ((model.getModelType() == null || model.getModelType() == Model.MODEL_TYPE_BPMN)) {

                // Thumbnail
                //byte[] thumbnail = modelImageService.generateThumbnailImage(model, jsonNode);
                //Todo 缩略图
                byte[] thumbnail = null;
                if (thumbnail != null) {
                    model.setThumbnail(thumbnail);
                }

                modelRepository.save(model);

                // Relations
                handleBpmnProcessFormModelRelations(model, jsonNode);
                handleBpmnProcessDecisionTaskModelRelations(model, jsonNode);

            }

//            else if (model.getModelType().intValue() == Model.MODEL_TYPE_CMMN) {
//
//                // Thumbnail
//                byte[] thumbnail = modelImageService.generateCmmnThumbnailImage(model, jsonNode);
//                if (thumbnail != null) {
//                    model.setThumbnail(thumbnail);
//                }
//
//                modelRepository.save(model);
//
//                // Relations
//                handleCmmnFormModelRelations(model, jsonNode);
//                handleCmmnDecisionModelRelations(model, jsonNode);
//                handleCmmnCaseModelRelations(model, jsonNode);
//                handleCmmnProcessModelRelations(model, jsonNode);
//
//            }


            else if (model.getModelType() == Model.MODEL_TYPE_FORM ||
                    model.getModelType() == Model.MODEL_TYPE_DECISION_TABLE) {

                jsonNode.put("name", model.getName());
                jsonNode.put("key", model.getKey());
                modelRepository.save(model);

            } else if (model.getModelType() == Model.MODEL_TYPE_APP) {

                modelRepository.save(model);
                handleAppModelProcessRelations(model, jsonNode);
            }
        }

        return model;
    }

    private void handleBpmnProcessFormModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
        List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(editorJsonNode));
        Set<String> formIds = JsonConverterUtil.gatherStringPropertyFromJsonNodes(formReferenceNodes, "id");

        handleModelRelations(bpmnProcessModel, formIds, ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
    }

    private void handleAppModelProcessRelations(AbstractModel appModel, ObjectNode appModelJsonNode) {
        Set<String> processModelIds = JsonConverterUtil.getAppModelReferencedModelIds(appModelJsonNode);
        handleModelRelations(appModel, processModelIds, ModelRelationTypes.TYPE_PROCESS_MODEL);
    }

    private void handleModelRelations(AbstractModel bpmnProcessModel, Set<String> idsReferencedInJson, String relationshipType) {

        // Find existing persisted relations
        List<ModelRelation> persistedModelRelations = modelRelationRepository.findByParentModelIdAndType(bpmnProcessModel.getId(), relationshipType);

        // if no ids referenced now, just delete them all
        if (idsReferencedInJson == null || idsReferencedInJson.size() == 0) {
            for (ModelRelation modelRelation : persistedModelRelations) {
                modelRelationRepository.delete(modelRelation);
            }
            return;
        }

        Set<String> alreadyPersistedModelIds = new HashSet<>(persistedModelRelations.size());
        for (ModelRelation persistedModelRelation : persistedModelRelations) {
            if (!idsReferencedInJson.contains(persistedModelRelation.getModelId())) {
                // model used to be referenced, but not anymore. Delete it.
                modelRelationRepository.delete(persistedModelRelation);
            } else {
                alreadyPersistedModelIds.add(persistedModelRelation.getModelId());
            }
        }

        // Loop over all referenced ids and see which one are new
        for (String idReferencedInJson : idsReferencedInJson) {

            // if model is referenced, but it is not yet persisted = create it
            if (!alreadyPersistedModelIds.contains(idReferencedInJson)) {

                // Check if model actually still exists. Don't create the relationship if it doesn't exist. The client UI will have cope with this too.
//                if (modelRepository.get(idReferencedInJson) != null) {
//                    modelRelationRepository.save(new ModelRelation(bpmnProcessModel.getId(), idReferencedInJson, relationshipType));
//                }
                System.out.println(idReferencedInJson);
            }
        }
    }

    private void handleBpmnProcessDecisionTaskModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
        List<JsonNode> decisionTableNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelDecisionTableReferences(editorJsonNode));
        Set<String> decisionTableIds = JsonConverterUtil.gatherStringPropertyFromJsonNodes(decisionTableNodes, "id");

        handleModelRelations(bpmnProcessModel, decisionTableIds, ModelRelationTypes.TYPE_DECISION_TABLE_MODEL_CHILD);
    }

    private Model internalSave(String name, String key, String description, String editorJson, boolean newVersion,
                                 String newVersionComment, byte[] imageBytes, User updatedBy, Model modelObject) {

        if (!newVersion) {

            modelObject.setLastUpdated(new Date());
            modelObject.setLastUpdatedBy(updatedBy.getId());
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModelEditorJson(editorJson);

            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }

        } else {

            ModelHistory historyModel = createNewModelhistory(modelObject);
            persistModelHistory(historyModel);

            modelObject.setVersion(modelObject.getVersion() + 1);
            modelObject.setLastUpdated(new Date());
            modelObject.setLastUpdatedBy(updatedBy.getId());
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModelEditorJson(editorJson);
            modelObject.setComment(newVersionComment);

            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }
        }

        return persistModel(modelObject);
    }

    private ModelHistory createNewModelhistory(Model model) {
        ModelHistory historyModel = new ModelHistory();
        historyModel.setName(model.getName());
        historyModel.setKey(model.getKey());
        historyModel.setDescription(model.getDescription());
        historyModel.setCreated(model.getCreated());
        historyModel.setLastUpdated(model.getLastUpdated());
        historyModel.setCreatedBy(model.getCreatedBy());
        historyModel.setLastUpdatedBy(model.getLastUpdatedBy());
        historyModel.setModelEditorJson(model.getModelEditorJson());
        historyModel.setModelType(model.getModelType());
        historyModel.setVersion(model.getVersion());
        historyModel.setModelId(model.getId());
        historyModel.setComment(model.getComment());
        historyModel.setTenantId(model.getTenantId());
        historyModel.setId(UUIDUtils.getId());

        return historyModel;
    }

    private void persistModelHistory(ModelHistory modelHistory) {
        modelHistoryRepository.save(modelHistory);
    }

    private ModelRepresentation createModelRepresentation(AbstractModel model) {
        ModelRepresentation representation = null;
        if (model.getModelType() != null && model.getModelType() == 3) {
            representation = new AppDefinitionListModelRepresentation(model);

            AppDefinition appDefinition = null;
            try {
                appDefinition = objectMapper.readValue(model.getModelEditorJson(), AppDefinition.class);
            } catch (Exception e) {
                throw new InternalServerErrorException("Could not deserialize app definition");
            }
            ((AppDefinitionListModelRepresentation) representation).setAppDefinition(appDefinition);

        } else {
            representation = new ModelRepresentation(model);
        }
        return representation;
    }

    private String makeValidFilterText(String filterText) {
        String validFilter = null;

        if (filterText != null) {
            String trimmed = StringUtils.trim(filterText);
            Integer MIN_FILTER_LENGTH = 1;
            if (trimmed.length() >= MIN_FILTER_LENGTH) {
                validFilter = "%" + trimmed.toLowerCase() + "%";
            }
        }
        return validFilter;
    }

    private AbstractModel internalCreateNewModelVersion(Model modelObject, String comment, User updatedBy, boolean returnModelHistory) {
        modelObject.setLastUpdated(new Date());
        modelObject.setLastUpdatedBy(updatedBy.getId());
        modelObject.setComment(comment);

        ModelHistory historyModel = createNewModelhistory(modelObject);
        persistModelHistory(historyModel);

        modelObject.setVersion(modelObject.getVersion() + 1);
        persistModel(modelObject);

        return returnModelHistory ? historyModel : modelObject;
    }


}
