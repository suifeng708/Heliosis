package myau.util.shader.impl;

import myau.util.AnimationUtil;
import myau.util.RenderUtil;
import myau.util.font.CenterMode;
import myau.util.font.FontManager;
import myau.util.font.impl.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class GuiButton extends net.minecraft.client.gui.GuiButton
{
    private static final float RADIUS = 5.0F;

    private float hoverProgress;

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (!this.visible) return;

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        hoverProgress = AnimationUtil.animate(this.hovered && this.enabled ? 1.0F : 0.0F,
                hoverProgress, 0.18F, 1.0F);

        int backgroundNormal = new Color(17, 21, 30, this.enabled ? 210 : 140).getRGB();
        int backgroundHover = new Color(32, 44, 60, 240).getRGB();
        int borderNormal = new Color(255, 255, 255, this.enabled ? 42 : 22).getRGB();
        int borderHover = new Color(96, 200, 255, 230).getRGB();
        int textNormal = new Color(228, 233, 240, this.enabled ? 255 : 120).getRGB();

        int backgroundColor = AnimationUtil.interpolateColor(backgroundNormal, backgroundHover, hoverProgress);
        int borderColor = AnimationUtil.interpolateColor(borderNormal, borderHover, hoverProgress);
        int textColor = AnimationUtil.interpolateColor(textNormal, Color.WHITE.getRGB(), hoverProgress);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Soft drop shadow for depth
        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition + 1.0F, this.width, this.height,
                RADIUS, new Color(0, 0, 0, 70).getRGB(), true, true, true, true);

        // Accent glow that fades in on hover
        int glowAlpha = (int) (55.0F * hoverProgress);
        if (glowAlpha > 3) {
            RenderUtil.drawRoundedRect(this.xPosition - 1.5F, this.yPosition - 1.5F,
                    this.width + 3.0F, this.height + 3.0F, RADIUS + 1.5F,
                    new Color(96, 200, 255, glowAlpha).getRGB(), true, true, true, true);
        }

        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height,
                RADIUS, backgroundColor, true, true, true, true);

        // Thin inner top highlight for depth
        RenderUtil.drawRoundedRect(this.xPosition + 3.0F, this.yPosition + 1.0F, this.width - 6.0F, 1.0F,
                0.5F, new Color(255, 255, 255, 20).getRGB(), true, true, true, true);

        RenderUtil.drawRoundedRectOutline(this.xPosition + 0.5F, this.yPosition + 0.5F,
                this.width - 1.0F, this.height - 1.0F, RADIUS - 0.5F, 1.0F,
                borderColor, true, true, true, true);

        float highlightWidth = Math.max(0.0F, (this.width - 16.0F) * hoverProgress);
        if (highlightWidth > 0.0F) {
            RenderUtil.drawRoundedRect(this.xPosition + (this.width - highlightWidth) / 2.0F,
                    this.yPosition + this.height - 2.0F, highlightWidth, 1.0F, 0.5F,
                    new Color(96, 200, 255, 235).getRGB(), true, true, true, true);
        }

        FontRenderer fontRenderer = FontManager.harmonyOS_Sans20 != null
                ? FontManager.harmonyOS_Sans20 : FontManager.productSans20;
        if (fontRenderer != null) {
            // Draw at 1:1 scale — the atlas is already baked for the current GUI
            // scale, and any extra GL scaling resamples it into a blurry mess.
            fontRenderer.drawString(this.displayString,
                    this.xPosition + this.width / 2.0,
                    this.yPosition + this.height / 2.0 + 1.0,
                    CenterMode.XY, false, textColor);
        } else {
            this.drawCenteredString(mc.fontRendererObj, this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    textColor);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mouseDragged(mc, mouseX, mouseY);
    }
}
