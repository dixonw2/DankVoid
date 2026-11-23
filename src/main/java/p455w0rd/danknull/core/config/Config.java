package p455w0rd.danknull.core.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import net.minecraftforge.common.config.Configuration;

public final class Config {

    public static final String CATEGORY_TOOLTIPS = Configuration.CATEGORY_CLIENT + Configuration.CATEGORY_SPLITTER
            + "tooltips";
    public static final String CATEGORY_TOOLTIPS_FORMATTING = CATEGORY_TOOLTIPS + Configuration.CATEGORY_SPLITTER
            + "formatting";
    public static final String CATEGORY_TOOLTIPS_VISIBILITY = CATEGORY_TOOLTIPS + Configuration.CATEGORY_SPLITTER
            + "visibility";
    public static final String CATEGORY_TOOLTIPS_VISIBILITY_MISC = CATEGORY_TOOLTIPS_VISIBILITY
            + Configuration.CATEGORY_SPLITTER + "misc";
    public static final String CATEGORY_TOOLTIPS_VISIBILITY_SORT = CATEGORY_TOOLTIPS_VISIBILITY
            + Configuration.CATEGORY_SPLITTER + "sort";
    public static final String CATEGORY_TOOLTIPS_VISIBILITY_EXTRACTION = CATEGORY_TOOLTIPS_VISIBILITY
            + Configuration.CATEGORY_SPLITTER + "extraction";
    public static final String CATEGORY_TOOLTIPS_VISIBILITY_PLACEMENT = CATEGORY_TOOLTIPS_VISIBILITY
            + Configuration.CATEGORY_SPLITTER + "placement";

    public static final class ToolTips {

        public static final class Visibility {
            public static final String DEFAULT = "default";
            public static final String ALWAYS_DISPLAY = "alwaysDisplay";
            public static final String HIDE = "hide";

            public static final class Misc {
                public static boolean alwaysDisplayToolTips;
                public static int countThreshold;
                public static String preview;
                public static String itemsPerSlot;
                public static String numberOfSlots;
                public static String count;
            }

            public static final class Extraction {
                public static String header;
                public static String changeMode;
            }

            public static final class Placement {
                public static String header;
                public static String changeMode;
            }

            public static final class Sort {
                public static String autoSort;
                public static String changeMode;
                public static String header;
                public static String keybind;
            }
        }
    }

    private static Configuration config;
    private static final boolean DEBUG_RESET = true;

