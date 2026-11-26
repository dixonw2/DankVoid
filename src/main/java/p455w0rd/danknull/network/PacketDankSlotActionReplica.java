package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.inventory.DankNullReplicaHandler;

public class PacketDankSlotActionReplica implements IMessage {

    public enum Action {
        TOGGLE_SELECTION,
        CYCLE_EXTRACTION_MODE,
        CYCLE_PLACEMENT_MODE;

        private static final Action[] VALUES = values();
    }

    private int windowId;
    private int slot;
    private Action action;
    private boolean forward;

    public PacketDankSlotActionReplica() {
    }

    public PacketDankSlotActionReplica(final int windowId, final int slot, final Action action, final boolean forward) {
        this.windowId = windowId;
        this.slot = slot;
        this.action = action;
        this.forward = forward;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.windowId = buf.readInt();
        this.slot = buf.readInt();
        final int actionId = buf.readUnsignedByte();
        this.action = actionId < Action.VALUES.length ? Action.VALUES[actionId] : null;
        this.forward = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeInt(this.slot);
        buf.writeByte(this.action.ordinal());
        buf.writeBoolean(this.forward);
    }

    public static class Handler implements IMessageHandler<PacketDankSlotActionReplica, IMessage> {

        @Override
        public IMessage onMessage(final PacketDankSlotActionReplica message, final MessageContext ctx) {
            if (message.action == null) {
                return null;
            }

            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                final Container container = player.openContainer;
                if (!(container instanceof ContainerDankNullReplica) || container.windowId != message.windowId
                        || player.isSpectator() || !container.getCanCraft(player)
                        || !container.canInteractWith(player) || !player.inventory.getItemStack().isEmpty()) {
                    return;
                }

                final DankNullReplicaHandler handler = ((ContainerDankNullReplica) container).getHandler();
                if (message.slot < 0 || message.slot >= handler.getSlots()) {
                    return;
                }
                final ItemStack stack = handler.getStackInSlot(message.slot);
                if (stack.isEmpty()) {
                    return;
                }

                switch (message.action) {
                    case TOGGLE_SELECTION:
                        handler.setSelected(handler.getSelected() == message.slot ? -1 : message.slot);
                        break;
                    case CYCLE_EXTRACTION_MODE:
                        handler.cycleExtractionMode(stack, message.forward);
                        break;
                    case CYCLE_PLACEMENT_MODE:
                        handler.cyclePlacementMode(stack, message.forward);
                        break;
                }
            });
            return null;
        }
    }
}
