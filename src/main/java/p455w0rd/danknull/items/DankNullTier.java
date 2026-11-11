package p455w0rd.danknull.items;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.util.Constants;
import p455w0rd.danknull.DankNull;

public enum DankNullTier implements IRarity {

    REDSTONE(TextFormatting.RED),
    LAPIS(TextFormatting.BLUE),
    IRON(TextFormatting.WHITE),
    GOLD(TextFormatting.YELLOW),
    DIAMOND(TextFormatting.AQUA),
    EMERALD(TextFormatting.GREEN),
    CREATIVE(TextFormatting.LIGHT_PURPLE);

    /**
     * The tier's capitalized name
     */
    public final String NAME = StringUtils.capitalize(this.name().toLowerCase());

    private final TextFormatting color;
    private final String rarityName;

    DankNullTier(final TextFormatting color) {
        this.color = color;
        this.rarityName = "dn:" + this.name().toLowerCase(Locale.ROOT);
    }

    private final int PLAYER_INVENTORY_HEIGHT = 140;
    private final int ROW_HEIGHT = 20;

    private int maxStackSize;

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
        return new ResourceLocation(DankNull.MOD_ID, this.getUnlocalizedNameForDankNull());
    }

    // TODO: Remove replica from name/etc once ready to replace the original dank
    // nulls
    /**
     * Gets the registry name for the dank using the
     * {@link #getUnlocalizedNameForDankNull unlocalized name}
     * 
     * @return ResourceLocation for the dank registry name
     */
    public ResourceLocation getDankNullRegistryReplicaName() {
        return new ResourceLocation(DankNull.MOD_ID, this.getUnlocalizedNameForDankNull() + "_replica");
    }

    /**
     * Gets the registry name for the dank panel using the
     * {@link #getUnlocalizedNameForPanel unlocalized name}
     * 
     * @return ResourceLocation for the dank panel registry name
     */
    public ResourceLocation getDankNullPanelRegistryName() {
        return new ResourceLocation(DankNull.MOD_ID, this.getUnlocalizedNameForDankNullPanel());
    }

    // TODO: Remove replica from name/etc once ready to replace the original dank
    // nulls
    /**
     * Gets the registry name for the dank panel using the
     * {@link #getUnlocalizedNameForPanel unlocalized name}
     * 
     * @return ResourceLocation for the dank panel registry name
     */
    public ResourceLocation getDankNullPanelRegistryReplicaName() {
        return new ResourceLocation(DankNull.MOD_ID, this.getUnlocalizedNameForDankNullPanel() + "_replica");
    }

    /**
     * Gets the name of the dank for the tier in the form of
     * {@code dank_null_<tierOrdinal>}. Ordinal remains in use for backwards
     * compatibility.
     * 
     * @return unlocalized name of dank for the tier
     */
    public String getUnlocalizedNameForDankNull() {
        return "dank_null_" + this.ordinal();
    }

    /**
     * Gets the new name of the dank for the tier in the form of
     * {@code dankVoid.dank<TierName>}.
     * 
     * @return unlocalized new name of dank for the tier
     */
    public String getUnlocalizedNameForDankVoid() {
        return "dankVoid.dank" + this.NAME;
    }

    /**
     * Gets the name of the dank panel for the tier in the form of
     * {@code dank_null_panel_<tierOrdinal>}. Ordinal remains in use for backwards
     * compatibility.
     * 
     * @return unlocalized name of panel for the tier
     */
    public String getUnlocalizedNameForDankNullPanel() {
        return "dank_null_panel_" + this.ordinal();
    }

    /**
     * Gets the new name of the panel for the tier in the form of
     * {@code dankVoid.panel<TierName>}.
     * 
     * @return unlocalized new name of panel for the tier
     */
    public String getUnlocalizedNameForDankVoidPanel() {
        return "dankVoid.panel" + this.NAME;
    }

    /**
     * Sets the custom max stack size for the tier.
     * 
     * @param maxStackSize an integer greater than 0
     */
    public void setMaxStackSize(final int maxStackSize) {
        if (maxStackSize > 0) {
            this.maxStackSize = maxStackSize;
        } else {
            DankNull.LOG.error("Capacity must be greater than zero for tier: " + this.name());
        }
    }

    /**
     * Gets the default max stack size for the tier. It uses the formula 2^(9 + 3 *
     * tierOrdinal) to calculate the default max stack size. The Emerald/Creative
     * tiers will return Integer.MAX_VALUE.
     * 
     * @return default max stack size
     */
    private int getDefaultMaxStackSize() {
        if (this == EMERALD || this == CREATIVE) {
            return Integer.MAX_VALUE;
        }

        return (int) Math.pow(2, 9 + 3 * this.ordinal());
    }

    /**
     * Gets the stack size per slot for the tier. If the max stack size is set to a
     * value greater than 0, that value will be used. Otherwise, the default max
     * stack size will be used.
     * 
     * @return max logical stack size for the tier
     */
    public int getEffectiveMaxStackSize() {
        return this.maxStackSize > 0 ? this.maxStackSize : this.getDefaultMaxStackSize();
    }

    /**
     * @return number of rows for the tier
     */
    public int getNumRows() {
        int numRows = ordinal() + 1;
        if (this.isCreative()) {
            numRows--;
        }
        return numRows;
    }

    /**
     * @return number of slots for the tier
     */
    public int getNumSlots() {
        return this.getNumRows() * 9;
    }

    public int getNumRowsMultiplier() {
        return this.getNumRows() - 1;
    }

    /**
     * @return true if the tier is CREATIVE, false otherwise
     */
    public boolean isCreative() {
        return this == CREATIVE;
    }

    /**
     * @return the height of the GUI for the tier
     */
    public int getGuiHeight() {
        return PLAYER_INVENTORY_HEIGHT + this.getNumRowsMultiplier() * ROW_HEIGHT + this.getNumRowsMultiplier() + 1;
    }

    /**
     * @return ResourceLocation for the GUI background texture for the tier
     */
    public ResourceLocation getGuiBackground() {
        return new ResourceLocation(DankNull.MOD_ID,
                "textures/gui/danknullscreen" + this.ordinal() + ".png");
    }

    @Override
    public TextFormatting getColor() {
        return color;
    }

    @Override
    public String getName() {
        return rarityName;
    }

    public int getHexColor(final boolean opaque) {
        return opaque ? OPAQUE_HEX_COLORS[this.ordinal()] : HEX_COLORS[this.ordinal()];
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
