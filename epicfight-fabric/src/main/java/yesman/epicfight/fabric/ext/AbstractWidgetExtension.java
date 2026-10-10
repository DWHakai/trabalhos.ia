package yesman.epicfight.fabric.ext;

/// NeoForge's foreground color on `AbstractWidget` (`packedFGColor`, `getFGColor`, `setFGColor`,
/// `clearFGColor`); injected into `AbstractWidget`, implemented by `MixinAbstractWidget`.
public interface AbstractWidgetExtension {
    int UNSET_FG_COLOR = -1;

    default int getFGColor() {
        throw new AssertionError("implemented by mixin");
    }

    default void setFGColor(int color) {
        throw new AssertionError("implemented by mixin");
    }

    default void clearFGColor() {
        this.setFGColor(UNSET_FG_COLOR);
    }

    /// Fabric port: NeoForge code reads the `packedFGColor` field directly; this is its getter.
    default int getPackedFGColor() {
        throw new AssertionError("implemented by mixin");
    }
}
