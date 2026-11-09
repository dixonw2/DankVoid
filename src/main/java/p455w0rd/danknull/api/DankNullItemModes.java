package p455w0rd.danknull.api;

import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author p455w0rd
 */
public class DankNullItemModes {

    private static String formatTooltip(final String translationKey, final int numberToKeep) {
        String message = new TextComponentTranslation(translationKey).getUnformattedText();
        if (numberToKeep == Integer.MAX_VALUE) {
            return new TextComponentTranslation("dn.do.desc").getUnformattedText() + " " + message;
        }
        if (numberToKeep > 0) {
            message += " " + numberToKeep;
        }
        return message.isEmpty() ? message
                : Character.toUpperCase(message.charAt(0)) + message.substring(1);
    }

    public enum ItemExtractionMode {

        KEEP_ALL(Integer.MAX_VALUE, "dn.not_extract.desc"),
        KEEP_1(1, "dn.extract_all_but.desc"),
        KEEP_16(16, "dn.extract_all_but.desc"),
        KEEP_64(64, "dn.extract_all_but.desc"),
        KEEP_NONE(0, "dn.extract_all.desc");

        public static ItemExtractionMode[] VALUES = values();
        private final int number;
        private final String translationKey;

        ItemExtractionMode(final int numberToKeep, final String translationKey) {
            number = numberToKeep;
            this.translationKey = translationKey;
        }

        public int getNumberToKeep() {
            return number;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public String getTooltip() {
            return formatTooltip(translationKey, number);
        }

    }

    public enum ItemPlacementMode {

        KEEP_ALL(Integer.MAX_VALUE, "dn.not_place.desc"),
        KEEP_1(1, "dn.place_all_but.desc"),
        KEEP_16(16, "dn.place_all_but.desc"),
        KEEP_64(64, "dn.place_all_but.desc"),
        KEEP_NONE(0, "dn.place_all.desc");

        public static ItemPlacementMode[] VALUES = values();
        private final int number;
        private final String translationKey;

        ItemPlacementMode(final int numberToKeep, final String translationKey) {
            number = numberToKeep;
            this.translationKey = translationKey;
        }

        public int getNumberToKeep() {
            return number;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public String getTooltip() {
            return formatTooltip(translationKey, number);
        }

    }

}
