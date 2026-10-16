package p455w0rd.danknull.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author p455w0rd, FleetingDream
 */
public class ModGuiFactory implements IModGuiFactory {

    private static final String TITLE = ModGlobals.NAME + " Config";

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
