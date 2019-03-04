package com.huazhu.springbootflowable.modules.task.service;

import com.huazhu.springbootflowable.modules.modeler.entity.domain.Model;
import org.flowable.idm.api.User;

public interface AppDefinitionPublishService {

    void publishAppDefinition(String comment, Model appDefinitionModel, User user);
}
