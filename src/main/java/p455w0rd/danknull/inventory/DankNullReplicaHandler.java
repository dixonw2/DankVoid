package p455w0rd.danknull.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import p455w0rd.danknull.common.ModConstants;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rd.danknull.items.ItemDankNullReplica;

public class DankNullReplicaHandler extends ItemStackHandler {

    private static final String DATA_TAG = "DankNullCap";

    public final DankNullTier tier;
    private final ItemStack dank;
    private boolean needsInitialNBT = true;

    public DankNullReplicaHandler(final DankNullTier tier, final ItemStack dank) {
        super(tier.getNumSlots());
        this.tier = tier;
        this.dank = dank;
    }

    public void loadInventory() {
        // Stack copies receive their regular NBT after capabilities are created
        if (this.needsInitialNBT && this.dank.hasTagCompound()
                && this.dank.getTagCompound().hasKey(DATA_TAG)) {
            this.needsInitialNBT = false;
            this.deserializeNBT(this.dank.getTagCompound().getCompoundTag(DATA_TAG));
        }
    }

    @Override
    protected void onContentsChanged(final int slot) {
        this.needsInitialNBT = false;

        if (!this.dank.hasTagCompound()) {
            this.dank.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound root = this.dank.getTagCompound();
        final NBTTagCompound data = root.getCompoundTag(DATA_TAG).copy();

        // replace inventory data but preserve settings
        data.removeTag(ModConstants.NBT.DANKNULL_INVENTORY);
        data.merge(this.serializeNBT());

        root.setTag(DATA_TAG, data);
    }

    @Override
    public int getSlotLimit(final int slot) {
        return this.tier.getEffectiveMaxStackSize();
    }

    @Override
    protected int getStackLimit(final int slot, final ItemStack stack) {
        return this.getSlotLimit(slot);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack,
            final boolean simulate) {
        if (!this.isItemValid(slot, stack)) {
            return stack;
        }

        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof ItemDankNullReplica)
                && !(stack.getItem() instanceof ItemDankNull);
    }

    public void markSlotChanged(final int slot) {
        this.validateSlotIndex(slot);
        this.onContentsChanged(slot);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        final NBTTagList items = new NBTTagList();

        for (int slot = 0; slot < this.getSlots(); slot++) {
            final ItemStack stored = this.stacks.get(slot);
            if (stored.isEmpty()) {
                continue;
            }

            final ItemStack copy = stored.copy();
            copy.setCount(1);

            final NBTTagCompound item = new NBTTagCompound();
            copy.writeToNBT(item);
            item.setInteger("Slot", slot);
            item.setInteger("Count", stored.getCount());

            items.appendTag(item);
        }

        if (!items.isEmpty()) {
            nbt.setTag(ModConstants.NBT.DANKNULL_INVENTORY, items);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        this.setSize(this.tier.getNumSlots());

        final NBTTagList items = nbt.getTagList(
                ModConstants.NBT.DANKNULL_INVENTORY,
                Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); i++) {
            final NBTTagCompound item = items.getCompoundTagAt(i);
            final int slot = item.getInteger("Slot");

            if (slot >= 0 && slot < this.getSlots()) {
                final ItemStack stack = new ItemStack(item);
                stack.setCount(item.getInteger("Count"));
                this.stacks.set(slot, stack);
            }
        }

        this.onLoad();
    }
}
