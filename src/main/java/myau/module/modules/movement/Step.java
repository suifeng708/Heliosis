package myau.module.modules.movement;

import myau.event.EventTarget;
import myau.events.UpdateEvent;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "Step", enabled = "false", hidden = "false", description = "Climbing the stairs quickly.", category = Category.MOVEMENT)
public class Step extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final FloatProperty steps = new FloatProperty("StepHeight", 1.0F, 0.6F, 10F);

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        mc.thePlayer.stepHeight = steps.getValue();
    }

    @Override
    public void onDisabled() {
        mc.thePlayer.stepHeight = 0.6F;
        super.onDisabled();
    }
}
