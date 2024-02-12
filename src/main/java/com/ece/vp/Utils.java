package com.ece.vp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final Map<String, Relation.RelationType> RelationMap;

    static {
        Map<String, Relation.RelationType> relations = new HashMap<String, Relation.RelationType>();
        relations.put("0..1|0..*", Relation.RelationType.OneToMany);
        relations.put("1|0..*", Relation.RelationType.OneToMany);

        relations.put("1|1", Relation.RelationType.OneToOne);
        relations.put("0..1|1", Relation.RelationType.OneToOne);
        relations.put("1|0..1", Relation.RelationType.OneToOne);
        relations.put("0..1|0..1", Relation.RelationType.OneToOne);

        relations.put("0..*|0..1", Relation.RelationType.ManyToOne);
        relations.put("0..*|1", Relation.RelationType.ManyToOne);

        relations.put("0..*|0..*", Relation.RelationType.ManyToMany);

        RelationMap = Collections.unmodifiableMap(relations);
    }

    public static Relation.RelationType getRelationType(String fromMultiplicity, String toMultiplicity){

        return RelationMap.get(fromMultiplicity+"|"+toMultiplicity);
    }
}
