package p455w0rd.danknull.inventory;

import p455w0rd.danknull.api.IDankNullHandler.SortMode;

import java.util.Objects;

/** The selected mode and first-press behavior belong to one opening of the GUI. */
public final class SortSession {
    private SortMode selectedMode;
    private boolean applySelectedModeFirst;

    public SortSession(final boolean automaticSorting,
            final SortMode activeMode) {
        selectedMode = Objects.requireNonNull(activeMode, "mode");
        applySelectedModeFirst = !automaticSorting;
    }

    public SortMode getSelectedMode() {
        return selectedMode;
    }

    public SortMode pressSortKey() {
        if (applySelectedModeFirst) {
            applySelectedModeFirst = false;
        } else {
            selectedMode = selectedMode.next();
        }
        return selectedMode;
    }
}
