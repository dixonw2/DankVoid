package p455w0rd.danknull.core;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import p455w0rd.danknull.core.config.Config;
import p455w0rd.danknull.network.PacketHandler;

public class CommonProxyReplica {

    public void preInit(final FMLPreInitializationEvent e) {
        Config.load(e.getModConfigurationDirectory());
        PacketHandler.init();
    }

    public void init(final FMLInitializationEvent e) {
    }

    public void postInit(final FMLPostInitializationEvent e) {
    }

    public void serverStarting(final FMLServerStartingEvent e) {
    }
}
