package p455w0rd.danknull.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import p455w0rd.danknull.client.gui.GuiDankNull;

/**
 * @author p455w0rd
 */
public class ModKeyBindings {

    private static final int MIDDLE_MOUSE_BUTTON = -98;
    private static final IKeyConflictContext SORT_CONTEXT = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return Minecraft.getMinecraft().currentScreen instanceof GuiDankNull;
        }

        @Override
        public boolean conflicts(final IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.GUI;
        }
    };

    private static final String CATEGORY = "key.categories.danknull";
    private static KeyBinding nextItem;
    private static KeyBinding previousItem;
    private static KeyBinding openDankNull;
    private static KeyBinding toggleHUDOverlay;
    private static KeyBinding sort;

    public static void register() {
        ClientRegistry.registerKeyBinding(getNextItemKeyBind());
        ClientRegistry.registerKeyBinding(getPreviousItemKeyBind());
        ClientRegistry.registerKeyBinding(getOpenDankNullKeyBind());
        ClientRegistry.registerKeyBinding(getToggleHUDKeyBind());
        ClientRegistry.registerKeyBinding(getSortKeyBind());
    }

    public static boolean isAnyModKeybindPressed() {
        return getNextItemKeyBind().isKeyDown()
                ||
                getPreviousItemKeyBind().isKeyDown() ||
                getOpenDankNullKeyBind().isKeyDown() ||
                getToggleHUDKeyBind().isKeyDown();
    }

    public static KeyBinding getNextItemKeyBind() {
        if (nextItem == null) {
            nextItem = new KeyBinding("key.next_item.desc", Keyboard.CHAR_NONE, CATEGORY);
        }
        return nextItem;
    }

    public static KeyBinding getPreviousItemKeyBind() {
        if (previousItem == null) {
            previousItem = new KeyBinding("key.previous_item.desc", Keyboard.CHAR_NONE, CATEGORY);
        }
        return previousItem;
    }

    public static KeyBinding getOpenDankNullKeyBind() {
        if (openDankNull == null) {
            openDankNull = new KeyBinding("key.open_danknull.desc", Keyboard.CHAR_NONE, CATEGORY);
        }
        return openDankNull;
    }

    public static KeyBinding getToggleHUDKeyBind() {
        if (toggleHUDOverlay == null) {
            toggleHUDOverlay = new KeyBinding("key.togglehud.desc", Keyboard.CHAR_NONE, CATEGORY);
        }
        return toggleHUDOverlay;
    }

    public static KeyBinding getSortKeyBind() {
        if (sort == null) {
            sort = new KeyBinding("key.sort.desc", SORT_CONTEXT, MIDDLE_MOUSE_BUTTON, CATEGORY);
        }
        return sort;
    }

}
