package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.api.IDankNullHandler.SortMode;
import p455w0rd.danknull.container.ContainerDankNull;
import p455w0rd.danknull.inventory.DankNullHandler;
import p455w0rd.danknull.inventory.AutomaticSorting;

public class PacketSort implements IMessage {

    private int windowId;
    private String modeName;
    private boolean automaticSorting;

    public PacketSort() {
    }

    public PacketSort(final int windowId, final SortMode mode, final boolean automaticSorting) {
        this.windowId = windowId;
        this.modeName = mode.name();
        this.automaticSorting = automaticSorting;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        windowId = buf.readInt();
        modeName = ByteBufUtils.readUTF8String(buf);
        automaticSorting = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(windowId);
        ByteBufUtils.writeUTF8String(buf, modeName);
        buf.writeBoolean(automaticSorting);
    }

    public static class Handler
            implements IMessageHandler<PacketSort, IMessage> {

        @Override
        public IMessage onMessage(
                final PacketSort message,
                final MessageContext ctx) {

            FMLCommonHandler.instance()
                    .getWorldThread(ctx.netHandler)
                    .addScheduledTask(() -> {
                        final EntityPlayerMP player =
                                ctx.getServerHandler().player;
                        final Container open = player.openContainer;

                        if (!player.isEntityAlive()
                                || !(open instanceof ContainerDankNull)
                                || open.windowId != message.windowId
                                || !open.canInteractWith(player)
                                || !player.inventory.getItemStack().isEmpty()) {
                            return;
                        }

                        final SortMode mode;

                        try {
                            mode = SortMode.fromName(message.modeName);
                        } catch (IllegalArgumentException e) {
                            return;
                        }

                        final ContainerDankNull container =
                                (ContainerDankNull) open;

                        if (!(container.getHandler()
                                instanceof DankNullHandler)) {
                            return;
                        }

                        AutomaticSorting.updateSortSettings(player, mode, message.automaticSorting);
                        container.sortNextMode(mode, message.automaticSorting);

                        player.inventory.markDirty();
                        container.detectAndSendChanges();
                    });

            return null;
        }
    }
}
