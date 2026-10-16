package p455w0rd.danknull.network;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.danknull.init.ModGlobals;

/**
 * Registers on both sides during item registration, before any connection can
 * use the channel.
 */
@Mod.EventBusSubscriber(modid = ModGlobals.MODID)
public final class BulkExtractionNetworking {

    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(ModGlobals.MODID + "_bulk");

    private BulkExtractionNetworking() {
    }

    @SubscribeEvent
    public static void registerMessages(final RegistryEvent.Register<Item> event) {
        CHANNEL.registerMessage(PacketBulkExtract.Handler.class, PacketBulkExtract.class, 0, Side.SERVER);
    }

    public static void requestExtraction(final int windowId, final int slotId) {
        CHANNEL.sendToServer(new PacketBulkExtract(windowId, slotId));
    }
}
