package com.huazhu.springbootflowable.modules.modeler.entity.domain;

import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_DE_MODEL")
public class Model extends AbstractModel {

    private byte[] thumbnail;

    public Model() {
        super();
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }



}
