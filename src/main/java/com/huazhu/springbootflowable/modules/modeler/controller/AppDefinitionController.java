package com.huazhu.springbootflowable.modules.modeler.controller;

import com.huazhu.springbootflowable.common.response.R;
import com.huazhu.springbootflowable.modules.modeler.entity.AppDefinitionSaveRepresentation;
import com.huazhu.springbootflowable.modules.modeler.service.AppDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-definitions")
public class AppDefinitionController {

    @Autowired
    private AppDefinitionService appDefinitionService;

    @PutMapping("/{modelId}")
    public R updateAppDefinition(@PathVariable("modelId") String modelId, @RequestBody AppDefinitionSaveRepresentation updatedModel) {
        return R.ok(appDefinitionService.updateAppDefinition(modelId,updatedModel));
    }
}
