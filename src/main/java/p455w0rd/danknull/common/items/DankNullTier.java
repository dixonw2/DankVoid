package p455w0rd.danknull.common.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.util.Constants;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.common.ModConstants.Rarities;

public enum DankNullTier {

    REDSTONE, LAPIS, IRON, GOLD, DIAMOND, EMERALD, CREATIVE, NONE;

    private static final int[] OPAQUE_HEX_COLORS = new int[] {
            0xFFEC4848,
            0xFF4885EC,
            0xFFFFFFFF,
            0xFFFFFF00,
            0xFF00FFFF,
            0xFF17FF6D,
            0xFF8F15D4,
            0x0
    };
    private static final int[] HEX_COLORS = new int[] {
            0x99EC4848,
            0x994885EC,
            0x99FFFFFF,
            0x99FFFF00,
            0x9900FFFF,
            0x9917FF6D,
            0x998F15D4,
            0x0
    };
    public static DankNullTier[] VALUES = values();

    public ResourceLocation getDankNullRegistryName() {
        return new ResourceLocation(DankNull.MOD_ID, getUnlocalizedNameForDankNull());
    }

    public ResourceLocation getDankNullPanelRegistryName() {
        return new ResourceLocation(DankNull.MOD_ID, getUnlocalizedNameForPanel());
    }

    public String getUnlocalizedNameForDankNull() {
        return "dank_null_" + ordinal();
    }

    public String getUnlocalizedNameForPanel() {
        return "dank_null_panel_" + ordinal();
    }

    public int getMaxStackSize() {
        final int level = ordinal() + 1;
        if (level >= 6) {
            return Integer.MAX_VALUE;
        }
        return level * 128 * level;
    }

    public int getNumRows() {
        int numRows = ordinal();
        if (isCreative()) {
            numRows--;
        }
        return numRows + 1;
    }

    public int getNumRowsMultiplier() {
        return getNumRows() - 1;
    }

    public boolean isCreative() {
        return ordinal() == 6;
    }

    // 140=player inv
    public int getGuiHeight() {
        return 140 + getNumRowsMultiplier() * 20 + getNumRowsMultiplier() + 1;
    }

    public ResourceLocation getGuiBackground() {
        return new ResourceLocation(DankNull.MOD_ID,
                "textures/gui/danknullscreen" + (getNumRowsMultiplier() + (isCreative() ? 1 : 0)) + ".png");
    }

    public IRarity getRarity() {
        return Rarities.getRarityFromMeta(ordinal());
    }

    public int getHexColor(final boolean opaque) {
        return opaque ? OPAQUE_HEX_COLORS[ordinal()] : HEX_COLORS[ordinal()];
    }

    public ItemStack getUpgradedVersion(final ItemStack dankNull) {
        if (ordinal() < 6) {
            final NBTTagCompound raw = dankNull.serializeNBT();
            if (raw.hasKey("id", Constants.NBT.TAG_STRING)) {
                final String[] id = raw.getString("id").split(":");
                if (id.length > 0 && id[1].startsWith("dank_null_")) {
                    raw.setString("id", id[0] + ":dank_null_" + (ordinal() + 1));
                    return new ItemStack(raw);
                }
            }
        }
        return dankNull.copy();
    }

}
