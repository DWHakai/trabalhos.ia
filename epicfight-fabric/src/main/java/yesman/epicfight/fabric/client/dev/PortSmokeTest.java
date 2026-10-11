package yesman.epicfight.fabric.client.dev;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/// Development-only smoke test of the port, turned on by the `EPICFIGHT_SMOKE_TEST` environment
/// variable (`./gradlew runClient --args='--quickPlaySingleplayer "<world>"'`).
///
/// Once in the world it gives itself the weapons, switches to Epic Fight mode, swings at a zombie
/// and saves screenshots to `run/screenshots/smoke_*.png`, then closes the game. Nothing here runs
/// in a normal game.
public final class PortSmokeTest {
    private static final String[] WEAPONS = {
        "iron_longsword", "iron_tachi", "iron_spear", "iron_greatsword", "iron_dagger", "uchigatana", "bokken"
    };

    private record Step(int tick, Consumer<Minecraft> action) {}

    private static final List<Step> STEPS = new ArrayList<>();
    private static int ticksInWorld;
    private static int nextStep;

    public static void registerIfEnabled() {
        if (System.getenv("EPICFIGHT_SMOKE_TEST") == null) {
            return;
        }

        EpicFightMod.LOGGER.warn("[smoke test] enabled");
        buildSteps();
        ClientTickEvents.END_CLIENT_TICK.register(PortSmokeTest::tick);
    }

    private static void at(int tick, Consumer<Minecraft> action) {
        STEPS.add(new Step(tick, action));
    }

    private static void command(Minecraft minecraft, String command) {
        minecraft.player.connection.sendCommand(command);
    }

    /// The hotbar shot keeps the GUI; the others hide it (F1) so the chat doesn't cover the player.
    private static void screenshot(Minecraft minecraft, String name) {
        minecraft.gui.getChat().clearMessages(false);
        EpicFightCapabilities.getUnparameterizedEntityPatch(minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch ->
            EpicFightMod.LOGGER.warn("[smoke test] {}: epic fight mode {}, animated first person {}", name, playerpatch.isEpicFightMode(), ClientConfig.enableAnimatedFirstPersonModel));
        Screenshot.grab(minecraft.gameDirectory, "smoke_" + name + ".png", minecraft.getMainRenderTarget(), message -> {});
        EpicFightMod.LOGGER.warn("[smoke test] screenshot {}", name);
    }

    private static void attack(Minecraft minecraft) {
        KeyMapping.click(minecraft.options.keyAttack.getDefaultKey());
    }

    private static void logZombie(Minecraft minecraft, String when) {
        for (Zombie zombie : minecraft.level.getEntitiesOfClass(Zombie.class, minecraft.player.getBoundingBox().inflate(8.0D))) {
            EpicFightMod.LOGGER.warn("[smoke test] zombie {} health {}", when, ((LivingEntity)zombie).getHealth());
        }
    }

    private static void buildSteps() {
        at(40, mc -> {
            command(mc, "gamemode creative");
            command(mc, "time set day");
            command(mc, "weather clear");
            command(mc, "gamerule doDaylightCycle false");
            command(mc, "gamerule doMobSpawning false");
            command(mc, "kill @e[type=!player]");
            command(mc, "clear");
            command(mc, "epicfight mode vanilla");

            for (int i = 0; i < WEAPONS.length; i++) {
                command(mc, "item replace entity @s hotbar." + i + " with epicfight:" + WEAPONS[i]);
            }

            command(mc, "tp @s ~ ~ ~ 0 10");
            mc.player.getInventory().selected = 0;
        });
        // GUI icons (hotbar) and the first person base model
        at(70, mc -> command(mc, "summon item ^-1 ^1 ^2.5 {Item:{id:\"epicfight:iron_greatsword\",count:1},PickupDelay:32767}"));
        at(80, mc -> screenshot(mc, "1_hotbar_first_person"));
        at(84, mc -> screenshot(mc, "1_first_person_vanilla"));
        at(85, mc -> {
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        });
        at(100, mc -> screenshot(mc, "2_vanilla_third_person"));
        at(105, mc -> command(mc, "epicfight mode epicfight"));
        at(130, mc -> screenshot(mc, "3_epicfight_third_person"));
        at(131, mc -> {
            mc.options.hideGui = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        });
        at(140, mc -> screenshot(mc, "3_first_person_epicfight"));
        at(141, mc -> {
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        });
        at(142, mc -> mc.player.getInventory().selected = 3);
        at(150, mc -> screenshot(mc, "3_epicfight_third_person_greatsword"));
        at(152, mc -> mc.player.getInventory().selected = 2);
        at(170, mc -> screenshot(mc, "3_epicfight_third_person_spear"));
        at(175, mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));

        // a combo with each weapon
        int tick = 180;

        for (int slot = 0; slot < WEAPONS.length; slot++) {
            final int s = slot;
            at(tick, mc -> {
                mc.player.getInventory().selected = s;
                command(mc, "kill @e[type=husk]");
                command(mc, "summon husk ^ ^ ^2.5 {NoAI:1b,PersistenceRequired:1b}");
            });
            tick += 10;
            at(tick - 2, mc -> logZombie(mc, "before " + WEAPONS[s]));

            for (int hit = 0; hit < 3; hit++) {
                at(tick, PortSmokeTest::attack);

                if (hit == 0) {
                    at(tick + 4, mc -> screenshot(mc, "4_attack_" + WEAPONS[s]));
                }

                if (hit == 1) {
                    at(tick + 7, mc -> screenshot(mc, "5_attack2_" + WEAPONS[s]));
                }

                tick += 14;
            }

            at(tick, mc -> logZombie(mc, "after " + WEAPONS[s]));
            tick += 10;
        }

        // first person in Epic Fight mode, from a fixed spot
        at(tick, mc -> {
            command(mc, "kill @e[type=husk]");
            command(mc, "tp @s -268.9 71 38.6 0 0");
            mc.player.getInventory().selected = 8;
            mc.options.hideGui = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        });
        tick += 20;
        at(tick, mc -> screenshot(mc, "6_fp_empty_pitch0"));
        at(tick + 1, mc -> command(mc, "tp @s -268.9 71 38.6 0 60"));
        at(tick + 10, mc -> screenshot(mc, "6_fp_empty_pitch60"));
        at(tick + 11, mc -> {
            command(mc, "tp @s -268.9 71 38.6 0 0");
            mc.player.getInventory().selected = 0;
        });
        tick += 30;
        at(tick, mc -> screenshot(mc, "6_fp_sword_idle"));
        at(tick + 2, PortSmokeTest::attack);

        for (int frame = 3; frame <= 12; frame += 3) {
            final int f = frame;
            at(tick + 2 + frame, mc -> screenshot(mc, "6_fp_sword_attack_" + f));
        }

        tick += 30;

        at(tick, mc -> {
            EpicFightMod.LOGGER.warn("[smoke test] done");
            mc.stop();
        });
        STEPS.sort(Comparator.comparingInt(Step::tick));
    }

    private static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null && ticksInWorld == 0) {
            return;
        }

        while (nextStep < STEPS.size() && STEPS.get(nextStep).tick() <= ticksInWorld) {
            try {
                STEPS.get(nextStep).action().accept(minecraft);
            } catch (Exception e) {
                EpicFightMod.LOGGER.error("[smoke test] step {} failed", nextStep, e);
            }

            nextStep++;
        }

        ticksInWorld++;
    }
}
