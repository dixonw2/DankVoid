package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;

public class PacketSelectSlotReplica implements IMessage {

    private int windowId;
    private int slot;

    public PacketSelectSlotReplica() {
    }

    public PacketSelectSlotReplica(final int windowId, final int slot) {
        this.windowId = windowId;
        this.slot = slot;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.windowId = buf.readInt();
        this.slot = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeInt(this.slot);
    }

    public static class Handler implements IMessageHandler<PacketSelectSlotReplica, IMessage> {

        @Override
        public IMessage onMessage(final PacketSelectSlotReplica message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                final Container container = player.openContainer;
                if (!(container instanceof ContainerDankNullReplica) || container.windowId != message.windowId
                        || player.isSpectator() || !container.getCanCraft(player)
                        || !container.canInteractWith(player) || !player.inventory.getItemStack().isEmpty()) {
                    return;
                }

                final DankNullReplicaHandler handler = ((ContainerDankNullReplica) container).getHandler();
                if (message.slot >= 0 && message.slot < handler.getSlots()
                        && !handler.getStackInSlot(message.slot).isEmpty()) {
                    handler.setSelected(handler.getSelected() == message.slot ? -1 : message.slot);
                }
            });
            return null;
        }
    }
}
