package p455w0rd.danknull.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.client.render.TESRDankNullDock.DankNullDockItemRenderer;
import p455w0rd.danknull.util.Tags;
import p455w0rd.danknull.init.ModBlocks;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rdslib.api.client.ICustomItemRenderer;
import p455w0rdslib.api.client.IModelHolder;
import p455w0rdslib.api.client.ItemLayerWrapper;

/**
 * @author p455w0rd
 */
public class ItemBlockDankNullDock extends ItemBlock implements IModelHolder {

    ItemLayerWrapper wrappedModel;

    public ItemBlockDankNullDock() {
        super(ModBlocks.DANKNULL_DOCK);
        setRegistryName(ModBlocks.DANKNULL_DOCK.getRegistryName());
    }

    public static ItemStack getDockedDankNull(final ItemStack dankDock) {
        ItemStack dockedDank = ItemStack.EMPTY;
        if (dankDock.hasTagCompound()
                && dankDock.getTagCompound().hasKey(Tags.BLOCK_ENTITY_TAG, Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound nbt = dankDock.getTagCompound().getCompoundTag(Tags.BLOCK_ENTITY_TAG);
            if (!nbt.isEmpty()) {
                dockedDank = new ItemStack(nbt.getCompoundTag(Tags.DOCKED_STACK));
            }
        }
        return dockedDank;
    }

    public static boolean isDankNullDock(final ItemStack stack) {
        return stack.getItem() instanceof ItemBlockDankNullDock;
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        return Options.formatDisplayName(super.getItemStackDisplayName(stack));
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, getModelResource(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemLayerWrapper getWrappedModel() {
        return wrappedModel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setWrappedModel(final ItemLayerWrapper wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldUseInternalTEISR() {
        return true;
    }

    @Override
    public ICustomItemRenderer getRenderer() {
        return DankNullDockItemRenderer.getRendererForItem(this);
    }

}
