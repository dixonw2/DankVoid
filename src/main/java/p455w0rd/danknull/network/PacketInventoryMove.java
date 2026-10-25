package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNull;
import p455w0rd.danknull.integration.BogoInventoryMoves;

/**
 * The server derives the source item and destination from the open container.
 */
public class PacketInventoryMove implements IMessage {
    public enum Action {
        MOVE_ALL, MOVE_SIMILAR, MOVE_SINGLE
    }

    private int windowId;
    private int slotId;
    private Action action;

    public PacketInventoryMove() {
    }

    public PacketInventoryMove(final int windowId, final int slotId, final Action action) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.action = action;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        windowId = buf.readInt();
        slotId = buf.readInt();
        final int actionId = buf.readUnsignedByte();
        final Action[] actions = Action.values();
        action = actionId < actions.length ? actions[actionId] : null;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(slotId);
        buf.writeByte(action.ordinal());
    }

    public static class Handler implements IMessageHandler<PacketInventoryMove, IMessage> {
        @Override
        public IMessage onMessage(final PacketInventoryMove message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                final Container container = player.openContainer;
                if (message.action != null && !player.isSpectator() && Loader.isModLoaded("bogosorter")
                        && container instanceof ContainerDankNull
                        && container.windowId == message.windowId
                        && container.canInteractWith(player)
                        && player.inventory.getItemStack().isEmpty()) {
                    BogoInventoryMoves.execute((ContainerDankNull) container,
                            player, message.slotId, message.action);
                }
            });
            return null;
        }
    }
}
