package myau.module.modules.player;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.util.ItemUtil;
import myau.util.TeamUtil;
import myau.property.properties.BooleanProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "GhostHand", enabled = "false", hidden = "false", description = "", category = Category.PLAYER)
public class GhostHand extends Module {
    public final BooleanProperty teamsOnly = new BooleanProperty("team-only", true);
    public final BooleanProperty ignoreWeapons = new BooleanProperty("ignore-weapons", false);
    public boolean shouldSkip(Entity entity) {
        return entity instanceof EntityPlayer
                && !TeamUtil.isBot((EntityPlayer) entity)
                && (!this.teamsOnly.getValue() || TeamUtil.isSameTeam((EntityPlayer) entity))
                && (!this.ignoreWeapons.getValue() || !ItemUtil.hasRawUnbreakingEnchant());
    }
}
