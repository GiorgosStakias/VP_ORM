package com.ece.vp;

import java.io.Serializable;

public class Relation implements Serializable {

    public enum RelationType {
        OneToOne,
        OneToMany,
        ManyToOne,
        ManyToMany
    }

    public RelationType getType() {
        return Type;
    }

    public void setType(RelationType type) {
        Type = type;
    }

    public String getRelatedEntity() {
        return RelatedEntity;
    }

    public void setRelatedEntity(String relatedEntity) {
        RelatedEntity = relatedEntity;
    }

    public String getReferenceColumn() {
        return ReferenceColumn;
    }

    public void setReferenceColumn(String referenceColumn) {
        ReferenceColumn = referenceColumn;
    }

    private RelationType Type;
    private String RelatedEntity;
    private String ReferenceColumn;
}

