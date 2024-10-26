package com.ece.vp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

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

    public static void fixMissingRelations(List<EntityJsonData> entityList) {
        // Iterate through each entity to check for ownership-defined relations
        for (EntityJsonData entity : entityList) {
            System.out.println("\n\nRelations of " + entity.getName() +"\n\n");
            for (Attribute attribute : entity.getAttributes()) {
                List<Relation> relations = attribute.getRelations();

                if (relations != null) {
                    for (Relation relation : relations) {
                        Optional<EntityJsonData> relatedEntityOpt = findEntityByName(entityList, relation.getRelatedEntity());

                        if (relatedEntityOpt.isPresent()) {
                            EntityJsonData relatedEntity = relatedEntityOpt.get();
                            boolean inverseRelationExists = checkInverseRelationExists(relatedEntity, entity.getName(), relation.getType());

                            if (!inverseRelationExists) {
                                // Add the appropriate inverse relationship
                                addInverseRelation(relatedEntity, entity, relation, attribute.getName());
                                System.out.println("Added relation " + relatedEntity.getName() + " ---> " + entity.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper to find an entity by name in the list
    private static Optional<EntityJsonData> findEntityByName(List<EntityJsonData> entityList, String entityName) {
        return entityList.stream()
                .filter(e -> e.getName().equals(entityName))
                .findFirst();
    }

    // Helper to check if the inverse relation already exists
    private static boolean checkInverseRelationExists(EntityJsonData entity, String relatedEntityName, Relation.RelationType originalRelationType) {
        for (Attribute attribute : entity.getAttributes()) {
            List<Relation> relations = attribute.getRelations();
            if (relations != null) {
                for (Relation relation : relations) {
                    // Check if the inverse relation is already defined
                    if (relation.getRelatedEntity().equals(relatedEntityName) &&
                            relation.getType() == getInverseRelationType(originalRelationType)) {
                        System.out.println(entity.getName() + " ---> " + relatedEntityName + " exists");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Helper to add the inverse relationship to the non-owning entity
    private static void addInverseRelation(EntityJsonData relatedEntity, EntityJsonData ownerEntity, Relation originalRelation, String refColumn) {
        Relation inverseRelation = new Relation();
        inverseRelation.setType(getInverseRelationType(originalRelation.getType()));
        inverseRelation.setRelatedEntity(ownerEntity.getName());
        inverseRelation.setReferenceColumn(refColumn);

        Attribute attribute = new Attribute();
        attribute.setName(ownerEntity.getName().toLowerCase()); // The inverse relation name based on the owner's name
        attribute.setType("DBColumn | foreign"); // Assuming 'foreign' as a placeholder type

        // Assign the new inverse relation to the attribute
        attribute.setRelations(new ArrayList<>());
        attribute.getRelations().add(inverseRelation);

        // Add the attribute with the inverse relation to the related entity
        relatedEntity.getAttributes().add(attribute);
    }

    // Helper to determine the inverse relation type
    private static Relation.RelationType getInverseRelationType(Relation.RelationType originalRelationType) {
        if (originalRelationType == Relation.RelationType.ManyToOne) {
            return Relation.RelationType.OneToMany;
        } else if (originalRelationType == Relation.RelationType.OneToOne) {
            return Relation.RelationType.OneToOne;
        } else if (originalRelationType == Relation.RelationType.OneToMany) {
            return Relation.RelationType.ManyToOne;
        }
        throw new IllegalArgumentException("Unsupported relation type: " + originalRelationType);
    }

    public static String mapToTypeScriptClass(EntityJsonData entityJsonData) {
        StringBuilder tsClass = new StringBuilder();
        String entityName = entityJsonData.getName();
        List<Attribute> attributes = entityJsonData.getAttributes();
        Set<String> relatedEntities = new HashSet<>();

        System.out.println(entityName + " TS Mapping");

        // Start building the TypeScript class
        tsClass.append("import { Entity, PrimaryColumn, Column, ");

        // Add relationship decorators if any exist
        if (attributesHasRelations(attributes)) {
            tsClass.append("ManyToOne, OneToMany, JoinColumn");
        }

        collectRelatedEntities(attributes, relatedEntities);

        tsClass.append(" } from 'typeorm';\n\n");

        for (String relatedEntity : relatedEntities) {
            tsClass.append("import { ").append(toPascalCase(relatedEntity)).append(" } from './").append(relatedEntity).append("';\n");
        }


        // Add the @Entity decorator and class definition
        tsClass.append("\n@Entity('").append(entityName).append("')\n");
        tsClass.append("export class ").append(toPascalCase(entityName)).append(" {\n\n");

        // Process each attribute and generate the corresponding TypeScript property
        for (Attribute attribute : attributes) {

            String fieldName = attribute.getName();
            String fieldType = getTypescriptType(attribute.getType());
            boolean isPrimary = attribute.isPrimary();
            boolean isNullable = attribute.isNullable();
            boolean isUnique = attribute.isUnique();
            List<Relation> relations = attribute.getRelations();

            System.out.println(fieldName);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(attribute));
            // Handle primary key
            if(relations == null || relations.isEmpty()) {
                System.out.println(fieldName + " has no relations");
                if (isPrimary) {
                    tsClass.append("  @PrimaryColumn()\n");
                } else {
                    tsClass.append("  @Column(");
                    if (isUnique || isNullable) {
                        tsClass.append("{ ");
                        if (isUnique) tsClass.append("unique: true, ");
                        if (isNullable) tsClass.append("nullable: true ");
                        tsClass.append("}");
                    }
                    tsClass.append(")\n");
                }

                // Add the field definition
                tsClass.append("  ").append(fieldName).append(": ").append(fieldType).append(";\n\n");
            }
            // Handle relationships (e.g., ManyToOne)
            else {
                for (Relation relation : relations) {
                    String relationType = relation.getType().name();
                    String relatedEntity = toPascalCase(relation.getRelatedEntity());
                    String referenceColumn = relation.getReferenceColumn();

                    if (relationType.equals("ManyToOne")) {
                        tsClass.append("  @ManyToOne(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(entityName.toLowerCase()).append(", { nullable: true })\n");
                        tsClass.append("  @JoinColumn({ name: '").append(referenceColumn).append("' })\n");
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append(";\n\n");
                    }
                    // Handle OneToMany
                    if (relationType.equals("OneToMany")) {
                        tsClass.append("  @OneToMany(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(referenceColumn).append(")\n");
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append("[];\n\n");
                    }

                    // Handle OneToOne
                    if (relationType.equals("OneToOne")) {
                        tsClass.append("  @OneToOne(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(referenceColumn).append(", { nullable: true })\n");
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append(";\n\n");
                    }

                }
            }
        }

        tsClass.append("}\n");
        return tsClass.toString();
    }

    private static void collectRelatedEntities(List<Attribute> attributes, Set<String> relatedEntities) {
        for (Attribute attribute : attributes) {
            List<Relation> relations = attribute.getRelations();
            if (relations != null) {
                for (Relation relation : relations) {
                    relatedEntities.add(relation.getRelatedEntity());
                }
            }
        }
    }

    // Helper to check if any attribute contains relationships
    private static boolean attributesHasRelations(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            List<Relation> relations = attribute.getRelations();
            if (relations != null && !relations.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // Helper to convert the provided type (e.g., "DBColumn | varchar") into TypeScript types
    private static String getTypescriptType(String dbType) {
        if (dbType.contains("bigint") || dbType.contains("int")
            || dbType.contains("float") || dbType.contains("double")
            || dbType.contains("bit")) {
            return "number";
        } else if (dbType.contains("varchar")) {
            return "string";
        } else if (dbType.contains("timestamp")) {
            return "Date";
        }
        return "any"; // Fallback type
    }

    // Helper to convert the entity name to PascalCase (e.g., "data_providers" to "DataProviders")
    private static String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.split("_")) {
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }
}
