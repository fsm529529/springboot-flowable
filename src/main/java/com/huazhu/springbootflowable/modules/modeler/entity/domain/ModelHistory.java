package com.huazhu.springbootflowable.modules.modeler.entity.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ACT_DE_MODEL_HISTORY")
public class ModelHistory extends AbstractModel {

    protected String modelId;
    protected Date removalDate;

    public ModelHistory() {
        super();
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Date getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate;
    }
}
