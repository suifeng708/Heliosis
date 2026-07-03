package myau.module;

import myau.Myau;
import myau.module.modules.render.HUD;
import myau.util.KeyBindUtil;

public abstract class Module {
    protected final String name;
    protected final String description;
    protected final Category category;
    protected final boolean defaultEnabled;
    protected final int defaultKey;
    protected final boolean defaultHidden;
    protected boolean enabled;
    protected int key;
    protected boolean hidden;

    public Module() {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        if (info == null) {
            throw new RuntimeException("Module " + getClass().getSimpleName() + " must have @ModuleInfo annotation or call super(...)");
        }
        this.name = info.name();
        this.description = info.description();
        this.category = info.category();
        this.enabled = this.defaultEnabled = Boolean.parseBoolean(info.enabled());
        this.hidden = this.defaultHidden = Boolean.parseBoolean(info.hidden());
        this.key = this.defaultKey = 0;
    }

    public Module(String name, boolean enabled, Category category) {
        this(name, enabled, false, "", category);
    }

    public Module(String name, boolean enabled, boolean hidden, Category category) {
        this(name, enabled, hidden, "", category);
    }

    public Module(String name, boolean enabled, boolean hidden, String description, Category category) {
        this.name = name;
        this.description = description;
        this.enabled = this.defaultEnabled = enabled;
        this.category = category;
        this.key = this.defaultKey = 0;
        this.hidden = this.defaultHidden = hidden;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public String formatModule() {
        return String.format(
                "%s%s &r(%s&r)",
                this.key == 0 ? "" : String.format("&l[%s] &r", KeyBindUtil.getKeyName(this.key)),
                this.name,
                this.enabled ? "&a&lON" : "&c&lOFF"
        );
    }

    public String[] getSuffix() {
        return new String[0];
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                this.onEnabled();
            } else {
                this.onDisabled();
            }
        }
    }

    public boolean toggle() {
        boolean enabled = !this.enabled;
        this.setEnabled(enabled);
        if (this.enabled == enabled) {
            if (((HUD) Myau.moduleManager.modules.get(HUD.class)).toggleSound.getValue()) {
                Myau.moduleManager.playSound();
            }

            // Add a transient in-game notification for toggles
            try {
                if (Myau.notificationManager != null) {
                    String action = this.enabled ? "was toggled successfully" : "was untoggled successfully";
                    // green for enabled, red for disabled
                    int color = this.enabled ? 0x00FF00 : 0xFF0000;
                    Myau.notificationManager.add(this.getName() + " " + action, color);
                }
            } catch (Exception ignored) {
            }

            return true;
        } else {
            return false;
        }
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int integer) {
        this.key = integer;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean boolean1) {
        this.hidden = boolean1;
    }

    public void onEnabled() {
    }

    public void onDisabled() {
    }

    public void verifyValue(String string) {
    }

    public boolean shouldKeepSprint() {
        return false;
    }
}
