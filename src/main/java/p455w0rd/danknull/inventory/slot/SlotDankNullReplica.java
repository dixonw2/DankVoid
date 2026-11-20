package p455w0rd.danknull.inventory.slot;

import java.util.function.Supplier;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;

public class SlotDankNullReplica extends SlotItemHandler {

    private final Supplier<DankNullReplicaHandler> handler;

    public SlotDankNullReplica(final Supplier<DankNullReplicaHandler> handler,
            final int index, final int x, final int y) {
        super(handler.get(), index, x, y);
        this.handler = handler;
    }

    @Override
    public DankNullReplicaHandler getItemHandler() {
        return this.handler.get();
    }

    @Override
    public boolean isItemValid(final ItemStack stack) {
        return this.getItemHandler().isItemValid(this.getSlotIndex(), stack);
    }

    @Override
    public int getSlotStackLimit() {
        return this.getItemHandler().getSlotLimit(this.getSlotIndex());
    }

    @Override
    public boolean isSameInventory(final Slot other) {
        return other instanceof SlotItemHandler
                && ((SlotItemHandler) other).getItemHandler() == this.getItemHandler();
    }

    @Override
    public void onSlotChanged() {
        this.getItemHandler().markSlotChanged(this.getSlotIndex());
    }
}
