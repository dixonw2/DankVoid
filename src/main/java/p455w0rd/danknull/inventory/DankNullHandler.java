package p455w0rd.danknull.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.danknull.api.DankNullItemModes.ItemExtractionMode;
import p455w0rd.danknull.api.DankNullItemModes.ItemPlacementMode;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.init.ModConfig;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rd.danknull.init.ModGlobals;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rdslib.util.ItemUtils;

import javax.annotation.Nonnull;
import java.util.*;

import p455w0rd.danknull.api.IDankNullHandler.SortMode;

/**
 * @author BrockWS, FleetingDream
 */
public class DankNullHandler implements IDankNullHandler {

    public int selected;
    public boolean isLocked;
    private ModGlobals.DankNullTier tier;
    private NonNullList<ItemStack> stacks;
    private Map<ItemStack, Boolean> oreStacks;
    private Map<ItemStack, ItemExtractionMode> extractionStacks;
    private Map<ItemStack, ItemPlacementMode> placementStacks;
    private SortMode sortMode;
    private boolean sortModeInitialized;
    // Tracks preference changes only; sortMode is the sole active sorting mode.
    private SortMode lastConfiguredDefaultMode = SortMode.INSERTION;
    private boolean automaticSorting;
    private boolean automaticSortPending;
    private boolean automaticSortWaitsForShiftRelease;
    private boolean insertingPickup;
    private int sortRevision;
    private long inventoryRevision;
    private final long[] insertionOrder;
    private long nextInsertionOrder;
    // SlotItemHandler temporarily clears and restores slots when checking insertion
    // limits.
    private final ItemStack[] probedStacks;
    private final long[] probedInsertionOrder;

    public DankNullHandler(final ModGlobals.DankNullTier tier) {
        this.tier = tier;
        stacks = NonNullList.withSize(this.tier.getNumRows() * 9, ItemStack.EMPTY);
        insertionOrder = new long[stacks.size()];
        Arrays.fill(insertionOrder, -1);
        probedStacks = new ItemStack[stacks.size()];
        probedInsertionOrder = new long[stacks.size()];
        oreStacks = new HashMap<>();
        extractionStacks = new HashMap<>();
        placementStacks = new HashMap<>();
        selected = -1;
        isLocked = false;
        sortMode = SortMode.INSERTION;
    }

    @Override
    public SortMode getSortMode() {
        return sortMode;
    }

    @Override
    public void setSortMode(final SortMode mode) {
        Objects.requireNonNull(mode, "mode");
        if (!sortModeInitialized || sortMode != mode) {
            sortModeInitialized = true;
            sortMode = mode;
            onDataChanged();
        }
    }

    public void configureAutomaticSorting(final SortMode mode, final boolean enabled) {
        Objects.requireNonNull(mode, "mode");
        final boolean effectiveEnabled = enabled && supportsAutomaticSorting();
        final boolean initializeMode = !sortModeInitialized;
        if (initializeMode || lastConfiguredDefaultMode != mode || automaticSorting != effectiveEnabled) {
            if (initializeMode || lastConfiguredDefaultMode != mode) {
                sortMode = mode;
                sortModeInitialized = true;
            }
            lastConfiguredDefaultMode = mode;
            automaticSorting = effectiveEnabled;
            requestAutomaticSort(false);
            onDataChanged();
        }
    }

    public boolean isAutomaticSorting() {
        return automaticSorting;
    }

    protected boolean supportsAutomaticSorting() {
        return true;
    }

    public SortMode getAutomaticSortMode() {
        return sortMode;
    }

    public void useSortModeForAutomaticSorting(final SortMode mode) {
        if (automaticSorting) {
            setSortMode(mode);
        }
    }

    private void inventoryChanged(final boolean inserted) {
        inventoryRevision++;
        if (inserted) {
            if (automaticSorting && !insertingPickup) {
                // Complete insertion sorting before returning or publishing the inventory.
                automaticSortPending = false;
                automaticSortWaitsForShiftRelease = false;
                sortContents(sortMode);
            }
        } else if (automaticSorting) {
            if (DankNull.PROXY.isShiftHeld(this)) {
                requestAutomaticSort(true);
            } else {
                // Finish extraction sorting before the caller updates the cursor.
                automaticSortPending = false;
                automaticSortWaitsForShiftRelease = false;
                sortContents(sortMode);
            }
        }
    }

