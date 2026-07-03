package myau.module.modules.player;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

@ModuleInfo(name = "SpeedMine", enabled = "false", hidden = "false", description = "", category = Category.PLAYER)
public class SpeedMine extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final PercentProperty speed = new PercentProperty("speed", 15);
    public final IntProperty delay = new IntProperty("delay", 0, 0, 4);
    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (!mc.playerController.isInCreativeMode()) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                    ((IAccessorPlayerControllerMP) mc.playerController)
                            .setBlockHitDelay(Math.min(((IAccessorPlayerControllerMP) mc.playerController).getBlockHitDelay(), this.delay.getValue() + 1));
                    if (((IAccessorPlayerControllerMP) mc.playerController).getIsHittingBlock()) {
                        float curBlockDamageMP = ((IAccessorPlayerControllerMP) mc.playerController).getCurBlockDamageMP();
                        float damage = 0.3F * (this.speed.getValue().floatValue() / 100.0F);
                        if (curBlockDamageMP < damage) {
                            ((IAccessorPlayerControllerMP) mc.playerController).setCurBlockDamageMP(damage);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%d%%", this.speed.getValue())};
    }
}
