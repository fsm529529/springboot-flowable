package com.huazhu.springbootflowable.modules.task.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huazhu.springbootflowable.exception.BadRequestException;
import com.huazhu.springbootflowable.exception.InternalServerErrorException;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AbstractModel;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AppDefinition;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.AppModelDefinition;
import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import com.huazhu.springbootflowable.modules.modeler.mapper.ModelMapper;
import com.huazhu.springbootflowable.modules.task.service.AppDefinitionPublishService;
import com.huazhu.springbootflowable.modules.modeler.service.ModelService;
import com.huazhu.springbootflowable.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.app.api.AppRepositoryService;
import org.flowable.app.api.repository.AppDeploymentBuilder;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.dmn.xml.converter.DmnXMLConverter;
import org.flowable.editor.dmn.converter.DmnJsonConverter;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class AppDefinitionPublishServiceImpl implements AppDefinitionPublishService {

    @Autowired
    private ModelService modelService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    protected AppRepositoryService appRepositoryService;


    private DmnJsonConverter dmnJsonConverter = new DmnJsonConverter();
    private DmnXMLConverter dmnXMLConverter = new DmnXMLConverter();

    @Override
    public void publishAppDefinition(String comment, Model appDefinitionModel, User user) {
        // Create new version of the app model
        modelService.createNewModelVersion(appDefinitionModel, comment, user);

        String deployableZipName = appDefinitionModel.getKey() + ".zip";

        AppDefinition appDefinition;
        try {
            appDefinition = resolveAppDefinition(appDefinitionModel);
        } catch (Exception e) {
            log.error("Error deserializing app {}", appDefinitionModel.getId(), e);
            throw new InternalServerErrorException("Could not deserialize app definition");
        }

        if (appDefinition != null) {
            byte[] deployZipArtifact = createDeployableZipArtifact(appDefinitionModel, appDefinition);

            if (deployZipArtifact != null) {
                //部署流程定义
                deployZipArtifact(deployableZipName, deployZipArtifact, appDefinitionModel.getKey(), appDefinitionModel.getName());
            }
        }
    }

    private AppDefinition resolveAppDefinition(Model appDefinitionModel) throws IOException {
        return objectMapper.readValue(appDefinitionModel.getModelEditorJson(), AppDefinition.class);
    }

    private byte[] createDeployableZipArtifact(Model appDefinitionModel, AppDefinition appDefinition) {

        byte[] deployZipArtifact = null;
        Map<String, byte[]> deployableAssets = new HashMap<>();

        if (CollectionUtils.isNotEmpty(appDefinition.getModels()) || CollectionUtils.isNotEmpty(appDefinition.getCmmnModels())) {
            String appDefinitionJson = getAppDefinitionJson(appDefinitionModel, appDefinition);
            byte[] appDefinitionJsonBytes = appDefinitionJson.getBytes(StandardCharsets.UTF_8);

            deployableAssets.put(appDefinitionModel.getKey() + ".app", appDefinitionJsonBytes);

            Map<String, Model> formMap = new HashMap<>();
            Map<String, Model> decisionTableMap = new HashMap<>();
            Map<String, Model> caseModelMap = new HashMap<>();
            Map<String, Model> processModelMap = new HashMap<>();

            createDeployableAppModels(appDefinitionModel, appDefinition, deployableAssets, formMap, decisionTableMap, caseModelMap, processModelMap);

            if (formMap.size() > 0) {
                for (String formId : formMap.keySet()) {
                    Model formInfo = formMap.get(formId);
                    String formModelEditorJson = formInfo.getModelEditorJson();
                    byte[] formModelEditorJsonBytes = formModelEditorJson.getBytes(StandardCharsets.UTF_8);
                    deployableAssets.put("form-" + formInfo.getKey() + ".form", formModelEditorJsonBytes);
                }
            }

            if (decisionTableMap.size() > 0) {
                for (String decisionTableId : decisionTableMap.keySet()) {
                    Model decisionTableInfo = decisionTableMap.get(decisionTableId);
                    try {
                        JsonNode decisionTableNode = objectMapper.readTree(decisionTableInfo.getModelEditorJson());
                        DmnDefinition dmnDefinition = dmnJsonConverter.convertToDmn(decisionTableNode, decisionTableInfo.getId(),
                                decisionTableInfo.getVersion(), decisionTableInfo.getLastUpdated());
                        byte[] dmnXMLBytes = dmnXMLConverter.convertToXML(dmnDefinition);
                        deployableAssets.put("dmn-" + decisionTableInfo.getKey() + ".dmn", dmnXMLBytes);
                    } catch (Exception e) {
                        throw new InternalServerErrorException(String.format("Error converting decision table %s to XML", decisionTableInfo.getName()));
                    }
                }
            }

            deployZipArtifact = createDeployZipArtifact(deployableAssets);
        }

        return deployZipArtifact;
    }

    private void createDeployableAppModels(Model appDefinitionModel, AppDefinition appDefinition, Map<String, byte[]> deployableAssets, Map<String, Model> formMap, Map<String, Model> decisionTableMap, Map<String, Model> caseModelMap, Map<String, Model> processModelMap) {
        List<AppModelDefinition> appModels = new ArrayList<>();
        if (appDefinition.getModels() != null) {
            appModels.addAll(appDefinition.getModels());
        }

        if (appDefinition.getCmmnModels() != null) {
            appModels.addAll(appDefinition.getCmmnModels());
        }

        for (AppModelDefinition appModelDef : appModels) {

            if (caseModelMap.containsKey(appModelDef.getId()) || processModelMap.containsKey(appModelDef.getId())) {
                return;
            }

            AbstractModel model = modelService.getModel(appModelDef.getId());
            if (model == null) {
                throw new BadRequestException(String.format("Model %s for app definition %s could not be found", appModelDef.getId(), appDefinitionModel.getId()));
            }

            createDeployableModels(model, deployableAssets, formMap, decisionTableMap, caseModelMap, processModelMap);
        }
    }

    private byte[] createDeployZipArtifact(Map<String, byte[]> deployableAssets) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : deployableAssets.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setSize(entry.getValue().length);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }

            return baos.toByteArray();

        } catch (IOException ioe) {
            throw new InternalServerErrorException("Could not create deploy zip artifact");
        }
    }

    private String getAppDefinitionJson(Model appDefinitionModel, AppDefinition appDefinition) {
        ObjectNode appDefinitionNode = objectMapper.createObjectNode();
        appDefinitionNode.put("key", appDefinitionModel.getKey());
        appDefinitionNode.put("name", appDefinitionModel.getName());
        appDefinitionNode.put("description", appDefinitionModel.getDescription());
        appDefinitionNode.put("theme", appDefinition.getTheme());
        appDefinitionNode.put("icon", appDefinition.getIcon());
        appDefinitionNode.put("usersAccess", appDefinition.getUsersAccess());
        appDefinitionNode.put("groupsAccess", appDefinition.getGroupsAccess());
        return appDefinitionNode.toString();
    }

    private void deployZipArtifact(String artifactName, byte[] zipArtifact, String deploymentKey, String deploymentName) {
        try {
            AppDeploymentBuilder deploymentBuilder = appRepositoryService.createDeployment();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(zipArtifact);
            deploymentBuilder.addZipInputStream(new ZipInputStream(inputStream));
            deploymentBuilder.name(deploymentName);
            deploymentBuilder.key(deploymentKey);
            deploymentBuilder.tenantId(SecurityUtils.getCurrentTenantId());
            deploymentBuilder.deploy();
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void createDeployableModels(AbstractModel parentModel, Map<String, byte[]> deployableAssets,
                                          Map<String, Model> formMap, Map<String, Model> decisionTableMap, Map<String, Model> caseModelMap, Map<String, Model> processModelMap) {

        List<Model> referencedModels = modelMapper.selectModelByParentModelId(parentModel.getId());
        for (Model childModel : referencedModels) {
            if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
                formMap.put(childModel.getId(), childModel);

            } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
                decisionTableMap.put(childModel.getId(), childModel);

            } else if (Model.MODEL_TYPE_CMMN == childModel.getModelType()) {
                caseModelMap.put(childModel.getId(), childModel);
                createDeployableModels(childModel, deployableAssets, formMap, decisionTableMap, caseModelMap, processModelMap);

            } else if (Model.MODEL_TYPE_BPMN == childModel.getModelType()) {
                processModelMap.put(childModel.getId(), childModel);
                createDeployableModels(childModel, deployableAssets, formMap, decisionTableMap, caseModelMap, processModelMap);
            }
        }

        if (parentModel.getModelType() == null || parentModel.getModelType() == AbstractModel.MODEL_TYPE_BPMN) {
            BpmnModel bpmnModel = modelService.getBpmnModel(parentModel, formMap, decisionTableMap);
            Map<String, StartEvent> startEventMap = processNoneStartEvents(bpmnModel);

            for (Process process : bpmnModel.getProcesses()) {
                processUserTasks(process.getFlowElements(), process, startEventMap);
            }

            byte[] modelXML = modelService.getBpmnXML(bpmnModel);
            deployableAssets.put(parentModel.getKey().replaceAll(" ", "") + ".bpmn", modelXML);
        }
        // Todo cmmn 模型
//        } else {
//            CmmnModel cmmnModel = modelService.getCmmnModel(parentModel, formMap, decisionTableMap, caseModelMap, processModelMap);
//
//            byte[] modelXML = modelService.getCmmnXML(cmmnModel);
//            deployableAssets.put(parentModel.getKey().replaceAll(" ", "") + ".cmmn", modelXML);
//        }
    }

    private Map<String, StartEvent> processNoneStartEvents(BpmnModel bpmnModel) {
        Map<String, StartEvent> startEventMap = new HashMap<>();
        for (Process process : bpmnModel.getProcesses()) {
            for (FlowElement flowElement : process.getFlowElements()) {
                if (flowElement instanceof StartEvent) {
                    StartEvent startEvent = (StartEvent) flowElement;
                    if (CollectionUtils.isEmpty(startEvent.getEventDefinitions())) {
                        if (StringUtils.isEmpty(startEvent.getInitiator())) {
                            startEvent.setInitiator("initiator");
                        }
                        startEventMap.put(process.getId(), startEvent);
                        break;
                    }
                }
            }
        }
        return startEventMap;
    }

    private void processUserTasks(Collection<FlowElement> flowElements, Process process, Map<String, StartEvent> startEventMap) {

        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                if ("$INITIATOR".equals(userTask.getAssignee())) {
                    if (startEventMap.get(process.getId()) != null) {
                        userTask.setAssignee("${" + startEventMap.get(process.getId()).getInitiator() + "}");
                    }
                }

            } else if (flowElement instanceof SubProcess) {
                processUserTasks(((SubProcess) flowElement).getFlowElements(), process, startEventMap);
            }
        }
    }

}
