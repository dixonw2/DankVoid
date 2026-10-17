package p455w0rd.danknull.init;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.network.PacketConfigSync;
import p455w0rd.danknull.util.NonNullListSerializable;
import p455w0rd.danknull.util.WeakHashMapSerializable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.config.Configuration.CATEGORY_CLIENT;
import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * @author p455w0rd, FleetingDream
 */
public class ModConfig {

    private static final String SERVER_CAT_LEGACY = "Server Rules";
    private static final String NAME_CREATIVE_BLACKLIST_LEGACY = "CreativeBlacklist";
    private static final String NAME_CREATIVE_WHITELIST_LEGACY = "CreativeWhitelist";
    private static final String NAME_OREDICT_BLACKLIST_LEGACY = "OreDictBlacklist";
    private static final String NAME_OREDICT_WHITELIST_LEGACY = "OreDictWhitelist";
    private static final String NAME_DISABLE_OREDICT_LEGACY = "DisableOreDictMode";
    private static final String NAME_ALLOW_DOCK_INSERTION_LEGACY = "AllowDockInsertion";
    private static final String NAME_CALL_IT_DEVNULL_LEGACY = "CallItDevNull";
    private static final String NAME_SUPERSHINE_LEGACY = "SuperShine";
    public static final boolean DEBUG_RESET = false;
    public static final String NAME_CREATIVE_BLACKLIST = "creativeBlacklist";
    public static final String NAME_CREATIVE_WHITELIST = "creativeWhitelist";
    public static final String NAME_OREDICT_BLACKLIST = "oreDictBlacklist";
    public static final String NAME_OREDICT_WHITELIST = "oreDictWhitelist";
    public static final String NAME_DISABLE_OREDICT = "disableOreDictMode";
    public static final String NAME_ALLOW_DOCK_INSERTION = "allowDockInsertion";
    public static final String NAME_DISPLAY_NAME = "displayName";
    public static final String NAME_SUPERSHINE = "superShine";
    public static final String NAME_ONLY_CYCLE_BLOCKS = "onlyCycleBlocks";
    public static final String NAME_SHOW_HUD = "showHUD";
    public static final String[] DISPLAY_NAMES = {
            "/dank/null", "/dev/null", "/dank/void"
    };

    private static Configuration CONFIG = null;

    private static Configuration config() {
        if (CONFIG == null) {
            final File target = new File("config/DankVoid.cfg");
            final File legacy = new File("config/DankNull.cfg");
            final boolean shouldMigrate = !target.exists() && legacy.isFile();

            CONFIG = new Configuration(target);

            if (shouldMigrate) {
                migrateLegacyConfig(new Configuration(legacy), CONFIG);
                DankNull.LOGGER.info("Legacy config migrated.");
            }
        }

        return CONFIG;
    }

