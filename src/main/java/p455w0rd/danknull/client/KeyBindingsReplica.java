package p455w0rd.danknull.client;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class KeyBindingsReplica {

    private static final int MIDDLE_MOUSE_BUTTON = -98;

    public static final KeyBinding EXTRACTION = new KeyBinding("key.dankVoid.extractAction", KeyConflictContext.GUI,
            Keyboard.KEY_X, "key.categories.dankVoid");
    public static final KeyBinding PLACEMENT = new KeyBinding("key.dankVoid.placeAction", KeyConflictContext.GUI,
            Keyboard.KEY_P, "key.categories.dankVoid");
    public static final KeyBinding SELECTION = new KeyBinding(
            "key.dankVoid.selectAction", KeyConflictContext.GUI, Keyboard.KEY_C, "key.categories.dankVoid");
    public static final KeyBinding SORT = new KeyBinding("key.dankVoid.sort", KeyConflictContext.GUI,
            MIDDLE_MOUSE_BUTTON,
            "key.categories.dankVoid");
    public static final KeyBinding SORT_ACTION = new KeyBinding("key.dankVoid.sortAction", KeyConflictContext.GUI,
            Keyboard.KEY_S, "key.categories.dankVoid");

    public static void register() {
        ClientRegistry.registerKeyBinding(EXTRACTION);
        ClientRegistry.registerKeyBinding(PLACEMENT);
        ClientRegistry.registerKeyBinding(SELECTION);
        ClientRegistry.registerKeyBinding(SORT);
        ClientRegistry.registerKeyBinding(SORT_ACTION);
    }

    public static boolean isExtractionHeld() {
        return GameSettings.isKeyDown(EXTRACTION)
                && EXTRACTION.getKeyModifier().isActive(EXTRACTION.getKeyConflictContext());
    }

    public static boolean isPlacementHeld() {
        return GameSettings.isKeyDown(PLACEMENT)
                && PLACEMENT.getKeyModifier().isActive(PLACEMENT.getKeyConflictContext());
    }

    public static boolean isSelectionHeld() {
        return GameSettings.isKeyDown(SELECTION)
                && SELECTION.getKeyModifier().isActive(SELECTION.getKeyConflictContext());
    }

    public static boolean isSorttActionHeld() {
        return GameSettings.isKeyDown(SORT_ACTION)
                && SORT_ACTION.getKeyModifier().isActive(SORT_ACTION.getKeyConflictContext());
    }
}
