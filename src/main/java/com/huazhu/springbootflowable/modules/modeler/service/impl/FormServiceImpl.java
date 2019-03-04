package com.huazhu.springbootflowable.modules.modeler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huazhu.springbootflowable.exception.BadRequestException;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import com.huazhu.springbootflowable.modules.modeler.entity.ModelKeyRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AbstractModel;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import com.huazhu.springbootflowable.modules.modeler.entity.form.FormRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.form.FormSaveRepresentation;
import com.huazhu.springbootflowable.modules.modeler.service.FormService;
import com.huazhu.springbootflowable.modules.modeler.service.ModelService;
import com.huazhu.springbootflowable.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;


@Service
@Slf4j
public class FormServiceImpl implements FormService {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FormRepresentation getForm(String formId) {
        Model model = modelService.getModel(formId);
        FormRepresentation form = createFormRepresentation(model);
        return form;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public FormRepresentation saveForm(String formId, FormSaveRepresentation saveRepresentation) {
        User user = SecurityUtils.getCurrentUserObject();
        Model model = modelService.getModel(formId);

        String formKey = saveRepresentation.getFormRepresentation().getKey();
        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), formKey);
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + formKey);
        }

        model.setName(saveRepresentation.getFormRepresentation().getName());
        model.setKey(formKey);
        model.setDescription(saveRepresentation.getFormRepresentation().getDescription());

        String editorJson = null;
        try {
            editorJson = objectMapper.writeValueAsString(saveRepresentation.getFormRepresentation().getFormDefinition());
        } catch (Exception e) {
            log.error("Error while processing form json", e);
            throw new InternalServerErrorException("Form could not be saved " + formId);
        }

        //String filteredImageString = saveRepresentation.getFormImageBase64().replace("data:image/png;base64,", "");
        //byte[] imageBytes = Base64.getDecoder().decode(filteredImageString);
        //Todo 不需要缩略图
        byte[] imageBytes = null;
        model = modelService.saveModel(model, editorJson, imageBytes, saveRepresentation.isNewVersion(), saveRepresentation.getComment(), user);
        FormRepresentation result = new FormRepresentation(model);
        result.setFormDefinition(saveRepresentation.getFormRepresentation().getFormDefinition());
        return result;
    }

    protected FormRepresentation createFormRepresentation(AbstractModel model) {
        SimpleFormModel formDefinition = null;
        try {
            formDefinition = objectMapper.readValue(model.getModelEditorJson(), SimpleFormModel.class);
        } catch (Exception e) {
            log.error("Error deserializing form", e);
            throw new InternalServerErrorException("Could not deserialize form definition");
        }

        FormRepresentation result = new FormRepresentation(model);
        result.setFormDefinition(formDefinition);
        return result;
    }
}
