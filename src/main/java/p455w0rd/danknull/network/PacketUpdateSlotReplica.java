package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNullReplica;

public class PacketUpdateSlotReplica implements IMessage {

    private int windowId;
    private int slotId;
    private ItemStack stack;

    public PacketUpdateSlotReplica() {
    }

    public PacketUpdateSlotReplica(final int windowId, final int slotId, final ItemStack stack) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.stack = stack;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.windowId = buf.readInt();
        this.slotId = buf.readInt();
        this.stack = ByteBufUtils.readItemStack(buf);
        this.stack.setCount(buf.readInt());
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeInt(this.slotId);
        final ItemStack serialized = this.stack.copy();
        // The vanilla stack codec stores counts in a byte.
        serialized.setCount(1);
        ByteBufUtils.writeItemStack(buf, serialized);
        buf.writeInt(this.stack.getCount());
    }

    public static class Handler implements IMessageHandler<PacketUpdateSlotReplica, IMessage> {

        @Override
        public IMessage onMessage(final PacketUpdateSlotReplica message, final MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                final Minecraft minecraft = Minecraft.getMinecraft();
                if (minecraft.player == null) {
                    return;
                }
                final Container container = minecraft.player.openContainer;
                if (!(container instanceof ContainerDankNullReplica)
                        || container.windowId != message.windowId
                        || message.slotId < 0 || message.slotId >= container.inventorySlots.size()) {
                    return;
                }
                container.putStackInSlot(message.slotId, message.stack);
            });
            return null;
        }
    }
}
