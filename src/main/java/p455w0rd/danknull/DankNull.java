package p455w0rd.danknull;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import p455w0rd.danknull.proxy.CommonProxy;

@Mod(modid = DankNull.MOD_ID, name = DankNull.NAME, version = DankNull.VERSION, dependencies = DankNull.DEPENDENCIES, guiFactory = DankNull.GUI_FACTORY, acceptedMinecraftVersions = "[1.12.2]", certificateFingerprint = "@FINGERPRINT@")
public class DankNull {

    public static final String MOD_ID = "danknull";
    public static final String NAME = "/dank/void";
    public static final String VERSION = "@VERSION@";

    public static final String DEPENDENCIES = "required-after:p455w0rdslib@[2.3.161,);"
            + "after:stg;after:jei;after:waila;after:theoneprobe;after:nei;after:bogosorter;after:modularui";
    public static final String CLIENT_PROXY = "p455w0rd.danknull.proxy.ClientProxy";
    public static final String SERVER_PROXY = "p455w0rd.danknull.proxy.CommonProxy";
    public static final String GUI_FACTORY = "p455w0rd.danknull.init.ModGuiFactory";

    public static final Logger LOG = LogManager.getLogger(DankNull.MOD_ID);

    @SidedProxy(clientSide = DankNull.CLIENT_PROXY, serverSide = DankNull.SERVER_PROXY)
    public static CommonProxy PROXY;

    @Mod.Instance(DankNull.MOD_ID)
    public static DankNull INSTANCE;

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        PROXY.init(event);
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        PROXY.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(final FMLServerStartingEvent event) {
        PROXY.serverStarting(event);
    }
}
