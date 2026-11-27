package p455w0rd.danknull.client.gui;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.client.KeyBindingsReplica;
import p455w0rd.danknull.client.render.StackCountRenderer;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.core.config.Config;
import p455w0rd.danknull.integration.ChiselReplica;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler.ItemMode;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.network.BulkExtractionNetworking;
import p455w0rd.danknull.network.PacketDankSlotActionReplica;
import p455w0rd.danknull.network.PacketDankSlotActionReplica.Action;
import p455w0rd.danknull.network.PacketHandler;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@SideOnly(Side.CLIENT)
public class GuiDankNullReplica extends GuiContainer
        implements IMTModGuiContainer2 {

    private static final int ITEM_SIZE = 16;
    private static final float GHOST_GRAY = 139.0F / 255.0F;
    private static final FloatBuffer GHOST_BLEND = BufferUtils.createFloatBuffer(4);

    static {
        GHOST_BLEND.put(GHOST_GRAY).put(GHOST_GRAY).put(GHOST_GRAY).put(0.65F);
        GHOST_BLEND.flip();
    }

    private final DankNullTier tier;
    private final Set<Integer> consumedMouseButtons = new HashSet<>();
    private Framebuffer ghostIcon;
    private int ghostIconSize;

    // pulled from ContainerDankNullReplica; see about where to put these to reduce
    // duplication
    private static final int MOUSE_LEFT_CLICK = 0;
    private static final int MOUSE_RIGHT_CLICK = 1;

    public GuiDankNullReplica(final ContainerDankNullReplica container,
            final DankNullTier tier) {
        super(container);
        this.tier = tier;
        this.xSize = 210;
        this.ySize = tier.getGuiHeight();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.ghostIconSize = ITEM_SIZE * new ScaledResolution(this.mc).getScaleFactor();
    }

    @Override
    public void onGuiClosed() {
        if (this.ghostIcon != null) {
            final int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            this.ghostIcon.deleteFramebuffer();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, previousFramebuffer);
            this.ghostIcon = null;
        }
        super.onGuiClosed();
    }

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

        if (handler.getSlotItem(selected).isEmpty()) {
            return;
        }

        final Slot slot = container.getSlot(selected);
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

        final boolean ghost = stack.isEmpty();
        if (ghost) {
            stack = ((SlotDankNullReplica) slot).getItemHandler().getGhostStack(slot.getSlotIndex());
            if (stack.isEmpty()) {
                return;
            }
        }

        this.itemRender.zLevel = 100.0F;
        GlStateManager.enableDepth();
        if (ghost) {
            this.drawGhostIcon(stack, slot.xPos, slot.yPos);
        } else {
            this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, slot.xPos, slot.yPos);
            // prevent vanilla quantity rendering
            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, slot.xPos, slot.yPos, "");
            StackCountRenderer.drawStackCount(this.fontRenderer, stack.getCount(), slot.xPos, slot.yPos);
        }
        this.itemRender.zLevel = 0.0F;
    }

    private void drawGhostIcon(final ItemStack stack, final int x, final int y) {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, x, y);
            StackCountRenderer.drawStackCount(this.fontRenderer, 0, x, y);
            return;
        }

        final int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_SCISSOR_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        try {
            if (this.ghostIcon == null) {
                this.ghostIcon = new Framebuffer(this.ghostIconSize, this.ghostIconSize, true);
                this.ghostIcon.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            } else if (this.ghostIcon.framebufferWidth != this.ghostIconSize) {
                this.ghostIcon.createBindFramebuffer(this.ghostIconSize, this.ghostIconSize);
            }

            this.ghostIcon.framebufferClear();
            this.ghostIcon.bindFramebuffer(false);

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0, ITEM_SIZE, ITEM_SIZE, 0, -1000, 1000);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            try {
                this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, stack, 0, 0);
            } finally {
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }
        } finally {
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, previousFramebuffer);
            GL11.glPopAttrib();
        }

        this.drawGhostTexture(x, y);
    }

    private void drawGhostTexture(final int x, final int y) {
        final int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        this.ghostIcon.bindFramebufferTexture();
        GlStateManager.enableAlpha();
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        try {
            // Blend the icon's RGB toward gray while preserving its texture alpha.
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
            GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, GHOST_BLEND);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL13.GL_INTERPOLATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE1_RGB, GL13.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE2_RGB, GL13.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1);

            final float z = this.itemRender.zLevel + 150.0F;
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x, y + ITEM_SIZE, z).tex(0, 0).endVertex();
            buffer.pos(x + ITEM_SIZE, y + ITEM_SIZE, z).tex(1, 0).endVertex();
            buffer.pos(x + ITEM_SIZE, y, z).tex(1, 1).endVertex();
            buffer.pos(x, y, z).tex(0, 1).endVertex();
            tessellator.draw();
        } finally {
            GL11.glPopAttrib();
            GlStateManager.bindTexture(previousTexture);
            GlStateManager.disableAlpha();
        }
    }

    @Override
    protected void renderHoveredToolTip(final int mouseX, final int mouseY) {
        final Slot slot = this.getSlotUnderMouse();
        if (slot instanceof SlotDankNullReplica && this.mc.player.inventory.getItemStack().isEmpty()) {
            final ItemStack ghost = ((SlotDankNullReplica) slot).getItemHandler().getGhostStack(slot.getSlotIndex());
            if (!ghost.isEmpty()) {
                this.renderToolTip(ghost, mouseX, mouseY);
                return;
            }
        }

        super.renderHoveredToolTip(mouseX, mouseY);
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
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        final boolean leftClick = mouseButton == MOUSE_LEFT_CLICK;
        if ((leftClick || mouseButton == MOUSE_RIGHT_CLICK) && this.mc.player.inventory.getItemStack().isEmpty()) {
            final Slot slot = super.getSlotAtPosition(mouseX, mouseY);
            if (slot instanceof SlotDankNullReplica
                    && !((SlotDankNullReplica) slot).getItemHandler().getSlotItem(slot.getSlotIndex()).isEmpty()) {
                final boolean selection = leftClick && KeyBindingsReplica.isSelectionHeld();
                final boolean extraction = KeyBindingsReplica.isExtractionHeld();
                final boolean placement = KeyBindingsReplica.isPlacementHeld();
                final boolean sortMode = leftClick && KeyBindingsReplica.isSorttActionHeld();
                final int actions = (selection ? 1 : 0)
                        + (extraction ? 1 : 0) + (placement ? 1 : 0) + (sortMode ? 1 : 0);

                if (actions > 1 || selection || extraction || placement) {
                    this.consumedMouseButtons.add(mouseButton);
                    this.doubleClick = false;

                    if (actions == 1) {
                        final Action action = selection ? Action.TOGGLE_SELECTION
                                : extraction ? Action.CYCLE_EXTRACTION_MODE : Action.CYCLE_PLACEMENT_MODE;
                        PacketHandler.sendToServer(new PacketDankSlotActionReplica(
                                this.inventorySlots.windowId, slot.getSlotIndex(), action, leftClick));
                    }
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.consumedMouseButtons.remove(mouseButton)) {
            return;
        }

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
    private static final String UNBOUND_LOCALE_KEY = "tooltip.dankVoid.value.unbound";

    // TODO: Refactor this. Kinda weird, had that issue with tooltipIndex++ not
    // working with the selection stuff (or maybe sort?) etc
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

        if (Loader.isModLoaded("chisel")) {
            final String variant = ChiselReplica.getVariantName(stack);
            for (int i = tooltipIndex; !variant.isEmpty() && i < tooltip.size(); i++) {
                if (variant.equals(TextFormatting.getTextWithoutFormattingCodes(tooltip.get(i)))) {
                    tooltip.remove(i);
                    final String value = DankNullReplicaToolTip.formatHintValue(variant, valColor, hintColor);
                    tooltip.add(tooltipIndex++, DankNullReplicaToolTip.formatHintText(
                            I18n.format("tooltip.dankVoid.chisel.variant", value), hintColor));
                    break;
                }
            }
        }

        final int count = stack.getCount();
        final boolean ghost = !slot.getHasStack();
        if (!ghost && count >= Config.ToolTips.Visibility.Misc.countThreshold
                && DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Misc.count)) {
            tooltip.add(tooltipIndex++, DankNullReplicaToolTip.formatCount(count, tierColor));
        }

        if (ghost) {
            final TextFormatting nameColor = Config.ToolTips.Formatting.Ghost.nameColor;
            final String name = TextFormatting.getTextWithoutFormattingCodes(tooltip.get(0));
            tooltip.set(0, (nameColor == null ? tierColor : nameColor) + Config.ToolTips.Formatting.Ghost.nameStyle
                    + name);

            if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Ghost.delete)) {
                final TextFormatting color = Config.ToolTips.Formatting.Ghost.deleteColor;
                tooltip.add(tooltipIndex++, (color == null ? tierColor : color)
                        + Config.ToolTips.Formatting.Ghost.deleteStyle + I18n.format("tooltip.dankVoid.ghost.delete"));
            }
        }

        tooltip.addAll(tooltipIndex,
                this.getDetailedToolTips(stack, slot.getSlotIndex(), headerColor, hintColor, valColor));

        final FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, this.width, this.height, -1,
                font == null ? this.fontRenderer : font);

    }

    private List<String> getDetailedToolTips(final ItemStack stack, final int slotIndex,
            final TextFormatting headerColor, final TextFormatting hintColor, final TextFormatting varColor) {
        final DankNullReplicaHandler handler = ((ContainerDankNullReplica) this.inventorySlots).getHandler();
        final List<String> tips = new ArrayList<>();
        tips.addAll(this.getSortToolTips(headerColor, hintColor, varColor));
        tips.addAll(this.getExtractionToolTips(handler.getExtractionMode(stack), headerColor, hintColor, varColor));
        // TODO: Only shows for blocks that can be placed
        tips.addAll(this.getPlacementToolTips(handler.getPlacementMode(stack), headerColor, hintColor, varColor));
        tips.addAll(this.getSelectionToolTips(handler.getSelected() == slotIndex, headerColor, hintColor, varColor));
        return tips;
    }

    private static boolean isBound(final KeyBinding binding) {
        return binding.getKeyCode() != Keyboard.KEY_NONE;
    }

    private static String getClickShortcut(final KeyBinding binding) {
        return isBound(binding)
                ? I18n.format("tooltip.dankVoid.hint.changeMode.plusClick", binding.getDisplayName())
                : I18n.format(UNBOUND_LOCALE_KEY);
    }

    private List<String> getModeToolTips(final String headerKey, final String hintKey,
            final String headerVisibility, final String hintVisibility,
            final ItemMode mode, final KeyBinding binding, final boolean sectionKeyHeld,
            final TextFormatting headerColor, final TextFormatting hintColor, final TextFormatting varColor) {
        final List<String> tips = new ArrayList<>();
        if (DankNullReplicaToolTip.displayToolTip(headerVisibility, sectionKeyHeld)) {
            final String amountText;
            if (mode == ItemMode.ALL) {
                amountText = I18n.format("tooltip.dankVoid.value.all");
            } else if (mode == ItemMode.NONE) {
                amountText = I18n.format("tooltip.dankVoid.value.none");
            } else {
                amountText = I18n.format("tooltip.dankVoid.value.partial", mode.getKeepCount());
            }
            tips.add(DankNullReplicaToolTip.formatHeader(I18n.format(headerKey, amountText), headerColor));
        }
        if (DankNullReplicaToolTip.displayToolTip(hintVisibility, sectionKeyHeld)) {
            final String action = DankNullReplicaToolTip.formatHintValue(
                    getClickShortcut(binding), varColor, hintColor);
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format(hintKey, action), hintColor));
        }
        return tips;
    }

    private List<String> getExtractionToolTips(final ItemMode mode, final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.extract", "tooltip.dankVoid.hint.changeMode.extraction",
                Config.ToolTips.Visibility.Extraction.header, Config.ToolTips.Visibility.Extraction.changeMode,
                mode, KeyBindingsReplica.EXTRACTION, KeyBindingsReplica.isExtractionHeld(),
                headerColor, hintColor, varColor);
    }

    private List<String> getPlacementToolTips(final ItemMode mode, final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        return this.getModeToolTips("tooltip.dankVoid.place", "tooltip.dankVoid.hint.changeMode.placement",
                Config.ToolTips.Visibility.Placement.header, Config.ToolTips.Visibility.Placement.changeMode,
                mode, KeyBindingsReplica.PLACEMENT, KeyBindingsReplica.isPlacementHeld(),
                headerColor, hintColor, varColor);
    }

    private List<String> getSortToolTips(final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting varColor) {
        final String sortMode = I18n.format("sorting.dankVoid.mode.manual");
        final boolean autoSort = false;
        final boolean sectionKeyHeld = KeyBindingsReplica.isSorttActionHeld();
        final KeyBinding sort = KeyBindingsReplica.SORT;
        final KeyBinding sortAction = KeyBindingsReplica.SORT_ACTION;
        final boolean sortBound = isBound(sort);

        final List<String> tips = new ArrayList<>();
        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.header, sectionKeyHeld)) {
            tips.add(DankNullReplicaToolTip.formatHeader(
                    I18n.format("tooltip.dankVoid.sortMode", sortMode),
                    headerColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.keybind, sectionKeyHeld)) {
            final String shortcut = sortBound ? sort.getDisplayName() : I18n.format(UNBOUND_LOCALE_KEY);
            final String sortKey = DankNullReplicaToolTip.formatHintValue(shortcut, varColor, hintColor);
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.hint.sort", sortKey),
                    hintColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.autoSort, sectionKeyHeld)) {
            final String autoSortState = DankNullReplicaToolTip.formatHintValue(
                    I18n.format(autoSort ? "tooltip.dankVoid.value.enabled" : "tooltip.dankVoid.value.disabled"),
                    varColor, hintColor);
            final String shortcut = sortBound && isBound(sortAction)
                    ? I18n.format("tooltip.dankVoid.hint.changeMode.plusKeys",
                            sortAction.getDisplayName(), sort.getDisplayName())
                    : I18n.format(UNBOUND_LOCALE_KEY);
            final String toggleAutoSort = DankNullReplicaToolTip.formatHintValue(shortcut, varColor, hintColor);
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.autoSort", autoSortState, toggleAutoSort),
                    hintColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Sort.changeMode, sectionKeyHeld)) {
            final String changeMode = DankNullReplicaToolTip.formatHintValue(
                    getClickShortcut(sortAction), varColor, hintColor);
            tips.add(DankNullReplicaToolTip.formatHint(
                    I18n.format("tooltip.dankVoid.hint.changeMode.sort", changeMode),
                    hintColor));
        }

        return tips;
    }

    private List<String> getSelectionToolTips(final boolean selected, final TextFormatting headerColor,
            final TextFormatting hintColor, final TextFormatting valColor) {
        final List<String> tips = new ArrayList<>();
        final boolean sectionKeyHeld = KeyBindingsReplica.isSelectionHeld();
        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Selection.header, sectionKeyHeld)) {
            final String selectedString = selected ? I18n.format("tooltip.dankVoid.value.selected")
                    : I18n.format("tooltip.dankVoid.value.notSelected");
            tips.add(DankNullReplicaToolTip.formatHeader(selectedString, headerColor));
        }

        final KeyBinding selection = KeyBindingsReplica.SELECTION;
        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Selection.keybind, sectionKeyHeld)) {
            final String action = DankNullReplicaToolTip.formatHintValue(
                    getClickShortcut(selection), valColor, hintColor);
            final String hintKey = selected ? "tooltip.dankVoid.hint.changeMode.deselect"
                    : "tooltip.dankVoid.hint.changeMode.select";
            tips.add(DankNullReplicaToolTip.formatHint(I18n.format(hintKey, action), hintColor));
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
