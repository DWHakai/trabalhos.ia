package yesman.epicfight.main;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.EpicFight;
import yesman.epicfight.generated.LangKeys;

import java.util.function.BiConsumer;
import java.util.function.Function;

/// Fabric port: this class used to be the NeoForge `@Mod` entrypoint. The loader-specific
/// initialization moved to [yesman.epicfight.fabric.EpicFightFabric]; only the static helpers
/// the rest of the code base still uses are kept here.
public class EpicFightMod {
	/// @deprecated Use [yesman.epicfight.EpicFight#MODID] instead
	@Deprecated(forRemoval = true)
	public static final String MODID = EpicFight.MODID;

	/// @deprecated Use [yesman.epicfight.EpicFight#EPICSKINS_MODID] instead
	@Deprecated(forRemoval = true)
	public static final String EPICSKINS_MODID = EpicFight.EPICSKINS_MODID;

	/// @deprecated Use [yesman.epicfight.EpicFight#LOGGER] instead
	@Deprecated(forRemoval = true, since = "26.1")
	public static final Logger LOGGER = EpicFight.LOGGER;

	public static String prefix(String s) {
		return String.format("%s:%s", MODID, s);
	}

    /// @deprecated Consider using the generated object [LangKeys],
    /// which is type-safe and not error-prone to runtime bugs or crashes.
    @Deprecated(forRemoval = true)
	public static String format(String s) {
		return String.format(s, MODID);
	}

	public static void logAndStacktraceIfDevSide(BiConsumer<Logger, String> logFunction, String message, Function<String, Throwable> exceptionProvider) {
		logAndStacktraceIfDevSide(logFunction, message, exceptionProvider, message);
	}

	public static void logAndStacktraceIfDevSide(BiConsumer<Logger, String> logFunction, String message, Function<String, Throwable> exceptionProvider, String stackTraceMessage) {
		logFunction.accept(LOGGER, message);
		stacktraceIfDevSide(message, exceptionProvider, stackTraceMessage);
	}

	public static void stacktraceIfDevSide(String message, Function<String, Throwable> exceptionProvider) {
		stacktraceIfDevSide(message, exceptionProvider, message);
	}

	public static void stacktraceIfDevSide(String message, Function<String, Throwable> exceptionProvider, String stackTraceMessage) {
		if (exceptionProvider != null && EpicFightSharedConstants.IS_DEV_ENV) {
			exceptionProvider.apply(stackTraceMessage).printStackTrace();
		}
	}

	/// @deprecated Use [yesman.epicfight.EpicFight#identifier(String)] instead
	@Deprecated(forRemoval = true)
	public static @NotNull ResourceLocation identifier(@NotNull String path) {
        return EpicFight.identifier(path);
	}

	/// @deprecated Use [#identifier(String)] instead. [Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).
	@Deprecated(forRemoval = true)
	public static @NotNull ResourceLocation rl(@NotNull String path) {
		return identifier(path);
	}
}
