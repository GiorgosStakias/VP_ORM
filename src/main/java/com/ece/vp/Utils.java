package com.ece.vp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
        inverseRelation.setOwnerSide(false);

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

    public static void writeFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs(); // Create directories if they don't exist
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }
    }

    public static void deleteDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }





}
