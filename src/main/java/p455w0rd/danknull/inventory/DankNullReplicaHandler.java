package p455w0rd.danknull.inventory;

import net.minecraftforge.items.ItemStackHandler;
import p455w0rd.danknull.items.DankNullTier;

public class DankNullReplicaHandler extends ItemStackHandler {
    public DankNullReplicaHandler(final DankNullTier tier) {
        super(tier.getNumSlots());
    }
}
