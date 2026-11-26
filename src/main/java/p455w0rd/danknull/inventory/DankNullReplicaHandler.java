package p455w0rd.danknull.inventory;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

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

    public final DankNullTier tier;

    private final ItemStack dank;
    // TODO: Should extraction/placement modes be combined? Why have different modes because they're logically both extracting?
    private final Map<ItemStack, ItemMode> extractionModes = new LinkedHashMap<>();
    private final Map<ItemStack, ItemMode> placementModes = new LinkedHashMap<>();
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

    public ItemMode getExtractionMode(final ItemStack stack) {
        return getMode(this.extractionModes, stack);
    }

    public ItemMode getPlacementMode(final ItemStack stack) {
        return getMode(this.placementModes, stack);
    }

    public void cycleExtractionMode(final ItemStack stack, final boolean forward) {
        this.cycleMode(this.extractionModes, stack, forward);
    }

    public void cyclePlacementMode(final ItemStack stack, final boolean forward) {
        this.cycleMode(this.placementModes, stack, forward);
    }

    private void cycleMode(final Map<ItemStack, ItemMode> modes, final ItemStack stack, final boolean forward) {
        final Map.Entry<ItemStack, ItemMode> entry = findMode(modes, stack);
        if (entry == null) {
            modes.put(ItemHandlerHelper.copyStackWithSize(stack, 1), ItemMode.ALL.cycle(forward));
        } else {
            entry.setValue(entry.getValue().cycle(forward));
        }
        this.saveData();
    }

    private static ItemMode getMode(final Map<ItemStack, ItemMode> modes, final ItemStack stack) {
        final Map.Entry<ItemStack, ItemMode> entry = findMode(modes, stack);
        return entry == null ? ItemMode.ALL : entry.getValue();
    }

    @Nullable
    private static Map.Entry<ItemStack, ItemMode> findMode(final Map<ItemStack, ItemMode> modes, final ItemStack stack) {
        for (final Map.Entry<ItemStack, ItemMode> entry : modes.entrySet()) {
            if (ItemHandlerHelper.canItemStacksStack(entry.getKey(), stack)) {
                return entry;
            }
        }
        return null;
    }

    public boolean canInsertIntoSlot(final int slot, final ItemStack stack) {
        if (!this.isItemValid(slot, stack)) {
            return false;
        }

        this.validateSlotIndex(slot);
        if (!this.isOccupiedOrFirstEmpty(slot)) {
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

    public ItemStack insertAndVoidOverflow(final ItemStack stack) {
        int destination = -1;

        for (int slot = 0; slot < this.getSlots(); slot++) {
            final ItemStack stored = this.getStackInSlot(slot);

            if (stored.isEmpty() || ItemHandlerHelper.canItemStacksStack(stored, stack)) {
                destination = slot;
                break;
            }
        }

        if (destination == -1 || !this.isItemValid(destination, stack)) {
            return stack;
        }

        super.insertItem(destination, stack, false);
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
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        final ItemStack stored = this.getStackInSlot(slot);
        if (stored.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final int available = stored.getCount() - this.getExtractionMode(stored).getKeepCount();
        final int toExtract = Math.min(Math.min(amount, stored.getMaxStackSize()), available);
        if (toExtract <= 0) {
            return ItemStack.EMPTY;
        }
        if (simulate || toExtract < stored.getCount()) {
            return super.extractItem(slot, toExtract, simulate);
        }

        this.setStackInSlot(slot, ItemStack.EMPTY);
        return stored;
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack) {
        this.validateSlotIndex(slot);
        if (!stack.isEmpty()) {
            if (!this.isOccupiedOrFirstEmpty(slot)) {
                throw new IllegalArgumentException("Cannot leave an empty slot before slot " + slot);
            }
            super.setStackInSlot(slot, stack);
            return;
        }
        if (this.stacks.get(slot).isEmpty()) {
            return;
        }

        final boolean removedSelected = slot == this.selected;
        this.stacks.set(slot, ItemStack.EMPTY);
        this.compactSlots();
        if (removedSelected) {
            this.selected = this.stacks.get(slot).isEmpty() ? slot - 1 : slot;
        }
        this.onContentsChanged(slot);
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

    private boolean isOccupiedOrFirstEmpty(final int slot) {
        return slot == 0 || !this.stacks.get(slot - 1).isEmpty();
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

        writeModes(nbt, Tags.EXTRACTION_MODES, this.extractionModes);
        writeModes(nbt, Tags.PLACEMENT_MODES, this.placementModes);
        nbt.setInteger(Tags.SELECTED_INDEX, this.selected);
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

        readModes(nbt, Tags.EXTRACTION_MODES, this.extractionModes);
        readModes(nbt, Tags.PLACEMENT_MODES, this.placementModes);
        this.compactSlots();
        this.onLoad();
    }

    private static void writeModes(final NBTTagCompound nbt, final String key, final Map<ItemStack, ItemMode> modes) {
        if (modes.isEmpty()) {
            return;
        }

        final NBTTagList entries = new NBTTagList();
        for (final Map.Entry<ItemStack, ItemMode> mode : modes.entrySet()) {
            final NBTTagCompound entry = new NBTTagCompound();
            entry.setTag(Tags.STACK, mode.getKey().serializeNBT());
            entry.setInteger(Tags.MODE, mode.getValue().savedId);
            entries.appendTag(entry);
        }
        nbt.setTag(key, entries);
    }

    private static void readModes(final NBTTagCompound nbt, final String key, final Map<ItemStack, ItemMode> modes) {
        modes.clear();
        final NBTTagList entries = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
        for (int index = 0; index < entries.tagCount(); index++) {
            final NBTTagCompound entry = entries.getCompoundTagAt(index);
            final ItemStack stack = new ItemStack(entry.getCompoundTag(Tags.STACK));
            if (stack.isEmpty() || !entry.hasKey(Tags.MODE, Constants.NBT.TAG_INT)) {
                continue;
            }

            final ItemMode mode = ItemMode.fromSavedId(entry.getInteger(Tags.MODE));
            final Map.Entry<ItemStack, ItemMode> existing = findMode(modes, stack);
            if (existing == null) {
                modes.put(ItemHandlerHelper.copyStackWithSize(stack, 1), mode);
            } else {
                existing.setValue(mode);
            }
        }
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

        // Replace the handler's data while preserving other settings.
        data.removeTag(Tags.DANK_NULL_INVENTORY);
        data.removeTag(Tags.EXTRACTION_MODES);
        data.removeTag(Tags.PLACEMENT_MODES);
        data.merge(this.serializeNBT());

        root.setTag(Tags.DATA_TAG, data);
    }

    public enum ItemMode {
        ALL(4, 0),
        ALL_BUT_1(1, 1),
        ALL_BUT_16(2, 16),
        ALL_BUT_64(3, 64),
        NONE(0, Integer.MAX_VALUE);

        private static final ItemMode[] VALUES = values();

        // Saved IDs match the legacy KEEP_* enum ordinals.
        private final int savedId;
        private final int keepCount;

        ItemMode(final int savedId, final int keepCount) {
            this.savedId = savedId;
            this.keepCount = keepCount;
        }

        public int getKeepCount() {
            return this.keepCount;
        }

        private ItemMode cycle(final boolean forward) {
            return VALUES[Math.floorMod(this.ordinal() + (forward ? 1 : -1), VALUES.length)];
        }

        private static ItemMode fromSavedId(final int id) {
            for (final ItemMode mode : VALUES) {
                if (mode.savedId == id) {
                    return mode;
                }
            }
            return ALL;
        }
    }
}
