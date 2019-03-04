package com.huazhu.springbootflowable.modules.modeler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huazhu.springbootflowable.common.response.R;
import com.huazhu.springbootflowable.exception.BadRequestException;
import com.huazhu.springbootflowable.exception.ConflictingRequestException;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import com.huazhu.springbootflowable.modules.modeler.entity.ModelKeyRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.ModelRepresentation;
import com.huazhu.springbootflowable.modules.modeler.service.ModelService;
import com.huazhu.springbootflowable.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/models")
@Slf4j
public class ModelController {

    @Autowired
    private ModelService modelService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public R addModel(@RequestBody ModelRepresentation modelRepresentation) {
        modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));
        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(null, modelRepresentation.getModelType(), modelRepresentation.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new ConflictingRequestException("Provided model key already exists: " + modelRepresentation.getKey());
        }
        String json = modelService.createModelJson(modelRepresentation);
        Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUserObject());
        return R.ok(new ModelRepresentation(newModel));
    }

    @GetMapping
    public R getModels(@RequestParam(required = false) String filterText, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer modelType) {
        return R.ok(modelService.getModels(filterText,sort,modelType));
    }

    @GetMapping("/{modelId}/editor/json")
    public R getModelJson(@PathVariable String modelId) {
        Model model = modelService.getModel(modelId);
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put("modelId", model.getId());
        modelNode.put("name", model.getName());
        modelNode.put("key", model.getKey());
        modelNode.put("description", model.getDescription());
        modelNode.putPOJO("lastUpdated", model.getLastUpdated());
        modelNode.put("lastUpdatedBy", model.getLastUpdatedBy());
        if (StringUtils.isNotEmpty(model.getModelEditorJson())) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
                editorJsonNode.put("modelType", "model");
                modelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                log.error("Error reading editor json {}", modelId, e);
                throw new InternalServerErrorException("Error reading editor json " + modelId);
            }

        } else {
            ObjectNode editorJsonNode = objectMapper.createObjectNode();
            editorJsonNode.put("id", "canvas");
            editorJsonNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonNode.put("modelType", "model");
            modelNode.set("model", editorJsonNode);
        }
        return R.ok(modelNode);
    }

    @PostMapping("/{modelId}/editor/json")
    public R saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        String name = values.getFirst("name");
        String key = values.getFirst("key").replaceAll(" ", "");
        String description = values.getFirst("description");
        String isNewVersionString = values.getFirst("newversion");
        String newVersionComment = values.getFirst("comment");
        String json = values.getFirst("json_xml");
        boolean newVersion = "true".equals(isNewVersionString);
        Model model;
        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

            ObjectNode propertiesNode = (ObjectNode) editorJsonNode.get("properties");

            propertiesNode.put("process_id", key);
            propertiesNode.put("name", name);
            if (StringUtils.isNotEmpty(description)) {
                propertiesNode.put("documentation", description);
            }
            editorJsonNode.set("properties", propertiesNode);
            model = modelService.saveModel(modelId, name, key, description, editorJsonNode.toString(), newVersion,
                    newVersionComment, SecurityUtils.getCurrentUserObject());

        } catch (Exception e) {
            log.error("Error saving model {}", modelId, e);
            throw new BadRequestException("Process model could not be saved " + modelId);
        }
        return R.ok(new ModelRepresentation(model));
    }
}
