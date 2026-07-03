package myau.module.modules;

import myau.Myau;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.module.modules.render.ClickGUIModule;
import myau.ui.impl.clickgui.normal.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", enabled = "false", hidden = "false", description = "", category = Category.RENDER)
public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public GuiModule() {
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
        ClickGUIModule clickGui = (ClickGUIModule) Myau.moduleManager.getModule("ClickGUI");
        if (clickGui != null) {
            if (clickGui.isEnabled()) {
                clickGui.openSelectedGui();
            } else {
                clickGui.setEnabled(true);
            }
            return;
        }
        mc.displayGuiScreen(ClickGuiScreen.getInstance());
    }
}
