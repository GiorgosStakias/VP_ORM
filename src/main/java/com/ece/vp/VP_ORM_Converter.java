package com.ece.vp;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.diagram.IShapeUIModel;
import com.vp.plugin.diagram.shape.INodeUIModel;
import com.vp.plugin.model.IDBColumn;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.IStereotype;

import javax.swing.*;
import java.util.Iterator;

public class VP_ORM_Converter {

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


        Iterator elementIterator = activeDiagram.diagramElementIterator();

        while(elementIterator.hasNext()){

            IDiagramElement element = (IDiagramElement) elementIterator.next();
            IModelElement modelElement = element.getModelElement();//element.getStyle();
            System.out.println(modelElement.getName() +": "+ modelElement.getModelType() +"\nChildren:\n");

            if(modelElement.getModelType() == "DBTable"){

                IModelElement [] children = modelElement.toChildArray();

                for (IModelElement child: children) {

                    System.out.println(child.getName() + " " + ((IDBColumn)child).getTypeInText() + " !");
                }

                System.out.println("----------------------------------------");
            }



        }

    }
}
