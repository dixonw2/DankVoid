package p455w0rd.danknull.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.items.DankNullTier;
import yalter.mousetweaks.api.IMTModGuiContainer2;

public class GuiDankNullReplica extends GuiContainer
        implements IMTModGuiContainer2 {

    private final DankNullTier tier;

    public GuiDankNullReplica(final ContainerDankNullReplica container,
            final DankNullTier tier) {
        super(container);
        this.tier = tier;
        this.xSize = 210;
        this.ySize = tier.getGuiHeight();
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
        // Leave ignored Dank slots to vanilla.
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
}
