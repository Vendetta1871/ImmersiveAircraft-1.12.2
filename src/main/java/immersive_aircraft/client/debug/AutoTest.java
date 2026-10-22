package immersive_aircraft.client.debug;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BambooHopperEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.CargoAirshipEntity;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.WarshipEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.network.c2s.CommandMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Development autotest, inert unless {@code IA_AUTOTEST} is set. Opens a flat creative world (or joins
 * {@code IA_AUTOTEST_SERVER=host:port}, where the player must be op), sets the scene up through commands so
 * both paths behave the same, and saves screenshots to {@code run/screenshots}:
 * <ul>
 *     <li>all aircraft lined up, then again after a resource reload (display lists are rebuilt)</li>
 *     <li>a dyed warship (dyeing ran a client-only method on the server before)</li>
 *     <li>a biplane take-off, first and third person, with the flight state logged</li>
 *     <li>an airship firing its rotary cannon</li>
 * </ul>
 * Weapons sit in every weapon slot, including slots without mounts, which crashed the entity tick before.
 * Every step logs with the {@code [IA-autotest]} prefix; {@code IA_AUTOTEST_STAY} keeps the game open at the end.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Side.CLIENT)
public final class AutoTest {
    private static final boolean ENABLED = System.getenv("IA_AUTOTEST") != null;
    private static final String SERVER = System.getenv("IA_AUTOTEST_SERVER");

    private static final int LINEUP_Z = 16;

    private static int stage;
    private static int ticks;
    private static int step;
    private static int stepTicks;
    private static final List<Step> STEPS = new ArrayList<>();

    private static VehicleEntity riding;
    private static int bulletsSeen;

    private AutoTest() {
    }

    private interface Action {
        /**
         * @return true when the step is done, false to run it again next tick
         */
        boolean run(Minecraft mc, int tick);
    }

    private static final class Step {
        final String name;
        final int delay;
        final Action action;

        Step(String name, int delay, Action action) {
            this.name = name;
            this.delay = delay;
            this.action = action;
        }
    }

    private static void step(String name, int delay, Action action) {
        STEPS.add(new Step(name, delay, action));
    }

