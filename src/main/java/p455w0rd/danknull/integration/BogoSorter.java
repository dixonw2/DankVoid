package p455w0rd.danknull.integration;

import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.modularui.keybind.KeyBindAPI;
import net.minecraftforge.fml.common.Optional;
import p455w0rd.danknull.init.ModKeyBindings;

public class BogoSorter {
    private BogoSorter() {
    }

    @Optional.Method(modid = "bogosorter")
    public static void init() {
        KeyBindAPI.setCompatible(
                ModKeyBindings.getSortKeyBind(),
                ClientEventHandler.sortKey);
    }
}