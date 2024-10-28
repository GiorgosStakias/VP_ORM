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

    public String getOnUpdate() {
        return OnUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        OnUpdate = onUpdate;
    }

    public String getOnDelete() {
        return OnDelete;
    }

    public void setOnDelete(String onDelete) {
        OnDelete = onDelete;
    }

    public Boolean getOwnerSide() { return IsOwnerSide; }

    public void setOwnerSide(Boolean ownerSide) { IsOwnerSide = ownerSide; }


    private String OnUpdate;
    private String OnDelete;
    private RelationType Type;
    private String RelatedEntity;
    private String ReferenceColumn;
    private Boolean IsOwnerSide;
}

