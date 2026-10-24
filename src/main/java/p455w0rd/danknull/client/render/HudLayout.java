package p455w0rd.danknull.client.render;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import p455w0rd.danknull.init.ModConfig;

/**
 * Saved positions are fractions of the available travel on each screen axis.
 */
public final class HudLayout {
    private static final String CATEGORY = Configuration.CATEGORY_CLIENT + ".hud";
    private static RelativePosition modernPosition;
    private static RelativePosition legacyPosition;

    private HudLayout() {
    }

    public static final class Position {
        public final int x;
        public final int y;

        public Position(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    /** Resolution-independent position: 0 is the start edge, 1 is the end edge. */
    public static final class RelativePosition {
        public final double x;
        public final double y;

        public RelativePosition(final double x, final double y) {
            this.x = fraction(x);
            this.y = fraction(y);
        }
    }

    /**
     * Draggable extents relative to the renderer's anchor; modern item names are
     * excluded.
     */
    public static final class Bounds {
        public final int left;
        public final int top;
        public final int right;
        public final int bottom;

        public Bounds(final int left, final int top, final int right, final int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public boolean contains(final int mouseX, final int mouseY, final Position anchor) {
            return mouseX >= anchor.x + left && mouseX < anchor.x + right
                    && mouseY >= anchor.y + top && mouseY < anchor.y + bottom;
        }
    }

    public static RelativePosition defaults(final boolean legacy, final int width,
            final int height, final Bounds bounds) {
        final Position anchor = new Position(
                width - (legacy ? 106 : 39),
                height - (legacy ? 45 : 22));

        return relative(anchor, width, height, bounds, new RelativePosition(1D, 1D));
    }

    public static RelativePosition load(final boolean legacy) {
        if (modernPosition == null || legacyPosition == null) {
            reload();
        }
        return legacy ? legacyPosition : modernPosition;
    }

    /**
     * Refresh after configuration synchronization, never during ordinary rendering.
     */
    public static void reload() {
        ModConfig.getInstance().getCategory(CATEGORY).setShowInGui(false);
        modernPosition = readPosition(false);
        legacyPosition = readPosition(true);
    }

    private static RelativePosition readPosition(final boolean legacy) {
        final Configuration config = ModConfig.getInstance();
        final String prefix = legacy ? "legacy" : "modern";
        for (final String axis : new String[] { "X", "Y" }) {
            final String key = prefix + axis;
            if (config.hasKey(CATEGORY, key)
                    && config.getCategory(CATEGORY).get(key).getType() != Property.Type.DOUBLE) {
                config.getCategory(CATEGORY).remove(key);
            }
        }

        // make constants
        final Property x = config.get(CATEGORY, prefix + "X", legacy ? 1D : 0.975,
                "Horizontal position within the available HUD travel: 0 = left, 1 = right.")
                .setShowInGui(false);
        final Property y = config.get(CATEGORY, prefix + "Y", 1D,
                "Vertical position within the available HUD travel: 0 = top, 1 = bottom.")
                .setShowInGui(false);
        final RelativePosition position = new RelativePosition(x.getDouble(1D), y.getDouble(1D));
        // Previous pixel offsets cannot be resolved without their original window size.
        x.set(position.x);
        y.set(position.y);
        return position;
    }

    private static double fraction(final double value) {
        return Double.isFinite(value) && value >= 0D && value <= 1D ? value : 1D;
    }

    public static void save(final boolean legacy, final RelativePosition position) {
        load(legacy);
        final Configuration config = ModConfig.getInstance();
        final String prefix = legacy ? "legacy" : "modern";
        config.getCategory(CATEGORY).get(prefix + "X").set(position.x);
        config.getCategory(CATEGORY).get(prefix + "Y").set(position.y);
        config.save();
        if (legacy) {
            legacyPosition = position;
        } else {
            modernPosition = position;
        }
    }

    public static Position resolve(final RelativePosition position, final int screenWidth,
            final int screenHeight, final Bounds bounds) {
        final int travelX = Math.max(0, screenWidth - (bounds.right - bounds.left));
        final int travelY = Math.max(0, screenHeight - (bounds.bottom - bounds.top));
        return new Position(-bounds.left + (int) Math.round(position.x * travelX),
                -bounds.top + (int) Math.round(position.y * travelY));
    }

    public static RelativePosition relative(final Position anchor, final int screenWidth,
            final int screenHeight, final Bounds bounds, final RelativePosition previous) {
        final Position clamped = clamp(anchor.x, anchor.y, screenWidth, screenHeight, bounds);
        final int travelX = screenWidth - (bounds.right - bounds.left);
        final int travelY = screenHeight - (bounds.bottom - bounds.top);
        // Retain the saved fraction when a tiny window leaves no room to drag on an
        // axis.
        return new RelativePosition(travelX > 0 ? (clamped.x + bounds.left) / (double) travelX : previous.x,
                travelY > 0 ? (clamped.y + bounds.top) / (double) travelY : previous.y);
    }

    public static Position clamp(final int x, final int y, final int screenWidth,
            final int screenHeight, final Bounds bounds) {
        return new Position(clampAxis(x, screenWidth, bounds.left, bounds.right),
                clampAxis(y, screenHeight, bounds.top, bounds.bottom));
    }

    private static int clampAxis(final int value, final int size, final int start, final int end) {
        final int minimum = -start;
        final int maximum = size - end;
        return maximum < minimum ? minimum : Math.max(minimum, Math.min(maximum, value));
    }

}
