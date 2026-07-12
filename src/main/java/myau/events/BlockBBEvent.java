package myau.events;

import myau.event.events.Event;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class BlockBBEvent implements Event {
    private final Block block;
    private final BlockPos pos;
    private AxisAlignedBB boundingBox;
    private boolean modified;

    public BlockBBEvent(Block block, BlockPos pos, AxisAlignedBB boundingBox) {
        this.block = block;
        this.pos = pos;
        this.boundingBox = boundingBox;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }
}
