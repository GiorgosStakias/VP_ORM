package com.ece.vp;

import java.io.Serializable;
import java.util.List;

public class EntityJsonData implements Serializable {

    public List<Attribute> getAttributes() {
        return Attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        Attributes = attributes;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    private List<Attribute> Attributes;
    private String Name;

}
