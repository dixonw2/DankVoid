package p455w0rd.danknull.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.api.DankNullItemModes.ItemPlacementMode;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.init.ModConfig;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rd.danknull.init.ModGlobals;
import p455w0rd.danknull.init.ModItems;
import p455w0rd.danknull.client.gui.GuiHudEditor;
import net.minecraft.init.Blocks;
import p455w0rd.danknull.init.ModGlobals.DankNullTier;
import p455w0rd.danknull.init.ModKeyBindings;
import p455w0rd.danknull.inventory.DankNullHandler;
import p455w0rd.danknull.inventory.cap.CapabilityDankNull;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rdslib.util.GuiUtils;
import p455w0rdslib.util.RenderUtils;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/**
 * @author p455w0rd
 */
public class HUDRenderer {

    private static final ResourceLocation LEGACY_TEXTURE = new ResourceLocation(ModGlobals.MODID,
            "textures/gui/danknullscreen0.png");

    private static void showLegacyHud(final Minecraft mc, final ScaledResolution scaledRes,
            final ItemStack currentItem) {
        if (!currentItem.isEmpty() && ItemDankNull.isDankNull(currentItem)) {
            final IDankNullHandler dankNullHandler = currentItem.getCapability(
                    CapabilityDankNull.DANK_NULL_CAPABILITY,
                    null);
            if (dankNullHandler.getSelected() < 0) {
                return;
            }
            final ItemStack selectedStack = dankNullHandler
                    .getFullStackInSlot(dankNullHandler.getSelected());
            final TextureManager tm = mc.renderEngine;
            if (tm != null && !selectedStack.isEmpty()) {

                tm.bindTexture(LEGACY_TEXTURE);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GuiUtils.drawTexturedModalRect(scaledRes.getScaledWidth() - 106,
                        scaledRes.getScaledHeight() - 45, 0,
                        210, 106, 45, 0);
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                drawLegacyText(mc, scaledRes, currentItem.getDisplayName(),
                        scaledRes.getScaledWidth() * 2 - 212 + 55,
                        scaledRes.getScaledHeight() * 2 - 83,
                        dankNullHandler.getTier().getHexColor(true));
                String selectedStackName = selectedStack.getDisplayName();
                final int itemNameWidth = mc.fontRenderer.getStringWidth(selectedStackName);
                if (itemNameWidth >= 88 && selectedStackName.length() >= 14) {
                    selectedStackName = selectedStackName.substring(0, 14).trim() + "...";
                }
                final ItemPlacementMode placementMode = dankNullHandler
                        .getPlacementMode(selectedStack);
                drawLegacyText(mc, scaledRes,
                        I18n.translateToLocal("dn.selected_item.desc") + ": "
                                + selectedStackName,
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 72, 16777215);
                drawLegacyText(mc, scaledRes,
                        I18n.translateToLocal("dn.count.desc") + ": "
                                + (ItemDankNull.getTier(
                                        currentItem) == DankNullTier.CREATIVE
                                                ? "Infinite"
                                                : selectedStack.getCount()),
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 61, 16777215);
                drawLegacyText(mc, scaledRes,
                        I18n.translateToLocal("dn.place.desc") + ": "
                                + placementMode.getTooltip()
                                        .replace(I18n.translateToLocal(
                                                "dn.extract.desc")
                                                .toLowerCase(Locale.ENGLISH),
                                                I18n.translateToLocal(
                                                        "dn.place.desc")
                                                        .toLowerCase(Locale.ENGLISH))
                                        .replace(I18n.translateToLocal(
                                                "dn.extract.desc"),
                                                I18n.translateToLocal(
                                                        "dn.place.desc")),
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 50, 16777215);
                drawLegacyText(mc, scaledRes,
                        I18n.translateToLocal("dn.extract.desc") + ": "
                                + dankNullHandler
                                        .getExtractionMode(
                                                selectedStack)
                                        .getTooltip(),
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 40, 16777215);

                final String keyBind = ModKeyBindings.getOpenDankNullKeyBind().getDisplayName();
                drawLegacyText(mc, scaledRes,
                        keyBind.equalsIgnoreCase("none")
                                ? I18n.translateToLocal(
                                        "dn.no_open_keybind.desc")
                                : I18n.translateToLocal("dn.open_with.desc")
                                        + " " + keyBind,
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 29, 16777215);
                String oreDictMode = I18n.translateToLocal("dn.ore_dictionary.desc") + ": "
                        + (dankNullHandler.isOre(selectedStack)
                                ? I18n.translateToLocal("dn.enabled.desc")
                                : I18n.translateToLocal("dn.disabled.desc"));
                final boolean isOreDicted = DankNullHandler.getOreNames(selectedStack)
                        .size() > 0;
                if (!isOreDicted) {
                    oreDictMode = I18n.translateToLocal("dn.not_oredicted.desc");
                }

                drawLegacyText(mc, scaledRes, oreDictMode,
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 18, 16777215);

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtils.getRenderItem().renderItemAndEffectIntoGUI(currentItem,
                        scaledRes.getScaledWidth() - 106 + 5,
                        scaledRes.getScaledHeight() - 20);
                GlStateManager.popMatrix();
            }
        }
    }

