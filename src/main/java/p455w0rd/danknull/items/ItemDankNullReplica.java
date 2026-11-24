package p455w0rd.danknull.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.client.gui.DankNullReplicaToolTip;
import p455w0rd.danknull.core.CreativeTab;
import p455w0rd.danknull.core.GuiHandler;
import p455w0rd.danknull.core.config.Config;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rd.danknull.inventory.cap.DankNullReplicaCapabilityProvider;

public class ItemDankNullReplica extends Item {

    public final DankNullTier tier;

    public ItemDankNullReplica(DankNullTier tier) {
        this.tier = tier;
        this.setRegistryName(this.tier.getDankNullRegistryReplicaName());
        this.setCreativeTab(CreativeTab.INSTANCE);
        this.setTranslationKey(this.tier.getUnlocalizedNameForDankVoid());
        this.setMaxStackSize(1);
    }

    // public static boolean isDankNull(final ItemStack stack) {
    // return !stack.isEmpty() &&
    // stack.hasCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null);
    // }

    public static boolean isItemDankNull(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemDankNull;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world,
            final List<String> tooltip, final ITooltipFlag advanced) {
        final TextFormatting tierColor = this.tier.getColor();
        final TextFormatting configuredHintColor = Config.ToolTips.Formatting.hintColor;
        final TextFormatting configuredValueColor = Config.ToolTips.Formatting.valueColor;
        final TextFormatting hintColor = configuredHintColor == null ? tierColor : configuredHintColor;
        final TextFormatting valueColor = configuredValueColor == null ? tierColor : configuredValueColor;

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Misc.numberOfSlots)) {
            final String slots = DankNullReplicaToolTip.formatHintValue(
                    Integer.toString(this.tier.getNumSlots()), valueColor, hintColor);
            tooltip.add(DankNullReplicaToolTip.formatHintText(
                    I18n.format("tooltip.dankVoid.numberOfSlots", slots), hintColor));
        }

        if (DankNullReplicaToolTip.displayToolTip(Config.ToolTips.Visibility.Misc.itemsPerSlot)) {
            final String maxQuantity = this.tier.isCreative()
                    ? I18n.format("tooltip.dankVoid.infinite")
                    : Integer.toString(this.tier.getEffectiveMaxStackSize());
            final String value = DankNullReplicaToolTip.formatHintValue(
                    maxQuantity, valueColor, hintColor);
            tooltip.add(DankNullReplicaToolTip.formatHintText(
                    I18n.format("tooltip.dankVoid.itemsPerSlot", value), hintColor));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            // Player inventory slot 40 is the offhand.
            final int inventorySlot = hand == EnumHand.MAIN_HAND
                    ? player.inventory.currentItem
                    : 40;

            player.openGui(DankNull.instance, GuiHandler.DANK_REPLICA,
                    world, inventorySlot, 0, 0);
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

    @Override
    public IRarity getForgeRarity(final ItemStack stack) {
        return this.tier;
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        return Options.formatDisplayName(super.getItemStackDisplayName(stack));
    }

    @Override
    public ICapabilityProvider initCapabilities(
            final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new DankNullReplicaCapabilityProvider(this.tier, stack);
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
}
