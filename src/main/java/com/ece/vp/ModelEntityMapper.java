package com.ece.vp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.model.*;


import javax.swing.*;
import java.io.*;
import java.util.*;

public class ModelEntityMapper {

    // Main function to handle all elements in the iterator
    public static List<EntityJsonData> processDiagramElements(Iterator elementIterator) {

        List<EntityJsonData> modelEntities = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        while (elementIterator.hasNext()) {
            IDiagramElement element = (IDiagramElement) elementIterator.next();
            IModelElement modelElement = element.getModelElement();

            if (!checkModelElement(modelElement) || seenNames.contains(modelElement.getName())) {
                 continue;
            }

            seenNames.add(modelElement.getName()); // Track the entity name to prevent duplicates

            if (modelElement != null && modelElement.getModelType().equals("DBTable")) {
                EntityJsonData modelData = handleModelElement(modelElement);
                if (modelData != null) {
                    modelEntities.add(modelData);
                }
            }
        }
        return modelEntities;
    }

    private static Boolean checkModelElement(IModelElement modelElement){
        try {
            String modelName = modelElement.getName();
            String modelType = modelElement.getModelType();
        }
        catch (Exception e){
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    // Function to handle the current model element and return EntityJsonData
    private static EntityJsonData handleModelElement(IModelElement modelElement) {
        System.out.println("Inside ModelElement: " + modelElement.getName() + ": " + modelElement.getModelType() + " Children:");
        EntityJsonData modelData = new EntityJsonData();
        modelData.setName(modelElement.getName());

        IModelElement[] children = modelElement.toChildArray();
        List<Attribute> modelAttributes = handleModelElementChildren(children);

        modelData.setAttributes(modelAttributes);
        return modelData;
    }

    // Function to handle children of the model element and return a list of Attributes
    private static List<Attribute> handleModelElementChildren(IModelElement[] children) {
        List<Attribute> modelAttributes = new ArrayList<>();

        for (IModelElement child : children) {
            System.out.println(child.getName() + " " + child.getModelType());
            try {
                IDBColumn col = (IDBColumn) child;
                Attribute attribute = new Attribute();
                attribute.setName(child.getName());
                attribute.setNullable(col.isNullable());
                attribute.setPrimary(col.isPrimaryKey());
                attribute.setUnique(col.isUnique());
                attribute.setType(child.getModelType() + " | " + col.getTypeInText());

                List<Relation> modelRelations = handleChildRelations(col);
                attribute.setRelations(modelRelations);
                modelAttributes.add(attribute);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return modelAttributes;
    }

    // Function to handle relations of each child element and return a list of Relations
    private static List<Relation> handleChildRelations(IDBColumn col) {
        List<Relation> modelRelations = new ArrayList<>();
        Iterator fkIterator = col.foreignKeyConstraintIterator();

        while (fkIterator.hasNext()) {
            IDBForeignKeyConstraint fkConstraint = (IDBForeignKeyConstraint) fkIterator.next();
            IDBForeignKey fk = fkConstraint.getForeignKey();

            Relation relation = new Relation();
            relation.setType(Utils.getRelationType(fk.getToMultiplicity(), fk.getFromMultiplicity()));
            relation.setRelatedEntity(fkConstraint.getRefColumn().getParent().getName());
            relation.setReferenceColumn(fkConstraint.getRefColumn().getName());
            relation.setOwnerSide(true);
            relation.setOnUpdate(fk.getOnUpdate());
            relation.setOnDelete(fk.getOnDelete());

            modelRelations.add(relation);

            // Logging details for the relation
            System.out.println("\t" + fk.getFrom().getName() + " ------> " + fk.getTo().getName());
            System.out.println("\tRef Column: " + fkConstraint.getRefColumn().getParent().getName() + "." + fkConstraint.getRefColumn().getName());
            System.out.println("\tFrom multiplicity: " + fk.getFromMultiplicity());
            System.out.println("\tTo multiplicity: " + fk.getToMultiplicity());
            System.out.println("\tOn update: " + fk.getOnUpdate());
            System.out.println("\tOn delete: " + fk.getOnDelete());
        }

        return modelRelations;
    }

}
