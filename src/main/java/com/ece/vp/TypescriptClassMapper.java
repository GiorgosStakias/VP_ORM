package com.ece.vp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypescriptClassMapper {

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
        tsClass.append("\n@Entity('").append(toPascalCase(entityName).toLowerCase()).append("')\n");
        tsClass.append("export class ").append(toPascalCase(entityName)).append(" {\n\n");

        // Process each attribute and generate the corresponding TypeScript property
        for (Attribute attribute : attributes) {

            String fieldName = attribute.getName();
            String fieldType = getTypescriptType(attribute.getType());
            boolean isPrimary = attribute.isPrimary();
            boolean isNullable = attribute.isNullable();
            boolean isUnique = attribute.isUnique();
            List<Relation> relations = attribute.getRelations();

            // Handle primary key
            if(relations == null || relations.isEmpty()) {
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
                    Boolean isOwnerSide = relation.getOwnerSide();

                    if (relationType.equals("ManyToOne")) {
                        tsClass.append("  @ManyToOne(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(entityName.toLowerCase()).append(", { nullable: true })\n");
                        tsClass.append("  @JoinColumn({ name: '").append(fieldName).append("', referencedColumnName: '").append(referenceColumn).append("' })\n");
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
                        if(isOwnerSide) {
                            tsClass.append("  @JoinColumn({ name: '").append(fieldName).append("', referencedColumnName: '").append(referenceColumn).append("' })\n");
                        }
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append(";\n\n");
                    }

                }
            }
        }

        tsClass.append("}\n");
        return tsClass.toString();
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

    // Helper to convert the provided type (e.g., "DBColumn | varchar") into TypeScript types
    private static String getTypescriptType(String dbType) {
        if (dbType.contains("bigint") || dbType.contains("int")
                || dbType.contains("float") || dbType.contains("double")
                || dbType.contains("bit")) {
            return "number";
        } else if (dbType.contains("varchar")) {
            return "string";
        } else if (dbType.contains("timestamp") || dbType.contains("datetime")) {
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
