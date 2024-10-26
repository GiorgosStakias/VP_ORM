package com.ece.vp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.diagram.IERDiagramUIModel;
import com.vp.plugin.diagram.IShapeUIModel;
import com.vp.plugin.diagram.format.IDiagramElementLineModel;
import com.vp.plugin.diagram.shape.INodeUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.property.IModelProperty;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class VP_ORM_Converter {

    public final String DiagramDirectory = "D:\\Giorgos_Stakias\\SHMMY\\0_Diploma\\DiagramsJson";

    public void convert(){

        DiagramManager diagramManager = ApplicationManager.instance().getDiagramManager();
        IDiagramUIModel activeDiagram = diagramManager.getActiveDiagram();


        if (activeDiagram.getType() != "ERDiagram"){

            ViewManager viewManager = ApplicationManager.instance().getViewManager();

            JFrame parentFrame = (JFrame) viewManager.getRootFrame();
            viewManager.showMessageDialog(
                    parentFrame,
                    "The currently open diagram is not an ER diagram!",
                    "Bad Diagram Type",
                    2
            );

            return;
        }

        //get an iterator on the elements of the active diagram
        Iterator elementIterator = activeDiagram.diagramElementIterator();

        ///loop the elements and for each table-entity make a json file
        ///with the necessary for the class generation data
        System.out.println("Active Diagram: " + activeDiagram.getName());
        File Directory = new File(DiagramDirectory+"\\"+activeDiagram.getName());
        if(Directory.exists()) Directory.delete();
        Directory.mkdir();

        List<EntityJsonData> modelEntitites = new ArrayList<>();

        while(elementIterator.hasNext()){

            IDiagramElement element = (IDiagramElement) elementIterator.next();
            System.out.println("Got element");
            IModelElement modelElement = element.getModelElement();             // get the model element. This is the class containing the entity data.
            System.out.println("Got model element");
            String modelName = "";
            String modelType = "";
            try {
                modelName = modelElement.getName();
                modelType = modelElement.getModelType();
            }
            catch (Exception e){
                e.printStackTrace();
                continue;
            }
            System.out.println(modelElement.getName() +": "+ modelElement.getModelType() +" Children:\n");

            // for the entities we loop through their attributes to add them to the json data
            if(modelElement.getModelType().equals("DBTable")){
                System.out.println("Inside ModelElement: "+modelElement.getName() +": "+ modelElement.getModelType() +" Children:\n");
                IModelElement [] children = modelElement.toChildArray();
                EntityJsonData modelData = new EntityJsonData();
                modelData.setName(modelElement.getName());
                List<Attribute> modelAttributes = new ArrayList<Attribute>();

                for (IModelElement child: children) {
                    System.out.println(child.getName() +" " +child.getModelType());
                    List<Relation> modelRelations = new ArrayList<Relation>();

                    try{
                        IDBColumn col = (IDBColumn)child;
                        Attribute att = new Attribute();
                        att.setName(child.getName());
                        att.setNullable(col.isNullable());
                        att.setPrimary(((IDBColumn)child).isPrimaryKey());
                        att.setUnique(((IDBColumn)child).isUnique());
                        att.setType(child.getModelType()+ " | "+((IDBColumn)child).getTypeInText());
                        Iterator fkIterator = col.foreignKeyConstraintIterator();
                        while(fkIterator.hasNext()){

                            IDBForeignKeyConstraint fkConstraint = (IDBForeignKeyConstraint) fkIterator.next();
                            IDBForeignKey fk = fkConstraint.getForeignKey();
                            Relation relation = new Relation();
                            relation.setType(Utils.getRelationType(fk.getToMultiplicity(), fk.getFromMultiplicity()));
                            relation.setRelatedEntity(fkConstraint.getRefColumn().getParent().getName());
                            relation.setReferenceColumn(fkConstraint.getRefColumn().getName());
                            if(fk.getOnUpdate() != null) relation.setOnUpdate(fk.getOnUpdate());
                            if(fk.getOnDelete() != null) relation.setOnDelete(fk.getOnDelete());

                            modelRelations.add(relation);
                            System.out.println("\t"+fk.getFrom().getName()+" ------> "+fk.getTo().getName());
                            System.out.println("\tRef Column: " + fkConstraint.getRefColumn().getParent().getName() +"." +fkConstraint.getRefColumn().getName());
                            System.out.println("\tFrom multiplicity: "+fk.getFromMultiplicity());
                            System.out.println("\tTo multiplicity: "+fk.getToMultiplicity());
                            System.out.println("\tOn update: "+fk.getOnUpdate());
                            System.out.println("\tOn delete: "+fk.getOnDelete());
                        }
                        att.setRelations(modelRelations);
                        modelAttributes.add(att);
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                        continue;
                    }
                    modelData.setAttributes(modelAttributes);
                }

                modelEntitites.add(modelData);
            }
        }

        Gson help = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Before fixing relations");
        System.out.println(help.toJson(modelEntitites));
        System.out.println("Fixing relations");

        List<EntityJsonData> uniqueEntities = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (EntityJsonData entity : modelEntitites) {
            if (!seenNames.contains(entity.getName())) {
                uniqueEntities.add(entity);
                seenNames.add(entity.getName()); // Track the entity name to prevent duplicates
            }
        }
        Utils.fixMissingRelations(uniqueEntities);
        System.out.println("After fixing relations");
        System.out.println(help.toJson(uniqueEntities));

        for (EntityJsonData entity: uniqueEntities) {
            try {
                Writer writer = new FileWriter(DiagramDirectory+"\\"+activeDiagram.getName()+"\\"+entity.getName()+".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(entity, writer);
                writer.close();

                Writer tsClassWriter = new FileWriter(DiagramDirectory+"\\"+activeDiagram.getName()+"\\"+entity.getName()+".ts");
                tsClassWriter.append(Utils.mapToTypeScriptClass(entity));
                tsClassWriter.close();

            } catch (IOException e) {
                // Handle any potential IOException that may occur during file operations
                e.printStackTrace();
            }
        }

    }

}
