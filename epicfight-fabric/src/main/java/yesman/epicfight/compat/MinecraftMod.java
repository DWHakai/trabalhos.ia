package yesman.epicfight.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.EpicFight;

// List of mods with custom compatibility modules.
// Only includes mods requiring manual registration via ICompatModule.
// Mods with official API entry-points (e.g., Shoulder Surfing, Controlify, JEI, KubeJS) are excluded.
public enum MinecraftMod {
    // Fabric port: only the integrations whose mods exist on Fabric are listed (see PORTING.md).
    ;

    private final @NotNull String modId;
    private final boolean isClientOnly;
    private final @NotNull Class<? extends ICompatModule> compatibilityModule;

    MinecraftMod(@NotNull String modId, boolean isClientOnly, @NotNull Class<? extends ICompatModule> compatibilityModule) {
        this.modId = modId;
        this.isClientOnly = isClientOnly;
        this.compatibilityModule = compatibilityModule;
    }

    public @NotNull String getModId() {
        return modId;
    }

    public boolean isClientOnly() {
        return isClientOnly;
    }

    public String versionString() {
        return FabricLoader.getInstance().getModContainer(modId).map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    // https://semver.org
    public enum VersionComponent {
        MAJOR(0), MINOR(1), PATCH(2);

        final int index;

        VersionComponent(int index) {
            this.index = index;
        }
    }

    /// Returns the parsed component version value or `null` if parsing fails.
    ///
    /// Example values for version `"2.4.9"`:
    ///
    /// - [VersionComponent#MAJOR] -> `2`
    /// - [VersionComponent#MINOR] -> `4`
    /// - [VersionComponent#PATCH] -> `9`
    ///
    /// Usage example:
    ///
    /// ```java
    /// MinecraftMod mod = MinecraftMod.AZURE_LIB; // Version "3.4.11"
    ///
    /// Integer major = mod.getVersionComponent(VersionComponent.MAJOR); // 3
    /// Integer minor = mod.getVersionComponent(VersionComponent.MINOR); // 4
    /// Integer patch = mod.getVersionComponent(VersionComponent.PATCH); // 11
    ///```
    public @Nullable Integer getVersionComponent(@NotNull VersionComponent component) {
        final String version = versionString();

        try {
            final String[] parts = version.split("\\.");
            return Integer.parseInt(parts[component.index]);
        } catch (Exception e) {
            EpicFight.LOGGER.error("Failed to parse the '{}' mod version '{}': {}", name(), version, e.toString());
            return null;
        }
    }

    public @NotNull Class<? extends ICompatModule> getCompatibilityModule() {
        return compatibilityModule;
    }
}
