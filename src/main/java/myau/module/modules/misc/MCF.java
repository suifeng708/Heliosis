package myau.module.modules.misc;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.KeyEvent;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

@ModuleInfo(name = "MCF", enabled = "false", hidden = "true", description = "", category = Category.MISC)
public class MCF extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @EventTarget
    public void onKey(KeyEvent event) {
        if (this.isEnabled() && event.getKey() == -98) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                String hitName = mc.objectMouseOver.entityHit.getName();
                if (!Myau.friendManager.isFriend(hitName)) {
                    Myau.friendManager.add(hitName);
                    ChatUtil.sendFormatted(String.format("%sAdded &o%s&r to your friend list&r", Myau.clientName, hitName));
                } else {
                    Myau.friendManager.remove(hitName);
                    ChatUtil.sendFormatted(String.format("%sRemoved &o%s&r from your friend list&r", Myau.clientName, hitName));
                }
            }
        }
    }
}