    private static void migrateLegacyConfig(
            final Configuration legacy,
            final Configuration target) {

        migrateBoolean(legacy, target,
                CATEGORY_CLIENT, NAME_SUPERSHINE_LEGACY,
                CATEGORY_CLIENT, NAME_SUPERSHINE);

        migrateBoolean(legacy, target,
                CATEGORY_CLIENT, NAME_ONLY_CYCLE_BLOCKS,
                CATEGORY_CLIENT, NAME_ONLY_CYCLE_BLOCKS);

        migrateBoolean(legacy, target,
                CATEGORY_CLIENT, NAME_SHOW_HUD,
                CATEGORY_CLIENT, NAME_SHOW_HUD);

        migrateBoolean(legacy, target,
                SERVER_CAT_LEGACY, NAME_DISABLE_OREDICT_LEGACY,
                CATEGORY_GENERAL, NAME_DISABLE_OREDICT);

        migrateBoolean(legacy, target,
                SERVER_CAT_LEGACY, NAME_ALLOW_DOCK_INSERTION_LEGACY,
                CATEGORY_GENERAL, NAME_ALLOW_DOCK_INSERTION);

        migrateString(legacy, target,
                SERVER_CAT_LEGACY, NAME_CREATIVE_BLACKLIST_LEGACY,
                CATEGORY_GENERAL, NAME_CREATIVE_BLACKLIST);

        migrateString(legacy, target,
                SERVER_CAT_LEGACY, NAME_CREATIVE_WHITELIST_LEGACY,
                CATEGORY_GENERAL, NAME_CREATIVE_WHITELIST);

        migrateString(legacy, target,
                SERVER_CAT_LEGACY, NAME_OREDICT_BLACKLIST_LEGACY,
                CATEGORY_GENERAL, NAME_OREDICT_BLACKLIST);

        migrateString(legacy, target,
                SERVER_CAT_LEGACY, NAME_OREDICT_WHITELIST_LEGACY,
                CATEGORY_GENERAL, NAME_OREDICT_WHITELIST);

        final Property callItDevNull = getLegacyProperty(legacy, CATEGORY_CLIENT, NAME_CALL_IT_DEVNULL_LEGACY,
                Property.Type.BOOLEAN);

        if (callItDevNull != null) {
            final String value = callItDevNull.getString().trim();

            if ("true".equalsIgnoreCase(value)
                    || "false".equalsIgnoreCase(value)) {
                final String displayName = Boolean.parseBoolean(value)
                        ? "/dev/null"
                        : "/dank/null";

                target.get(CATEGORY_CLIENT, NAME_DISPLAY_NAME, DISPLAY_NAMES[0])
                        .set(displayName);
            } else {
                DankNull.LOGGER.warn(
                        "Skipping legacy setting 'client.CallItDevNull': expected true or false");
            }
        }
    }

    private static Property getLegacyProperty(
            final Configuration legacy,
            final String category,
            final String name,
            final Property.Type expectedType) {

        if (!legacy.hasKey(category, name)) {
            return null;
        }

        final Property property = legacy.getCategory(category).get(name);

        if (property.isList() || property.getType() != expectedType) {
            return null;
        }

        return property;
    }

