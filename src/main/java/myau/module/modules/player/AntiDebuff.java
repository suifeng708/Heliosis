package myau.module.modules.player;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.BooleanProperty;

@ModuleInfo(name = "AntiDebuff", enabled = "false", hidden = "false", description = "", category = Category.PLAYER)
public class AntiDebuff extends Module {
    public final BooleanProperty blindness = new BooleanProperty("blindness", true);
    public final BooleanProperty nausea = new BooleanProperty("nausea", true);
}
