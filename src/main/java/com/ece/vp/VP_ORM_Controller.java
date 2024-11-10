package com.ece.vp;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.VPPlugin;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;

import javax.swing.*;
import java.io.File;

public class VP_ORM_Controller implements VPActionController {

    private VP_ORM_Converter converter = new VP_ORM_Converter();

    public void performAction(VPAction vpAction) {

        String apiDirectory = selectDirectory();
        if(apiDirectory == "") return;

        converter.convert(apiDirectory);
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
