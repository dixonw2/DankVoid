package p455w0rd.danknull.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.client.gui.GuiDankNull;
import p455w0rd.danknull.init.ModCreativeTab;
import p455w0rd.danknull.init.ModIntegration;
import p455w0rd.danknull.init.ModItems;
import p455w0rd.danknull.init.ModKeyBindings;
import p455w0rd.danknull.inventory.DankNullHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public boolean isShiftHeld(final DankNullHandler handler) {
        return FMLCommonHandler.instance().getEffectiveSide().isClient()
                ? GuiScreen.isShiftKeyDown() : super.isShiftHeld(handler);
    }

    @Override
    public void preInit(final FMLPreInitializationEvent e) {
        super.preInit(e);
        ModItems.registerCustomRenderedItems();
        ModCreativeTab.init();
        ModKeyBindings.register();
    }

    @Override
    public void init(final FMLInitializationEvent e) {
        super.init(e);
    }

    @Override
    public void postInit(final FMLPostInitializationEvent e) {
        super.postInit(e);
        ModIntegration.postInit();
    }

    @Override
    public void serverStarting(final FMLServerStartingEvent e) {
        super.serverStarting(e);
    }

    @Override
    public World getWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void onInventoryCompacted(IDankNullHandler handler) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiDankNull) {
            GuiDankNull gui = (GuiDankNull) screen;

            if (gui.getDankNullHandler() == handler) {
                gui.resetDoubleClick();
            }
        }
    }
}