    public static void load(final File configDirectory) {
        final File configFile = new File(configDirectory, "DankVoidAlpha.cfg");

        if (DEBUG_RESET) {
            try {
                Files.deleteIfExists(configFile.toPath());
            } catch (IOException e) {
                throw new UncheckedIOException("Could not reset " + configFile, e);
            }
        }

        config = new Configuration(configFile);
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS, "config.dankVoid.tooltips");
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS_VISIBILITY, "config.dankVoid.tooltips.visibility");
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS_VISIBILITY_MISC, "config.dankVoid.tooltips.visibility.misc");
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS_VISIBILITY_EXTRACTION,
                "config.dankVoid.tooltips.visibility.extraction");
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS_VISIBILITY_SORT,
                "config.dankVoid.tooltips.visibility.sort");
        config.setCategoryLanguageKey(CATEGORY_TOOLTIPS_VISIBILITY_PLACEMENT,
                "config.dankVoid.tooltips.visibility.placement");
        sync();
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void sync() {
        // #region Client

        // #region ToolTips
        // #region Visibility
        final String[] options = new String[] {
                ToolTips.Visibility.DEFAULT,
                ToolTips.Visibility.ALWAYS_DISPLAY,
                ToolTips.Visibility.HIDE };
        final String[] optionTranslationKeys = new String[] {
                "config.dankVoid.tooltips.visibility.default",
                "config.dankVoid.tooltips.visibility.alwaysDisplay",
                "config.dankVoid.tooltips.visibility.hide"
        };

        ToolTips.Visibility.Misc.alwaysDisplayToolTips = config.getBoolean("alwaysDisplayToolTips",
                CATEGORY_TOOLTIPS_VISIBILITY_MISC, false,
                "Always display tooltips regardless of Shift being held",
                "config.dankVoid.tooltips.visibility.misc.alwaysDisplayTooltips");
        ToolTips.Visibility.Misc.countThreshold = config.getInt("countThreshold", CATEGORY_TOOLTIPS_VISIBILITY_MISC,
                10000, 0, Integer.MAX_VALUE,
                "The minimum value for the Count tooltip to display",
                "config.dankVoid.tooltips.visibility.misc.countThreshold");
        ToolTips.Visibility.Misc.preview = config.getString("preview", CATEGORY_TOOLTIPS_VISIBILITY_MISC,
                ToolTips.Visibility.DEFAULT, "When the Contents Preview is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.misc.preview");
        ToolTips.Visibility.Misc.itemsPerSlot = config.getString("itemsPerSlot", CATEGORY_TOOLTIPS_VISIBILITY_MISC,
                ToolTips.Visibility.DEFAULT, "When the Items Per Slot tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.misc.itemsPerSlot");
        ToolTips.Visibility.Misc.numberOfSlots = config.getString("numberOfSlots", CATEGORY_TOOLTIPS_VISIBILITY_MISC,
                ToolTips.Visibility.DEFAULT, "When the Number of Slots tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.misc.numberOfSlots");

        ToolTips.Visibility.Misc.count = config.getString("count", CATEGORY_TOOLTIPS_VISIBILITY_MISC,
                ToolTips.Visibility.ALWAYS_DISPLAY, "When the Count tooltip is displayed once the threshold is met",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.misc.count");

        ToolTips.Visibility.Sort.autoSort = config.getString("autoSort", CATEGORY_TOOLTIPS_VISIBILITY_SORT,
                ToolTips.Visibility.DEFAULT, "When the Auto Sort tooltip is displayed", options, optionTranslationKeys,
                "config.dankVoid.tooltips.visibility.sort.autoSort");
        ToolTips.Visibility.Sort.changeMode = config.getString("changeSortMode", CATEGORY_TOOLTIPS_VISIBILITY_SORT,
                ToolTips.Visibility.DEFAULT, "When the Change Sort Mode tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.sort.changeMode");
        ToolTips.Visibility.Sort.header = config.getString("header", CATEGORY_TOOLTIPS_VISIBILITY_SORT,
                ToolTips.Visibility.DEFAULT, "When the Sort Header tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.sort.header");
        ToolTips.Visibility.Sort.keybind = config.getString("keybind", CATEGORY_TOOLTIPS_VISIBILITY_SORT,
                ToolTips.Visibility.DEFAULT, "When the Sort keybind tooltip is displayed", options,
                optionTranslationKeys, "config.dankVoid.tooltips.visibility.sort.keybind");

        ToolTips.Visibility.Extraction.header = config.getString("header", CATEGORY_TOOLTIPS_VISIBILITY_EXTRACTION,
                ToolTips.Visibility.DEFAULT, "When the Extraction Header tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.extraction.header");
        ToolTips.Visibility.Extraction.changeMode = config.getString("changeMode",
                CATEGORY_TOOLTIPS_VISIBILITY_EXTRACTION,
                ToolTips.Visibility.DEFAULT, "When the Change Extraction Mode tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.extraction.changeMode");

        ToolTips.Visibility.Placement.header = config.getString("header", CATEGORY_TOOLTIPS_VISIBILITY_PLACEMENT,
                ToolTips.Visibility.DEFAULT, "When the Placement Header tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.placement.header");
        ToolTips.Visibility.Placement.changeMode = config.getString("changePlacementMode",
                CATEGORY_TOOLTIPS_VISIBILITY_PLACEMENT, ToolTips.Visibility.DEFAULT,
                "When the Change Placement Mode tooltip is displayed",
                options, optionTranslationKeys, "config.dankVoid.tooltips.visibility.placement.changeMode");
        // #endregion

        // #endregion

        // #endregion

        if (config.hasChanged()) {
            config.save();
        }
    }
}
