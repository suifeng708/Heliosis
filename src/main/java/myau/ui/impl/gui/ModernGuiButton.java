package myau.ui.impl.gui;

import myau.module.modules.render.ClickGUIModule;
import myau.util.AnimationUtil;
import myau.util.RenderUtil;
import myau.util.font.CenterMode;
import myau.util.font.FontManager;
import myau.util.font.impl.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class ModernGuiButton extends GuiButton {
    private static final float RADIUS = 5.0F;

    private float hoverProgress;

    public ModernGuiButton(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, width, height, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        hoverProgress = AnimationUtil.animate(this.hovered && this.enabled ? 1.0f : 0.0f, hoverProgress, 0.25f, 1.0f);

        int bgNormal = new Color(17, 21, 30, this.enabled ? 210 : 140).getRGB();
        int bgHover = new Color(32, 44, 60, 240).getRGB();
        int borderNormal = new Color(255, 255, 255, this.enabled ? 42 : 22).getRGB();
        int borderHover = new Color(96, 200, 255, 230).getRGB();
        int textNormal = new Color(228, 233, 240, this.enabled ? 255 : 120).getRGB();

        int finalBg = AnimationUtil.interpolateColor(bgNormal, bgHover, hoverProgress);
        int finalBorder = AnimationUtil.interpolateColor(borderNormal, borderHover, hoverProgress);
        int finalText = AnimationUtil.interpolateColor(textNormal, -1, hoverProgress);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        boolean fillet = ClickGUIModule.isFilletEnabled();
        float radius = fillet ? RADIUS : 0.0F;

        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition + 1.0F, this.width, this.height,
                radius, new Color(0, 0, 0, 70).getRGB(), fillet, fillet, fillet, fillet);

        int glowAlpha = (int) (55.0F * hoverProgress);
        if (glowAlpha > 3) {
            RenderUtil.drawRoundedRect(this.xPosition - 1.5F, this.yPosition - 1.5F,
                    this.width + 3.0F, this.height + 3.0F, radius + 1.5F,
                    new Color(96, 200, 255, glowAlpha).getRGB(), fillet, fillet, fillet, fillet);
        }

        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height,
                radius, finalBg, fillet, fillet, fillet, fillet);

        RenderUtil.drawRoundedRect(this.xPosition + 3.0F, this.yPosition + 1.0F, this.width - 6.0F, 1.0F,
                0.5F, new Color(255, 255, 255, 20).getRGB(), fillet, fillet, fillet, fillet);

        RenderUtil.drawRoundedRectOutline(this.xPosition + 0.5F, this.yPosition + 0.5F,
                this.width - 1.0F, this.height - 1.0F, fillet ? RADIUS - 0.5F : 0.0F, 1.0F,
                finalBorder, fillet, fillet, fillet, fillet);

        float highlightWidth = Math.max(0.0F, (this.width - 16.0F) * hoverProgress);
        if (highlightWidth > 0.0F) {
            RenderUtil.drawRoundedRect(this.xPosition + (this.width - highlightWidth) / 2.0F,
                    this.yPosition + this.height - 2.0F, highlightWidth, 1.0F, 0.5F,
                    new Color(96, 200, 255, 235).getRGB(), fillet, fillet, fillet, fillet);
        }

        FontRenderer fr = FontManager.harmonyOS_Sans20 != null
                ? FontManager.harmonyOS_Sans20 : FontManager.productSans20;
        if (fr != null) {
            // Draw at 1:1 scale — the atlas is already baked for the current GUI
            // scale, and any extra GL scaling resamples it into a blurry mess.
            fr.drawString(this.displayString,
                    this.xPosition + this.width / 2.0,
                    this.yPosition + this.height / 2.0 + 1.0,
                    CenterMode.XY, false, finalText);
        } else {
            this.drawCenteredString(mc.fontRendererObj, this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    finalText);
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mouseDragged(mc, mouseX, mouseY);
    }
}
