package p455w0rd.danknull.integration;

import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import p455w0rd.danknull.container.ContainerDankNull;
import p455w0rd.danknull.inventory.BulkExtraction;
import p455w0rd.danknull.inventory.slot.SlotDankNull;
import p455w0rd.danknull.network.PacketInventoryMove.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes BOGO's move actions through the dank's real insertion/extraction
 * paths.
 */
public final class BogoInventoryMoves {
    private BogoInventoryMoves() {
    }

    public static void execute(final ContainerDankNull container, final EntityPlayerMP player,
            final int slotId, final Action action) {
        final Slot clicked = container.getSlot(slotId);
        if (clicked == null || !canTake(clicked, player) || !clicked.getHasStack()) {
            return;
        }

        final ItemStack selected = clicked.getStack().copy();
        final boolean fromDank = clicked instanceof SlotDankNull;
        if ((fromDank && action == Action.MOVE_ALL)
                || (!fromDank && !isPlayerSlot(clicked, player))) {
            return;
        }

        if (fromDank) {
            moveToPlayer(container, player, selected, action);
        } else {
            final boolean fromHotbar = clicked.getSlotIndex() < 9;
            for (final Slot source : container.inventorySlots) {
                if (!isPlayerSlot(source, player) || !canTake(source, player)
                        || (source.getSlotIndex() < 9) != fromHotbar
                        || !source.getHasStack()
                        || (action == Action.MOVE_SINGLE && source != clicked)
                        || (action == Action.MOVE_SIMILAR
                                && !ItemStack.areItemsEqual(source.getStack(), selected))) {
                    continue;
                }

                final ItemStack original = source.getStack().copy();
                final int amount = action == Action.MOVE_SINGLE ? 1 : original.getCount();
                final ItemStack remainder = container.addStack(
                        ItemHandlerHelper.copyStackWithSize(original, amount));
                final int moved = amount - remainder.getCount();
                if (moved > 0) {
                    source.putStack(ItemHandlerHelper.copyStackWithSize(
                            original, original.getCount() - moved));
                    source.onTake(player, ItemHandlerHelper.copyStackWithSize(original, moved));
                }
            }
        }

        player.inventory.markDirty();
        container.detectAndSendChanges();
        player.updateHeldItem();
    }

    private static void moveToPlayer(final ContainerDankNull container, final EntityPlayerMP player,
            final ItemStack selected, final Action action) {
        // Snapshot item identities, not slot indices: each extraction may sort or
        // compact the dank.
        final List<ItemStack> types = new ArrayList<>();
        for (final Slot source : container.inventorySlots) {
            if (!(source instanceof SlotDankNull) || !source.getHasStack()
                    || (action == Action.MOVE_SINGLE && !BulkExtraction.matches(source.getStack(), selected))
                    || (action == Action.MOVE_SIMILAR
                            && !ItemStack.areItemsEqual(source.getStack(), selected))) {
                continue;
            }
            final ItemStack stack = source.getStack();
            if (types.stream().noneMatch(type -> BulkExtraction.matches(type, stack))) {
                types.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
            }
        }

        int remaining = action == Action.MOVE_SINGLE ? 1 : Integer.MAX_VALUE;
        for (final ItemStack type : types) {
            // Merge into existing player stacks before filling empty slots.
            for (int pass = 0; pass < 2 && remaining > 0; pass++) {
                for (final Slot destination : container.inventorySlots) {
                    if (!isPlayerSlot(destination, player) || isLocked(destination, player)
                            || !destination.isItemValid(type)) {
                        continue;
                    }
                    ItemStack current = destination.getStack();
                    if ((pass == 0 && current.isEmpty()) || (pass == 1 && !current.isEmpty())
                            || (!current.isEmpty() && !BulkExtraction.matches(current, type))) {
                        continue;
                    }

                    final int limit = Math.min(type.getMaxStackSize(), destination.getItemStackLimit(type));
                    int space = Math.min(remaining, limit - current.getCount());
                    for (int sourceIndex = 0; sourceIndex < container.inventorySlots.size() && space > 0;) {
                        final Slot source = container.inventorySlots.get(sourceIndex);
                        if (!(source instanceof SlotDankNull) || !canTake(source, player)
                                || !BulkExtraction.matches(source.getStack(), type)) {
                            sourceIndex++;
                            continue;
                        }

                        final ItemStack extracted = source.decrStackSize(space);
                        if (extracted.isEmpty()) {
                            sourceIndex++;
                            continue;
                        }
                        final ItemStack updated = current.isEmpty() ? extracted.copy() : current.copy();
                        if (!current.isEmpty()) {
                            updated.grow(extracted.getCount());
                        }
                        destination.putStack(updated);
                        source.onTake(player, extracted);
                        current = updated;
                        space -= extracted.getCount();
                        remaining -= extracted.getCount();
                        sourceIndex = 0;
                    }
                }
            }
        }
    }

    private static boolean isPlayerSlot(final Slot slot, final EntityPlayerMP player) {
        return slot.inventory == player.inventory && slot.getSlotIndex() >= 0
                && slot.getSlotIndex() < player.inventory.mainInventory.size();
    }

    private static boolean canTake(final Slot slot, final EntityPlayerMP player) {
        return !isLocked(slot, player) && slot.canTakeStack(player);
    }

    private static boolean isLocked(final Slot slot, final EntityPlayerMP player) {
        return isPlayerSlot(slot, player)
                && LockSlotCapability.getForPlayer(player).isSlotLocked(slot.getSlotIndex());
    }
}
