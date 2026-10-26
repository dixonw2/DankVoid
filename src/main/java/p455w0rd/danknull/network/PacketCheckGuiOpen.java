package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.container.ContainerDankNullItem;
import p455w0rd.danknull.init.ModGuiHandler;
import p455w0rd.danknull.init.ModGuiHandler.GUIType;
import p455w0rd.danknull.items.ItemDankNull;

public class PacketCheckGuiOpen implements IMessage {

    private EnumHand hand;

    public PacketCheckGuiOpen() {
    }

    public PacketCheckGuiOpen(final EnumHand hand) {
        this.hand = hand;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        hand = buf.readBoolean() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeBoolean(hand == EnumHand.OFF_HAND);
    }

    public static class Handler implements IMessageHandler<PacketCheckGuiOpen, IMessage> {

        @Override
        public IMessage onMessage(final PacketCheckGuiOpen message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                if (!player.isSneaking()
                        || !(player.getHeldItem(message.hand).getItem() instanceof ItemDankNull)) {
                    return;
                }

                final double reach = Math.max(0D,
                        player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()
                                - (player.capabilities.isCreativeMode ? 0D : 0.5D));
                final Vec3d start = player.getPositionEyes(1F);
                final Vec3d look = player.getLook(1F);
                final Vec3d end = start.add(
                        look.x * reach, look.y * reach, look.z * reach);
                final RayTraceResult hit = player.world.rayTraceBlocks(
                        start, end, false, false, true);

                if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                    return;
                }

                final Container previousContainer = player.openContainer;
                ModGuiHandler.launchGui(
                        GUIType.DANKNULL, player, player.world, player.getPosition());

                if (player.openContainer != previousContainer
                        && player.openContainer instanceof ContainerDankNullItem) {
                    player.swingArm(message.hand);
                    player.connection.sendPacket(new SPacketAnimation(
                            player, message.hand == EnumHand.MAIN_HAND ? 0 : 3));
                }
            });
            return null;
        }
    }
}
