package myau.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPlayerSP.class)
public interface IAccessorEntityPlayerSP {
    @Accessor("horseJumpPowerCounter")
    void setHorseJumpPowerCounter(int counter);

    @Accessor("horseJumpPower")
    void setHorseJumpPower(float power);
}
