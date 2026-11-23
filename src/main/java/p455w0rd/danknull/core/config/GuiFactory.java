package p455w0rd.danknull.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import p455w0rd.danknull.client.gui.GuiDankVoidConfig;

public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(final Minecraft mc) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(final GuiScreen parent) {
        return new GuiDankVoidConfig(parent);
    }

    @Nullable
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
