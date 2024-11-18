package com.ece.vp;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.VPPlugin;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VP_ORM_Controller implements VPActionController {

    private VP_ORM_Converter converter = new VP_ORM_Converter();

    public void performAction(VPAction vpAction) {

        DiagramManager diagramManager = ApplicationManager.instance().getDiagramManager();
        IDiagramUIModel activeDiagram = diagramManager.getActiveDiagram();

        if (activeDiagram.getType() != "ERDiagram"){

            ViewManager viewManager = ApplicationManager.instance().getViewManager();

            Component parentFrame = viewManager.getRootFrame();
            viewManager.showMessageDialog(
                    parentFrame,
                    "The currently open diagram is not an ER diagram!",
                    "Bad Diagram Type",
                    2
            );
            return;
        }

        String apiDirectory = selectDirectory();
        if(apiDirectory == "") return;

        String generatedFolder = converter.convert(apiDirectory, activeDiagram);

        ViewManager viewManager = ApplicationManager.instance().getViewManager();
        Component parentFrame = viewManager.getRootFrame();
        viewManager.showMessageDialog(
                parentFrame,
                "Typescript API generated in folder: " + generatedFolder,
                "Successful API generation!",
                1
        );
    }

    public void update(VPAction vpAction) {

    }

    public String selectDirectory() {
        File selectedDirectory;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select a Directory");

        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            System.out.println("Selected directory: " + selectedDirectory.getAbsolutePath());
            return selectedDirectory.getAbsolutePath();
        } else {
            System.out.println("No directory selected");
            return "";
        }
    }


}
