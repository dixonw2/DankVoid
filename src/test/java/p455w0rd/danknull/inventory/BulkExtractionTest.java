package p455w0rd.danknull.inventory;

import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Before;
import org.junit.Test;
import p455w0rd.danknull.api.DankNullItemModes.ItemExtractionMode;
import p455w0rd.danknull.init.ModGlobals;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BulkExtractionTest {

    private DankNullHandler source;
    private InventoryBasic inventory;
    private List<Slot> destinations;
    private Item item;

    @Before
    public void setup() {
        Bootstrap.register();
        source = new DankNullHandler(ModGlobals.DankNullTier.EMERALD);
        inventory = new InventoryBasic("test", false, 36);
        destinations = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            destinations.add(new Slot(inventory, slot, 0, 0));
        }
        item = new Item();
        source.setExtractionMode(new ItemStack(item), ItemExtractionMode.KEEP_NONE);
    }

    @Test
    public void fillsInventoryAndLeavesExcessInSource() {
        source.setStackInSlot(0, new ItemStack(item, 5000));
        final ItemStack cursor = new ItemStack(item, 7);

        assertEquals(2304, BulkExtraction.transferMatching(source, destinations, cursor));
        assertEquals(2696, source.getFullStackInSlot(0).getCount());
        assertEquals(7, cursor.getCount());
        for (int slot = 0; slot < 36; slot++) {
            assertEquals(64, inventory.getStackInSlot(slot).getCount());
        }
    }

    @Test
    public void topsUpPartialStackBeforeUsingEmptySlots() {
        inventory.setInventorySlotContents(20, new ItemStack(item, 60));
        source.setStackInSlot(0, new ItemStack(item, 10));

        assertEquals(10, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        assertEquals(64, inventory.getStackInSlot(20).getCount());
        assertEquals(6, inventory.getStackInSlot(0).getCount());
        assertTrue(source.getFullStackInSlot(0).isEmpty());
    }

    @Test
    public void fullInventoryDoesNotRemoveAnything() {
        final Item other = new Item();
        for (int slot = 0; slot < 36; slot++) {
            inventory.setInventorySlotContents(slot, new ItemStack(other, 64));
        }
        source.setStackInSlot(0, new ItemStack(item, 500));

        assertEquals(0, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        assertEquals(500, source.getFullStackInSlot(0).getCount());
    }

    @Test
    public void respectsKeepLimitsAcrossMatchingSourceSlots() {
        source.setExtractionMode(new ItemStack(item), ItemExtractionMode.KEEP_16);
        source.setStackInSlot(0, new ItemStack(item, 100));
        source.setStackInSlot(1, new ItemStack(item, 30));

        assertEquals(98, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        assertEquals(16, source.getFullStackInSlot(0).getCount());
        assertEquals(16, source.getFullStackInSlot(1).getCount());
    }

    @Test
    public void keepAllPreventsExtraction() {
        source.setExtractionMode(new ItemStack(item), ItemExtractionMode.KEEP_ALL);
        source.setStackInSlot(0, new ItemStack(item, 100));

        assertEquals(0, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        assertEquals(100, source.getFullStackInSlot(0).getCount());
    }

    @Test
    public void onlyExtractsExactMetadataAndNbtMatches() {
        source.setStackInSlot(0, new ItemStack(item, 20));
        source.setStackInSlot(1, new ItemStack(item, 30, 1));
        final ItemStack named = new ItemStack(item, 40);
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setString("test", "different");
        named.setTagCompound(tag);
        source.setStackInSlot(2, named);

        assertEquals(20, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        assertEquals(30, source.getFullStackInSlot(1).getCount());
        assertEquals(40, source.getFullStackInSlot(2).getCount());
    }

    @Test
    public void respectsUnstackableItemLimits() {
        item.setMaxStackSize(1);
        source.setStackInSlot(0, new ItemStack(item, 3));

        assertEquals(3, BulkExtraction.transferMatching(source, destinations, new ItemStack(item)));
        for (int slot = 0; slot < 3; slot++) {
            assertEquals(1, inventory.getStackInSlot(slot).getCount());
        }
    }

    @Test
    public void creativeTierDoesNotGenerateItems() {
        final DankNullHandler creative = new DankNullHandler(ModGlobals.DankNullTier.CREATIVE);
        creative.setStackInSlot(0, new ItemStack(item, 100));

        assertEquals(0, BulkExtraction.transferMatching(creative, destinations, new ItemStack(item)));
        assertTrue(inventory.isEmpty());
        assertEquals(100, creative.getFullStackInSlot(0).getCount());
    }
}
