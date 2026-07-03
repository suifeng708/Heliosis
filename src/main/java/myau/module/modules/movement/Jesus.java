package myau.module.modules.movement;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@ModuleInfo(name = "Jesus", enabled = "false", hidden = "false", description = "", category = Category.MOVEMENT)
public class Jesus extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
    public final FloatProperty speed = new FloatProperty("speed", 2.5F, 0.0F, 3.0F);
    public final BooleanProperty noPush = new BooleanProperty("no-push", true);
    public final BooleanProperty groundOnly = new BooleanProperty("ground-only", true);
    @Override
    public String[] getSuffix() {
        return new String[]{df.format(this.speed.getValue())};
    }
}
