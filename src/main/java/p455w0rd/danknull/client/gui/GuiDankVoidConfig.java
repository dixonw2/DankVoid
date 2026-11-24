package p455w0rd.danknull.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
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
        for (final String category : config.getCategoryNames()) {
            for (final Property property : config.getCategory(category).values()) {
                final String[] values = property.getValidValues();
                if (property.getType() == Property.Type.STRING && !property.isList()
                        && values != null && values.length > 0 && property.getConfigEntryClass() == null) {
                    property.setConfigEntryClass(ReversibleCycleEntry.class);
                }
            }
        }
        final List<IConfigElement> elements = new ArrayList<>();
        elements.add(new ConfigElement(config.getCategory(Config.CATEGORY_TOOLTIPS)));
        // TODO: Update localization
        // elements.add(new DummyConfigElement.DummyCategoryElement("hudPosition",
        // "dankVoid.hudEditor.entry", HudPositionEntry.class));
        return elements;
    }

    public static class ReversibleCycleEntry extends GuiConfigEntries.SelectValueEntry {

        public ReversibleCycleEntry(final GuiConfig screen, final GuiConfigEntries entries,
                final IConfigElement element) {
            super(screen, entries, element, Collections.emptyMap());
        }

        @Override
        public void updateValueButtonText() {
            final int index = Arrays.asList(this.configElement.getValidValues()).indexOf(this.currentValue);
            final String[] labels = this.configElement.getValidValuesDisplay();
            this.btnValue.displayString = labels != null && index >= 0 && index < labels.length && labels[index] != null
                    ? I18n.format(labels[index])
                    : this.currentValue.toString();
        }

        @Override
        public void valueButtonPressed(final int slotIndex) {
            this.cycleValue(GuiScreen.isShiftKeyDown() ? -1 : 1);
        }

        @Override
        public void mouseClicked(final int x, final int y, final int mouseButton) {
            // Forge forwards right-clicks to every entry, including rows outside the visible list.
            if (mouseButton != 1 || !this.enabled() || !this.owningEntryList.isMouseYWithinSlotBounds(y)) {
                return;
            }

            final int slotIndex = this.owningEntryList.getSlotIndexFromScreenCoords(x, y);
            if (slotIndex >= 0 && this.owningEntryList.getListEntry(slotIndex) == this
                    && this.btnValue.mousePressed(this.mc, x, y)) {
                this.btnValue.playPressSound(this.mc.getSoundHandler());
                this.cycleValue(-1);
            }
        }

        private void cycleValue(final int step) {
            final String[] values = this.configElement.getValidValues();
            final int index = Arrays.asList(values).indexOf(this.currentValue);
            final int nextIndex = index < 0 ? 0 : Math.floorMod(index + step, values.length);
            this.setValueFromChildScreen(values[nextIndex]);
        }
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
