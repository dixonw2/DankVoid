package p455w0rd.danknull.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.network.BulkExtractionNetworking;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@SideOnly(Side.CLIENT)
public class GuiDankNullReplica extends GuiContainer
        implements IMTModGuiContainer2 {

    private final DankNullTier tier;
    private final int CONFIG_VALUE_COUNT_THRESHOLD = 90;

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
    protected void renderToolTip(ItemStack stack, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            return;
        }

        final Slot slot = this.getSlotUnderMouse();
        if (!(slot instanceof SlotDankNullReplica)) {
            super.renderToolTip(stack, mouseX, mouseY);
            return;
        }

        int curIndex = 1;
        final List<String> tooltip = this.getItemToolTip(stack);

        final TextFormatting headerColor = TextFormatting.WHITE;
        final TextFormatting hintColor = TextFormatting.GRAY;
        final TextFormatting varColor = this.tier.getColor();

        final int count = stack.getCount();
        if (count >= CONFIG_VALUE_COUNT_THRESHOLD) {
            tooltip.add(curIndex++, hintColor + I18n.format("tooltip.dankVoid.count", Integer.toString(count)));
        }

        // this should probably be moved out of this and make isShiftKeyDown be the
        // condition to render?
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.addAll(curIndex++, getTooltipsForStack(stack, null, null,
                    false, headerColor, hintColor, varColor));
        }
        this.drawHoveringText(tooltip, mouseX, mouseY);

    }

    private List<String> getTooltipsForStack(final ItemStack stack, final Slot slot,
            final DankNullReplicaHandler handler, final boolean showAll, final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        final List<String> tips = new ArrayList<>();

        if (!stack.isEmpty()) {
            // TODO: Only shows for blocks that can be placed
            tips.addAll(this.getExtractionToolTips(headerColor, hintColor, varColor));
            tips.addAll(this.getPlacementToolTips(headerColor, hintColor, varColor));
            tips.addAll(this.getSortToolTips(headerColor, hintColor, varColor));
        }

        return tips;
    }

    private String formatHeader(final String text, final TextFormatting color) {
        return color + TextFormatting.UNDERLINE.toString() + text;
    }

    private String formatHint(final String text, final TextFormatting color) {
        // indent by 2 spaces
        return color + "  " + text;
    }

    private String formatHintValue(final String text, final TextFormatting color, final TextFormatting prevColor) {
        return color + text + prevColor;
    }

    private List<String> getModeToolTips(final String headerKey,
            final boolean partial, final int amount, final String keybind,
            final TextFormatting headerColor, final TextFormatting hintColor, final TextFormatting varColor) {
        final String amountText = partial
                ? I18n.format("tooltip.dankVoid.partial", amount)
                : I18n.format("tooltip.dankVoid.all");

        final String action = this.formatHintValue(
                I18n.format("tooltip.dankVoid.hint.changeMode.plusClick", keybind), varColor, hintColor);

        final List<String> tips = new ArrayList<>();
        tips.add(this.formatHeader(I18n.format(headerKey, amountText), headerColor));
        tips.add(this.formatHint(
                I18n.format("tooltip.dankVoid.hint.changeMode", action), hintColor));
        return tips;
    }

    private List<String> getExtractionToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.extract", true,
                16, "X", headerColor, hintColor, varColor);
    }

    private List<String> getPlacementToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.place", true,
                1, "P", headerColor, hintColor, varColor);
    }

    private List<String> getSortToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        final String sortMode = I18n.format("sorting.dankVoid.mode.insertOrder");
        final boolean autoSort = false;

        final String autoSortState = this.formatHintValue(
                I18n.format(autoSort
                        ? "tooltip.dankVoid.enabled"
                        : "tooltip.dankVoid.disabled"),
                varColor, hintColor);

        final String toggleAutoSort = this.formatHintValue(
                I18n.format("tooltip.dankVoid.hint.changeMode.plusKeys", "S", "Middle Click"),
                varColor, hintColor);

        final String sortAction = this.formatHintValue(
                "Middle Click", varColor, hintColor);

        final String changeMode = this.formatHintValue(
                I18n.format("tooltip.dankVoid.hint.changeMode.plusClick", "S"),
                varColor, hintColor);

        final List<String> tips = new ArrayList<>();
        tips.add(this.formatHeader(
                I18n.format("tooltip.dankVoid.sortMode", sortMode),
                headerColor));
        tips.add(this.formatHint(
                I18n.format("tooltip.dankVoid.autoSort", autoSortState, toggleAutoSort),
                hintColor));
        tips.add(this.formatHint(
                I18n.format("tooltip.dankVoid.hint.sort", sortAction),
                hintColor));
        tips.add(this.formatHint(
                I18n.format("tooltip.dankVoid.hint.changeMode", changeMode),
                hintColor));

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
