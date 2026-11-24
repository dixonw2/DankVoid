package p455w0rd.danknull.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.client.render.StackCountRenderer;
import p455w0rd.danknull.core.config.Config;
import p455w0rd.danknull.items.ItemDankNullReplica;

@Mod.EventBusSubscriber(modid = DankNull.MOD_ID, value = Side.CLIENT)
public final class DankNullReplicaToolTip {

    private static final int SLOTS_PER_ROW = 9;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 20;
    private static final int PREVIEW_WIDTH = (SLOTS_PER_ROW - 1) * SLOT_SPACING + SLOT_SIZE;
    private static final int TEXTURE_SLOT_X = 8;
    private static final int TEXTURE_SLOT_Y = 18;
    private static final int TOOLTIP_LINE_HEIGHT = 10;
    private static final int TOOLTIP_TITLE_GAP = 2;
    private static final int PREVIEW_TOP_PADDING = 2;
    private static final int PREVIEW_BOTTOM_PADDING = 6;
    private static final int ITEM_Z_LEVEL = 400;
    // Invisible formatting identifies the space reserved for the preview
    private static final String PREVIEW_MARKER = "\u00a7r\u00a7r";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreRenderToolTip(final RenderTooltipEvent.Pre event) {
        if (event.getLines().isEmpty() || findPreviewLine(event.getLines()) >= 0) {
            return;
        }
        final IItemHandler inventory = getPreviewInventory(event.getStack());
        if (inventory == null) {
            return;
        }
        int occupiedSlots = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                occupiedSlots++;
            }
        }
        if (occupiedSlots == 0) {
            return;
        }

        final int rows = (occupiedSlots + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;
        final int gridHeight = (rows - 1) * SLOT_SPACING + SLOT_SIZE;
        final int lineCount = (gridHeight + PREVIEW_TOP_PADDING + PREVIEW_BOTTOM_PADDING
                + TOOLTIP_LINE_HEIGHT - 1) / TOOLTIP_LINE_HEIGHT;
        final int spaceWidth = Math.max(1, event.getFontRenderer().getCharWidth(' '));
        final char[] padding = new char[(PREVIEW_WIDTH + spaceWidth - 1) / spaceWidth];
        Arrays.fill(padding, ' ');
        final List<String> reserved = new ArrayList<>(Collections.nCopies(lineCount, PREVIEW_MARKER));
        reserved.set(0, PREVIEW_MARKER + new String(padding));
        final List<String> lines = new ArrayList<>(event.getLines());
        lines.addAll(1, reserved);

        // Forge exposes read-only lines in Pre. Draw our copy through Forge so PostText still runs.
        event.setCanceled(true);
        GuiUtils.drawHoveringText(event.getStack(), lines, event.getX(), event.getY(),
                event.getScreenWidth(), event.getScreenHeight(), event.getMaxWidth(), event.getFontRenderer());
    }

    // Draw before tooltip scaling handlers restore their matrices in PostText.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderToolTip(final RenderTooltipEvent.PostText event) {
        final int firstLine = findPreviewLine(event.getLines());
        if (firstLine < 0) {
            return;
        }

        final IItemHandler inventory = getPreviewInventory(event.getStack());
        if (inventory == null) {
            return;
        }
        final List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                contents.add(stack);
            }
        }
        if (contents.isEmpty()) {
            return;
        }
        final float scale = Math.min(1.0F, event.getWidth() / (float) PREVIEW_WIDTH);
        final ResourceLocation texture = ((ItemDankNullReplica) event.getStack().getItem()).tier.getGuiBackground();
        final int previewY = event.getY() + firstLine * TOOLTIP_LINE_HEIGHT
                + TOOLTIP_TITLE_GAP + PREVIEW_TOP_PADDING;
        drawPreview(contents, event.getFontRenderer(), texture, event.getX(), previewY, scale);
    }

    private static int findPreviewLine(final List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (line.contains(PREVIEW_MARKER)
                    && TextFormatting.getTextWithoutFormattingCodes(line).trim().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static String formatHeader(final String text, final TextFormatting color) {
        return color + Config.ToolTips.Formatting.headerStyle + text;
    }

    public static String formatHint(final String text, final TextFormatting color) {
        return TextFormatting.RESET + "  " + formatHintText(text, color);
    }

    public static String formatHintText(final String text, final TextFormatting color) {
        return color + Config.ToolTips.Formatting.hintStyle + text;
    }

    public static String formatHintValue(final String text, final TextFormatting color,
            final TextFormatting prevColor) {
        return color + Config.ToolTips.Formatting.valueStyle + text + prevColor + Config.ToolTips.Formatting.hintStyle;
    }

    public static String formatCount(final int count, final TextFormatting tierColor) {
        final TextFormatting configuredHintColor = Config.ToolTips.Formatting.Count.hintColor;
        final TextFormatting configuredValueColor = Config.ToolTips.Formatting.Count.valueColor;
        final TextFormatting hintColor = configuredHintColor == null ? tierColor : configuredHintColor;
        final TextFormatting valueColor = configuredValueColor == null ? tierColor : configuredValueColor;
        final String hintFormat = hintColor + Config.ToolTips.Formatting.Count.hintStyle;
        final String value = valueColor + Config.ToolTips.Formatting.Count.valueStyle + count + hintFormat;
        return hintFormat + I18n.format("tooltip.dankVoid.count", value);
    }

    public static boolean displayToolTip(String tip) {
        return !Config.ToolTips.Visibility.HIDE.equalsIgnoreCase(tip)
                && (Config.ToolTips.Visibility.ALWAYS_DISPLAY.equalsIgnoreCase(tip)
                        || Config.ToolTips.Visibility.Misc.alwaysDisplayToolTips || GuiScreen.isShiftKeyDown());
    }

    private static void drawPreview(final List<ItemStack> contents, final FontRenderer fontRenderer,
            final ResourceLocation texture, final int x, final int y, final float scale) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        final RenderItem itemRenderer = minecraft.getRenderItem();
        final float previousZ = itemRenderer.zLevel;
        final int rows = (contents.size() + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 1.0F);
            minecraft.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            for (int slot = 0; slot < rows * SLOTS_PER_ROW; slot++) {
                Gui.drawModalRectWithCustomSizedTexture(
                        slot % SLOTS_PER_ROW * SLOT_SPACING, slot / SLOTS_PER_ROW * SLOT_SPACING,
                        TEXTURE_SLOT_X, TEXTURE_SLOT_Y, SLOT_SIZE, SLOT_SIZE, 256, 256);
            }
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            itemRenderer.zLevel = ITEM_Z_LEVEL;
            for (int slot = 0; slot < contents.size(); slot++) {
                final ItemStack stack = contents.get(slot);
                final int slotX = slot % SLOTS_PER_ROW * SLOT_SPACING + 1;
                final int slotY = slot / SLOTS_PER_ROW * SLOT_SPACING + 1;
                itemRenderer.renderItemAndEffectIntoGUI(stack, slotX, slotY);
                itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, slotX, slotY, "");
                StackCountRenderer.drawStackCount(fontRenderer, stack.getCount(), slotX, slotY);
            }
        } finally {
            itemRenderer.zLevel = previousZ;
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Nullable
    private static IItemHandler getPreviewInventory(final ItemStack dank) {
        final boolean showPreview = displayToolTip(Config.ToolTips.Visibility.Misc.preview);
        if (!showPreview
                || !(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)
                || !(dank.getItem() instanceof ItemDankNullReplica)) {
            return null;
        }

        return dank.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }
}
