package myau.module.modules.player;

import myau.event.EventTarget;
import myau.events.PacketEvent;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.util.ChatUtil;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "FlagDetector", enabled = "false", hidden = "true", description = "Detects server flags like lagback and teleportation", category = Category.PLAYER)
public class FlagDetector extends Module {
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled())
            return;

        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ChatUtil.sendFormatted("&7[&cFlagDetector&7] &fServer flag detected (Lagback)!");
        }
    }
}
