package p455w0rd.danknull.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IRarity;
import p455w0rd.danknull.init.ModConfig.Options;

public class ItemDankNullPanelReplica extends Item {
    public final DankNullTier tier;

    public ItemDankNullPanelReplica(final DankNullTier tier) {
        this.tier = tier;
        this.setRegistryName(this.tier.getDankNullPanelRegistryReplicaName());
        this.setTranslationKey(this.tier.getUnlocalizedNameForPanel() + "_replica");
    }

    @Override
    public IRarity getForgeRarity(final ItemStack stack) {
        return this.tier;
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        return Options.formatDisplayName(super.getItemStackDisplayName(stack));
    }
}
