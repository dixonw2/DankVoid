package p455w0rd.danknull.inventory.cap;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;
import p455w0rd.danknull.items.DankNullTier;

public class DankNullReplicaCapabilityProvider
        implements ICapabilitySerializable<NBTTagCompound> {

    private final DankNullReplicaHandler inventory;

    public DankNullReplicaCapabilityProvider(final DankNullTier tier) {
        this.inventory = new DankNullReplicaHandler(tier);
    }

    // #region Capability
    @Override
    public boolean hasCapability(final Capability<?> capability,
            @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(final Capability<T> capability,
            @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory)
                : null;
    }

    // #endregion

    // #region NBT
    @Override
    public NBTTagCompound serializeNBT() {
        return this.inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        this.inventory.deserializeNBT(nbt);
    }
    // #endregion
}