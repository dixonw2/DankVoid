package p455w0rd.danknull.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import p455w0rd.danknull.client.render.HUDRenderer;
import p455w0rd.danknull.client.render.HUDRenderer.Preview;
import p455w0rd.danknull.client.render.HudLayout;
import p455w0rd.danknull.client.render.HudLayout.Bounds;
import p455w0rd.danknull.client.render.HudLayout.Position;
import p455w0rd.danknull.client.render.HudLayout.RelativePosition;
import p455w0rd.danknull.init.ModConfig.Options;

/**
 * Edits each HUD independently; opening this screen does not change the active
 * HUD.
 */
public final class GuiHudEditor extends GuiScreen {
    private final GuiScreen parent;
    private final RelativePosition[] positions = { HudLayout.load(false), HudLayout.load(true) };
    private boolean legacy = Options.showLegacyHud;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public GuiHudEditor(final GuiScreen parent) {
        this.parent = parent;
    }

    private int index() {
        return legacy ? 1 : 0;
    }

    @Override
    public void initGui() {
        finishDrag();
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 152, 36, 100, 20,
                I18n.format(legacy ? "dankVoid.hudEditor.legacy" : "dankVoid.hudEditor.modern")));
        buttonList.add(new GuiButton(1, width / 2 - 50, 36, 100, 20,
                I18n.format("dankVoid.hudEditor.reset")));
        buttonList.add(new GuiButton(2, width / 2 + 52, 36, 100, 20,
                I18n.format("gui.done")));
    }

    private Bounds bounds() {
        return HUDRenderer.preparePreview(mc, legacy).bounds;
    }

    private Position anchor(final Bounds bounds) {
        return HudLayout.resolve(positions[index()], width, height, bounds);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        final Preview preview = HUDRenderer.preparePreview(mc, legacy);
        final Bounds bounds = preview.bounds;
        if (dragging) {
            moveTo(mouseX, mouseY, bounds);
        }
        final Position anchor = anchor(bounds);
        HUDRenderer.renderPreview(mc, new ScaledResolution(mc), preview, anchor.x, anchor.y);

        final int left = anchor.x + bounds.left;
        final int top = anchor.y + bounds.top;
        final int right = anchor.x + bounds.right;
        final int bottom = anchor.y + bounds.bottom;
        final int color = dragging || bounds.contains(mouseX, mouseY, anchor) ? 0xFF55FFFF : 0x8055FFFF;
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top, left + 1, bottom, color);
        drawRect(right - 1, top, right, bottom, color);
        drawCenteredString(fontRenderer, I18n.format("dankVoid.hudEditor.title"), width / 2, 8, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("dankVoid.hudEditor.help"), width / 2, 22, 0xCCCCCC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int button) throws IOException {
        for (final GuiButton control : buttonList) {
            if (button == 0 && control.mousePressed(mc, mouseX, mouseY)) {
                super.mouseClicked(mouseX, mouseY, button);
                return;
            }
        }
        if (button == 0) {
            final Bounds bounds = bounds();
            final Position anchor = anchor(bounds);
            if (bounds.contains(mouseX, mouseY, anchor)) {
                dragging = true;
                dragOffsetX = mouseX - anchor.x;
                dragOffsetY = mouseY - anchor.y;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    private void moveTo(final int mouseX, final int mouseY, final Bounds bounds) {
        final Position anchor = HudLayout.clamp(mouseX - dragOffsetX, mouseY - dragOffsetY,
                width, height, bounds);
        positions[index()] = HudLayout.relative(anchor, width, height, bounds, positions[index()]);
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int button) {
        if (dragging && button == 0) {
            moveTo(mouseX, mouseY, bounds());
            finishDrag();
        }
        super.mouseReleased(mouseX, mouseY, button);
    }

    private void finishDrag() {
        if (dragging) {
            HudLayout.save(legacy, positions[index()]);
            dragging = false;
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        finishDrag();
        if (button.id == 0) {
            legacy = !legacy;
            button.displayString = I18n.format(legacy ? "dankVoid.hudEditor.legacy" : "dankVoid.hudEditor.modern");
        } else if (button.id == 1) {
            positions[index()] = new RelativePosition(legacy ? 1D : 0.975, 1D);
            HudLayout.save(legacy, positions[index()]);
        } else if (button.id == 2)

        {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(final char character, final int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            finishDrag();
            mc.displayGuiScreen(parent);
        } else {
            super.keyTyped(character, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        finishDrag();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
