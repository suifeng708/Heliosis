package myau.module.modules.movement;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorEntity;
import myau.module.Category;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.property.properties.ModeProperty;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "NoWeb", enabled = "false", hidden = "false", description = "Reduces or removes cobweb slowdown", category = Category.MOVEMENT)
public class NoWeb extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{
            "None", "AAC", "LAAC", "IntaveOld", "IntaveNew", "Rewi", "OldGrim"
    });

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != EventType.PRE || mc.thePlayer == null || mc.theWorld == null) return;

        EntityPlayerSP player = mc.thePlayer;
        IAccessorEntity accessor = (IAccessorEntity) player;
        boolean inWeb = accessor.getIsInWeb();

        switch (mode.getValue()) {
            case 0:
                accessor.setIsInWeb(false);
                break;
            case 1:
                if (!inWeb) return;
                player.jumpMovementFactor = 0.59F;
                if (!mc.gameSettings.keyBindSneak.isKeyDown()) player.motionY = 0.0;
                break;
            case 2:
                if (!inWeb) return;
                player.jumpMovementFactor = player.movementInput.moveStrafe != 0.0F ? 1.0F : 1.21F;
                if (!mc.gameSettings.keyBindSneak.isKeyDown()) player.motionY = 0.0;
                if (player.onGround) player.jump();
                break;
            case 3:
                if (!inWeb) return;
                if (player.movementInput.moveStrafe == 0.0F
                        && mc.gameSettings.keyBindForward.isKeyDown() && player.isCollidedVertically) {
                    player.jumpMovementFactor = 0.74F;
                } else {
                    player.jumpMovementFactor = 0.2F;
                    player.onGround = true;
                }
                break;
            case 4:
                if (!inWeb) return;
                if (MoveUtil.isForwardPressed() && player.moveStrafing == 0.0F && player.onGround) {
                    if (player.ticksExisted % 3 == 0) {
                        MoveUtil.setSpeed(0.734F, MoveUtil.getMoveYaw());
                    } else {
                        player.jump();
                        MoveUtil.setSpeed(0.346F, MoveUtil.getMoveYaw());
                    }
                }
                break;
            case 5:
                if (!inWeb) return;
                player.jumpMovementFactor = 0.42F;
                if (player.onGround) player.jump();
                break;
            case 6:
                accessor.setIsInWeb(false);
                sendWebDigPackets(player);
                break;
        }
    }

    private void sendWebDigPackets(EntityPlayerSP player) {
        BlockPos center = new BlockPos(player.posX, player.posY, player.posZ);
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.web) {
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN
                        ));
                    }
                }
            }
        }
    }

    @Override
    public void onDisabled() {
        if (mc.thePlayer != null) mc.thePlayer.jumpMovementFactor = 0.02F;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{mode.getModeString()};
    }
}
