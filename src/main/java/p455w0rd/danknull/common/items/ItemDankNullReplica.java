package p455w0rd.danknull.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.integration.ExtraUtilities;
// import p455w0rd.danknull.DankNull;
// import p455w0rd.danknull.inventory.cap.CapabilityDankNull;
import p455w0rd.danknull.items.ItemDankNull;

public class ItemDankNullReplica extends Item {

    public final DankNullTier tier;

    public ItemDankNullReplica(DankNullTier tier) {
        this.tier = tier;
        // this.setRegistryName(DankNull.MOD_ID,
        // tier.getDankNullRegistryName().getPath() + "_replica");
        this.setRegistryName(tier.getDankNullRegistryReplicaName());
        // this.setRegistryName(tier.getDankNullRegistryName());
        this.setTranslationKey(tier.getUnlocalizedNameForDankNull() + "_replica");
        this.setMaxStackSize(1);
        // this.setMaxDamage(0);
    }

    // public static boolean isDankNull(final ItemStack stack) {
    // return !stack.isEmpty() &&
    // stack.hasCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
    // }

    public static boolean isItemDankNullReplica(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemDankNullReplica;
    }

    public static boolean isItemDankNull(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemDankNull;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) {
            player.sendMessage(new TextComponentString(
                    "I will make you see all of the things that you can be, believe in yourself, come follow me yeah yeah"));
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    // @Override
    // public ActionResult<ItemStack> onItemRightClick(
    // final World world, final EntityPlayer player, final EnumHand hand) {
    // final ItemStack stack = player.getHeldItem(hand);

    // // Backpack Opener may report sneaking only on the server.
    // if (world.isRemote) {
    // requestGuiOpen(player, hand);
    // }

    // if (player.isSneaking()) {
    // return ActionResult.newResult(EnumActionResult.FAIL, stack);
    // }

    // if (Mods.EXTRA_UTILITIES_2.isLoaded()
    // && ExtraUtilities.isAngelBlockSelected(stack)) {
    // if (!world.isRemote) {
    // return ExtraUtilities.tryPlaceAngelBlock(stack, player, hand);
    // }
    // player.swingArm(hand);
    // }

    // return ActionResult.newResult(EnumActionResult.FAIL, stack);
    // }
}
