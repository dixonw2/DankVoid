package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.api.IDankNullHandler.SortMode;
import p455w0rd.danknull.inventory.AutomaticSorting;

public class PacketSortPreferences implements IMessage {
    private String modeName;
    private boolean enabled;
    private boolean shiftHeld;

    public PacketSortPreferences() {
    }

    public PacketSortPreferences(final SortMode mode, final boolean enabled, final boolean shiftHeld) {
        modeName = mode.name();
        this.enabled = enabled;
        this.shiftHeld = shiftHeld;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        modeName = ByteBufUtils.readUTF8String(buf);
        enabled = buf.readBoolean();
        shiftHeld = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, modeName);
        buf.writeBoolean(enabled);
        buf.writeBoolean(shiftHeld);
    }

    public static class Handler implements IMessageHandler<PacketSortPreferences, IMessage> {
        @Override
        public IMessage onMessage(final PacketSortPreferences message, final MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                final SortMode mode;
                try {
                    mode = SortMode.fromName(message.modeName);
                } catch (IllegalArgumentException e) {
                    return;
                }
                AutomaticSorting.setPreferences(ctx.getServerHandler().player,
                        mode, message.enabled, message.shiftHeld);
            });
            return null;
        }
    }
}
