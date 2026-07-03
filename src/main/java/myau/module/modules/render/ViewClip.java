package myau.module.modules.render;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "ViewClip", enabled = "false", hidden = "false", description = "", category = Category.RENDER)
public class ViewClip extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void onEnabled() {
        if (mc.theWorld != null) {
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onDisabled() {
        if (mc.theWorld != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
