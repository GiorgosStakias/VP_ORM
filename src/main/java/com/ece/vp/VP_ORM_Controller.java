package com.ece.vp;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.VPPlugin;
import com.vp.plugin.ViewManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;

import javax.swing.*;

public class VP_ORM_Controller implements VPActionController {

    private VP_ORM_Converter converter = new VP_ORM_Converter();

    public void performAction(VPAction vpAction) {

        converter.convert();
    }

    public void update(VPAction vpAction) {

    }
}
