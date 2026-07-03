package myau.module.modules.movement;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.FloatProperty;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "Timer", enabled = "false", hidden = "false", description = "", category = Category.MOVEMENT)
public class Timer extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final FloatProperty speed = new FloatProperty("speed", 1.0F, 0.01F, 10.0F);
    @Override
    public void onDisabled() {
        net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();
        if (timer != null) {
            timer.timerSpeed = 1.0F;
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.PRE) {
            return;
        }

        net.minecraft.util.Timer timer = ((IAccessorMinecraft) mc).getTimer();
        if (timer != null) {
            timer.timerSpeed = this.speed.getValue();
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%.1fx", this.speed.getValue())};
    }
}