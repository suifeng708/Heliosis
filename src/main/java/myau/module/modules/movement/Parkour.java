package myau.module.modules.movement;

import myau.event.EventTarget;
import myau.events.MoveInputEvent;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(name = "Parkour", enabled = "false", hidden = "false", description = "Automatically jumps at ledges", category = Category.MOVEMENT)
public class Parkour extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null || !player.onGround || player.isSneaking()
                || mc.gameSettings.keyBindSneak.isKeyDown() || !MoveUtil.isForwardPressed()) return;

        double[] predicted = MoveUtil.predictMovement();
        double motionX = Math.abs(player.motionX) > Math.abs(predicted[0]) ? player.motionX : predicted[0];
        double motionZ = Math.abs(player.motionZ) > Math.abs(predicted[1]) ? player.motionZ : predicted[1];
        AxisAlignedBB nextStep = player.getEntityBoundingBox().offset(motionX, -0.5, motionZ);

        if (mc.theWorld.getCollidingBoundingBoxes(player, nextStep).isEmpty()) {
            player.movementInput.jump = true;
        }
    }
}
