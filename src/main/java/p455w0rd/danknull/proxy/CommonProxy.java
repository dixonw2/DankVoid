package p455w0rd.danknull.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.core.CommonProxyReplica;
import p455w0rd.danknull.core.GuiHandler;
import p455w0rd.danknull.init.*;
import p455w0rd.danknull.inventory.AutomaticSorting;
import p455w0rd.danknull.inventory.DankNullHandler;
import p455w0rd.danknull.inventory.cap.CapabilityDankNull;

public class CommonProxy {

    public void preInit(final FMLPreInitializationEvent e) {
        new CommonProxyReplica().preInit(e);

        ModDataFixing.registerWalkers();
        ModConfig.load();
        ModIntegration.preInit();
        ModNetworking.registerMessages();
        CapabilityDankNull.register();
    }

    public void init(final FMLInitializationEvent e) {
        ModDataFixing.registerFixes();
        ModIntegration.init();
    }

    public void postInit(final FMLPostInitializationEvent e) {
        GuiHandler.init();
    }

    public void serverStarting(final FMLServerStartingEvent e) {

    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public World getWorld() {
        return null;
    }

    public void onInventoryCompacted(IDankNullHandler handler) {
    }

    public boolean isShiftHeld(final DankNullHandler handler) {
        return AutomaticSorting.isShiftHeld(handler);
    }
}