    /** Collect into existing stacks without sorting between their slot indices. */
    public ItemStack insertPickedUpItem(final ItemStack stack) {
        ItemStack remainder = stack;
        final long previousRevision = inventoryRevision;
        final List<ItemStack> matchingStacks = new ArrayList<>();
        for (final int slot : findItemStacks(stack)) {
            matchingStacks.add(stacks.get(slot));
        }
        insertingPickup = true;
        try {
            for (final ItemStack stored : matchingStacks) {
                if (remainder.isEmpty()) {
                    break;
                }
                final int slot = stacks.indexOf(stored);
                if (slot >= 0) {
                    remainder = insertItem(slot, remainder, false);
                }
            }
        } finally {
            insertingPickup = false;
        }
        if (inventoryRevision != previousRevision && automaticSorting
                && sortMode == SortMode.QUANTITY) {
            sortContents(SortMode.QUANTITY);
        }
        return remainder;
    }

    private void requestAutomaticSort(final boolean waitForShiftRelease) {
        if (automaticSorting) {
            automaticSortPending = true;
            automaticSortWaitsForShiftRelease = waitForShiftRelease;
            AutomaticSorting.enqueue(this);
        }
    }

    boolean shouldWaitForShiftRelease() {
        return automaticSortPending && automaticSortWaitsForShiftRelease;
    }

    public void processAutomaticSort() {
        if (automaticSortPending) {
            automaticSortPending = false;
            automaticSortWaitsForShiftRelease = false;
            if (automaticSorting) {
                sortContents(sortMode);
            }
        }
    }

    public long getInventoryRevision() {
        return inventoryRevision;
    }

    public int getSortRevision() {
        return sortRevision;
    }

    public void writeSortingNBT(final NBTTagCompound tag) {
        ensureInsertionOrder();
        tag.setString("SortMode", sortMode.name());
        tag.setBoolean("SortModeInitialized", sortModeInitialized);
        tag.removeTag("AutoSortMode");
        tag.setString("AutoSortDefault", lastConfiguredDefaultMode.name());
        tag.setBoolean("AutoSort", automaticSorting);
        final NBTTagList order = new NBTTagList();
        for (final long sequence : insertionOrder) {
            order.appendTag(new NBTTagLong(sequence));
        }
        tag.setTag("InsertionOrder", order);
    }

    public void readSortingNBT(final NBTTagCompound tag) {
        sortMode = readSortMode(tag.getString("SortMode"));
        sortModeInitialized = tag.hasKey("SortModeInitialized")
                ? tag.getBoolean("SortModeInitialized")
                : tag.hasKey("SortMode");
        lastConfiguredDefaultMode = tag.hasKey("AutoSortDefault")
                ? readSortMode(tag.getString("AutoSortDefault"))
                : sortMode;
        automaticSorting = tag.getBoolean("AutoSort") && supportsAutomaticSorting();
        automaticSortPending = false;
        automaticSortWaitsForShiftRelease = false;
        Arrays.fill(insertionOrder, -1);
        Arrays.fill(probedStacks, null);
        nextInsertionOrder = 0;
        final NBTTagList order = tag.getTagList("InsertionOrder", 4);
        for (int slot = 0; slot < Math.min(order.tagCount(), insertionOrder.length); slot++) {
            insertionOrder[slot] = ((NBTPrimitive) order.get(slot)).getLong();
        }
    }

    private static SortMode readSortMode(final String name) {
        try {
            return SortMode.fromName(name);
        } catch (IllegalArgumentException e) {
            return SortMode.INSERTION;
        }
    }

