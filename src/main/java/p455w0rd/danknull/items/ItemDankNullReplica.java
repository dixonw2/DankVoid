package p455w0rd.danknull.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.init.ModConfig.Options;

public class ItemDankNullReplica extends Item {

    public final DankNullTier tier;

    private final boolean TEMP_CONFIG_ALWAYS_SHOW_TOOLTIP = false;

    public ItemDankNullReplica(DankNullTier tier) {
        this.tier = tier;
        this.setRegistryName(this.tier.getDankNullRegistryReplicaName());
        this.setTranslationKey(this.tier.getUnlocalizedNameForDankNull() + "_replica");
        this.setMaxStackSize(1);
    }

    // public static boolean isDankNull(final ItemStack stack) {
    // return !stack.isEmpty() &&
    // stack.hasCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
    // }

    public static boolean isItemDankNull(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemDankNull;
    }

    // TODO: Revisit this once config is being rewritten for tooltip config options
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> tooltip,
            final ITooltipFlag advanced) {
        if (TEMP_CONFIG_ALWAYS_SHOW_TOOLTIP || GuiScreen.isShiftKeyDown()) {
            tooltip.add(I18n.format("tooltip.dankVoid.numberOfSlots",
                    this.tier.getColor() + Integer.toString(this.tier.getNumSlots())
                            + TextFormatting.GRAY));

            final String maxQuantityText = (this.tier.isCreative() ? I18n.format("misc.dankVoid.infinite")
                    : Integer.toString(this.tier.getEffectiveMaxStackSize()));
            final String maxMsg = this.tier.getColor() + maxQuantityText + TextFormatting.GRAY;
            tooltip.add(I18n.format("tooltip.dankVoid.itemsPerSlot", maxMsg));
        }
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

    @Override
    public IRarity getForgeRarity(final ItemStack stack) {
        return this.tier;
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        return Options.formatDisplayName(super.getItemStackDisplayName(stack));
    }

    // don't reset block breaking progress unless it's a completely different item
    // progress would otherwise reset if an item was picked up by the held dank
    @Override
    public boolean shouldCauseBlockBreakReset(
            final ItemStack oldStack,
            final ItemStack newStack) {
        return !oldStack.isItemEqual(newStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack,
            final boolean slotChanged) {
        return !oldStack.isItemEqual(newStack) || slotChanged;
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
