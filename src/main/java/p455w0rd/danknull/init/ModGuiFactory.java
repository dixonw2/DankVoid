package p455w0rd.danknull.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author p455w0rd, FleetingDream
 */
public class ModGuiFactory implements IModGuiFactory {

    private static final String TITLE = ModGlobals.NAME + " Config";

    public static class DisplayNameEntry extends GuiConfigEntries.SelectValueEntry {
        public DisplayNameEntry(final GuiConfig screen, final GuiConfigEntries entries,
                final IConfigElement element) {
            super(screen, entries, element, choices());
        }

        private static Map<Object, String> choices() {
            final Map<Object, String> choices = new LinkedHashMap<>();
            for (final String name : ModConfig.DISPLAY_NAMES) {
                choices.put(name, name);
            }
            return choices;
        }
    }

    @Override
    public void initialize(final Minecraft mc) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(final GuiScreen parent) {
        return new GuiConfig(parent, ModConfig.getClientConfigElements(), ModGlobals.MODID, false, false, TITLE, "");
    }

    @Nullable
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

}