    public void ensureInsertionOrder() {
        long highest = -1;
        for (final long sequence : insertionOrder) {
            highest = Math.max(highest, sequence);
        }
        if (highest >= Long.MAX_VALUE - stacks.size() - 1
                || nextInsertionOrder >= Long.MAX_VALUE - stacks.size() - 1) {
            final List<Integer> occupied = new ArrayList<>();
            for (int slot = 0; slot < stacks.size(); slot++) {
                if (!stacks.get(slot).isEmpty()) {
                    occupied.add(slot);
                }
            }
            occupied.sort(
                    Comparator.comparingLong(slot -> insertionOrder[slot] < 0 ? Long.MAX_VALUE : insertionOrder[slot]));
            Arrays.fill(insertionOrder, -1);
            nextInsertionOrder = 0;
            for (final int slot : occupied) {
                insertionOrder[slot] = nextInsertionOrder++;
            }
            Arrays.fill(probedStacks, null);
        } else {
            nextInsertionOrder = Math.max(nextInsertionOrder, highest + 1);
        }
        final Set<Long> seen = new HashSet<>();
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (stacks.get(slot).isEmpty()) {
                insertionOrder[slot] = -1;
            } else if (insertionOrder[slot] < 0 || !seen.add(insertionOrder[slot])) {
                insertionOrder[slot] = nextInsertionOrder++;
                seen.add(insertionOrder[slot]);
            }
        }
    }

    public static List<String> getOreNames(final ItemStack stack) {
        final int[] oreIds = OreDictionary.getOreIDs(stack);
        if (oreIds.length > 0) {
            final List<String> nameList = new ArrayList<>();
            for (final int oreId : oreIds) {
                final String name = OreDictionary.getOreName(oreId);
                if (!name.equals("Unknown") && ModConfig.isValidOre(name)) {
                    nameList.add(name);
                }
            }
            return nameList;
        }
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public ItemStack getExtractableStackInSlot(final int slot) {
        validateSlot(slot);
        ItemStack slotStack = getStackList().get(slot).copy();
        if (!slotStack.isEmpty()) {
            if (getExtractionMode(slotStack) == ItemExtractionMode.KEEP_ALL) {
                slotStack = ItemStack.EMPTY;
            } else {
                final int amountToBeKept = getExtractionMode(slotStack).getNumberToKeep();
                if (slotStack.getCount() > amountToBeKept) {
                    final ItemStack availableStack = slotStack.copy();
                    availableStack.setCount(slotStack.getCount() - amountToBeKept);
                    return availableStack;
                }
                return ItemStack.EMPTY;
            }
        }
        return slotStack;
    }

    @Override
    public ItemStack getFullStackInSlot(final int slot) {
        validateSlot(slot);
        return getStackList().get(slot);
    }

    @Nonnull
    @Override
    public ItemStack getRenderableStackForSlot(final int slot) {
        final ItemStack visualStack = getStackInSlot(slot).copy();
        if (!visualStack.isEmpty()) {
            visualStack.setCount(1);
        }
        return visualStack;
    }

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack) {
        validateSlot(slot);
        ensureInsertionOrder();
        final ItemStack previous = stacks.get(slot);
        if (stack.isEmpty()) {
            if (!previous.isEmpty()) {
                probedStacks[slot] = previous;
                probedInsertionOrder[slot] = insertionOrder[slot];
            }
            insertionOrder[slot] = -1;
        } else if (previous.isEmpty() && probedStacks[slot] == stack) {
            insertionOrder[slot] = probedInsertionOrder[slot];
            probedStacks[slot] = null;
        } else if (previous.isEmpty() || !ItemHandlerHelper.canItemStacksStack(previous, stack)) {
            insertionOrder[slot] = nextInsertionOrder++;
            probedStacks[slot] = null;
        }
        getStackList().set(slot, stack);
        // Raw slot writes are used by network snapshots and slot simulations.
        // Compact only after a real insertion/extraction or a complete GUI action.
        onDataChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, final boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        stack = convertOreDict(stack);
        ItemStack existingStack = getStackList().get(slot);

        if (existingStack.isEmpty()) {
            final int inserted = Math.min(stack.getCount(), tier.getMaxStackSize());
            if (!simulate) {
                ensureInsertionOrder();
                insertionOrder[slot] = nextInsertionOrder++;
                Arrays.fill(probedStacks, null);
                getStackList().set(slot, ItemHandlerHelper.copyStackWithSize(stack, inserted));
                compactSlots();
                inventoryChanged(true);
                onDataChanged();
            }
            return inserted == stack.getCount() ? ItemStack.EMPTY
                    : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted);
        }

        if (!ItemHandlerHelper.canItemStacksStack(stack, existingStack)) {
            return stack;
        }

        final int available = Math.max(0, tier.getMaxStackSize() - existingStack.getCount());
        final int inserted = Math.min(stack.getCount(), available);
        if (inserted == 0) {
            return stack;
        }
        final int newInternalCount = existingStack.getCount() + inserted;
        final int returnCount = stack.getCount() - inserted;

        if (!simulate) {
            existingStack.setCount(newInternalCount);
            compactSlots();
            inventoryChanged(true);
            onDataChanged();
        }

        if (returnCount == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack returnStack = stack.copy();
        returnStack.setCount(returnCount);
        return returnStack;
    }

    @Override
    public boolean isOreDictFiltered(final ItemStack stack) {
        for (final ItemStack storedStack : getStackList()) {
            if (!storedStack.isEmpty() && oreMatches(storedStack, stack)) {
                return true;
            }
        }
        return false;
    }

    private ItemStack convertOreDict(final ItemStack incomingStack) {
        for (final ItemStack storedStack : getStackList()) {
            if (isOre(storedStack) && !isOre(incomingStack) && oreMatches(storedStack, incomingStack)) {
                final ItemStack newStack = storedStack.copy();
                newStack.setCount(incomingStack.getCount());
                return newStack;
            }
        }
        return incomingStack;
    }

    private boolean oreMatches(final ItemStack storedStack, final ItemStack incomingStack) {
        final List<String> oreNamesForStoredStack = getOreNames(storedStack);
        final List<String> oreNamesForIncomingStack = getOreNames(incomingStack);
        for (final String currentStoredName : oreNamesForStoredStack) {
            if (ModConfig.isValidOre(currentStoredName)) {
                for (final String currentIncomingName : oreNamesForIncomingStack) {
                    if (ModConfig.isValidOre(currentIncomingName)) {
                        if (currentIncomingName.equals(currentStoredName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (amount < 1) {
            return ItemStack.EMPTY;
        }
        validateSlot(slot);

        final ItemStack existing = getStackList().get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final int existingCount = existing.getCount();
        int requiredToKeep = getExtractionMode(existing).getNumberToKeep();
        final int extract = Math.min(Math.min(amount, existing.getMaxStackSize()), existingCount - requiredToKeep);
        if (extract < 1) {
            return ItemStack.EMPTY;
        }
        if (existingCount <= extract) {
            if (!simulate) {
                insertionOrder[slot] = -1;
                Arrays.fill(probedStacks, null);
                getStackList().set(slot, ItemStack.EMPTY);
                compactSlots();
                inventoryChanged(false);
                onDataChanged();
            }
            return existing;
        } else {
            if (!simulate) {
                getStackList().set(slot, ItemHandlerHelper.copyStackWithSize(existing, existingCount - extract));
                compactSlots();
                inventoryChanged(false);
                onDataChanged();
            }
            return ItemHandlerHelper.copyStackWithSize(existing, extract);
        }
    }

    @Override
    public boolean containsItemStack(@Nonnull final ItemStack stack) {
        return findItemStack(stack) > -1;
    }

    @Override
    public int findItemStack(@Nonnull final ItemStack stack) {
        for (int i = 0; i < getStackList().size(); i++) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(getStackList().get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ImmutableList<Integer> findItemStacks(@Nonnull ItemStack stack) {
        ImmutableList.Builder<Integer> results = ImmutableList.builder();
        for (int i = 0; i < getStackList().size(); i++) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(getStackList().get(i), stack)) {
                results.add(i);
            }
        }
        return results.build();
    }

    @Override
    public int stackCount() {
        int count = 0;
        for (final ItemStack stack : getStackList()) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Nonnull
    @Override
    public ModGlobals.DankNullTier getTier() {
        return tier;
    }

    @Override
    public int getSelected() {
        return selected;
    }

    @Override
    public void setSelected(final int slot) {
        selected = Ints.constrainToRange(slot, -1, stacks.size() - 1);
        onDataChanged();
    }

    @Override
    public void cycleSelected(final boolean forward) {
        final List<Integer> blockSlots = getBlockStacksSlots();
        if (Options.skipNonBlocksOnCycle) {
            final int numBlockSlots = blockSlots.size();
            if (numBlockSlots > 0) {
                if (blockSlots.size() == 1) {
                    if (getSelected() != blockSlots.get(0)) {
                        setSelected(blockSlots.get(0));
                    }
                    return;
                } else {
                    final int current = getSelected();
                    if (!blockSlots.contains(current)) {
                        setSelected(blockSlots.get(0));
                        return;
                    }
                    final int min = 0;
                    final int max = blockSlots.size() - 1;
                    final int currentIndex = getBlockSlotIndex(current);
                    if (forward) {
                        if (currentIndex != max) {
                            setSelected(blockSlots.get(currentIndex + 1));
                            return;
                        }
                        setSelected(blockSlots.get(0));
                    } else {
                        if (currentIndex != min) {
                            setSelected(blockSlots.get(currentIndex - 1));
                            return;
                        }
                        setSelected(blockSlots.get(max));
                    }
                    return;
                }
            }
        }
        final int current = getSelected();
        final int slotCount = getSlots();
        final int start = current >= 0 && current < slotCount ? current : (forward ? -1 : 0);
        for (int step = 1; step <= slotCount; step++) {
            final int newIndex = Math.floorMod(start + (forward ? step : -step), slotCount);
            if (!getFullStackInSlot(newIndex).isEmpty()) {
                if (current != newIndex) {
                    setSelected(newIndex);
                }
                return;
            }
        }
        if (current != -1) {
            setSelected(-1);
        }
    }

    private int getBlockSlotIndex(final int slot) {
        for (int i = 0; i < getBlockStacksSlots().size(); i++) {
            if (getBlockStacksSlots().get(i) == slot) {
                return i;
            }
        }
        return -1;
    }

    private List<Integer> getBlockStacksSlots() {
        final List<Integer> blockStackSlots = Lists.newArrayList();
        for (int i = 0; i < getSlots(); i++) {
            final ItemStack fullStack = getFullStackInSlot(i);
            if (!fullStack.isEmpty() && fullStack.getItem() instanceof ItemBlock) {
                blockStackSlots.add(i);
            }
        }
        Collections.sort(blockStackSlots);
        return blockStackSlots;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void setLocked(final boolean lock) {
        isLocked = lock;
        onDataChanged();
    }

    @Override
    public boolean isLockingSupported() {
        return tier.isCreative();
    }

    @Override
    public void setOre(@Nonnull ItemStack stack, final boolean ore) {
        if (stack.isEmpty()) {
            return;
        }
        for (final ItemStack currentStack : oreStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                oreStacks.put(currentStack, ore);
                onDataChanged();
                return;
            }
        }
        stack = stack.copy();
        stack.setCount(1);
        oreStacks.put(stack, ore);
        onDataChanged();
    }

    @Override
    public boolean isOre(@Nonnull final ItemStack stack) {
        for (final ItemStack currentStack : oreStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                return oreStacks.get(currentStack);
            }
        }
        return false;
    }

    @Override
    public boolean isOreSupported(@Nonnull final ItemStack stack) {
        return OreDictionary.getOreIDs(stack).length > 0;
    }

    @Nonnull
    @Override
    public Map<ItemStack, Boolean> getOres() {
        return oreStacks;
    }

    @Override
    public void setExtractionMode(@Nonnull ItemStack stack, @Nonnull final ItemExtractionMode mode) {
        if (stack.isEmpty()) {
            return;
        }
        for (final ItemStack currentStack : extractionStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                extractionStacks.put(currentStack, mode);
                onDataChanged();
                return;
            }
        }
        stack = stack.copy();
        stack.setCount(1);
        extractionStacks.put(stack, mode);
        onDataChanged();
    }

    @Override
    public void cycleExtractionMode(@Nonnull final ItemStack stack, final boolean forward) {
        ItemExtractionMode current = getExtractionMode(stack);
        final ItemExtractionMode[] values = ItemExtractionMode.values();
        if (forward) {
            if (current.ordinal() == values.length - 1) {
                current = values[0];
            } else {
                current = values[current.ordinal() + 1];
            }
        } else {
            if (current.ordinal() == 0) {
                current = values[values.length - 1];
            } else {
                current = values[current.ordinal() - 1];
            }
        }
        setExtractionMode(stack, current);
    }

    @Nonnull
    @Override
    public ItemExtractionMode getExtractionMode(@Nonnull final ItemStack stack) {
        for (final ItemStack currentStack : extractionStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                return extractionStacks.get(currentStack);
            }
        }
        return ItemExtractionMode.KEEP_1;
    }

    @Nonnull
    @Override
    public Map<ItemStack, ItemExtractionMode> getExtractionModes() {
        return extractionStacks;
    }

    @Override
    public void setPlacementMode(@Nonnull ItemStack stack, @Nonnull final ItemPlacementMode mode) {
        if (stack.isEmpty()) {
            return;
        }
        for (final ItemStack currentStack : placementStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                placementStacks.put(currentStack, mode);
                onDataChanged();
                return;
            }
        }
        stack = stack.copy();
        stack.setCount(1);
        placementStacks.put(stack, mode);
        onDataChanged();
    }

    @Override
    public void cyclePlacementMode(@Nonnull final ItemStack stack, final boolean forward) {
        ItemPlacementMode current = this.getPlacementMode(stack);
        final ItemPlacementMode[] values = ItemPlacementMode.values();
        if (forward) {
            if (current.ordinal() == values.length - 1) {
                current = values[0];
            } else {
                current = values[current.ordinal() + 1];
            }
        } else {
            if (current.ordinal() == 0) {
                current = values[values.length - 1];
            } else {
                current = values[current.ordinal() - 1];
            }
        }
        setPlacementMode(stack, current);
    }

    @Nonnull
    @Override
    public ItemPlacementMode getPlacementMode(@Nonnull final ItemStack stack) {
        for (final ItemStack currentStack : placementStacks.keySet()) {
            if (ItemUtils.areItemStacksEqualIgnoreSize(currentStack, stack)) {
                return placementStacks.get(currentStack);
            }
        }
        return ItemPlacementMode.KEEP_1;
    }

    @Override
    public Map<ItemStack, ItemPlacementMode> getPlacementMode() {
        return placementStacks;
    }

    @Override
    public int getSlots() {
        return getStackList().size();
    }

    @Override
    public NonNullList<ItemStack> getStackList() {
        return stacks;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return tier.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        final boolean isSame = ItemStack.areItemsEqual(getFullStackInSlot(slot), stack)
                && ItemStack.areItemStackTagsEqual(getFullStackInSlot(slot), stack);
        return !(stack.getItem() instanceof ItemDankNull) && (getFullStackInSlot(slot).isEmpty() || isSame);
    }

    // TODO: rename this
    public void sort() {
        if (compactSlots()) {
            onDataChanged();
        }
    }

    private boolean compactSlots() {
        ensureInsertionOrder();
        final int oldSelected = selected;
        int movedSelected = -1;
        int writeSlot = 0;
        boolean changed = false;
        for (int readSlot = 0; readSlot < stacks.size(); readSlot++) {
            final ItemStack stack = stacks.get(readSlot);
            if (stack.isEmpty()) {
                continue;
            }
            if (readSlot == oldSelected) {
                movedSelected = writeSlot;
            }
            if (readSlot != writeSlot) {
                stacks.set(writeSlot, stack);
                stacks.set(readSlot, ItemStack.EMPTY);
                insertionOrder[writeSlot] = insertionOrder[readSlot];
                insertionOrder[readSlot] = -1;
                changed = true;
            }
            writeSlot++;
        }
        if (movedSelected >= 0) {
            selected = movedSelected;
        } else {
            selected = writeSlot == 0 ? -1 : Math.min(Math.max(oldSelected, 0), writeSlot - 1);
        }

        if (changed) {
            Arrays.fill(probedStacks, null);
            // reset the double click window
            DankNull.PROXY.onInventoryCompacted(this);
        }

        return changed || oldSelected != selected;
    }

    public void sortContents() {
        // A manual request takes precedence over an earlier queued automatic sort.
        automaticSortPending = false;
        sortContents(sortMode);
    }

    private Comparator<ItemStack> sortingComparator(final SortMode mode) {
        final Comparator<ItemStack> byName = Comparator.comparing(
                ItemStack::getDisplayName,
                String.CASE_INSENSITIVE_ORDER);

        final Comparator<ItemStack> byItem = Comparator
                .comparing((ItemStack stack) -> Objects.toString(stack.getItem().getRegistryName(), ""))
                .thenComparingInt(ItemStack::getMetadata);

        final Comparator<ItemStack> comparator;

        switch (mode) {
            case INSERTION:
                // Insertion compares the separate slot sequence below.
                comparator = (left, right) -> 0;
                break;
            case NAME:
                comparator = byName.thenComparing(byItem);
                break;

            case QUANTITY:
                comparator = Comparator
                        .comparingInt(ItemStack::getCount)
                        .reversed()
                        .thenComparing(byName)
                        .thenComparing(byItem);
                break;

            case MOD:
                comparator = Comparator
                        .comparing((ItemStack stack) -> stack.getItem().getRegistryName().getNamespace())
                        .thenComparing(byName)
                        .thenComparing(byItem);
                break;

            default:
                throw new IllegalArgumentException("Unknown sort mode: " + mode);
        }

        return comparator;
    }

    private boolean isSorted(final SortMode mode, final Comparator<ItemStack> comparator) {
        int previousSlot = -1;
        boolean emptySlotSeen = false;
        for (int slot = 0; slot < stacks.size(); slot++) {
            final ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                emptySlotSeen = true;
            } else {
                if (emptySlotSeen || previousSlot >= 0 && (mode == SortMode.INSERTION
                        ? Long.compare(insertionOrder[previousSlot], insertionOrder[slot])
                        : comparator.compare(stacks.get(previousSlot), stack)) > 0) {
                    return false;
                }
                previousSlot = slot;
            }
        }
        return true;
    }

    private void sortContents(final SortMode mode) {
        if (mode == SortMode.INSERTION) {
            ensureInsertionOrder();
        }
        final Comparator<ItemStack> comparator = sortingComparator(mode);
        if (isSorted(mode, comparator)) {
            // Selection still needs repair even when the item order is already correct.
            if (selected < 0 || selected >= stacks.size() || stacks.get(selected).isEmpty()) {
                final int count = stackCount();
                final int newSelected = count == 0 ? -1 : Math.min(Math.max(selected, 0), count - 1);
                if (selected != newSelected) {
                    setSelected(newSelected);
                }
            }
            return;
        }
        if (mode != SortMode.INSERTION) {
            ensureInsertionOrder();
        }

        // Sort slot indices before changing the inventory.
        final List<Integer> order = new ArrayList<>();

        for (int slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                order.add(slot);
            }
        }

        order.sort((left, right) -> mode == SortMode.INSERTION
                ? Long.compare(insertionOrder[left], insertionOrder[right])
                : comparator.compare(stacks.get(left), stacks.get(right)));

        final List<ItemStack> sorted = new ArrayList<>(order.size());
        final long[] sortedInsertionOrder = new long[order.size()];
        int newSelected = -1;
        boolean moved = false;

        for (int newSlot = 0; newSlot < order.size(); newSlot++) {
            final int oldSlot = order.get(newSlot);
            sorted.add(stacks.get(oldSlot));
            sortedInsertionOrder[newSlot] = insertionOrder[oldSlot];

            if (oldSlot == selected) {
                newSelected = newSlot;
            }

            if (oldSlot != newSlot) {
                moved = true;
            }
        }

        // Match the existing compaction behavior for an empty selection.
        if (newSelected == -1 && !sorted.isEmpty()) {
            newSelected = Math.min(
                    Math.max(selected, 0), sorted.size() - 1);
        }

        if (!moved && selected == newSelected) {
            return;
        }

        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, slot < sorted.size()
                    ? sorted.get(slot)
                    : ItemStack.EMPTY);
            insertionOrder[slot] = slot < sortedInsertionOrder.length
                    ? sortedInsertionOrder[slot]
                    : -1;
        }

        selected = newSelected;

        if (moved) {
            Arrays.fill(probedStacks, null);
            sortRevision = (sortRevision + 1) & 0x7fff;
            DankNull.PROXY.onInventoryCompacted(this);
        }

        onDataChanged();
    }

    public void updateSelectedSlot() {
        sort();
    }

    protected void onDataChanged() {
    }

}
