package p455w0rd.danknull.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.danknull.DankNull;

public final class PacketHandler {

    private static int id = 0;

    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(DankNull.MOD_ID + "_replica");

    public static void init() {
        CHANNEL.registerMessage(PacketUpdateSlotReplica.Handler.class,
                PacketUpdateSlotReplica.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketDankSlotActionReplica.Handler.class,
                PacketDankSlotActionReplica.class, id++, Side.SERVER);
    }

    public static void sendTo(final EntityPlayerMP player, final IMessage message) {
        CHANNEL.sendTo(message, player);
    }

    public static void sendToServer(final IMessage message) {
        CHANNEL.sendToServer(message);
    }
}
