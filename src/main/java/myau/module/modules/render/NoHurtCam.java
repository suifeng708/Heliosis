package myau.module.modules.render;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.PercentProperty;

@ModuleInfo(name = "NoHurtCam", enabled = "false", hidden = "true", description = "", category = Category.RENDER)
public class NoHurtCam extends Module {
    public final PercentProperty multiplier = new PercentProperty("multiplier", 0);
}
