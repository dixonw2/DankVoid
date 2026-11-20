package p455w0rd.danknull.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import p455w0rd.danknull.init.ModNetworking;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.inventory.slot.SlotDankNullReplica;
import p455w0rd.danknull.inventory.slot.SlotLocked;
import p455w0rd.danknull.network.PacketUpdateSlot;

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
    // added to the vertical space occupied by the Dank's rows
    private static final int PLAYER_INVENTORY_Y_OFFSET = 28;
    // offset starting from top
    private static final int HOTBAR_Y_OFFSET = 95;

    // private static final int MOUSE_LEFT_CLICK = 0;
    private static final int MOUSE_RIGHT_CLICK = 1;

    private final EntityPlayer player;
    private final int inventorySlot;
    private final ItemStack dank;

    public ContainerDankNullReplica(final EntityPlayer player, final int inventorySlot) {
        this.player = player;
        this.inventorySlot = inventorySlot;
        this.dank = player.inventory.getStackInSlot(inventorySlot);

        final int rows = this.getHandler().getSlots() / SLOTS_PER_ROW;

        // create dank slots
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < SLOTS_PER_ROW; column++) {
                final int index = row * SLOTS_PER_ROW + column;

                this.addSlotToContainer(new SlotDankNullReplica(
                        this::getHandler,
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
                        SLOTS_PER_ROW + row * SLOTS_PER_ROW + column,
                        SLOTS_START_X + column * SLOT_SPACING,
                        PLAYER_INVENTORY_Y_OFFSET + (rows + row) * SLOT_SPACING));
            }
        }
    }

    public DankNullReplicaHandler getHandler() {
        final ItemStack current = this.player.inventory.getStackInSlot(this.inventorySlot);
        final IItemHandler handler = current.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(handler instanceof DankNullReplicaHandler)) {
            throw new IllegalStateException("The open Dank no longer provides a DankNullReplicaHandler");
        }
        return (DankNullReplicaHandler) handler;
    }

    public void bulkExtractToPlayer(EntityPlayer player, int clickedSlot) {
        if (player.world.isRemote || !this.canInteractWith(player)
                || player.inventory.getItemStack().isEmpty()
                || clickedSlot < 0) {
            return;
        }

        final DankNullReplicaHandler inventory = this.getHandler();
        if (clickedSlot >= inventory.getSlots()) {
            return;
        }

        ItemStack matching = inventory.getStackInSlot(clickedSlot).copy();
        if (matching.isEmpty()) {
            return;
        }

        for (int index = 0; index < inventory.getSlots(); index++) {
            while (true) {
                ItemStack stored = inventory.getStackInSlot(index);

                if (stored.isEmpty()
                        || !ItemStack.areItemsEqual(stored, matching)
                        || !ItemStack.areItemStackTagsEqual(stored, matching)) {
                    break;
                }

                if (this.transferStackInSlot(player, index).isEmpty()) {
                    break;
                }
            }
        }

        player.inventory.markDirty();
        this.detectAndSendChanges();
    }

    @Override
    public boolean canDragIntoSlot(final Slot slot) {
        return !(slot instanceof SlotDankNullReplica);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return player == this.player && !this.dank.isEmpty()
                && player.inventory.getStackInSlot(this.inventorySlot) == this.dank;
    }

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slot) {
        return !(slot instanceof SlotDankNullReplica);
    }

    @Override
    public void detectAndSendChanges() {
        for (int index = 0; index < this.inventorySlots.size(); index++) {
            final ItemStack current = this.inventorySlots.get(index).getStack();
            final ItemStack previous = this.inventoryItemStacks.get(index);

            if (ItemStack.areItemStacksEqual(previous, current)) {
                continue;
            }

            final ItemStack snapshot = current.copy();
            this.inventoryItemStacks.set(index, snapshot);

            for (final IContainerListener listener : this.listeners) {
                if (listener instanceof EntityPlayerMP) {
                    final EntityPlayerMP serverPlayer = (EntityPlayerMP) listener;

                    if (!serverPlayer.isChangingQuantityOnly) {
                        // TODO: Switch out of ModNetworking
                        ModNetworking.getInstance().sendTo(
                                new PacketUpdateSlot(index, snapshot),
                                serverPlayer);
                    }
                } else {
                    listener.sendSlotContents(this, index, snapshot);
                }
            }
        }
    }

    @Override
    public ItemStack slotClick(final int slotId, final int button,
            final ClickType clickType, final EntityPlayer player) {

        final DankNullReplicaHandler inventory = this.getHandler();
        final int slots = inventory.getSlots();
        final boolean dankSlot = slotId >= 0 && slotId < slots;

        if (clickType == ClickType.PICKUP && dankSlot) {
            final ItemStack cursor = player.inventory.getItemStack();
            final ItemStack stored = inventory.getStackInSlot(slotId);

            if (cursor.isEmpty()) {
                final int available = Math.min(
                        stored.getCount(), stored.getMaxStackSize());
                // if right click, half stack
                final int amount = button == MOUSE_RIGHT_CLICK
                        ? (available + 1) / 2
                        : available;

                player.inventory.setItemStack(
                        inventory.extractItem(slotId, amount, false));

                return ItemStack.EMPTY;
            }

            if (stored.isEmpty()
                    || ItemHandlerHelper.canItemStacksStack(cursor, stored)) {
                final ItemStack offered = cursor.copy();

                // Left-click inserts the held stack, right-click inserts one item
                if (button == MOUSE_RIGHT_CLICK) {
                    offered.setCount(1);
                }

                final ItemStack remainder = inventory.insertItem(slotId, offered, false);
                final int inserted = offered.getCount() - remainder.getCount();

                cursor.shrink(inserted);
                player.inventory.setItemStack(
                        cursor.isEmpty() ? ItemStack.EMPTY : cursor);

                // Both sides acknowledge custom pickups without serializing the oversized stored count.
                return ItemStack.EMPTY;
            }
            // An oversized stored stack cannot be swapped onto the player's cursor.
            if (stored.getCount() > stored.getMaxStackSize()) {
                return ItemStack.EMPTY;
            }
        } else if (clickType == ClickType.PICKUP_ALL) {
            final ItemStack cursor = player.inventory.getItemStack();

            if (cursor.isEmpty()
                    || cursor.getCount() >= cursor.getMaxStackSize()
                    || slotId < 0
                    || slotId >= this.inventorySlots.size()) {
                return ItemStack.EMPTY;
            }

            final Slot clicked = this.inventorySlots.get(slotId);
            if (clicked.getHasStack() && clicked.canTakeStack(player)) {
                return ItemStack.EMPTY;
            }

            // Do inventory merging first, then from dank
            super.slotClick(slotId, button, clickType, player);

            // Fill any remaining cursor space from the Dank
            for (int i = 0; i < slots
                    && cursor.getCount() < cursor.getMaxStackSize(); i++) {
                final ItemStack stored = inventory.getStackInSlot(i);

                if (!ItemHandlerHelper.canItemStacksStack(cursor, stored)) {
                    continue;
                }

                final ItemStack extracted = inventory.extractItem(
                        i, cursor.getMaxStackSize() - cursor.getCount(), false);

                cursor.grow(extracted.getCount());
            }

            return ItemStack.EMPTY;
        } else if (clickType == ClickType.QUICK_MOVE && dankSlot) {
            // only extract up to one normal stack
            return this.transferStackInSlot(player, slotId);
        } else if (clickType == ClickType.SWAP && (dankSlot || button == this.inventorySlot)) {
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
        final DankNullReplicaHandler inventory = this.getHandler();
        final int numDankSlots = inventory.getSlots();

        if (index < numDankSlots) {
            // Dank -> player inv/hotbar
            final ItemStack offered = inventory.extractItem(
                    index, stack.getMaxStackSize(), true);
            final int offeredCount = offered.getCount();

            if (!this.mergeItemStack(
                    offered, numDankSlots, this.inventorySlots.size(), false)) {
                return ItemStack.EMPTY;
            }

            final int moved = offeredCount - offered.getCount();
            final ItemStack extracted = inventory.extractItem(index, moved, false);

            slot.onTake(player, extracted);
            return extracted;
        } else {
            // player inv/hotbar -> Dank
            final ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, stack.copy(), false);

            if (remainder.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            stack.setCount(remainder.getCount());
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        }

        slot.onTake(player, stack);
        return original;
    }
}
