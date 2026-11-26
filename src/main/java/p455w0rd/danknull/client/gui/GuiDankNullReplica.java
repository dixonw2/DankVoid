package p455w0rd.danknull.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

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
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
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
    // // DankNullReplicaHandler t = new DankNullReplicaHandler(this.tier, null);
    // final DankNullReplicaHandler handler = ((ContainerDankNullReplica)
    // this.inventorySlots).getHandler();
    // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    // GlStateManager.disableLighting();
    // GlStateManager.disableBlend();
    // final int fontColor = 16777215;
    // final int yOffset = tier.getNumRows() * 20 + 18 + tier.getNumRows() - 1;
    // // final String name = Options.displayName;
    // // final String name = "/temp/void";
    // final String name = I18n.format("gui.dankVoid.title");
    // mc.fontRenderer.drawString(name, 7, 6, tier.getHexColor(true), true);
    // mc.fontRenderer.drawString(I18n.format("container.inventory"), 7, yOffset,
    // fontColor);
    // // if (handler.getSelected() >= 0) {
    // // mc.fontRenderer.drawString(" = " + I18n.format("gui.dankVoid.selected"),
    // // xSize -
    // // 64, 6, fontColor);
    // // }
    // GlStateManager.enableBlend();
    // GlStateManager.enableLighting();
    // }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        final int rows = this.tier.getNumRows();
        final int inventoryLabelY = rows * 20 + 18 + rows - 1;
        final String title = I18n.format("gui.dankVoid.title");
        final String invTitle = I18n.format("container.inventory");

        this.mc.fontRenderer.drawString(title, 7, 6, this.tier.getHexColor(true), true);
        this.mc.fontRenderer.drawString(invTitle, 7, inventoryLabelY, 0xFFFFFF);
        this.drawSelectedSlotBorder();

        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

    private void drawSelectedSlotBorder() {
        final ContainerDankNullReplica container = (ContainerDankNullReplica) this.inventorySlots;
        final DankNullReplicaHandler handler = container.getHandler();
        final int selected = handler.getSelected();
        if (selected < 0 || selected >= handler.getSlots()) {
            return;
        }

        final Slot slot = container.getSlot(selected);
        if (!slot.getHasStack()) {
            return;
        }

        final int x = slot.xPos;
        final int y = slot.yPos;
        final int color = this.tier == DankNullTier.REDSTONE ? 0xFFFFFF00 : 0xBBFF0000;
        this.drawHorizontalLine(x - 1, x + 16, y - 1, color);
        this.drawHorizontalLine(x - 1, x + 16, y + 16, color);
        this.drawVerticalLine(x - 1, y - 1, y + 16, color);
        this.drawVerticalLine(x + 16, y - 1, y + 16, color);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

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
        final TextFormatting configuredHeaderColor = Config.ToolTips.Formatting.headerColor;
        final TextFormatting configuredHintColor = Config.ToolTips.Formatting.hintColor;
        final TextFormatting configuredValueColor = Config.ToolTips.Formatting.valueColor;
        final TextFormatting headerColor = configuredHeaderColor == null ? tierColor : configuredHeaderColor;
        final TextFormatting hintColor = configuredHintColor == null ? tierColor : configuredHintColor;
        final TextFormatting valColor = configuredValueColor == null ? tierColor : configuredValueColor;

        final int count = stack.getCount();
        if (count >= Config.ToolTips.Visibility.Misc.countThreshold
                && DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Misc.count)) {
            tooltip.add(tooltipIndex++, DankNullReplicaToolTip.formatCount(count, tierColor));
        }

        // final DankNullReplicaHandler handler = ((ContainerDankNullReplica)
        // this.inventorySlots).getHandler();
        // tooltip.addAll(tooltipIndex++, this.getDetailedToolTips(headerColor,
        // hintColor, valColor));

        // boolean tempSelected = handler.getSelected() == slot.getSlotIndex();
        // tooltip.addAll(tooltipIndex++, this.getSelectedToolTips(tempSelected,
        // headerColor, hintColor, valColor));

        // final DankNullReplicaHandler handler = ((ContainerDankNullReplica)
        // this.inventorySlots).getHandler();
        // final boolean selected = handler.getSelected() == slot.getSlotIndex();

        // final List<String> tips = this.getDetailedToolTips(headerColor, hintColor,
        // valColor);
        // tips.addAll(this.getSelectedToolTips(selected, headerColor, hintColor,
        // valColor));
        // tooltip.addAll(tooltipIndex, tips);

        final List<String> tips = this.getDetailedToolTips(headerColor, hintColor, valColor);

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.DEFAULT)) {
            final DankNullReplicaHandler handler = ((ContainerDankNullReplica) this.inventorySlots).getHandler();
            final boolean selected = handler.getSelected() == slot.getSlotIndex();
            tips.addAll(this.getSelectedToolTips(selected, headerColor, hintColor, valColor));
        }

        tooltip.addAll(tooltipIndex, tips);

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

    private List<String> getSelectedToolTips(final boolean selected, final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting valColor) {
        // final DankNullReplicaHandler handler = ((ContainerDankNullReplica)
        // this.inventorySlots).getHandler();
        // final boolean selected = slotIndex == handler.getSelected();
        final String action = DankNullReplicaToolTip.formatHintValue("C + Click", valColor, hintColor);

        final List<String> tips = new ArrayList<>();
        tips.add(DankNullReplicaToolTip.formatHeader(selected ? "Selected" : "Not Selected", headerColor));
        tips.add(DankNullReplicaToolTip.formatHint("Set Selected: " + action, hintColor));
        return tips;
    }

    // private List<String> getSelectedToolTips(final TextFormatting headerColor,
    // final TextFormatting hintColor, final TextFormatting valColor) {
    // final DankNullReplicaHandler handler = ((ContainerDankNullReplica)
    // this.inventorySlots).getHandler();

    // if (slot.getSlotIndex() == handler.getSelected()) {
    // tooltip.add(tooltipIndex++, DankNullReplicaToolTip.formatHintValue(
    // I18n.format("tooltip.dankVoid.selected"), valColor, TextFormatting.RESET));
    // }
    // }

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
