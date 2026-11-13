package p455w0rd.danknull.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rd.danknull.items.ItemDankNullReplica;

public class DankNullReplicaHandler extends ItemStackHandler {
    public DankNullReplicaHandler(final DankNullTier tier) {
        super(tier.getNumSlots());
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof ItemDankNullReplica)
                && !(stack.getItem() instanceof ItemDankNull);
    }
}
