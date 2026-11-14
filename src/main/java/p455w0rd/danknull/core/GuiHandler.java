package p455w0rd.danknull.core;

import static p455w0rd.danknull.inventory.PlayerSlot.EnumInvCategory.MAIN;
import static p455w0rd.danknull.inventory.PlayerSlot.EnumInvCategory.OFF_HAND;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.blocks.tiles.TileDankNullDock;
import p455w0rd.danknull.client.gui.GuiDankNull;
import p455w0rd.danknull.client.gui.GuiDankNullReplica;
import p455w0rd.danknull.container.ContainerDankNullDock;
import p455w0rd.danknull.container.ContainerDankNullItem;
import p455w0rd.danknull.container.ContainerDankNullReplica;
import p455w0rd.danknull.inventory.PlayerSlot;
import p455w0rd.danknull.items.ItemDankNull;
import p455w0rd.danknull.items.ItemDankNullReplica;

/**
 * Routes replica and legacy inventory GUIs.
 *
 * Legacy implementation by p455w0rd.
 */
public class GuiHandler implements IGuiHandler {

    // #region Shared registration and routing

    public static final int DANK_REPLICA = 2;

    public static void init() {
        DankNull.log.info("Registering GUI Handler");
        NetworkRegistry.INSTANCE.registerGuiHandler(
                DankNull.instance, new GuiHandler());
    }

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player,
            final World world, final int x, final int y, final int z) {
        if (id == DANK_REPLICA) {
            return getReplicaServerGuiElement(player, x);
        }

        return getLegacyServerGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player,
            final World world, final int x, final int y, final int z) {
        if (id == DANK_REPLICA) {
            return getReplicaClientGuiElement(player, x);
        }

        return getLegacyClientGuiElement(id, player, world, x, y, z);
    }

    // #endregion

    // #region Replica GUI

    private static ItemStack getReplicaStack(final EntityPlayer player,
            final int inventorySlot) {
        // Accept hotbar slots and the offhand slot.
        if (!((inventorySlot >= 0 && inventorySlot < 9)
                || inventorySlot == 40)) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = player.inventory.getStackInSlot(inventorySlot);

        return stack.getItem() instanceof ItemDankNullReplica
                ? stack
                : ItemStack.EMPTY;
    }

    private static Object getReplicaServerGuiElement(
            final EntityPlayer player, final int inventorySlot) {
        if (getReplicaStack(player, inventorySlot).isEmpty()) {
            return null;
        }

        return new ContainerDankNullReplica(player, inventorySlot);
    }

    private static Object getReplicaClientGuiElement(
            final EntityPlayer player, final int inventorySlot) {
        final ItemStack stack = getReplicaStack(player, inventorySlot);
        if (stack.isEmpty()) {
            return null;
        }

        final ItemDankNullReplica item = (ItemDankNullReplica) stack.getItem();

        return new GuiDankNullReplica(
                new ContainerDankNullReplica(player, inventorySlot),
                item.tier);
    }

    // #endregion

    // #region Legacy GUI — migrated from ModGuiHandler

    public static void launchGui(final GUIType type, final EntityPlayer player,
            final World world, final BlockPos pos) {
        if (!world.isRemote) {
            player.openGui(DankNull.instance, type.ordinal(), world,
                    pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static PlayerSlot getDankNullSlot(final EntityPlayer player) {
        final InventoryPlayer playerInv = player.inventory;
        final ItemStack mainHand = player.getHeldItemMainhand();
        final ItemStack offHand = player.getHeldItemOffhand();

        if (mainHand.getItem() instanceof ItemDankNull) {
            return new PlayerSlot(playerInv.currentItem, MAIN);
        } else if (offHand.getItem() instanceof ItemDankNull) {
            return new PlayerSlot(0, OFF_HAND);
        }

        for (int i = 0; i < playerInv.mainInventory.size(); i++) {
            final ItemStack stack = playerInv.mainInventory.get(i);

            if (stack.getItem() instanceof ItemDankNull) {
                return new PlayerSlot(i, MAIN);
            }
        }

        return null;
    }

    private static Object getLegacyServerGuiElement(
            final int id, final EntityPlayer player, final World world,
            final int x, final int y, final int z) {
        if (id < 0 || id >= GUIType.VALUES.length) {
            return null;
        }

        switch (GUIType.VALUES[id]) {
            case DANKNULL:
                final PlayerSlot dankNull = getDankNullSlot(player);
                if (dankNull == null) {
                    return null;
                }

                return new ContainerDankNullItem(player, dankNull);

            case DANKNULL_TE:
                final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof TileDankNullDock) {
                    final TileDankNullDock dock = (TileDankNullDock) te;

                    if (!dock.getDankNull().isEmpty()) {
                        return new ContainerDankNullDock(player, dock);
                    }
                }
                break;

            default:
                break;
        }

        return null;
    }

    private static Object getLegacyClientGuiElement(
            final int id, final EntityPlayer player, final World world,
            final int x, final int y, final int z) {
        if (id < 0 || id >= GUIType.VALUES.length) {
            return null;
        }

        switch (GUIType.VALUES[id]) {
            case DANKNULL_TE:
                final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof TileDankNullDock) {
                    final TileDankNullDock dock = (TileDankNullDock) te;

                    if (!dock.getDankNull().isEmpty()) {
                        return new GuiDankNull(
                                new ContainerDankNullDock(player, dock));
                    }
                }

                // Preserve the legacy fallback to a carried Dank.
            case DANKNULL:
                final PlayerSlot dankNull = getDankNullSlot(player);
                if (dankNull == null) {
                    return null;
                }

                return new GuiDankNull(
                        new ContainerDankNullItem(player, dankNull));

            default:
                break;
        }

        return null;
    }

    public enum GUIType {
        DANKNULL,
        DANKNULL_TE;

        public static final GUIType[] VALUES = values();
    }

    // #endregion
}