    private static final ResourceLocation HOTBAR_TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/widgets.png");

    private static void showHud(final Minecraft mc, final ScaledResolution scaledRes,
            final Preview frame, final int anchorX, final int anchorY) {
        final IDankNullHandler handler = frame.handler;
        final ItemStack[] preview = frame.items;
        if (preview.length == 0) {
            return;
        }
        final boolean singleSlot = preview.length == 1;

        // Keep the selected slot in the same position when the neighbors disappear.
        final int x = scaledRes.getScaledWidth() - 70 + (singleSlot ? 20 : 0);
        final int middleWidth = (preview.length - 1) * 20;
        final int selectedOffset = (preview.length / 2) * 20;
        final int y = scaledRes.getScaledHeight() - 22;
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(HOTBAR_TEXTURE);

        // Join the vanilla hotbar edges around only the visible slots.
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 11, 22, 256, 256);
        if (middleWidth > 0) {
            Gui.drawModalRectWithCustomSizedTexture(x + 11, y, 11, 0, middleWidth, 22, 256, 256);
        }
        Gui.drawModalRectWithCustomSizedTexture(x + 11 + middleWidth, y, 171, 0, 11, 22, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x + selectedOffset - 1, y - 1, 0, 22, 24, 22, 256, 256);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        for (int i = 0; i < preview.length; i++) {
            final int itemX = x + i * 20 + 3;
            mc.getRenderItem().renderItemAndEffectIntoGUI(preview[i], itemX, y + 3);
            // Keep durability/cooldown overlays, but draw the abbreviated count separately.
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, preview[i], itemX, y + 3, "");
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        try {
            final ItemStack selected = preview[preview.length / 2];
            final String name = selected.getDisplayName();
            final int nameWidth = mc.fontRenderer.getStringWidth(name);
            // Preserve the entire name, even if it is wider than the viewport.
            final float nameScale = Math.min(1F,
                    Math.max(1, scaledRes.getScaledWidth() - 4) / (float) Math.max(1, nameWidth));
            final float renderedWidth = nameWidth * nameScale;
            final float nameX = Math.max(2F, Math.min(anchorX - renderedWidth / 2F,
                    scaledRes.getScaledWidth() - renderedWidth - 2F));
            final float aboveY = anchorY - mc.fontRenderer.FONT_HEIGHT * nameScale - 4F;
            final float nameY = aboveY >= 2F ? aboveY : anchorY + 26F;

            // The HUD matrix is already translated; convert screen coordinates back to its
            // drawing space.
            final int centerX = x + selectedOffset + 11;
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(nameX - anchorX + centerX, nameY - anchorY + y, 0F);
                GlStateManager.scale(nameScale, nameScale, 1F);
                mc.fontRenderer.drawStringWithShadow(name, 0F, 0F, 0xFFFFFF);
            } finally {
                GlStateManager.popMatrix();
            }

