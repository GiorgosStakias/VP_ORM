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
import java.nio.file.Paths;
import java.util.*;

public class VP_ORM_Converter {

    public void convert(String DiagramDirectory){

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
        if(Directory.exists()) {
            try {
                Utils.deleteDirectory(Paths.get(DiagramDirectory+"/"+activeDiagram.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Directory.mkdir();

        List<EntityJsonData> modelEntities = ModelEntityMapper.processDiagramElements(elementIterator);

        Utils.fixMissingRelations(modelEntities);



        for (EntityJsonData entity: modelEntities) {
            try {
//                Writer writer = new FileWriter(DiagramDirectory+"\\"+activeDiagram.getName()+"\\"+entity.getName()+".json");
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                gson.toJson(entity, writer);
//                writer.close();

                String tsClassFilePath = DiagramDirectory+"/"+activeDiagram.getName()+"/src/entities/"+entity.getName()+".ts";
                String tsClassContent = TypescriptClassMapper.mapToTypeScriptClass(entity);
                Utils.writeFile(tsClassFilePath, tsClassContent);

            } catch (IOException e) {
                // Handle any potential IOException that may occur during file operations
                e.printStackTrace();
            }
        }

        TypescriptClassMapper.generateCRUDForEntities(modelEntities, DiagramDirectory+"/"+activeDiagram.getName());
        TypescriptClassMapper.generatePostmanCollection(modelEntities, activeDiagram.getName(), DiagramDirectory+"/"+activeDiagram.getName());
        TypescriptClassMapper.generateExpressAppFiles(modelEntities, DiagramDirectory+"/"+activeDiagram.getName());
    }

}