    private static void log(String format, Object... args) {
        Main.LOGGER.info("[IA-autotest] " + format, args);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (stage == 0) {
            if (mc.currentScreen instanceof GuiMainMenu) {
                mc.gameSettings.pauseOnLostFocus = false;
                mc.gameSettings.tutorialStep = TutorialSteps.NONE;
                mc.getTutorial().setStep(TutorialSteps.NONE);
                if (SERVER != null) {
                    log("joining {}", SERVER);
                    mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, new ServerData("ia_autotest", SERVER, false)));
                } else {
                    WorldSettings settings = new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.FLAT);
                    settings.enableCommands();
                    mc.launchIntegratedServer("ia_autotest", "ia_autotest", settings);
                }
                buildScript();
                stage = 1;
            }
        } else if (stage == 1) {
            if (mc.player != null && mc.world != null && mc.currentScreen == null && ++ticks > 60) {
                log("in world, {} steps", STEPS.size());
                stage = 2;
            }
        } else if (stage == 2) {
            if (mc.player == null) {
                log("FAIL: disconnected during step {}", step < STEPS.size() ? STEPS.get(step).name : "end");
                mc.shutdown();
                return;
            }
            if (step >= STEPS.size()) {
                log("PASS: all steps finished");
                stage = 3;
                if (System.getenv("IA_AUTOTEST_STAY") == null) {
                    mc.shutdown();
                }
                return;
            }
            // command feedback would cover the screenshots; frames render after this
            mc.ingameGUI.getChatGUI().clearChatMessages(false);

            Step current = STEPS.get(step);
            if (++stepTicks < current.delay) {
                return;
            }
            if (current.action.run(mc, stepTicks - current.delay)) {
                log("step '{}' done", current.name);
                step++;
                stepTicks = 0;
            }
        }
    }

    private static void command(Minecraft mc, String command) {
        mc.player.sendChatMessage(command);
    }

    /**
     * Summons an aircraft and fills it through {@code /replaceitem}, since chat commands are cut at 256 characters:
     * coal in the boilers, {@code weapon} in every weapon slot and {@code ammo} in the first storage slot.
     */
    private static void summon(Minecraft mc, String name, Function<World, InventoryVehicleEntity> factory, int x, int z, float yaw, String weapon, String ammo) {
        command(mc, "/summon immersive_aircraft:" + name + " " + x + " 4 " + z + " {Rotation:[" + yaw + "f,0f]}");
        String target = "@e[type=immersive_aircraft:" + name + ",x=" + x + ",y=4,z=" + z + ",r=2,c=1]";
        VehicleInventoryDescription description = factory.apply(mc.world).getInventoryDescription();
        for (VehicleInventoryDescription.Slot slot : description.getSlots(VehicleInventoryDescription.SlotType.BOILER)) {
            command(mc, "/replaceitem entity " + target + " slot.container." + slot.index + " minecraft:coal 64");
        }
        if (weapon != null) {
            for (VehicleInventoryDescription.Slot slot : description.getSlots(VehicleInventoryDescription.SlotType.WEAPON)) {
                command(mc, "/replaceitem entity " + target + " slot.container." + slot.index + " " + weapon + " 1");
            }
        }
        List<VehicleInventoryDescription.Slot> storage = description.getSlots(VehicleInventoryDescription.SlotType.INVENTORY);
        if (ammo != null && !storage.isEmpty()) {
            command(mc, "/replaceitem entity " + target + " slot.container." + storage.get(0).index + " " + ammo + " 64");
        }
    }

    private static <T extends Entity> T nearest(Minecraft mc, Class<T> type) {
        T best = null;
        for (T entity : mc.world.getEntities(type, e -> true)) {
            if (best == null || entity.getDistanceSq(mc.player) < best.getDistanceSq(mc.player)) {
                best = entity;
            }
        }
        return best;
    }

    private static void screenshot(Minecraft mc, String name) {
        ScreenShotHelper.saveScreenshot(mc.gameDir, "ia_" + name + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        log("screenshot {} (fps {})", name, Minecraft.getDebugFPS());
    }

    private static boolean mount(Minecraft mc, Class<? extends VehicleEntity> type) {
        VehicleEntity vehicle = nearest(mc, type);
        if (vehicle == null) {
            log("FAIL: no {} found", type.getSimpleName());
            return true;
        }
        mc.playerController.interactWithEntity(mc.player, vehicle, EnumHand.MAIN_HAND);
        riding = vehicle;
        return true;
    }

    private static void logFlight(EngineAircraft aircraft, int tick) {
        log("flight t={} y={} onGround={} speed={} pitch={} engine={}/{} fuel={} riding={}",
                tick, String.format("%.2f", aircraft.posY), aircraft.onGround,
                String.format("%.3f", aircraft.getVelocity().length()), String.format("%.1f", aircraft.getPitch()),
                String.format("%.2f", aircraft.getEnginePower()), aircraft.getEngineTarget(),
                aircraft.getFuelUtilization(), aircraft.isPassenger(Minecraft.getMinecraft().player));
    }

    private static void buildScript() {
        // IA_AUTOTEST_SCENE=warship: only two warships, with and without weapons, from a distance
        if ("warship".equals(System.getenv("IA_AUTOTEST_SCENE"))) {
            step("setup", 0, (mc, t) -> {
                command(mc, "/gamemode 1");
                command(mc, "/time set 6000");
                command(mc, "/gamerule doDaylightCycle false");
                command(mc, "/kill @e[type=!player]");
                command(mc, "/tp @p 0 8 0 0 10");
                return true;
            });
            step("summon", 20, (mc, t) -> {
                summon(mc, "warship", WarshipEntity::new, -8, 20, 180, null, null);
                summon(mc, "warship", WarshipEntity::new, 8, 20, 180, "immersive_aircraft:bomb_bay", null);
                command(mc, "/tp @p 0 8 0 0 10");
                return true;
            });
            step("screenshot", 80, (mc, t) -> {
                screenshot(mc, "warships");
                return true;
            });
            return;
        }

        step("setup", 0, (mc, t) -> {
            command(mc, "/gamemode 1");
            command(mc, "/time set 6000");
            command(mc, "/gamerule doDaylightCycle false");
            command(mc, "/gamerule doMobSpawning false");
            command(mc, "/weather clear 100000");
            command(mc, "/kill @e[type=!player]");
            command(mc, "/tp @p 0 4 0 0 0");
            return true;
        });

        // Lineup; the airships carry a weapon in each weapon slot, the second one has no mount
        step("summon lineup", 20, (mc, t) -> {
            summon(mc, "biplane", BiplaneEntity::new, -24, LINEUP_Z, 180, null, null);
            summon(mc, "gyrodyne", GyrodyneEntity::new, -16, LINEUP_Z, 180, null, null);
            summon(mc, "quadrocopter", QuadrocopterEntity::new, -9, LINEUP_Z, 180, null, null);
            summon(mc, "airship", AirshipEntity::new, -3, LINEUP_Z, 180, "immersive_aircraft:rotary_cannon", "minecraft:gunpowder");
            summon(mc, "cargo_airship", CargoAirshipEntity::new, 4, LINEUP_Z, 180, "immersive_aircraft:heavy_crossbow", "minecraft:arrow");
            summon(mc, "bamboo_hopper", BambooHopperEntity::new, 12, LINEUP_Z, 180, null, null);
            summon(mc, "warship", WarshipEntity::new, 24, LINEUP_Z, 180, "immersive_aircraft:bomb_bay", "minecraft:tnt");
            command(mc, "/tp @p 0 9 -8 0 15");
            return true;
        });
        step("lineup screenshot", 80, (mc, t) -> {
            log("aircraft in client world: {}", mc.world.getEntities(VehicleEntity.class, e -> true).size());
            screenshot(mc, "lineup");
            return true;
        });

        // F3+T drops the meshes and their display lists
        step("resource reload", 0, (mc, t) -> {
            mc.refreshResources();
            return true;
        });
        step("lineup after reload", 40, (mc, t) -> {
            screenshot(mc, "lineup_reloaded");
            return true;
        });

        // Third person close-ups
        step("close-up position", 0, (mc, t) -> {
            command(mc, "/tp @p -10 7 6 30 20");
            return true;
        });
        step("close-up screenshot", 30, (mc, t) -> {
            screenshot(mc, "closeup_small");
            return true;
        });

        // Dye the warship (EnumDyeColor#getColorValue is client-only)
        step("dye warship", 0, (mc, t) -> {
            command(mc, "/tp @p 24 5 " + (LINEUP_Z - 5) + " 0 10");
            command(mc, "/replaceitem entity @p slot.hotbar.0 minecraft:dye 1 1");
            return true;
        });
        step("interact warship", 20, (mc, t) -> {
            mc.player.inventory.currentItem = 0;
            WarshipEntity warship = nearest(mc, WarshipEntity.class);
            if (warship != null) {
                mc.playerController.interactWithEntity(mc.player, warship, EnumHand.MAIN_HAND);
            }
            return true;
        });
        step("check dye", 20, (mc, t) -> {
            WarshipEntity warship = nearest(mc, WarshipEntity.class);
            log("warship dye color {}", warship == null ? null : Integer.toHexString(warship.getDyeColor()));
            command(mc, "/replaceitem entity @p slot.hotbar.0 minecraft:air");
            command(mc, "/tp @p 24 10 " + (LINEUP_Z - 14) + " 0 15");
            return true;
        });
        step("warship screenshot", 30, (mc, t) -> {
            screenshot(mc, "warship_dyed");
            return true;
        });

        // Warship turret: a mob boards as pilot, the player becomes the gunner (second passenger)
        step("warship pilot", 0, (mc, t) -> {
            command(mc, "/summon pig 24 4 " + LINEUP_Z + " {NoAI:1b}");
            command(mc, "/tp @p 24 4 " + (LINEUP_Z - 4) + " 0 0");
            return true;
        });
        step("mount warship", 30, (mc, t) -> {
            WarshipEntity warship = nearest(mc, WarshipEntity.class);
            log("warship passengers before: {}", warship == null ? null : warship.getPassengers().size());
            return mount(mc, WarshipEntity.class);
        });
        step("aim turret", 10, (mc, t) -> {
            if (!(riding instanceof WarshipEntity) || !riding.isPassenger(mc.player)) {
                log("FAIL: not riding the warship");
                return true;
            }
            WarshipEntity warship = (WarshipEntity) riding;
            mc.player.rotationYaw = warship.getYaw() + 40.0f;
            mc.player.rotationPitch = 20.0f;
            mc.player.rotationYawHead = mc.player.rotationYaw;
            if (t == 30) {
                log("warship passengers: {}", warship.getPassengers());
                log("turret gunner={} yaw={} pitch={}", warship.isTurretGunner(mc.player),
                        String.format("%.1f", warship.turretYaw.getSmooth(1.0f)), String.format("%.1f", warship.turretPitch.getSmooth(1.0f)));
                mc.gameSettings.thirdPersonView = 1;
            }
            if (t == 45) {
                screenshot(mc, "warship_turret_back");
                mc.gameSettings.thirdPersonView = 2;
            }
            if (t == 60) {
                screenshot(mc, "warship_turret_front");
                mc.gameSettings.thirdPersonView = 0;
                return true;
            }
            return false;
        });
        step("dismount warship", 0, (mc, t) -> {
            NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, riding.getVelocity()));
            riding = null;
            return true;
        });

        // Biplane take-off: the client steers, the server only has to accept it
        step("summon flight biplane", 0, (mc, t) -> {
            summon(mc, "biplane", BiplaneEntity::new, 0, -12, 180, null, null);
            command(mc, "/tp @p 0 4 -10 180 0");
            return true;
        });
        step("mount biplane", 30, (mc, t) -> mount(mc, BiplaneEntity.class));
        step("fly", 10, (mc, t) -> {
            if (!(riding instanceof EngineAircraft)) {
                log("FAIL: not riding a biplane");
                return true;
            }
            EngineAircraft aircraft = (EngineAircraft) riding;
            if (t == 0) {
                log("mounted={}", aircraft.isPassenger(mc.player));
            }
            aircraft.setEngineTarget(1.0f);
            // nose up once there is speed
            if (aircraft.getVelocity().length() > 0.5) {
                aircraft.setPitch(-12.0f);
            }
            if (t % 10 == 0) {
                logFlight(aircraft, t);
            }
            if (t == 160) {
                screenshot(mc, "flight_first_person");
                mc.gameSettings.thirdPersonView = 1;
            }
            if (t == 190) {
                screenshot(mc, "flight_third_person");
                mc.gameSettings.thirdPersonView = 0;
                log("flight result: y={} (start 4)", String.format("%.2f", aircraft.posY));
                return true;
            }
            return false;
        });
        step("dismount", 0, (mc, t) -> {
            if (riding == null) {
                return true;
            }
            NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, riding.getVelocity()));
            return true;
        });
        step("check dismount", 20, (mc, t) -> {
            log("dismounted={}", riding != null && !riding.isPassenger(mc.player));
            riding = null;
            return true;
        });

        // Rotary cannon of the lineup airship
        step("go to airship", 0, (mc, t) -> {
            command(mc, "/tp @p -3 4 " + (LINEUP_Z - 3) + " 0 0");
            return true;
        });
        step("mount airship", 30, (mc, t) -> mount(mc, AirshipEntity.class));
        step("shoot", 10, (mc, t) -> {
            if (!(riding instanceof AirshipEntity) || !riding.isPassenger(mc.player)) {
                log("FAIL: not riding the airship");
                return true;
            }
            // look along the airship, away from the lineup
            mc.player.rotationYaw = riding.getYaw();
            mc.player.rotationPitch = -5.0f;
            ((AirshipEntity) riding).clientFireWeapons(mc.player);
            bulletsSeen = Math.max(bulletsSeen, mc.world.getEntities(BulletEntity.class, e -> true).size());
            if (t == 30) {
                mc.gameSettings.thirdPersonView = 1;
            }
            if (t == 38) {
                screenshot(mc, "shooting");
            }
            if (t == 40) {
                mc.gameSettings.thirdPersonView = 0;
                log("weapons on airship: {}, bullets seen: {}", ((AirshipEntity) riding).getWeapons().size(), bulletsSeen);
                return true;
            }
            return false;
        });
        step("dismount airship", 0, (mc, t) -> {
            if (riding != null) {
                NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, riding.getVelocity()));
            }
            return true;
        });

        // Let everything tick for a while: the weapons in mountless slots must not crash the entities
        step("idle", 100, (mc, t) -> {
            log("aircraft alive: {}", mc.world.getEntities(VehicleEntity.class, e -> true).size());
            return true;
        });
    }
}
