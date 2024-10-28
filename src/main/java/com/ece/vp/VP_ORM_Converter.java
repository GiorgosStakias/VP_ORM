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

        List<EntityJsonData> modelEntities = ModelEntityMapper.processDiagramElements(elementIterator);

        Utils.fixMissingRelations(modelEntities);

        for (EntityJsonData entity: modelEntities) {
            try {
                Writer writer = new FileWriter(DiagramDirectory+"\\"+activeDiagram.getName()+"\\"+entity.getName()+".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(entity, writer);
                writer.close();

                Writer tsClassWriter = new FileWriter(DiagramDirectory+"\\"+activeDiagram.getName()+"\\"+entity.getName()+".ts");
                tsClassWriter.append(TypescriptClassMapper.mapToTypeScriptClass(entity));
                tsClassWriter.close();

            } catch (IOException e) {
                // Handle any potential IOException that may occur during file operations
                e.printStackTrace();
            }
        }

    }

}
