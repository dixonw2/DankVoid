package p455w0rd.danknull.client.gui;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.client.render.StackCountRenderer;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.core.config.Config;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.network.BulkExtractionNetworking;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@SideOnly(Side.CLIENT)
public class GuiDankNullReplica extends GuiContainer
        implements IMTModGuiContainer2 {

    private final DankNullTier tier;

    // pulled from ContainerDankNullReplica; see about where to put these to reduce
    // duplication
    private static final int MOUSE_LEFT_CLICK = 0;
    // private static final int MOUSE_RIGHT_CLICK = 1;

    public GuiDankNullReplica(final ContainerDankNullReplica container,
            final DankNullTier tier) {
        super(container);
        this.tier = tier;
        this.xSize = 210;
        this.ySize = tier.getGuiHeight();
    }

    // @Override
    // protected void drawGuiContainerForegroundLayer(final int mouseX, final int
    // mouseY) {
    // // final IDankNullHandler dankNullHandler = getDankNullHandler();
    // DankNullReplicaHandler t = new DankNullReplicaHandler(this.tier, null);
    // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    // GlStateManager.disableLighting();
    // GlStateManager.disableBlend();
    // final int fontColor = 16777215;
    // final int yOffset = tier.getNumRows() * 20 + 18 + tier.getNumRows() - 1;
    // // final String name = Options.displayName;
    // final String name = "/temp/void";
    // mc.fontRenderer.drawString(name, 7, 6, tier.getHexColor(true), true);
    // mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, yOffset,
    // fontColor);
    // if (dankNullHandler.getSelected() > -1) {
    // mc.fontRenderer.drawString(" = " + I18n.format("dn.selected.desc"), xSize -
    // 64, 6, fontColor);
    // }
    // GlStateManager.enableBlend();
    // GlStateManager.enableLighting();
    // }

    @Override
    protected void drawGuiContainerBackgroundLayer(
            final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.tier.getGuiBackground());
        this.drawTexturedModalRect(
                this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY,
            final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void drawSlot(final Slot slot) {
        if (!(slot instanceof SlotDankNullReplica)) {
            super.drawSlot(slot);
            return;
        }

        ItemStack stack = slot.getStack();
        if (slot == this.clickedSlot && !this.draggedStack.isEmpty()) {
            if (!this.isRightMouseClick) {
                return;
            }
            stack = stack.copy();
            stack.setCount(stack.getCount() / 2);
        }

        if (stack.isEmpty()) {
            return;
        }

        this.itemRender.zLevel = 100.0F;
        GlStateManager.enableDepth();
        this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, slot.xPos, slot.yPos);
        // prevent vanilla quantity rendering
        this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, slot.xPos, slot.yPos, "");
        StackCountRenderer.drawStackCount(this.fontRenderer, stack.getCount(), slot.xPos, slot.yPos);
        this.itemRender.zLevel = 0.0F;
    }

    // default click to slot 0 if the cursor is in the dank inventory
    // used for clicking to insert an item but the cursor is between slots
    @Override
    public Slot getSlotAtPosition(final int mouseX, final int mouseY) {
        final Slot slot = super.getSlotAtPosition(mouseX, mouseY);
        if (slot != null || this.mc.player.inventory.getItemStack().isEmpty()) {
            return slot;
        }

        final Slot first = this.inventorySlots.getSlot(0);
        final Slot last = this.inventorySlots.getSlot(this.tier.getNumSlots() - 1);
        final int width = last.xPos - first.xPos + 16;
        final int height = last.yPos - first.yPos + 16;

        return this.isPointInRegion(first.xPos, first.yPos, width, height, mouseX, mouseY)
                ? first
                : null;
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        final Slot slot = this.getSlotUnderMouse();

        if (mouseButton == MOUSE_LEFT_CLICK && this.doubleClick && isShiftKeyDown()
                && !this.mc.player.inventory.getItemStack().isEmpty() && slot instanceof SlotDankNullReplica
                && slot.getHasStack()) {
            this.doubleClick = false;
            this.dragSplitting = false;
            this.dragSplittingSlots.clear();
            this.ignoreMouseUp = false;

            // change to custom packet
            BulkExtractionNetworking.requestExtraction(this.inventorySlots.windowId, slot.slotNumber);
            return;
        }

        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    // #region ToolTips
    @Override
    protected void renderToolTip(final ItemStack stack, final int mouseX, final int mouseY) {
        if (stack.isEmpty()) {
            return;
        }

        final Slot slot = this.getSlotUnderMouse();
        if (!(slot instanceof SlotDankNullReplica)) {
            super.renderToolTip(stack, mouseX, mouseY);
            return;
        }

        int tooltipIndex = 1;
        final List<String> tooltip = this.getItemToolTip(stack);

        final TextFormatting tierColor = this.tier.getColor();
        final TextFormatting headerColor = firstNonNull(Config.ToolTips.Formatting.headerColor, tierColor);
        final TextFormatting hintColor = firstNonNull(Config.ToolTips.Formatting.hintColor, tierColor);
        final TextFormatting varColor = firstNonNull(Config.ToolTips.Formatting.valueColor, tierColor);

        final int count = stack.getCount();
        if (count >= Config.ToolTips.Visibility.Misc.countThreshold
                && DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Misc.count)) {
            final String countText = DankNullReplicaToolTip.formatHintValue(Integer.toString(count), varColor, hintColor);
            tooltip.add(tooltipIndex++, DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.count", countText), hintColor));
        }

        tooltip.addAll(tooltipIndex, this.getDetailedToolTips(headerColor, hintColor, varColor));
        final FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, this.width, this.height, -1,
                font == null ? this.fontRenderer : font);

    }

    private List<String> getDetailedToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        final List<String> tips = new ArrayList<>();
        tips.addAll(this.getSortToolTips(headerColor, hintColor, varColor));
        tips.addAll(this.getExtractionToolTips(headerColor, hintColor, varColor));
        // TODO: Only shows for blocks that can be placed
        tips.addAll(this.getPlacementToolTips(headerColor, hintColor, varColor));
        return tips;
    }

    private List<String> getModeToolTips(final String headerKey, final String hintKey,
            final String headerVisibility, final String hintVisibility,
            final boolean partial, final int amount, final String keybind,
            final TextFormatting headerColor, final TextFormatting hintColor, final TextFormatting varColor) {
        final List<String> tips = new ArrayList<>();
        if (DankNullReplicaToolTip.displayToolTip(headerVisibility)) {
            final String amountText = partial
                    ? I18n.format("tooltip.dankVoid.partial", amount)
                    : I18n.format("tooltip.dankVoid.all");
            tips.add(DankNullReplicaToolTip.formatHeader(I18n.format(headerKey, amountText), headerColor));
        }
        if (DankNullReplicaToolTip.displayToolTip(hintVisibility)) {
            final String action = DankNullReplicaToolTip.formatHintValue(
                    I18n.format("tooltip.dankVoid.hint.changeMode.plusClick", keybind), varColor, hintColor);
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format(hintKey, action), hintColor));
        }
        return tips;
    }

    private List<String> getExtractionToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.extract", "tooltip.dankVoid.hint.changeMode.extraction",
                Config.ToolTips.Visibility.Extraction.header, Config.ToolTips.Visibility.Extraction.changeMode,
                true, 16, "X", headerColor, hintColor, varColor);
    }

    private List<String> getPlacementToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.place", "tooltip.dankVoid.hint.changeMode.placement",
                Config.ToolTips.Visibility.Placement.header, Config.ToolTips.Visibility.Placement.changeMode,
                true, 1, "P", headerColor, hintColor, varColor);
    }

    private List<String> getSortToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        final String sortMode = I18n.format("sorting.dankVoid.mode.manual");
        final boolean autoSort = false;

        final String autoSortState = DankNullReplicaToolTip.formatHintValue(
                I18n.format(autoSort
                        ? "tooltip.dankVoid.enabled"
                        : "tooltip.dankVoid.disabled"),
                varColor, hintColor);

        final String toggleAutoSort = DankNullReplicaToolTip.formatHintValue(
                I18n.format("tooltip.dankVoid.hint.changeMode.plusKeys", "S", "Middle Click"),
                varColor, hintColor);

        final String sortAction = DankNullReplicaToolTip.formatHintValue(
                "Middle Click", varColor, hintColor);

        final String changeMode = DankNullReplicaToolTip.formatHintValue(
                I18n.format("tooltip.dankVoid.hint.changeMode.plusClick", "S"),
                varColor, hintColor);

        final List<String> tips = new ArrayList<>();
        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.header)) {
            tips.add(DankNullReplicaToolTip.formatHeader(
                    I18n.format("tooltip.dankVoid.sortMode", sortMode),
                    headerColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.keybind)) {
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.hint.sort", sortAction),
                    hintColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.autoSort)) {
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.autoSort", autoSortState, toggleAutoSort),
                    hintColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.changeMode)) {
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.hint.changeMode.sort", changeMode),
                    hintColor));
        }

        return tips;
    }

    private List<String> getOreDictToolTips() {
        return null;
    }

    // #endregion

    // #region MouseTweaks
    @Override
    public boolean MT_isMouseTweaksDisabled() {
        return false;
    }

    @Override
    public boolean MT_isWheelTweakDisabled() {
        return false;
    }

    @Override
    public Container MT_getContainer() {
        return this.inventorySlots;
    }

    @Override
    public Slot MT_getSlotUnderMouse() {
        return this.getSlotUnderMouse();
    }

    @Override
    public boolean MT_isCraftingOutput(final Slot slot) {
        return false;
    }

    @Override
    public boolean MT_isIgnored(final Slot slot) {
        return slot instanceof SlotDankNullReplica;
    }

    @Override
    public boolean MT_disableRMBDraggingFunctionality() {
        // Leave ignored Dank slots to vanilla
        if (this.MT_isIgnored(this.MT_getSlotUnderMouse())) {
            return false;
        }

        // Prevent vanilla
        this.ignoreMouseUp = true;

        if (this.dragSplitting && this.dragSplittingButton == 1) {
            this.dragSplitting = false;
            return true;
        }

        return false;
    }

    // #endregion
}
