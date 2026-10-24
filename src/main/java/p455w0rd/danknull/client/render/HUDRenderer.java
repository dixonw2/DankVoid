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

    private static void showLegacyHud(final Minecraft mc, final ScaledResolution scaledRes) {
        ItemStack currentItem = mc.player.inventory.getCurrentItem();
        if (currentItem.isEmpty() || !ItemDankNull.isDankNull(currentItem)) {
            currentItem = mc.player.getHeldItemOffhand();
        }
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

                tm.bindTexture(new ResourceLocation(ModGlobals.MODID,
                        "textures/gui/danknullscreen0.png"));
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GuiUtils.drawTexturedModalRect(scaledRes.getScaledWidth() - 106,
                        scaledRes.getScaledHeight() - 45, 0,
                        210, 106, 45, 0);
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                mc.fontRenderer.drawStringWithShadow(currentItem.getDisplayName(),
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
                mc.fontRenderer.drawStringWithShadow(
                        I18n.translateToLocal("dn.selected_item.desc") + ": "
                                + selectedStackName,
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 72, 16777215);
                mc.fontRenderer.drawStringWithShadow(
                        I18n.translateToLocal("dn.count.desc") + ": "
                                + (ItemDankNull.getTier(
                                        currentItem) == DankNullTier.CREATIVE
                                                ? "Infinite"
                                                : selectedStack.getCount()),
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 61, 16777215);
                mc.fontRenderer.drawStringWithShadow(
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
                mc.fontRenderer.drawStringWithShadow(
                        I18n.translateToLocal("dn.extract.desc") + ": "
                                + dankNullHandler
                                        .getExtractionMode(
                                                selectedStack)
                                        .getTooltip(),
                        scaledRes.getScaledWidth() * 2 - 212 + 45,
                        scaledRes.getScaledHeight() * 2 - 40, 16777215);

                final String keyBind = ModKeyBindings.getOpenDankNullKeyBind().getDisplayName();
                mc.fontRenderer.drawStringWithShadow(
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

                mc.fontRenderer.drawStringWithShadow(oreDictMode,
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

    private static void showHud(final Minecraft mc, final ScaledResolution scaledRes) {
        ItemStack dank = mc.player.getHeldItemMainhand();
        if (!ItemDankNull.isDankNull(dank)) {
            dank = mc.player.getHeldItemOffhand();
        }
        if (!ItemDankNull.isDankNull(dank)) {
            return;
        }

        final IDankNullHandler handler = dank.getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
        if (handler == null || handler.getSelected() < 0
                || handler.getFullStackInSlot(handler.getSelected()).isEmpty()) {
            return;
        }

        final List<Integer> cycleSlots = getCycleSlots(handler);
        if (cycleSlots.isEmpty()) {
            return;
        }
        final int current = cycleSlots.indexOf(handler.getSelected());
        final int previous = current < 0 ? 0 : Math.floorMod(current - 1, cycleSlots.size());
        final int next = current < 0 ? 0 : (current + 1) % cycleSlots.size();
        final boolean singleSlot = cycleSlots.size() == 1 && current == 0;
        final ItemStack[] preview = singleSlot
                ? new ItemStack[] { handler.getFullStackInSlot(handler.getSelected()) }
                : new ItemStack[] {
                        handler.getFullStackInSlot(cycleSlots.get(previous)),
                        handler.getFullStackInSlot(handler.getSelected()),
                        handler.getFullStackInSlot(cycleSlots.get(next))
                };

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
        final List<Integer> blocks = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            final ItemStack stack = handler.getFullStackInSlot(slot);
            if (!stack.isEmpty()) {
                occupied.add(slot);
                if (stack.getItem() instanceof ItemBlock) {
                    blocks.add(slot);
                }
            }
        }
        return Options.skipNonBlocksOnCycle && !blocks.isEmpty() ? blocks : occupied;
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

    @SideOnly(Side.CLIENT)
    public static void renderHUD(final Minecraft mc, final ScaledResolution scaledRes) {
        if (!Options.showHud
                || !mc.playerController.shouldDrawHUD() && !mc.player.capabilities.isCreativeMode) {
            return;
        }

        if (Options.showLegacyHud) {
            showLegacyHud(mc, scaledRes);
        } else {
            showHud(mc, scaledRes);
        }
    }

    public static void toggleHUD() {
        Options.showHud = !Options.showHud;
        ModConfig.getInstance().get(Configuration.CATEGORY_CLIENT, ModConfig.NAME_SHOW_HUD, true)
                .setValue(Options.showHud);
        ModConfig.getInstance().save();
    }

}
