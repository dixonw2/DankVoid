package p455w0rd.danknull.client.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class StackCountRenderer {

    public static void drawStackCount(final FontRenderer fontRenderer, final int count, final int x, final int y) {
        if (count <= 1) {
            return;
        }

        final String text = formatStackCount(count);
        final float scale = 0.5F;
        final boolean unicode = fontRenderer.getUnicodeFlag();
        fontRenderer.setUnicodeFlag(false);
        final float textX = (x + 15) / scale - fontRenderer.getStringWidth(text);
        final float textY = (y + 15) / scale - 7;

        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.scale(scale, scale, scale);
            fontRenderer.drawStringWithShadow(text, textX, textY, 0xFFFFFF);
        } finally {
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.enableLighting();
            fontRenderer.setUnicodeFlag(unicode);
        }
    }

    private static String formatStackCount(final int count) {
        if (count < 10000) {
            return Integer.toString(count);
        }

        int divisor = 1000;
        int unit = 0;
        while (count / divisor >= 1000) {
            divisor *= 1000;
            unit++;
        }

        // format 1,000,001 as 1.0M, 12,500,000 as 12.5M, Int.MAX as 2.14B, etc
        final int whole = count / divisor;
        final char suffix = "KMB".charAt(unit);

        if (whole >= 100) {
            return Integer.toString(whole) + suffix;
        }

        final int precision = whole < 10 ? 100 : 10;
        final int fraction = count % divisor / (divisor / precision);

        final String decimal;
        if (precision == 10 || fraction % 10 == 0) {
            decimal = Integer.toString(precision == 100 ? fraction / 10 : fraction);
        } else {
            decimal = fraction < 10 ? "0" + fraction : Integer.toString(fraction);
        }

        return whole + "." + decimal + suffix;
    }
}
