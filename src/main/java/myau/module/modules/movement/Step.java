package myau.module.modules.movement;

import jdk.jfr.Enabled;
import myau.event.EventTarget;
import myau.events.StepEvent;
import myau.events.UpdateEvent;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Step", enabled = "false", hidden = "false", description = "Climbing the stairs quickly.", category = Category.MOVEMENT)
public class Step extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"vanilla","ncp"});
    private final FloatProperty steps = new FloatProperty("StepHeight", 1.0F, 0.6F, 10F, () -> mode.getValue() == 0);

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (!isEnabled()) return;
        if (mode.getValue() == 0) {
            mc.thePlayer.stepHeight = steps.getValue();
        }
    }

    @EventTarget
    public void onStep(StepEvent e) {
        if (mode.getValue() == 1) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.75D, mc.thePlayer.posZ, false));
        }
    }

    @Override
    public void onDisabled() {
        mc.thePlayer.stepHeight = 0.6F;
        super.onDisabled();
    }
}
