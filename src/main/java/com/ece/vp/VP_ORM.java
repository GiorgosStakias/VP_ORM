package com.ece.vp;

import com.vp.plugin.VPPlugin;
import com.vp.plugin.VPPluginInfo;

public class VP_ORM implements VPPlugin {
    public void loaded(VPPluginInfo vpPluginInfo) {
        System.out.println("Hello VP plugin world!");
    }

    public void unloaded() {

    }
}
