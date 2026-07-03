package myau.module.modules.misc;

import myau.module.Module;
import myau.module.ModuleInfo;
import myau.module.Category;

@ModuleInfo(name = "AntiObfuscate", enabled = "false", hidden = "true", description = "", category = Category.MISC)
public class AntiObfuscate extends Module {
    public String stripObfuscated(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("§k", "");
    }
}
