package p455w0rd.danknull.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import p455w0rd.danknull.DankNull;
import p455w0rd.danknull.core.config.Config;

public class GuiDankVoidConfig extends GuiConfig {

    public GuiDankVoidConfig(final GuiScreen parent) {
        super(parent, getConfigElements(), DankNull.MOD_ID, false, false,
                I18n.format("gui.config.dankVoid.title"));
    }

    private static List<IConfigElement> getConfigElements() {
        final Configuration config = Config.getConfig();
        final List<IConfigElement> elements = new ArrayList<>();
        elements.add(new ConfigElement(config.getCategory(Config.CATEGORY_TOOLTIPS)));
        // TODO: Update localization
        // elements.add(new DummyConfigElement.DummyCategoryElement("hudPosition",
        // "dankVoid.hudEditor.entry", HudPositionEntry.class));
        return elements;
    }

    public static class HudPositionEntry extends GuiConfigEntries.CategoryEntry {

        public HudPositionEntry(final GuiConfig screen, final GuiConfigEntries entries,
                final IConfigElement element) {
            super(screen, entries, element);
        }

        @Override
        protected GuiScreen buildChildScreen() {
            return new GuiHudEditor(this.owningScreen);
        }
    }
}
