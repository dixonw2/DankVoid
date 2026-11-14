package p455w0rd.danknull.inventory.slot;

import net.minecraftforge.items.SlotItemHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;

public class SlotDankNullReplica extends SlotItemHandler {
    public SlotDankNullReplica(final DankNullReplicaHandler handler, final int index, final int x, final int y) {
        super(handler, index, x, y);
    }

    @Override
    public DankNullReplicaHandler getItemHandler() {
        return (DankNullReplicaHandler) super.getItemHandler();
    }

    @Override
    public void onSlotChanged() {
        this.getItemHandler().markSlotChanged(this.getSlotIndex());
    }
}
