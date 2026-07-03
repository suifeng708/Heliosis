package myau.module.modules.misc;

import myau.enums.ChatColors;
import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.BooleanProperty;
import myau.property.properties.TextProperty;
import net.minecraft.client.Minecraft;

import java.util.regex.Matcher;

@ModuleInfo(name = "NickHider", enabled = "false", hidden = "true", description = "", category = Category.MISC)
public class NickHider extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final TextProperty protectName = new TextProperty("name", "You");
    public final BooleanProperty scoreboard = new BooleanProperty("scoreboard", true);
    public final BooleanProperty level = new BooleanProperty("level", true);
    public String replaceNick(String input) {
        if (input != null && mc.thePlayer != null) {
            if (this.scoreboard.getValue() && input.matches("§7\\d{2}/\\d{2}/\\d{2}(?:\\d{2})?  ?§8.*")) {
                input = input.replaceAll("§8", "§8§k").replaceAll("[^\\x00-\\x7F§]", "?");
            }
            return input.replaceAll(
                    mc.thePlayer.getName(), Matcher.quoteReplacement(ChatColors.formatColor(this.protectName.getValue()))
            );
        } else {
            return input;
        }
    }
}