    private static void migrateBoolean(
            final Configuration legacy,
            final Configuration target,
            final String legacyCategory,
            final String legacyName,
            final String targetCategory,
            final String targetName) {

        final Property property = getLegacyProperty(
                legacy, legacyCategory, legacyName, Property.Type.BOOLEAN);

        if (property == null) {
            return;
        }

        final String value = property.getString().trim();

        if ("true".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value)) {
            target.get(targetCategory, targetName, false)
                    .set(Boolean.parseBoolean(value));
        }
    }

    private static void migrateString(
            final Configuration legacy,
            final Configuration target,
            final String legacyCategory,
            final String legacyName,
            final String targetCategory,
            final String targetName) {

        final Property property = getLegacyProperty(
                legacy, legacyCategory, legacyName, Property.Type.STRING);

        if (property != null) {
            target.get(targetCategory, targetName, "")
                    .set(property.getString().trim());
        }
    }

    public static Configuration getInstance() {
        return config();
    }

    public static void load() {
        if (DEBUG_RESET) {
            config().getConfigFile().delete();
        }
        sync();
    }

    public static void sync() {
        syncDisplayName();
        Options.superShine = config().getBoolean(NAME_SUPERSHINE, CATEGORY_CLIENT, false, "Make items ultra shiny!");
        Options.skipNonBlocksOnCycle = config().getBoolean(NAME_ONLY_CYCLE_BLOCKS, CATEGORY_CLIENT, false,
                "When cycling selected item with /dank/null in-hand, should it try to only cycle blocks?");
        Options.creativeBlacklist = config().getString(NAME_CREATIVE_BLACKLIST, CATEGORY_GENERAL, "",
                "A semicolon separated list of items that are not allowed to be placed into the creative /dank/null\nFormat: modid:name:meta (meta optional: modid:name is acceptable) - Example: minecraft:diamond;minecraft:coal:1")
                .trim();
        Options.creativeWhitelist = config().getString(NAME_CREATIVE_WHITELIST, CATEGORY_GENERAL, "",
                "A semicolon separated list of items that are allowed to be placed into the creative /dank/null\nSame format as Blacklist and whitelist superceeds blacklist.\nIf whitelist is non-empty, then ONLY whitelisted items can be added to the Creative /dank/null")
                .trim();
        Options.oreBlacklist = config().getString(NAME_OREDICT_BLACKLIST, CATEGORY_GENERAL, "itemSkull",
                "A semicolon separated list of Ore Dictionary entries (strings) which WILL NOT be allowed to be used with /dank/null's Ore Dictionary functionality.");
        Options.oreWhitelist = config().getString(NAME_OREDICT_WHITELIST, CATEGORY_GENERAL, "",
                "A semicolon separated list of Ore Dictionary entries (strings) which WILL BE allowed to be used with /dank/null's Ore Dictionary functionality. Whitelist superceeds blacklist.\nIf whitelist is non-empty, then ONLY Ore Dictionary items matching the entries will\nbe able to take advantage of /dank/null's Ore Dictionary functionality.");
        Options.disableOreDictMode = config().getBoolean(NAME_DISABLE_OREDICT, CATEGORY_GENERAL, false,
                "If set to true, then Ore Dictionary Mode will not be available (overrides Ore Dictionary White/Black lists)");
        Options.showHUD = config().getBoolean("showHUD", CATEGORY_CLIENT, true, "Show the /dank/null HUD overlay?");
        Options.allowDockInserting = config().getBoolean(NAME_ALLOW_DOCK_INSERTION, CATEGORY_GENERAL, true,
                "If true, you will be able to pipe items into the /dank/null Docking Station");
        if (config().hasChanged()) {
            config().save();
        }
    }

    private static void syncDisplayName() {
        final boolean hasDisplayName = config().hasKey(CATEGORY_CLIENT, NAME_DISPLAY_NAME);
        String migratedName = DISPLAY_NAMES[0];
        final Property property = config().get(CATEGORY_CLIENT, NAME_DISPLAY_NAME, DISPLAY_NAMES[0],
                "Choose the name shown in-game.");
        property.setValidValues(DISPLAY_NAMES);
        if (!hasDisplayName) {
            property.set(migratedName);
        }
        final String value = property.getString();
        Options.displayName = DISPLAY_NAMES[0];
        for (final String choice : DISPLAY_NAMES) {
            if (choice.equals(value)) {
                Options.displayName = choice;
                break;
            }
        }
        if (!Options.displayName.equals(value)) {
            property.set(Options.displayName);
        }
    }

    @SideOnly(Side.CLIENT)
    public static List<IConfigElement> getClientConfigElements() {
        return new ConfigElement(getInstance().getCategory(CATEGORY_CLIENT)).getChildElements();
    }

    @SideOnly(Side.SERVER)
    public static void sendConfigsToClient(final EntityPlayerMP player) {
        final WeakHashMapSerializable<String, Object> map = new WeakHashMapSerializable<>();
        map.put(ModConfig.NAME_CREATIVE_BLACKLIST, Options.creativeBlacklist);
        map.put(ModConfig.NAME_CREATIVE_WHITELIST, Options.creativeWhitelist);
        map.put(ModConfig.NAME_OREDICT_BLACKLIST, Options.oreBlacklist);
        map.put(ModConfig.NAME_OREDICT_WHITELIST, Options.oreWhitelist);
        map.put(ModConfig.NAME_DISABLE_OREDICT, Options.disableOreDictMode);
        ModNetworking.getInstance().sendTo(new PacketConfigSync(map), player);
    }

    public static boolean isOreDictBlacklistEnabled() {
        return !Options.getOreBlacklist().isEmpty() && !isOreDictWhitelistEnabled();
    }

    public static boolean isOreDictWhitelistEnabled() {
        return !Options.getOreWhitelist().isEmpty();
    }

    public static boolean isOreBlacklisted(final String oreName) {
        for (final String currentOre : Options.getOreBlacklist()) {
            if (currentOre.equals(oreName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOreWhitelisted(final String oreName) {
        for (final String currentOre : Options.getOreWhitelist()) {
            if (currentOre.equals(oreName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidOre(final String oreName) {
        boolean isValid = true;
        if (isOreConfigEnabled()) {
            isValid = isOreDictWhitelistEnabled() && isOreWhitelisted(oreName);
            if (!isValid) {
                isValid = isOreDictBlacklistEnabled() && !isOreBlacklisted(oreName);
            }
        }
        return isValid;
    }

    public static boolean isOreConfigEnabled() {
        return !Options.disableOreDictMode && isOreDictBlacklistEnabled() || isOreDictWhitelistEnabled();
    }

    public static boolean isItemOreDictBlacklisted(final ItemStack stack) {
        if (isOreConfigEnabled() && isOreDictBlacklistEnabled()) {
            for (final int id : OreDictionary.getOreIDs(stack)) {
                final String currentOreName = OreDictionary.getOreName(id);
                if (isValidOre(currentOreName)) {
                    for (final String oreName : Options.getOreBlacklist()) {
                        if (isValidOre(oreName) && oreName.equals(currentOreName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isItemOreDictWhitelisted(final ItemStack stack) {
        if (isOreConfigEnabled() && isOreDictWhitelistEnabled()) {
            for (final int id : OreDictionary.getOreIDs(stack)) {
                final String currentOreName = OreDictionary.getOreName(id);
                if (isValidOre(currentOreName)) {
                    for (final String oreName : Options.getOreWhitelist()) {
                        if (isValidOre(oreName) && oreName.equals(OreDictionary.getOreName(id))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static class Options {

        public static String displayName = DISPLAY_NAMES[0];

        public static String formatDisplayName(final String localizedName) {
            return localizedName.replace("/dank/null", displayName)
                    .replace("/dev/null", displayName).replace("/dank/void", displayName);
        }

        public static boolean superShine = false;
        public static String creativeBlacklist = "";
        public static String creativeWhitelist = "";
        public static String oreBlacklist = "";
        public static String oreWhitelist = "";
        public static boolean showHUD = true;
        public static boolean disableOreDictMode = false;
        public static boolean allowDockInserting = true;
        public static boolean skipNonBlocksOnCycle = false;
        public static boolean autoSort = false;
        private static NonNullListSerializable<ItemStack> creativeItemBlacklist;
        private static NonNullListSerializable<ItemStack> creativeItemWhitelist;
        private static ArrayList<String> oreStringBlacklist = Lists.newArrayList();
        private static ArrayList<String> oreStringWhitelist = Lists.newArrayList();

        public static List<String> getOreBlacklist() {
            String[] tmpList = null;
            if (oreStringBlacklist.isEmpty() && !oreBlacklist.isEmpty() && getOreWhitelist().isEmpty()) {
                tmpList = oreBlacklist.split(";");
            }
            if (tmpList != null) {
                for (final String string : tmpList) {
                    if (OreDictionary.doesOreNameExist(string)) {
                        oreStringBlacklist.add(string);
                    }
                }
            }
            return oreStringBlacklist;
        }

        public static List<String> getOreWhitelist() {
            String[] tmpList = null;
            if (oreStringWhitelist.isEmpty() && !oreWhitelist.isEmpty()) {
                tmpList = oreWhitelist.split(";");
            }
            if (tmpList != null) {
                for (final String string : tmpList) {
                    if (OreDictionary.doesOreNameExist(string)) {
                        oreStringWhitelist.add(string);
                    }
                }
            }
            return oreStringWhitelist;
        }

        public static NonNullListSerializable<ItemStack> getCreativeBlacklistedItems() throws Exception {
            if (creativeItemBlacklist == null && getCreativeWhitelistedItems().isEmpty()) {
                creativeItemBlacklist = (NonNullListSerializable<ItemStack>) NonNullListSerializable
                        .<ItemStack>create();
                if (!creativeBlacklist.isEmpty()) {
                    final List<String> itemStringList = Lists.newArrayList(creativeBlacklist.split(";"));
                    for (final String itemString : itemStringList) {
                        final String[] params = itemString.split(":");
                        final int numColons = params.length - 1;
                        if (numColons > 2 || numColons <= 0) {
                            throw new Exception(new Throwable("Invalid format for item blacklisting, check "
                                    + config().getConfigFile() + " for an example"));
                        }
                        if (numColons == 1) { // no meta
                            final Item item = ForgeRegistries.ITEMS
                                    .getValue(new ResourceLocation(params[0], params[1]));
                            if (item == null) {
                                DankNull.LOGGER.warn("Item \"" + params[0] + ":" + params[1] + "\" not found");
                            } else {
                                creativeItemBlacklist.add(new ItemStack(item));
                            }
                        } else if (numColons == 2) {
                            final Item item = ForgeRegistries.ITEMS
                                    .getValue(new ResourceLocation(params[0], params[1]));
                            if (item == null) {
                                DankNull.LOGGER.warn("Item \"" + params[0] + ":" + params[1] + "\" not found");
                            } else {
                                int meta = -1;
                                try {
                                    meta = Integer.parseInt(params[2]);
                                } catch (final NumberFormatException e) {
                                    meta = -1;
                                }
                                if (meta < 0) {
                                    DankNull.LOGGER.warn("Invalid metadata for item \"" + params[0] + ":" + params[1]
                                            + "\" (" + params[2] + ")");
                                } else {
                                    creativeItemWhitelist.add(new ItemStack(item, 1, meta));
                                }
                            }
                        }
                    }
                }
            }
            return creativeItemBlacklist;
        }

        public static NonNullListSerializable<ItemStack> getCreativeWhitelistedItems() throws Exception {
            if (creativeItemWhitelist == null) {
                creativeItemWhitelist = (NonNullListSerializable<ItemStack>) NonNullListSerializable
                        .<ItemStack>create();
                if (!creativeWhitelist.isEmpty()) {
                    final List<String> itemStringList = Lists.newArrayList(creativeWhitelist.split(";"));
                    for (final String itemString : itemStringList) {
                        final String[] params = itemString.split(":");
                        final int numColons = params.length - 1;
                        if (numColons > 2 || numColons <= 0) {
                            throw new Exception(new Throwable("Invalid format for item whitelisting, check "
                                    + config().getConfigFile() + " for an example"));
                        }
                        if (numColons == 1) { // no meta
                            final Item item = ForgeRegistries.ITEMS
                                    .getValue(new ResourceLocation(params[0], params[1]));
                            if (item == null) {
                                DankNull.LOGGER.warn("Item \"" + params[0] + ":" + params[1] + "\" not found");
                            } else {
                                creativeItemWhitelist.add(new ItemStack(item));
                            }
                        } else if (numColons == 2) {
                            final Item item = ForgeRegistries.ITEMS
                                    .getValue(new ResourceLocation(params[0], params[1]));
                            if (item == null) {
                                DankNull.LOGGER.warn("Item \"" + params[0] + ":" + params[1] + "\" not found");
                            } else {
                                int meta = -1;
                                try {
                                    meta = Integer.parseInt(params[2]);
                                } catch (final NumberFormatException e) {
                                    meta = -1;
                                }
                                if (meta < 0) {
                                    DankNull.LOGGER.warn("Invalid metadata for item \"" + params[0] + ":" + params[1]
                                            + "\" (" + params[2] + ")");
                                } else {
                                    creativeItemWhitelist.add(new ItemStack(item, 1, meta));
                                }
                            }
                        }
                    }
                }
            }
            return creativeItemWhitelist;
        }
    }

}
