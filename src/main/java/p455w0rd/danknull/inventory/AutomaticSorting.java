package p455w0rd.danknull.inventory;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import p455w0rd.danknull.api.IDankNullHandler;
import p455w0rd.danknull.api.IDankNullHandler.SortMode;
import p455w0rd.danknull.init.ModGlobals;
import p455w0rd.danknull.inventory.cap.CapabilityDankNull;
import p455w0rd.danknull.items.ItemDankNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/** Applies client preferences and runs deferred sorts when Shift is released. */
@Mod.EventBusSubscriber(modid = ModGlobals.MODID)
public final class AutomaticSorting {
    private static final Set<DankNullHandler> WAITING_FOR_SHIFT_RELEASE =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<EntityPlayerMP, Preferences> PREFERENCES = new WeakHashMap<>();

    private AutomaticSorting() {
    }

    public static void enqueue(final DankNullHandler handler) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            if (handler.shouldWaitForShiftRelease()) {
                WAITING_FOR_SHIFT_RELEASE.add(handler);
            } else {
                WAITING_FOR_SHIFT_RELEASE.remove(handler);
                handler.processAutomaticSort();
            }
        }
    }

    public static void setPreferences(final EntityPlayerMP player,
            final SortMode mode, final boolean enabled, final boolean shiftHeld) {
        final Preferences previous = PREFERENCES.get(player);
        PREFERENCES.put(player, new Preferences(mode, enabled, shiftHeld));
        applyPreferences(player);
        if (previous != null && previous.shiftHeld && !shiftHeld) {
            processShiftRelease(WAITING_FOR_SHIFT_RELEASE, AutomaticSorting::isShiftHeld);
        }
    }

    public static void updateSortSettings(final EntityPlayerMP player,
            final SortMode mode, final boolean enabled) {
        final Preferences previous = PREFERENCES.get(player);
        setPreferences(player, mode, enabled, previous != null && previous.shiftHeld);
    }

    public static boolean isShiftHeld(final DankNullHandler handler) {
        for (final Map.Entry<EntityPlayerMP, Preferences> entry : PREFERENCES.entrySet()) {
            if (!entry.getValue().shiftHeld) {
                continue;
            }
            final EntityPlayerMP player = entry.getKey();
            for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                final ItemStack stack = player.inventory.getStackInSlot(slot);
                if (stack.getItem() instanceof ItemDankNull
                        && stack.getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null) == handler) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void applyPreferences(final EntityPlayerMP player) {
        final Preferences preferences = PREFERENCES.get(player);
        if (preferences == null) {
            return;
        }
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            final ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemDankNull) {
                configure(stack.getCapability(CapabilityDankNull.DANK_NULL_CAPABILITY, null), preferences);
            }
        }
    }

    private static void configure(final IDankNullHandler handler, final Preferences preferences) {
        if (handler instanceof DankNullHandler) {
            ((DankNullHandler) handler).configureAutomaticSorting(preferences.mode, preferences.enabled);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP) {
            applyPreferences((EntityPlayerMP) event.player);
        }
    }

    static void processShiftRelease(final Set<DankNullHandler> waiting,
            final Predicate<DankNullHandler> shiftHeld) {
        final ArrayList<DankNullHandler> handlers = new ArrayList<>(waiting);
        for (final DankNullHandler handler : handlers) {
            if (!shiftHeld.test(handler)) {
                waiting.remove(handler);
                handler.processAutomaticSort();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PREFERENCES.remove(event.player);
            // Release pending inventory sorts when a player disconnects while holding Shift.
            processShiftRelease(WAITING_FOR_SHIFT_RELEASE, AutomaticSorting::isShiftHeld);
        }
    }

    private static final class Preferences {
        private final SortMode mode;
        private final boolean enabled;
        private final boolean shiftHeld;

        private Preferences(final SortMode mode, final boolean enabled, final boolean shiftHeld) {
            this.mode = mode;
            this.enabled = enabled;
            this.shiftHeld = shiftHeld;
        }
    }
}
