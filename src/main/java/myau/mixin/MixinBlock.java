package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.events.BlockBBEvent;
import myau.module.modules.misc.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SideOnly(Side.CLIENT)
@Mixin(value = Block.class, priority = 9999)
public abstract class MixinBlock {
    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing,
                                      CallbackInfoReturnable<Boolean> callbackInfo) {
        if (Myau.moduleManager == null) return;

        Xray xray = (Xray) Myau.moduleManager.modules.get(Xray.class);
        if (xray.isEnabled() && xray.mode.getValue() == 1
                && xray.shouldRenderSide(Block.getIdFromBlock((Block) (Object) this))) {
            BlockPos block = new BlockPos(
                    pos.getX() - facing.getDirectionVec().getX(),
                    pos.getY() - facing.getDirectionVec().getY(),
                    pos.getZ() - facing.getDirectionVec().getZ()
            );
            if (xray.checkBlock(block)) callbackInfo.setReturnValue(true);
        }
    }

    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private void getBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> callbackInfo) {
        if (Myau.moduleManager == null) return;

        Xray xray = (Xray) Myau.moduleManager.modules.get(Xray.class);
        if (xray.isEnabled()) {
            int id = Block.getIdFromBlock((Block) (Object) this);
            if (!xray.shouldRenderSide(id) || xray.mode.getValue() == 0 && !xray.isXrayBlock(id)) {
                callbackInfo.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT);
            }
        }
    }

    @Inject(method = "addCollisionBoxesToList", at = @At("HEAD"), cancellable = true)
    private void onAddCollisionBox(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask,
                                   List<AxisAlignedBB> boxes, Entity entity, CallbackInfo callbackInfo) {
        Block block = (Block) (Object) this;
        BlockBBEvent event = new BlockBBEvent(block, pos, block.getCollisionBoundingBox(world, pos, state));
        EventManager.call(event);
        if (!event.isModified()) return;

        AxisAlignedBB box = event.getBoundingBox();
        if (box != null && mask.intersectsWith(box)) boxes.add(box);
        callbackInfo.cancel();
    }
}
