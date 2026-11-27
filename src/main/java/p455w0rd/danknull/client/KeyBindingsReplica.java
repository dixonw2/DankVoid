package p455w0rd.danknull.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
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
        return isActionHeld(EXTRACTION);
    }

    public static boolean isPlacementHeld() {
        return isActionHeld(PLACEMENT);
    }

    public static boolean isSelectionHeld() {
        return isActionHeld(SELECTION);
    }

    public static boolean isSorttActionHeld() {
        return isActionHeld(SORT_ACTION);
    }

    private static boolean isActionHeld(final KeyBinding binding) {
        final KeyModifier modifier = binding.getKeyModifier();
        return GameSettings.isKeyDown(binding)
                && (modifier.isActive(binding.getKeyConflictContext())
                        || (modifier == KeyModifier.NONE && GuiScreen.isShiftKeyDown()));
    }
}
