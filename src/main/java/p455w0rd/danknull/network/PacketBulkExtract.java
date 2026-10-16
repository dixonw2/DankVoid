package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNull;
import p455w0rd.danknull.inventory.BulkExtraction;
import p455w0rd.danknull.inventory.slot.SlotDankNull;

import java.util.ArrayList;
import java.util.List;

/** The server derives the extracted item from the clicked slot, using its own inventory state. */
public class PacketBulkExtract implements IMessage {

    private int windowId;
    private int slotId;

    public PacketBulkExtract() {
    }

    public PacketBulkExtract(final int windowId, final int slotId) {
        this.windowId = windowId;
        this.slotId = slotId;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        windowId = buf.readInt();
        slotId = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(slotId);
    }

    public static class Handler implements IMessageHandler<PacketBulkExtract, IMessage> {
        @Override
        public IMessage onMessage(final PacketBulkExtract message, final MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                final EntityPlayerMP player = ctx.getServerHandler().player;
                final Container container = player.openContainer;
                if (container instanceof ContainerDankNull && container.windowId == message.windowId
                        && container.canInteractWith(player)) {
                    extractToPlayer((ContainerDankNull) container, player, message.slotId);
                }
            });
            return null;
        }

        private static void extractToPlayer(final ContainerDankNull container, final EntityPlayerMP player,
                final int slotId) {
            final Slot clicked = container.getSlot(slotId);
            final ItemStack cursor = player.inventory.getItemStack();
            if (!(clicked instanceof SlotDankNull) || !clicked.canTakeStack(player)
                    || cursor.isEmpty() || !clicked.getHasStack()) {
                return;
            }
            final ItemStack matchingStack = clicked.getStack().copy();
            matchingStack.setCount(1);

            final List<Slot> destinations = new ArrayList<>();
            for (final Slot slot : container.inventorySlots) {
                if (slot.inventory == player.inventory && slot.getSlotIndex() >= 0
                        && slot.getSlotIndex() < player.inventory.mainInventory.size()) {
                    destinations.add(slot);
                }
            }
            if (BulkExtraction.transferMatching(container.getHandler(), destinations, matchingStack) > 0) {
                player.inventory.markDirty();
                container.detectAndSendChanges();
            }
        }
    }
}
