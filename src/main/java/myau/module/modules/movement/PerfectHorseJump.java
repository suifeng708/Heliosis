package myau.module.modules.movement;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorEntityPlayerSP;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "PerfectHorseJump", enabled = "false", hidden = "false", description = "Always fully charges horse jumps", category = Category.MOVEMENT)
public class PerfectHorseJump extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != EventType.PRE || mc.thePlayer == null) return;
        IAccessorEntityPlayerSP accessor = (IAccessorEntityPlayerSP) mc.thePlayer;
        accessor.setHorseJumpPowerCounter(9);
        accessor.setHorseJumpPower(1.0F);
    }
}
