package p455w0rd.danknull.inventory;

import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import p455w0rd.danknull.api.DankNullItemModes.ItemExtractionMode;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rd.danknull.init.ModGlobals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScrollSelectionTest {
    private DankNullHandler handler;
    private boolean previousSkipNonBlocks;

    @Before
    public void setup() {
        Bootstrap.register();
        previousSkipNonBlocks = Options.skipNonBlocksOnCycle;
        Options.skipNonBlocksOnCycle = false;
        handler = new DankNullHandler(ModGlobals.DankNullTier.REDSTONE);
    }

    @After
    public void restoreOptions() {
        Options.skipNonBlocksOnCycle = previousSkipNonBlocks;
    }

    private void extractSelectedSlot(final int slot) {
        final ItemStack stack = new ItemStack(new Item(), 64);
        handler.setStackInSlot(slot, stack);
        handler.setExtractionMode(stack, ItemExtractionMode.KEEP_NONE);
        handler.setSelected(slot);
        assertEquals(64, handler.extractItem(slot, 64, false).getCount());
        assertTrue(handler.getFullStackInSlot(slot).isEmpty());
    }

    @Test
    public void selectsOnlyRemainingStackAfterFullExtractionInEitherDirection() {
        handler.setStackInSlot(7, new ItemStack(new Item()));
        extractSelectedSlot(0);
        handler.cycleSelected(true);
        assertEquals(7, handler.getSelected());
        handler.setSelected(0);
        handler.cycleSelected(false);
        assertEquals(7, handler.getSelected());
    }

    @Test
    public void skipsEmptySlotsAndWrapsInBothDirections() {
        handler.setStackInSlot(2, new ItemStack(new Item()));
        handler.setStackInSlot(7, new ItemStack(new Item()));
        extractSelectedSlot(4);
        handler.cycleSelected(true);
        assertEquals(7, handler.getSelected());
        handler.cycleSelected(true);
        assertEquals(2, handler.getSelected());
        handler.cycleSelected(false);
        assertEquals(7, handler.getSelected());
        handler.setSelected(4);
        handler.cycleSelected(false);
        assertEquals(2, handler.getSelected());
    }

    @Test
    public void recoversUnsetSelectionInBothDirections() {
        handler.setStackInSlot(2, new ItemStack(new Item()));
        handler.setStackInSlot(7, new ItemStack(new Item()));
        handler.cycleSelected(true);
        assertEquals(2, handler.getSelected());
        handler.setSelected(-1);
        handler.cycleSelected(false);
        assertEquals(7, handler.getSelected());
    }

    @Test
    public void clearsSelectionWhenEverythingWasExtracted() {
        extractSelectedSlot(4);
        handler.cycleSelected(true);
        assertEquals(-1, handler.getSelected());
        handler.cycleSelected(false);
        assertEquals(-1, handler.getSelected());
    }

    @Test
    public void preservesBlockOnlyCyclingAfterFullExtraction() {
        Options.skipNonBlocksOnCycle = true;
        handler.setStackInSlot(2, new ItemStack(new Item()));
        handler.setStackInSlot(7, new ItemStack(Blocks.STONE));
        extractSelectedSlot(4);
        handler.cycleSelected(true);
        assertEquals(7, handler.getSelected());
        handler.setSelected(4);
        handler.cycleSelected(false);
        assertEquals(7, handler.getSelected());
    }

    @Test
    public void fallsBackToItemsWhenThereAreNoBlocks() {
        Options.skipNonBlocksOnCycle = true;
        handler.setStackInSlot(7, new ItemStack(new Item()));
        extractSelectedSlot(4);
        handler.cycleSelected(false);
        assertEquals(7, handler.getSelected());
    }
}
