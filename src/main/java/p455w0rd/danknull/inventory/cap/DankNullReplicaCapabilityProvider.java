package p455w0rd.danknull.inventory.cap;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.items.DankNullTier;

public class DankNullReplicaCapabilityProvider implements ICapabilityProvider {

    private final DankNullReplicaHandler inventory;

    public DankNullReplicaCapabilityProvider(
            final DankNullTier tier, final ItemStack dank) {
        this.inventory = new DankNullReplicaHandler(tier, dank);
    }

    @Override
    public boolean hasCapability(final Capability<?> capability,
            @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(final Capability<T> capability,
            @Nullable final EnumFacing facing) {
        if (!this.hasCapability(capability, facing)) {
            return null;
        }

        this.inventory.loadInventory();
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
    }
}
