package myau.ui.impl.gui;

import myau.config.MenuConfig;
import myau.module.modules.render.ClickGUIModule;
import myau.util.RenderUtil;
import myau.util.font.FontManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.Color;
import java.io.IOException;

public class GuiBackgroundSelector extends GuiScreen {
    private final GuiScreen parent;
    private final long previewStartTime = System.currentTimeMillis();

    public GuiBackgroundSelector(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        BackgroundRenderer.init();
        this.buttonList.clear();

        int columns = 3;
        int buttonWidth = 94;
        int buttonHeight = 42;
        int gap = 10;
        int gridWidth = columns * buttonWidth + (columns - 1) * gap;
        int startX = this.width / 2 - gridWidth / 2;
        int startY = this.height / 2 - 54;

        for (int index = 0; index <= 5; index++) {
            int row = index / columns;
            int column = index % columns;
            int x = startX + column * (buttonWidth + gap);
            int y = startY + row * (buttonHeight + gap);
            this.buttonList.add(new ModernGuiButton(index, x, y, buttonWidth, buttonHeight, BackgroundRenderer.getBackgroundName(index)));
        }

        this.buttonList.add(new ModernGuiButton(100, this.width / 2 - 50, startY + 2 * (buttonHeight + gap) + 8, 100, 20, "Done"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (BackgroundRenderer.currentBackgroundIndex == BackgroundRenderer.BACKGROUND_CLASSIC) {
            BackgroundRenderer.drawClassic(this.previewStartTime);
        } else {
            BackgroundRenderer.draw(this.width, this.height);
        }

        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 120).getRGB());

        int panelWidth = 330;
        int panelHeight = 210;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        boolean fillet = ClickGUIModule.isFilletEnabled();
        int panelColor = new Color(18, 18, 22, 255).getRGB();
        if (fillet) {
            RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 12.0f, panelColor, true, true, true, true);
        } else {
            drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, panelColor);
        }

        String selectedName = BackgroundRenderer.getBackgroundName(BackgroundRenderer.currentBackgroundIndex);
        if (FontManager.productSans20 != null && FontManager.productSans16 != null) {
            FontManager.productSans20.drawCenteredString("Background", this.width / 2.0f, panelY + 15, -1);
            FontManager.productSans16.drawCenteredString("Current: " + selectedName, this.width / 2.0f, panelY + 32, new Color(210, 210, 210).getRGB());
        } else {
            this.drawCenteredString(this.fontRendererObj, "Background", this.width / 2, panelY + 15, -1);
            this.drawCenteredString(this.fontRendererObj, "Current: " + selectedName, this.width / 2, panelY + 32, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (GuiButton button : this.buttonList) {
            if (button.id == BackgroundRenderer.currentBackgroundIndex) {
                int color = new Color(100, 180, 255, 180).getRGB();
                int left = button.xPosition - 2;
                int top = button.yPosition - 2;
                int right = button.xPosition + button.width + 2;
                int bottom = button.yPosition + button.height + 2;
                if (fillet) {
                    RenderUtil.drawRoundedRectOutline(left, top, right - left, bottom - top, 6.0f, 1.5f, color, true, true, true, true);
                } else {
                    drawRect(left, top, right, top + 1, color);
                    drawRect(left, bottom - 1, right, bottom, color);
                    drawRect(left, top, left + 1, bottom, color);
                    drawRect(right - 1, top, right, bottom, color);
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            close();
        } else if (button.id >= 0 && button.id <= 5) {
            BackgroundRenderer.reloadShader(button.id);
            MenuConfig.save();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            close();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void close() {
        MenuConfig.save();
        this.mc.displayGuiScreen(this.parent);
    }
}
