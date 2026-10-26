package p455w0rd.danknull.init;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.integration.BogoSorter;
import p455w0rd.danknull.integration.ItemScroller;
import p455w0rd.danknull.integration.TOP;
import p455w0rd.danknull.integration.WAILA;
import p455w0rdslib.LibGlobals.Mods;

import net.minecraftforge.fml.common.event.FMLInterModComms;
import p455w0rd.danknull.inventory.slot.SlotDankNull;
import p455w0rd.danknull.inventory.slot.SlotDankNullDock;

/**
 * @author p455w0rd
 */
public class ModIntegration {

    public static void preInit() {
        if (Mods.TOP.isLoaded()) {
            TOP.init();
        } else {
            DankNull.LOGGER.info(Mods.TOP.getName() + " Integation: Disabled");
        }

        // forcefully blacklist dank in cpw's Inventory Sorter to prevent dank
        // corruption
        FMLInterModComms.sendMessage(
                "inventorysorter",
                "slotblacklist",
                SlotDankNull.class.getName());

        FMLInterModComms.sendMessage(
                "inventorysorter",
                "slotblacklist",
                SlotDankNullDock.class.getName());
    }

    public static void init() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (Mods.WAILA.isLoaded()) {
                WAILA.init();
            } else {
                DankNull.LOGGER.info("Waila Integation: Disabled");
            }
        }
    }

    public static void postInit() {
        if (Mods.ITEMSCROLLER.isLoaded()) {
            ItemScroller.blackListSlots();
        }
        if (Loader.isModLoaded("bogosorter") && Loader.isModLoaded("modularui")) {
            BogoSorter.init();
        }
    }

}
