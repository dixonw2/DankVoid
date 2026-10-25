package p455w0rd.danknull.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import p455w0rd.danknull.api.IDankNullHandler.SortMode;
import p455w0rd.danknull.client.gui.GuiDankNull;
import p455w0rd.danknull.init.ModConfig.Options;
import p455w0rd.danknull.init.ModGlobals;
import p455w0rd.danknull.init.ModNetworking;
import p455w0rd.danknull.network.PacketSortPreferences;

/**
 * Sends preferences and Shift state before inventory input reaches the server.
 */
@Mod.EventBusSubscriber(modid = ModGlobals.MODID, value = Side.CLIENT)
public final class ClientSortingPreferences {
    private static NetHandlerPlayClient lastConnection;
    private static SortMode lastMode;
    private static boolean lastEnabled;
    private static boolean lastShiftHeld;

    private ClientSortingPreferences() {
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            sendIfChanged();
        }
    }

    @SubscribeEvent
    public static void onMouseInput(final GuiScreenEvent.MouseInputEvent.Pre event) {
        sendIfChanged();
    }

    @SubscribeEvent
    public static void onKeyboardInput(final GuiScreenEvent.KeyboardInputEvent.Pre event) {
        sendIfChanged();
    }

    private static void sendIfChanged() {
        final Minecraft minecraft = Minecraft.getMinecraft();
        final NetHandlerPlayClient connection = minecraft.getConnection();
        if (minecraft.player == null || connection == null) {
            lastConnection = null;
            return;
        }
        final boolean shiftHeld = minecraft.currentScreen instanceof GuiDankNull
                && GuiScreen.isShiftKeyDown();
        if (connection != lastConnection || Options.defaultSortMode != lastMode
                || Options.autoSort != lastEnabled || shiftHeld != lastShiftHeld) {
            ModNetworking.getInstance().sendToServer(
                    new PacketSortPreferences(Options.defaultSortMode, Options.autoSort, shiftHeld));
            lastConnection = connection;
            lastMode = Options.defaultSortMode;
            lastEnabled = Options.autoSort;
            lastShiftHeld = shiftHeld;
        }
    }
}
