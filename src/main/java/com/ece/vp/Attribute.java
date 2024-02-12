package com.ece.vp;

import java.io.Serializable;
import java.util.List;

public class Attribute implements Serializable {

    private String Name;
    private String Type;
    private boolean IsPrimary;
    private boolean IsUnique;
    private boolean IsNullable;
    private List<Relation> Relations;

    public List<Relation> getRelations() {
        return Relations;
    }

    public void setRelations(List<Relation> relations) {
        Relations = relations;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public boolean isPrimary() {
        return IsPrimary;
    }

    public void setPrimary(boolean primary) {
        IsPrimary = primary;
    }

    public boolean isUnique() {
        return IsUnique;
    }

    public void setUnique(boolean unique) {
        IsUnique = unique;
    }

    public boolean isNullable() {
        return IsNullable;
    }

    public void setNullable(boolean nullable) {
        IsNullable = nullable;
    }

}
