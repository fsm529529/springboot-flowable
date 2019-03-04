package com.huazhu.springbootflowable.modules.modeler.service;

import com.huazhu.springbootflowable.modules.modeler.entity.form.FormRepresentation;
import com.huazhu.springbootflowable.modules.modeler.entity.form.FormSaveRepresentation;

public interface FormService {

    FormRepresentation getForm(String formId);

    FormRepresentation saveForm(String formId, FormSaveRepresentation saveRepresentation);
}