            for (int i = 0; i < preview.length; i++) {
                final String quantity = handler.getTier() == DankNullTier.CREATIVE
                        ? "\u221e"
                        : formatHudQuantity(preview[i].getCount());
                final int width = mc.fontRenderer.getStringWidth(quantity);
                final float scale = Math.min(0.75F, 18.0F / Math.max(1, width));
                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(x + i * 20 + 20 - width * scale,
                            y + 20 - mc.fontRenderer.FONT_HEIGHT * scale, 0F);
                    GlStateManager.scale(scale, scale, 1F);
                    mc.fontRenderer.drawStringWithShadow(quantity, 0F, 0F, 0xFFFFFF);
                } finally {
                    GlStateManager.popMatrix();
                }
            }
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }

    private static List<Integer> getCycleSlots(final IDankNullHandler handler) {
        final List<Integer> occupied = new ArrayList<>();
        final List<Integer> blocks = Options.skipNonBlocksOnCycle ? new ArrayList<>() : null;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            final ItemStack stack = handler.getFullStackInSlot(slot);
            if (!stack.isEmpty()) {
                occupied.add(slot);
                if (blocks != null && stack.getItem() instanceof ItemBlock) {
                    blocks.add(slot);
                }
            }
        }
        return blocks != null && !blocks.isEmpty() ? blocks : occupied;
    }

    private static String formatHudQuantity(final int count) {
        if (count <= 999) {
            return Integer.toString(count);
        }
        final long divisor = count >= 1_000_000_000 ? 1_000_000_000L
                : count >= 1_000_000 ? 1_000_000L : 1_000L;
        final String suffix = divisor == 1_000_000_000L ? "B" : divisor == 1_000_000L ? "M" : "K";
        // Truncate to avoid reporting a million before the stored count reaches it.
        final long tenths = count * 10L / divisor;
        final long whole = tenths / 10;
        if (whole < 100 && tenths % 10 != 0) {
            return whole + "." + tenths % 10 + suffix;
        }
        return whole + (divisor == 1_000L && whole < 10 ? ".0" : "") + suffix;
    }

    private static ItemStack editorSample;

    private static boolean hasSelection(final ItemStack stack) {
        if (stack.isEmpty() || !ItemDankNull.isDankNull(stack)) {
            return false;
        }
        final IDankNullHandler handler = stack.getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
        return handler != null && handler.getSelected() >= 0 && handler.getSelected() < handler.getSlots()
                && !handler.getFullStackInSlot(handler.getSelected()).isEmpty();
    }

    private static ItemStack displayDank(final Minecraft mc, final boolean preview) {
        if (mc.player != null) {
            ItemStack held = mc.player.getHeldItemMainhand();
            if (held.isEmpty() || !ItemDankNull.isDankNull(held)) {
                held = mc.player.getHeldItemOffhand();
            }
            if (hasSelection(held)) {
                return held;
            }
        }
        if (!preview) {
            return ItemStack.EMPTY;
        }
        if (editorSample == null) {
            editorSample = new ItemStack(ModItems.EMERALD_DANKNULL);
            final IDankNullHandler handler = editorSample
                    .getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
            handler.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 64_000));
            handler.setStackInSlot(1, new ItemStack(Blocks.STONE, 1_250_000));
            handler.setStackInSlot(2, new ItemStack(Blocks.DIRT, 999));
            handler.setSelected(1);
        }
        return editorSample;
    }

    private static ItemStack[] getPreviewItems(final IDankNullHandler handler) {
        final List<Integer> slots = getCycleSlots(handler);
        if (slots.isEmpty()) {
            return new ItemStack[0];
        }
        final int current = slots.indexOf(handler.getSelected());
        if (slots.size() == 1 && current == 0) {
            return new ItemStack[] { handler.getFullStackInSlot(handler.getSelected()) };
        }
        final int previous = current < 0 ? 0 : Math.floorMod(current - 1, slots.size());
        final int next = current < 0 ? 0 : (current + 1) % slots.size();
        return new ItemStack[] {
                handler.getFullStackInSlot(slots.get(previous)),
                handler.getFullStackInSlot(handler.getSelected()),
                handler.getFullStackInSlot(slots.get(next))
        };
    }

    private static void drawLegacyText(final Minecraft mc, final ScaledResolution resolution,
            final String text, final float x, final float y, final int color) {
        final int available = Math.max(0, (int) (resolution.getScaledWidth() * 2 - x - 4));
        final String fitted = mc.fontRenderer.getStringWidth(text) <= available ? text
                : mc.fontRenderer.trimStringToWidth(text,
                        Math.max(0, available - mc.fontRenderer.getStringWidth("..."))) + "...";
        mc.fontRenderer.drawStringWithShadow(fitted, x, y, color);
    }

    /** One inventory snapshot shared by bounds calculation and rendering. */
    public static final class Preview {
        private final boolean legacy;
        private final ItemStack dank;
        private final IDankNullHandler handler;
        private final ItemStack[] items;
        public final HudLayout.Bounds bounds;

        private Preview(final boolean legacy, final ItemStack dank) {
            this.legacy = legacy;
            this.dank = dank;
            handler = dank.getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
            items = legacy ? null : getPreviewItems(handler);
            bounds = boundsFor(legacy, legacy ? 0 : items.length);
        }
    }

    private static HudLayout.Bounds boundsFor(final boolean legacy, final int itemCount) {
        if (legacy) {
            return new HudLayout.Bounds(0, 3, 106, 45);
        }
        final int selectedOffset = (itemCount / 2) * 20;
        final int barLeft = -11 - selectedOffset;
        final int barRight = barLeft + (itemCount - 1) * 20 + 22;
        // Only the slots constrain dragging. The label changes position independently.
        return new HudLayout.Bounds(Math.min(barLeft, -12), -1, Math.max(barRight, 12), 22);
    }

    public static Preview preparePreview(final Minecraft mc, final boolean legacy) {
        return new Preview(legacy, displayDank(mc, true));
    }

    private static void drawAt(final Minecraft mc, final ScaledResolution resolution,
            final Preview frame, final int x, final int y) {
        final boolean legacy = frame.legacy;
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x - (resolution.getScaledWidth() - (legacy ? 106 : 39)),
                    y - (resolution.getScaledHeight() - (legacy ? 45 : 22)), 0F);
            if (legacy) {
                showLegacyHud(mc, resolution, frame.dank);
            } else {
                showHud(mc, resolution, frame, x, y);
            }
        } finally {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableDepth();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.popMatrix();
        }
    }

    public static void renderPreview(final Minecraft mc, final ScaledResolution resolution,
            final Preview frame, final int x, final int y) {
        drawAt(mc, resolution, frame, x, y);
    }

    @SideOnly(Side.CLIENT)
    public static void renderHUD(final Minecraft mc, final ScaledResolution resolution) {
        if (mc.currentScreen instanceof GuiHudEditor || !Options.showHud || mc.player == null
                || (!mc.playerController.shouldDrawHUD() && !mc.player.capabilities.isCreativeMode)) {
            return;
        }
        final ItemStack dank = displayDank(mc, false);
        if (dank.isEmpty()) {
            return;
        }
        final boolean legacy = Options.showLegacyHud;
        final Preview frame = new Preview(legacy, dank);
        final HudLayout.Position anchor = HudLayout.resolve(HudLayout.load(legacy),
                resolution.getScaledWidth(), resolution.getScaledHeight(), frame.bounds);
        drawAt(mc, resolution, frame, anchor.x, anchor.y);
    }

    public static void toggleHUD() {
        Options.showHud = !Options.showHud;
        ModConfig.getInstance().get(Configuration.CATEGORY_CLIENT, ModConfig.NAME_SHOW_HUD, true)
                .setValue(Options.showHud);
        ModConfig.getInstance().save();
    }

}
