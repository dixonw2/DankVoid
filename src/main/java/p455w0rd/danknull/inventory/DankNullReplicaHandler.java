package p455w0rd.danknull.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import p455w0rd.danknull.items.DankNullTier;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rd.danknull.items.ItemDankNullReplica;
import p455w0rd.danknull.util.Tags;

public class DankNullReplicaHandler extends ItemStackHandler {

    // private static final String DATA_TAG = "DankNullCap";

    public final DankNullTier tier;

    private final ItemStack dank;
    private int selected = -1;
    private boolean needsInitialNBT = true;

    public DankNullReplicaHandler(final DankNullTier tier, final ItemStack dank) {
        super(tier.getNumSlots());
        this.tier = tier;
        this.dank = dank;
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(int value) {
        if (selected != value && value >= -1 && value < this.getSlots()) {
            this.selected = value;
            this.saveData();
        }
    }

    public boolean canInsertIntoSlot(final int slot, final ItemStack stack) {
        if (!this.isItemValid(slot, stack)) {
            return false;
        }

        for (int index = 0; index < this.getSlots(); index++) {
            if (index != slot
                    && ItemHandlerHelper.canItemStacksStack(this.getStackInSlot(index), stack)) {
                return false;
            }
        }

        return true;
    }

    public void loadInventory() {
        // Stack copies receive their regular NBT after capabilities are created
        if (this.needsInitialNBT && this.dank.hasTagCompound()
                && this.dank.getTagCompound().hasKey(Tags.DATA_TAG)) {
            this.needsInitialNBT = false;
            this.deserializeNBT(this.dank.getTagCompound().getCompoundTag(Tags.DATA_TAG));
        }
    }

    public ItemStack insertStack(final ItemStack stack, final boolean simulate) {
        int destination = -1;

        for (int slot = 0; slot < this.getSlots(); slot++) {
            final ItemStack stored = this.getStackInSlot(slot);

            if (ItemHandlerHelper.canItemStacksStack(stored, stack)) {
                destination = slot;
                break;
            }

            if (stored.isEmpty() && destination == -1) {
                destination = slot;
            }
        }

        if (destination == -1 || !this.isItemValid(destination, stack)) {
            return stack;
        }

        super.insertItem(destination, stack, simulate);
        return ItemStack.EMPTY;
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
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (simulate || amount <= 0) {
            return super.extractItem(slot, amount, simulate);
        }

        final ItemStack stored = this.getStackInSlot(slot);
        // if compacting isn't necessary
        // TODO: This will probably get removed/changed when auto sorting is implemented
        if (stored.isEmpty() || stored.getCount() > Math.min(amount, stored.getMaxStackSize())) {
            return super.extractItem(slot, amount, false);
        }

        this.stacks.set(slot, ItemStack.EMPTY);
        this.compactSlots();
        this.onContentsChanged(slot);
        return stored;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack,
            final boolean simulate) {
        if (!this.canInsertIntoSlot(slot, stack)) {
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
    protected void onContentsChanged(final int slot) {
        this.saveData();
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
            nbt.setTag(Tags.DANK_NULL_INVENTORY, items);
        }

        if (this.selected > -1) {
            nbt.setInteger(Tags.SELECTED_INDEX, this.selected);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        this.setSize(this.tier.getNumSlots());

        final NBTTagList items = nbt.getTagList(
                Tags.DANK_NULL_INVENTORY,
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

        this.selected = nbt.hasKey(Tags.SELECTED_INDEX, Constants.NBT.TAG_INT)
                ? nbt.getInteger(Tags.SELECTED_INDEX)
                : -1;

        this.compactSlots();
        this.onLoad();
    }

    private void compactSlots() {
        int destination = 0;
        int selectedDestination = -1;
        for (int slot = 0; slot < this.getSlots(); slot++) {
            final ItemStack stack = this.stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (slot != destination) {
                this.stacks.set(destination, stack);
                this.stacks.set(slot, ItemStack.EMPTY);
            }

            if (slot == this.selected) {
                selectedDestination = destination;
            }

            destination++;
        }

        this.selected = selectedDestination;
    }

    private void saveData() {
        this.needsInitialNBT = false;

        if (!this.dank.hasTagCompound()) {
            this.dank.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound root = this.dank.getTagCompound();
        final NBTTagCompound data = root.getCompoundTag(Tags.DATA_TAG).copy();

        // replace inventory data but preserve settings
        data.removeTag(Tags.DANK_NULL_INVENTORY);
        data.merge(this.serializeNBT());

        root.setTag(Tags.DATA_TAG, data);
    }
}
