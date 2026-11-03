package p455w0rd.danknull.common;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.IRarity;

public class ModConstants {

    public static boolean GUI_DANKNULL_ISOPEN = false;
    public static float TIME = 0.0F;

    public static class Rarities {

        private static final IRarity[] RARITY_CACHE = new IRarity[] {
                createRarity("dn:redstone", TextFormatting.RED),
                createRarity("dn:lapis", TextFormatting.BLUE),
                createRarity("dn:iron", TextFormatting.WHITE),
                createRarity("dn:gold", TextFormatting.YELLOW),
                createRarity("dn:diamond", TextFormatting.AQUA),
                createRarity("dn:emerald", TextFormatting.GREEN),
                createRarity("dn:creative", TextFormatting.LIGHT_PURPLE)
        };

        public static IRarity getRarityFromMeta(final int meta) {
            return RARITY_CACHE[meta];
        }

        private static IRarity createRarity(final String name, final TextFormatting color) {
            return new IRarity() {
                @Override
                public TextFormatting getColor() {
                    return color;
                }

                @Override
                public String getName() {
                    return name;
                }
            };
        }

    }

    public static class NBT {

        // Vanilla
        public static final String ID = "id";
        public static final String DAMAGE = "Damage";
        public static final String BLOCKENTITYTAG = "BlockEntityTag";

        // Custom
        public static final String DANKNULL_INVENTORY = "danknull-inventory";
        public static final String DOCKEDSTACK = "DankNullStack";
        public static final String MODE = "Mode";
        public static final String STACK = "Stack";
        public static final String OREDICT = "OreDict";
        public static final String SELECTEDINDEX = "selectedIndex";
        public static final String EXTRACTION_MODES = "ExtractionModes";
        public static final String PLACEMENT_MODES = "PlacementModes";
        public static final String OREDICT_MODES = "OreDictModes";
        public static final String LOCKED = "Locked";

    }

}
