package p455w0rd.danknull.inventory;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import p455w0rd.danknull.api.IDankNullHandler;

import java.util.List;

/**
 * Transfers matching stored items into ordinary inventory slots without
 * touching the cursor.
 */
public final class BulkExtraction {

    private BulkExtraction() {
    }

    public static int transferMatching(final IDankNullHandler source, final List<Slot> destinations,
            final ItemStack matchingStack) {
        if (matchingStack.isEmpty() || source.getTier().isCreative()) {
            return 0;
        }

        int transferred = 0;
        // Top up existing stacks before using empty slots.
        for (int pass = 0; pass < 2; pass++) {
            for (final Slot destination : destinations) {
                ItemStack current = destination.getStack();
                if ((pass == 0 && current.isEmpty()) || (pass == 1 && !current.isEmpty())
                        || !destination.isItemValid(matchingStack)
                        || (!current.isEmpty() && !matches(current, matchingStack))) {
                    continue;
                }

                final int limit = Math.min(destination.getItemStackLimit(matchingStack),
                        matchingStack.getMaxStackSize());
                int space = limit - current.getCount();
                for (int sourceSlot = 0; sourceSlot < source.getSlots() && space > 0;) {
                    if (!matches(source.getFullStackInSlot(sourceSlot), matchingStack)) {
                        sourceSlot++;
                        continue;
                    }

                    // Remove first: only items actually extracted can be credited to the player.
                    // extractItem applies the configured keep/lock extraction rules.
                    final ItemStack extracted = source.extractItem(sourceSlot, space, false);
                    if (extracted.isEmpty()) {
                        sourceSlot++;
                        continue;
                    }

                    final ItemStack updated = current.isEmpty() ? extracted.copy() : current.copy();
                    if (!current.isEmpty()) {
                        updated.grow(extracted.getCount());
                    }
                    destination.putStack(updated);
                    current = updated;
                    space -= extracted.getCount();
                    transferred += extracted.getCount();
                    // Extraction can compact later stacks into earlier positions.
                    sourceSlot = 0;
                }
            }
        }
        return transferred;
    }

    public static boolean matches(final ItemStack first, final ItemStack second) {
        return !first.isEmpty() && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }
}
