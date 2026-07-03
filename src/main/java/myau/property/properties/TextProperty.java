package myau.property.properties;

import com.google.gson.JsonObject;
import myau.Myau;
import myau.property.Property;

import java.util.function.BooleanSupplier;

public class TextProperty extends Property<String> {
    public TextProperty(String name, String value) {
        this(name, value, null);
    }

    public TextProperty(String name, String value, BooleanSupplier booleanSupplier) {
        super(name, value, booleanSupplier);
    }

    @Override
    public String getValuePrompt() {
        return "text";
    }

    @Override
    public String formatValue() {
        return String.format("&f%s", this.getValue());
    }

    @Override
    public boolean parseString(String string) {
        return this.setValue(migrateBrandText(string));
    }

    private String migrateBrandText(String string) {
        if (string == null) {
            return null;
        }
        return string
                .replace("OpenMyau+", Myau.DISPLAY_NAME)
                .replace("OpenMyau", Myau.DISPLAY_NAME)
                .replace("PenMyau+", Myau.DISPLAY_NAME)
                .replace("Myau+", Myau.DISPLAY_NAME);
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        return this.parseString(jsonObject.get(this.getName()).getAsString());
    }

    @Override
    public void write(JsonObject jsonObject) {
        jsonObject.addProperty(this.getName(), this.getValue());
    }
}
