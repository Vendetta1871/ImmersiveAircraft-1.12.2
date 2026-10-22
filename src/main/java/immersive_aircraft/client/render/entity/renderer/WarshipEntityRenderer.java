package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.WarshipEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.resources.BBModelBridge;
import immersive_aircraft.util.Utils;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Warship renderer, bbmodel-based. The static pose is baked into meshes by
 * {@link BBModelBridge}; animations are procedural, mirroring the 1.20.1 animation
 * variables (balloon roll/pitch, engine_rotation propellers, turret yaw/pitch, sails).
 */
public class WarshipEntityRenderer<T extends WarshipEntity> extends DyeableVehicleEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/warship.bbmodel");

    private final ResourceLocation texture = Main.locate("textures/entity/warship.png");
    private final ResourceLocation colorTexture = Main.locate("textures/entity/warship_color.png");

    private static final String[] BALLOONS = {"left_balloon", "right_balloon", "centre_baloon"};
    private static final String[] PROPELLERS = {"propeller2", "propeller3", "propeller4"};
    private static final String[] SAILS = {"net", "tail_fin_flag", "nose_fin_top_flag", "nose_fin_bottom_flag"};

    /**
     * Envelope subtree keys rendered separately (animations, dye, sails).
     */
    private static final Set<String> ENVELOPE_SPECIAL = new HashSet<>(Arrays.asList(
            "propeller2", "propeller3", "propeller4",
            "net", "tail_fin_flag", "nose_fin_top_flag", "nose_fin_bottom_flag",
            "left_balloon", "left_balloon#1", "right_balloon", "right_balloon#1", "centre_baloon", "centre_baloon#1"
    ));

    private final Model model = new Model()
            .add(
                    new Object(id, "envelope")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta) -> {
                                        // 1.20.1: balloon_roll/pitch variables on the envelope bone.
                                        // The envelope's static pose contains a 180° flip, which negates
                                        // the local x/z axes in model space - hence the negations here.
                                        float roll = (float) (Utils.cosNoise((entity.ticksExisted + tickDelta) * 0.01f) * 0.2f) + entity.getRoll(tickDelta) * 0.5f;
                                        float pitch = (float) (Utils.cosNoise(77.0f + (entity.ticksExisted + tickDelta) * 0.02f) * 0.2f);

                                        float[] pivot = BBModelBridge.getPivot(id, "envelope");
                                        GlStateManager.translate(pivot[0], pivot[1], pivot[2]);
                                        GlStateManager.rotate(-pitch, 1.0f, 0.0f, 0.0f);
                                        GlStateManager.rotate(-roll, 0.0f, 0.0f, 1.0f);
                                        GlStateManager.translate(-pivot[0], -pivot[1], -pivot[2]);
                                    }
                            )
                            .setRenderConsumer(this::renderEnvelope)
            )
            .add(
                    new Object(id, "gondola").setRenderConsumer(this::renderGondola)
            );

    /**
     * Mesh key lists derived from the model tree, rebuilt only when the baked model changes
     * (resource reload) instead of every frame.
     */
    private BBModelBridge.Baked keysFor;
    private List<String> envelopeKeys, gondolaKeys, turretKeys, armsKeys, boltKeys;
    private List<String> leftLimbKeys, leftHingeKeys, rightLimbKeys, rightHingeKeys;

    private void updateKeys() {
        BBModelBridge.Baked baked = BBModelBridge.get(id);
        if (baked == keysFor) {
            return;
        }
        keysFor = baked;

        envelopeKeys = filterKeys(baked, "envelope", ENVELOPE_SPECIAL, true);

        // the whole turret moves: arms_joint (pitch) holds turret_seat (yaw), which holds the crossbow
        Set<String> gondolaExcluded = new HashSet<>(subtree(baked, "arms_joint"));
        gondolaExcluded.add("propeller");
        gondolaKeys = filterKeys(baked, "gondola", gondolaExcluded, true);

        armsKeys = filterKeys(baked, "arms_joint", subtree(baked, "turret_seat"), true);

        Set<String> turretExcluded = new HashSet<>(subtree(baked, "bolt"));
        turretExcluded.addAll(subtree(baked, "left_limb_base"));
        turretExcluded.addAll(subtree(baked, "right_limb_base"));
        turretKeys = filterKeys(baked, "turret_seat", turretExcluded, true);

        boltKeys = filterKeys(baked, "bolt", Collections.emptySet(), false);

        leftLimbKeys = filterKeys(baked, "left_limb_base", subtree(baked, "string_hinge"), false);
        leftHingeKeys = filterKeys(baked, "string_hinge", Collections.emptySet(), false);
        rightLimbKeys = filterKeys(baked, "right_limb_base", subtree(baked, "string_hinge2"), false);
        rightHingeKeys = filterKeys(baked, "string_hinge2", Collections.emptySet(), false);
    }

    private static List<String> subtree(BBModelBridge.Baked baked, String bone) {
        List<String> keys = baked.subtreeMeshKeys.get(bone);
        return keys == null ? Collections.emptyList() : keys;
    }

    /**
     * Unique mesh keys of the bone's subtree without the excluded ones, optionally only those of
     * the base texture (index 0).
     */
    private static List<String> filterKeys(BBModelBridge.Baked baked, String bone, Collection<String> excluded, boolean baseTextureOnly) {
        Set<String> keys = new LinkedHashSet<>();
        for (String key : subtree(baked, bone)) {
            if (!excluded.contains(key) && (!baseTextureOnly || baked.meshTexture.get(key) == 0)) {
                keys.add(key);
            }
        }
        return new ArrayList<>(keys);
    }

    public WarshipEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 2.5f;
    }

    private void renderEnvelope(T entity, float tickDelta) {
        updateKeys();
        bindTexture(getTexture(entity));
        GlStateManager.enableCull();

        // static envelope structure
        for (String key : envelopeKeys) {
            renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
        }

        // balloons (dyeable)
        for (String balloon : BALLOONS) {
            Mesh uncolored = BBModelBridge.getMesh(id, balloon);
            if (uncolored != null) {
                bindTexture(getTexture(entity));
                renderUndyed(entity, uncolored);
            }
            Mesh colored = BBModelBridge.getMesh(id, balloon + "#1");
            if (colored != null) {
                bindTexture(colorTexture);
                renderDyed(entity, colored, false, true);
            }
        }

        // sails (net + flags), colored by the dye slot
        bindTexture(getTexture(entity));
        GlStateManager.disableCull();
        float time = entity.ticksExisted + tickDelta;
        float[] rgb = getDyeSlotColor(entity);
        for (String sail : SAILS) {
            Mesh mesh = BBModelBridge.getMesh(id, sail);
            if (mesh != null) {
                float distanceScale = sail.equals("net") ? (float) (0.005f + entity.getVelocity().length() * 0.05f) : 0.025f;
                renderSailObjectBB(mesh, time, distanceScale, 0.0f, rgb[0], rgb[1], rgb[2], 1.0f);
            }
        }

        // spinning propellers
        bindTexture(getTexture(entity));
        GlStateManager.enableCull();
        for (String propeller : PROPELLERS) {
            Mesh mesh = BBModelBridge.getMesh(id, propeller);
            float[] pivot = BBModelBridge.getPivot(id, propeller);
            if (mesh != null && pivot != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(pivot[0], pivot[1], pivot[2]);
                GlStateManager.rotate(entity.engineRotation.getSmooth(tickDelta) * 40.0f, 0.0f, 0.0f, 1.0f);
                GlStateManager.translate(-pivot[0], -pivot[1], -pivot[2]);
                renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.popMatrix();
            }
        }
    }

    private void renderGondola(T entity, float tickDelta) {
        updateKeys();
        bindTexture(getTexture(entity));
        GlStateManager.enableCull();

        // static gondola structure
        for (String key : gondolaKeys) {
            renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
        }

        // engine propeller
        Mesh propeller = BBModelBridge.getMesh(id, "propeller");
        float[] propellerPivot = BBModelBridge.getPivot(id, "propeller");
        if (propeller != null && propellerPivot != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(propellerPivot[0], propellerPivot[1], propellerPivot[2]);
            GlStateManager.rotate(entity.engineRotation.getSmooth(tickDelta) * 40.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.translate(-propellerPivot[0], -propellerPivot[1], -propellerPivot[2]);
            renderObject(propeller, 1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }

        // turret, nested as in the model (1.20.1 animation variables): turret_pitch rotates
        // arms_joint, turret_yaw rotates turret_seat inside it (turret_yaw -> -yaw,
        // turret_pitch -> +pitch in model space)
        float[] armsPivot = BBModelBridge.getPivot(id, "arms_joint");
        float[] seatPivot = BBModelBridge.getPivot(id, "turret_seat");
        if (armsPivot != null && seatPivot != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(armsPivot[0], armsPivot[1], armsPivot[2]);
            GlStateManager.rotate(entity.turretPitch.getSmooth(tickDelta), 1.0f, 0.0f, 0.0f);
            GlStateManager.translate(-armsPivot[0], -armsPivot[1], -armsPivot[2]);

            for (String key : armsKeys) {
                renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(seatPivot[0], seatPivot[1], seatPivot[2]);
            GlStateManager.rotate(-entity.turretYaw.getSmooth(tickDelta), 0.0f, 1.0f, 0.0f);
            GlStateManager.translate(-seatPivot[0], -seatPivot[1], -seatPivot[2]);

            for (String key : turretKeys) {
                renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }

            // crossbow reload animation (1.20.1: turret_cooldown variable)
            float loaded = 1.0f - entity.getTurret().getCooldown();

            // bolt position
            if (!boltKeys.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0f, 0.0f, loaded * 2.0f / 16.0f);
                for (String key : boltKeys) {
                    renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
                }
                GlStateManager.popMatrix();
            }

            renderLimb(id, "left_limb_base", "string_hinge", -loaded * 4.0f, loaded * 20.0f, leftLimbKeys, leftHingeKeys);
            renderLimb(id, "right_limb_base", "string_hinge2", loaded * 5.0f, -loaded * 20.0f, rightLimbKeys, rightHingeKeys);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    /**
     * Renders a limb subtree rotated around the limb base pivot, with the string hinge
     * subtree nested inside with its own rotation (crossbow reload animation).
     */
    private void renderLimb(ResourceLocation id, String limbBone, String hingeBone, float limbDegrees, float hingeDegrees, List<String> limbKeys, List<String> hingeKeys) {
        if (limbKeys.isEmpty() && hingeKeys.isEmpty()) {
            return;
        }
        float[] limbPivot = BBModelBridge.getPivot(id, limbBone);

        GlStateManager.pushMatrix();
        if (limbPivot != null) {
            GlStateManager.translate(limbPivot[0], limbPivot[1], limbPivot[2]);
            GlStateManager.rotate(limbDegrees, 0.0f, 1.0f, 0.0f);
            GlStateManager.translate(-limbPivot[0], -limbPivot[1], -limbPivot[2]);
        }
        for (String key : limbKeys) {
            renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
        }

        if (!hingeKeys.isEmpty()) {
            float[] hingePivot = BBModelBridge.getPivot(id, hingeBone);
            GlStateManager.pushMatrix();
            if (hingePivot != null) {
                GlStateManager.translate(hingePivot[0], hingePivot[1], hingePivot[2]);
                GlStateManager.rotate(hingeDegrees, 0.0f, 1.0f, 0.0f);
                GlStateManager.translate(-hingePivot[0], -hingePivot[1], -hingePivot[2]);
            }
            for (String key : hingeKeys) {
                renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private float[] getDyeSlotColor(T entity) {        ItemStack stack = entity.getSlots(VehicleInventoryDescription.SlotType.DYE).get(0);
        EnumDyeColor color;
        if (stack.getItem() instanceof ItemDye) {
            color = EnumDyeColor.byDyeDamage(stack.getMetadata());
        } else {
            color = EnumDyeColor.WHITE;
        }
        return color.getColorComponentValues();
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return texture;
    }

    @Override
    public Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    public immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity) {
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.0f, 0.0f);
    }
}
