package p455w0rd.danknull.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.api.IDankNullHandler.SortMode;
import p455w0rd.danknull.init.ModNetworking;
import p455w0rd.danknull.inventory.DankNullHandler;
import p455w0rd.danknull.inventory.SortSession;
import p455w0rd.danknull.inventory.slot.SlotDankNull;
import p455w0rd.danknull.inventory.slot.SlotDankNullDock;
import p455w0rd.danknull.inventory.slot.SlotHotbar;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rd.danknull.network.PacketUpdateSlot;

/**
 * @author BrockWS
 */
public abstract class ContainerDankNull extends Container {

    protected final EntityPlayer player;
    public static final int SORT_SELECTED_PROPERTY = 0;
    public static final int SORT_MODE_PROPERTY = 1;
    public static final int SORT_REVISION_PROPERTY = 2;
    private SortSession sortSession;
    private long lastSortedInventoryRevision;
    private int lastSelected = Integer.MIN_VALUE;
    private int lastSortMode = -1;
    private int lastSortRevision = -1;

    public ContainerDankNull(final EntityPlayer player) {
        this.player = player;
    }

    protected void init() {
        final InventoryPlayer playerInv = player.inventory;
        final IDankNullHandler handler = getHandler();
        if (handler instanceof DankNullHandler) {
            ((DankNullHandler) handler).sort();
        }
        int lockedSlot = -1;
        final int numRows = handler.getTier().getNumRows();
        for (int i = 0; i < playerInv.getSizeInventory(); i++) {
            final ItemStack currStack = playerInv.getStackInSlot(i);
            if (!currStack.isEmpty() && currStack == getDankNullStack()) {
                lockedSlot = i;
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(
                    new SlotHotbar(playerInv, i, i * 20 + 9 + i, 90 + numRows - 1 + numRows * 20 + 6, lockedSlot == i));
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, j * 20 + 9 + j,
                        149 + numRows - 1 + i - (6 - numRows) * 20 + i * 20));
            }
        }
        for (int i = 0; i < handler.getTier().getNumRows(); i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(createDankNullSlot(handler, i, j));
            }
        }
    }

    public abstract IDankNullHandler getHandler();

    public abstract ItemStack getDankNullStack();

    public void sortNextMode(final SortMode defaultMode, final boolean automaticSorting) {
        // A sort request carries the player's current setting, not the item's saved flag.
        ((DankNullHandler) getHandler()).configureAutomaticSorting(defaultMode, automaticSorting);
        sortNextMode(defaultMode);
    }

    public void sortNextMode(final SortMode defaultMode) {
        final DankNullHandler handler = (DankNullHandler) getHandler();
        handler.configureAutomaticSorting(defaultMode, handler.isAutomaticSorting());
        // After items move, manual sorting reapplies the current mode before cycling.
        if (sortSession == null || !handler.isAutomaticSorting()
                && handler.getInventoryRevision() != lastSortedInventoryRevision) {
            sortSession = new SortSession(handler.isAutomaticSorting(), handler.getSortMode());
        }
        final SortMode nextMode = sortSession.pressSortKey();
        handler.setSortMode(nextMode);
        handler.useSortModeForAutomaticSorting(nextMode);
        handler.sortContents();
        lastSortedInventoryRevision = handler.getInventoryRevision();
    }

    @Override
    public void addListener(final IContainerListener listener) {
        super.addListener(listener);
        sendSortState(listener);
    }

    private void sendSortState(final IContainerListener listener) {
        listener.sendWindowProperty(this, SORT_SELECTED_PROPERTY, getHandler().getSelected());
        listener.sendWindowProperty(this, SORT_MODE_PROPERTY, getHandler().getSortMode().ordinal());
        if (getHandler() instanceof DankNullHandler) {
            listener.sendWindowProperty(this, SORT_REVISION_PROPERTY,
                    ((DankNullHandler) getHandler()).getSortRevision());
        }
    }

    protected boolean isDock() {
        return false;
    }

    private SlotDankNull createDankNullSlot(final IDankNullHandler handler, final int i, final int j) {
        return isDock() ? new SlotDankNullDock(handler, j + i * 9, j * 20 + 9 + j, 19 + i + i * 20)
                : new SlotDankNull(handler, j + i * 9, j * 20 + 9 + j, 19 + i + i * 20);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return getHandler() != null;
    }

    @Override
    public Slot getSlot(final int slotId) {
        if (slotId < inventorySlots.size() && slotId >= 0) {
            return inventorySlots.get(slotId);
        }
        return null;
    }

    @Override
    public ItemStack slotClick(final int index, final int dragType, final ClickType clickType,
            final EntityPlayer player) {
        final Slot slot = getSlot(index);
        if (slot == null || index < 36 && clickType != ClickType.QUICK_MOVE || clickType == ClickType.CLONE) {
            return super.slotClick(index, dragType, clickType, player);
        }
        if (clickType == ClickType.QUICK_MOVE) {
            return transferStackInSlot(player, index);
        }
        final InventoryPlayer inventoryPlayer = player.inventory;
        final ItemStack heldStack = inventoryPlayer.getItemStack();
        if (slot instanceof SlotDankNull && clickType == ClickType.PICKUP) {
            final ItemStack slotStack = slot.getStack();
            if (ItemDankNull.isDankNull(slotStack)) {
                return ItemStack.EMPTY;
            }
            if (!heldStack.isEmpty()) { // Want to insert held stack into DankNull
                final ItemStack toAdd = heldStack.copy();
                if (dragType == 1) {
                    toAdd.setCount(1);
                }
                final ItemStack leftover = addStack(toAdd);
                if (dragType == 0) {
                    if (!leftover.isEmpty()) {
                        inventoryPlayer.setItemStack(leftover);
                    } else {
                        inventoryPlayer.setItemStack(ItemStack.EMPTY);
                    }
                } else if (dragType == 1 && leftover.isEmpty()) { // We right clicked and was able to add 1 to DankNull
                    heldStack.shrink(1);
                    inventoryPlayer.setItemStack(heldStack);
                }
                if (player instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) player).updateHeldItem();
                }
            } else if (!slotStack.isEmpty()) { // Want to take stack out of DankNull
                int amount = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                if (dragType == 1) {
                    amount = Math.floorDiv(amount, 2);
                }
                final ItemStack newStack = slot.decrStackSize(amount);
                inventoryPlayer.setItemStack(newStack);
                if (player instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) player).updateHeldItem();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot clickSlot = getSlot(index);
        if (clickSlot == null || !clickSlot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }
        final ItemStack original = clickSlot.getStack().copy();
        if (clickSlot.getHasStack()) {
            // Shift click from Player Inventory
            if (!(clickSlot instanceof SlotDankNull)) {
                final ItemStack leftover = addStack(clickSlot.getStack());
                if (leftover.getCount() == original.getCount()) {
                    return ItemStack.EMPTY;
                }
                clickSlot.putStack(leftover);
                player.inventory.markDirty();
                return original;
            } else {
                final int slotIndex = clickSlot.getSlotIndex();
                final ItemStack slotStack = getHandler().extractItem(slotIndex,
                        getHandler().getFullStackInSlot(slotIndex).getMaxStackSize(), true);
                if (!getHandler().getTier().isCreative()) {
                    final IItemHandler playerHandler = new PlayerMainInvWrapper(player.inventory);
                    final ItemStack notAdded = ItemHandlerHelper.insertItemStacked(playerHandler, slotStack, false);
                    if (notAdded.getCount() < slotStack.getCount()) {
                        getHandler().extractItemIngoreExtractionMode(slotIndex,
                                slotStack.getCount() - notAdded.getCount(), false);
                        clickSlot.onSlotChanged();
                        // Click acknowledgements serialize the count as a byte.
                        // Return the extracted amount, not the potentially huge stored stack.
                        return ItemHandlerHelper.copyStackWithSize(slotStack,
                                slotStack.getCount() - notAdded.getCount());
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack addStack(final ItemStack stack) {
        ItemStack leftover = stack.copy();
        final IDankNullHandler handler = getHandler();
        if (handler instanceof DankNullHandler) {
            ((DankNullHandler) handler).sort();
        }
        boolean matchingSlot = false;
        // Sorting during insertion can move matching stacks to earlier slots.
        final ItemStack[] existingStacks = handler.getStackList().toArray(new ItemStack[0]);
        for (final ItemStack existing : existingStacks) {
            if (leftover.isEmpty()) {
                break;
            }
            if (existing.isEmpty()) {
                continue;
            }
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getFullStackInSlot(i) == existing && handler.isItemValid(i, leftover)) {
                    matchingSlot = true;
                    leftover = handler.insertItem(i, leftover, false);
                    break;
                }
            }
        }
        // GUI deposits void excess for an item already stored in the dank.
        if (matchingSlot) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < handler.getSlots() && !leftover.isEmpty(); i++) {
            if (handler.getFullStackInSlot(i).isEmpty() && handler.isItemValid(i, leftover)) {
                final ItemStack remainder = handler.insertItem(i, leftover, false);
                // Once registered in a slot, this item also voids excess at its limit.
                return remainder.getCount() < leftover.getCount() ? ItemStack.EMPTY : leftover;
            }
        }

        return leftover;
    }

    @Override
    public void updateProgressBar(final int id, final int value) {
        if (id == SORT_SELECTED_PROPERTY) {
            getHandler().setSelected(value);

            p455w0rd.danknull.DankNull.PROXY
                    .onInventoryCompacted(getHandler());
        } else if (id == SORT_MODE_PROPERTY) {
            final SortMode[] modes = SortMode.values();
            if (value >= 0 && value < modes.length) {
                getHandler().setSortMode(modes[value]);
                if (getHandler() instanceof DankNullHandler) {
                    ((DankNullHandler) getHandler()).useSortModeForAutomaticSorting(modes[value]);
                }
            }
        } else if (id == SORT_REVISION_PROPERTY) {
            p455w0rd.danknull.DankNull.PROXY.onInventoryCompacted(getHandler());
        } else {
            super.updateProgressBar(id, value);
        }
    }

    /**
     * Needed due to item stacks having a network limitation of size count byte
     */
    @Override
    public void detectAndSendChanges() {
        for (int i = 0; i < inventorySlots.size(); ++i) {
            final ItemStack slotStack = inventorySlots.get(i).getStack();
            ItemStack clientStack = inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(clientStack, slotStack)) {
                if (ItemStack.areItemStacksEqualUsingNBTShareTag(clientStack, slotStack)) {
                    continue;
                }

                clientStack = slotStack.isEmpty() ? ItemStack.EMPTY : slotStack.copy();
                inventoryItemStacks.set(i, clientStack);

                for (final IContainerListener listener : listeners) {
                    if (listener instanceof EntityPlayerMP) {
                        ModNetworking.getInstance().sendTo(new PacketUpdateSlot(i, clientStack),
                                (EntityPlayerMP) listener);
                    }
                }
            }
        }
        final IDankNullHandler handler = getHandler();
        final int revision = handler instanceof DankNullHandler
                ? ((DankNullHandler) handler).getSortRevision() : 0;
        if (handler.getSelected() != lastSelected || handler.getSortMode().ordinal() != lastSortMode
                || revision != lastSortRevision) {
            for (final IContainerListener listener : listeners) {
                sendSortState(listener);
            }
            lastSelected = handler.getSelected();
            lastSortMode = handler.getSortMode().ordinal();
            lastSortRevision = revision;
        }
    }

}
