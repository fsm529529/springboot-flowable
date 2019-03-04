package com.huazhu.springbootflowable.modules.modeler.controller;

import com.huazhu.springbootflowable.common.response.R;


import com.huazhu.springbootflowable.modules.modeler.entity.form.FormSaveRepresentation;
import com.huazhu.springbootflowable.modules.modeler.service.FormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/form-models")
@Slf4j
public class FormController {

    @Autowired
    private FormService formService;


    @GetMapping("/{formId}")
    public R getFormById(@PathVariable String formId) {
        return R.ok(formService.getForm(formId));
    }


    @PutMapping("/{formId}")
    public R updateForm(@PathVariable String formId,@RequestBody FormSaveRepresentation saveRepresentation) {
        return R.ok(formService.saveForm(formId, saveRepresentation));
    }
}
