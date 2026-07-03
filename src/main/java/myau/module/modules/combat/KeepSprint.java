package myau.module.modules.combat;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;

@ModuleInfo(name = "KeepSprint", enabled = "false", hidden = "false", description = "", category = Category.COMBAT)
public class KeepSprint extends Module {
    Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"Simple", "WTap"});
    public final PercentProperty slowdown = new PercentProperty("slowdown", 0, () -> this.mode.getValue() == 1);
    public final BooleanProperty groundOnly = new BooleanProperty("ground-only", false, () -> this.mode.getValue() == 1);
    public final BooleanProperty reachOnly = new BooleanProperty("reach-only", false, () -> this.mode.getValue() == 1);

    @Override
    public boolean shouldKeepSprint() {

        if (mc.thePlayer == null || mc.theWorld == null) {
            return false;
        }

        if (groundOnly.getValue() && !mc.thePlayer.onGround) {
            return false;
        }

        if (reachOnly.getValue()) {

            if (mc.objectMouseOver == null || mc.objectMouseOver.hitVec == null) {
                return false;
            }

            double distance = mc.objectMouseOver.hitVec.distanceTo(
                    mc.getRenderViewEntity().getPositionEyes(1.0F)
            );

            return distance > 3.0;
        }

        return true;
    }
}
