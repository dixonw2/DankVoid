package p455w0rd.danknull.integration;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import team.chisel.api.block.ICarvable;
import team.chisel.api.block.VariationData;

public class ChiselReplica {
    public static String getVariantName(final ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return "";
        }

        final Block block = ((ItemBlock) stack.getItem()).getBlock();
        if (!(block instanceof ICarvable)) {
            return "";
        }

        final VariationData variation = ((ICarvable) block).getVariationData(stack.getMetadata());
        final String key = block.getTranslationKey() + "." + variation.name + ".desc.1";
        return I18n.hasKey(key) ? I18n.format(key) : "";
    }
}
