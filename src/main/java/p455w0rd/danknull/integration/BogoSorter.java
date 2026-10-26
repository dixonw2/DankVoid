package p455w0rd.danknull.integration;

import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.modularui.keybind.KeyBindAPI;
import com.kbp.client.api.IPatchedKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import p455w0rd.danknull.client.gui.GuiDankNull;
import p455w0rd.danknull.init.ModKeyBindings;
import p455w0rd.danknull.init.ModNetworking;
import p455w0rd.danknull.inventory.slot.SlotDankNull;
import p455w0rd.danknull.network.PacketInventoryMove;
import p455w0rd.danknull.network.PacketInventoryMove.Action;

public final class BogoSorter {
    private final Shortcut[] shortcuts;
    private Shortcut mouseShortcut;
    private Action lastAction;
    private int lastSlot = -1;
    private int consumedMouseButton = -1;

    private BogoSorter() {
        shortcuts = new Shortcut[] {
                new Shortcut(ClientEventHandler.moveAllSame, Action.MOVE_SIMILAR),
                new Shortcut(ClientEventHandler.moveAll, Action.MOVE_ALL),
                new Shortcut(ClientEventHandler.moveSingle, Action.MOVE_SINGLE)
        };
        disableNativeShortcut(ClientEventHandler.moveSingleEmpty.getKeyBinding());
    }

    @Optional.Method(modid = "bogosorter")
    public static void init() {
        KeyBindAPI.setCompatible(
                ModKeyBindings.getSortKeyBind(),
                ClientEventHandler.sortKey);

        MinecraftForge.EVENT_BUS.register(new BogoSorter());
    }

    private static IKeyConflictContext disableNativeShortcut(final KeyBinding binding) {
        final IKeyConflictContext originalContext = binding.getKeyConflictContext();
        // BOGO's ordinary-slot writes must not bypass the dank's transfer rules.
        binding.setKeyConflictContext(new IKeyConflictContext() {
            @Override
            public boolean isActive() {
                return !(Minecraft.getMinecraft().currentScreen instanceof GuiDankNull)
                        && originalContext.isActive();
            }

            @Override
            public boolean conflicts(final IKeyConflictContext other) {
                return originalContext.conflicts(other);
            }
        });
        return originalContext;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMouseInput(final GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!(event.getGui() instanceof GuiDankNull)) {
            return;
        }
        final GuiDankNull gui = (GuiDankNull) event.getGui();
        final int button = Mouse.getEventButton();
        if (button >= 0 && !Mouse.getEventButtonState()) {
            if (button == consumedMouseButton) {
                mouseShortcut = null;
                lastAction = null;
                lastSlot = -1;
                consumedMouseButton = -1;
                event.setCanceled(true);
            }
            return;
        }

        if (button >= 0) {
            // Only an unconsumed press can start a move gesture. A press used to
            // select an item or change its mode must never become a move on drag.
            mouseShortcut = null;
            if (!event.isCanceled()) {
                final Shortcut shortcut = activeShortcut();
                if (shortcut != null && shortcut.binding.getKeyBinding().getKeyCode() < 0
                        && button == shortcut.mouseButton()) {
                    mouseShortcut = shortcut;
                    consumedMouseButton = button;
                }
            }
        }
        if (event.isCanceled()) {
            return;
        }

        if (mouseShortcut != null && mouseShortcut.isDown()) {
            request(gui, mouseShortcut,
                    Mouse.getEventX() * gui.width / Minecraft.getMinecraft().displayWidth,
                    gui.height - Mouse.getEventY() * gui.height / Minecraft.getMinecraft().displayHeight - 1,
                    button >= 0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(final GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(event.getGui() instanceof GuiDankNull)) {
            return;
        }
        final GuiDankNull gui = (GuiDankNull) event.getGui();
        if (!Keyboard.getEventKeyState()) {
            return;
        }

        final Shortcut shortcut = activeShortcut();
        final int key = Keyboard.getEventKey();
        if (shortcut != null && shortcut.binding.getKeyBinding().getKeyCode() == key) {
            if (!Keyboard.isRepeatEvent()) {
                request(gui, shortcut,
                        Mouse.getX() * gui.width / Minecraft.getMinecraft().displayWidth,
                        gui.height - Mouse.getY() * gui.height / Minecraft.getMinecraft().displayHeight - 1,
                        true);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        mouseShortcut = null;
        lastAction = null;
        lastSlot = -1;
        consumedMouseButton = -1;
    }

    private Shortcut activeShortcut() {
        Shortcut active = null;
        for (final Shortcut shortcut : shortcuts) {
            if (shortcut.isDown() && (active == null
                    || shortcut.binding.getCmbKeys().size() > active.binding.getCmbKeys().size())) {
                active = shortcut;
            }
        }
        return active;
    }

    private void request(final GuiDankNull gui, final Shortcut shortcut,
            final int mouseX, final int mouseY, final boolean newPress) {
        final Slot slot = gui.getSlotAtPos(mouseX, mouseY);
        if (!Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty()
                || slot == null || !slot.getHasStack()
                || (shortcut.action == Action.MOVE_ALL && slot instanceof SlotDankNull)) {
            return;
        }
        if (newPress || lastAction != shortcut.action || lastSlot != slot.slotNumber) {
            ModNetworking.getInstance().sendToServer(new PacketInventoryMove(
                    gui.inventorySlots.windowId, slot.slotNumber, shortcut.action));
            lastAction = shortcut.action;
            lastSlot = slot.slotNumber;
        }
    }

    private static boolean isInputDown(final int code) {
        if (code < 0) {
            final int button = code + 100;
            return button >= 0 && button < Mouse.getButtonCount() && Mouse.isButtonDown(button);
        }
        return code > 0 && code < Keyboard.KEYBOARD_SIZE && Keyboard.isKeyDown(code);
    }

    private static final class Shortcut {
        private final IPatchedKeyBinding binding;
        private final Action action;
        private final IKeyConflictContext originalContext;

        private Shortcut(final IPatchedKeyBinding binding, final Action action) {
            this.binding = binding;
            this.action = action;
            originalContext = disableNativeShortcut(binding.getKeyBinding());
        }

        private int mouseButton() {
            return binding.getKeyBinding().getKeyCode() + 100;
        }

        private boolean isDown() {
            return originalContext.isActive() && isInputDown(binding.getKeyBinding().getKeyCode())
                    && binding.getCmbKeys().stream().allMatch(BogoSorter::isInputDown);
        }
    }
}
