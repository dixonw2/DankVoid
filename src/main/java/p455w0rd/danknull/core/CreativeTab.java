package p455w0rd.danknull.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.init.ModItems;

public class CreativeTab extends CreativeTabs {

    public static final CreativeTab INSTANCE = new CreativeTab();

    public CreativeTab() {
        super(DankNull.MOD_ID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(ModItems.CREATIVE_DANKNULL);
    }
}
