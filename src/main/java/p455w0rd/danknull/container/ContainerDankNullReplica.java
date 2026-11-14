package p455w0rd.danknull.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.inventory.slot.SlotLocked;

public class ContainerDankNullReplica extends Container {

    private static final int SLOTS_PER_ROW = 9;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int HOTBAR_SLOT_COUNT = 9;

    // From left of one slot to the next one
    // slot is 16 pixels, space between slots 5 pixels
    private static final int SLOT_SPACING = 21;
    private static final int SLOTS_START_X = 9;
    // pixels from top to beginning of pixel for the top slot in dank
    private static final int DANK_SLOTS_START_Y = 19;
    // Added to the vertical space occupied by the Dank's rows.
    // 28 pixels from top of bottom slot to top slot's outlining pixels?
    private static final int PLAYER_INVENTORY_Y_OFFSET = 28;
    // offset starting from top
    private static final int HOTBAR_Y_OFFSET = 95;

    private final EntityPlayer player;
    private final DankNullReplicaHandler inventory;
    private final int inventorySlot;
    private final ItemStack dank;

    public ContainerDankNullReplica(final EntityPlayer player, final int inventorySlot) {
        this.player = player;
        this.inventorySlot = inventorySlot;
        this.dank = player.inventory.getStackInSlot(inventorySlot);

        final IItemHandler handler = this.dank.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (!(handler instanceof DankNullReplicaHandler)) {
            throw new IllegalArgumentException(
                    "The stack does not provide a DankNullReplicaHandler");
        }

        this.inventory = (DankNullReplicaHandler) handler;
        final int rows = this.inventory.getSlots() / SLOTS_PER_ROW;

        // create dank slots
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < SLOTS_PER_ROW; column++) {
                final int index = row * SLOTS_PER_ROW + column;

                this.addSlotToContainer(new SlotDankNullReplica(
                        this.inventory,
                        index,
                        SLOTS_START_X + column * SLOT_SPACING,
                        DANK_SLOTS_START_Y + row * SLOT_SPACING));
            }
        }

        // create player hotbar slots
        for (int column = 0; column < HOTBAR_SLOT_COUNT; column++) {
            final int x = SLOTS_START_X + column * SLOT_SPACING;
            final int y = HOTBAR_Y_OFFSET + rows * SLOT_SPACING;

            final Slot curSlot = column == this.inventorySlot
                    ? new SlotLocked(player.inventory, column, x, y)
                    : new Slot(player.inventory, column, x, y);

            this.addSlotToContainer(curSlot);
        }

        // create player inv slots
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int column = 0; column < SLOTS_PER_ROW; column++) {
                this.addSlotToContainer(new Slot(
                        player.inventory,
                        // HOTBAR_SLOT_COUNT? Should it just be SLOTS_PER_ROW?
                        HOTBAR_SLOT_COUNT + row * SLOTS_PER_ROW + column,
                        SLOTS_START_X + column * SLOT_SPACING,
                        PLAYER_INVENTORY_Y_OFFSET + (rows + row) * SLOT_SPACING));
            }
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return player == this.player && !this.dank.isEmpty()
                && player.inventory.getStackInSlot(this.inventorySlot) == this.dank;
    }

    @Override
    public ItemStack slotClick(final int slotId, final int button,
            final ClickType clickType, final EntityPlayer player) {
        if (clickType == ClickType.SWAP && button == this.inventorySlot) {
            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, button, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot slot = this.inventorySlots.get(index);

        // if slot doesn't have an ItemStack or can't take ItemStack out of slot
        if (!slot.getHasStack() || !slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getStack();
        final ItemStack original = stack.copy();
        final int dankSlots = this.inventory.getSlots();

        if (index < dankSlots) {
            // Dank -> player inv/hotbar
            if (!this.mergeItemStack(
                    stack, dankSlots, this.inventorySlots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player inv/hotbar -> Dank
            if (!this.mergeItemStack(stack, 0, dankSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        }

        slot.onTake(player, stack);
        return original;
    }
}
