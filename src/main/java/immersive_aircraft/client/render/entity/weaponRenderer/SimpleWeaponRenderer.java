package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.client.render.entity.renderer.AircraftEntityRenderer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.weapon.RotaryCannon;
import immersive_aircraft.entity.weapon.RotationalManager;
import immersive_aircraft.entity.weapon.Telescope;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.resources.BBModelBridge;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders a bbmodel weapon model at the weapon's mount transform.
 * If the model has a yaw/pitch/roll bone chain (rotary_cannon, telescope) and the
 * weapon has a RotationalManager, the bones are rotated procedurally, mirroring the
 * 1.20.1 animation variables (yaw -> -y rot, pitch -> +x rot, roll -> -z rot).
 */
public class SimpleWeaponRenderer extends WeaponRenderer<Weapon> {
    private final ResourceLocation id;

    /**
     * Mesh keys per rotating stage, rebuilt only when the baked model changes (resource reload)
     * instead of every frame.
     */
    private BBModelBridge.Baked keysFor;
    private List<String> baseKeys, yawKeys, pitchKeys, rollKeys;

    public SimpleWeaponRenderer(ResourceLocation id) {
        this.id = id;
    }

    private void updateKeys() {
        BBModelBridge.Baked baked = BBModelBridge.get(id);
        if (baked == keysFor) {
            return;
        }
        keysFor = baked;

        List<String> yaw = BBModelBridge.getSubtreeMeshKeys(id, "yaw");
        List<String> pitch = BBModelBridge.getSubtreeMeshKeys(id, "pitch");
        List<String> roll = BBModelBridge.getSubtreeMeshKeys(id, "roll");
        baseKeys = without(BBModelBridge.getSubtreeMeshKeys(id, BBModelBridge.ROOT_BONE), yaw);
        yawKeys = without(yaw, pitch);
        pitchKeys = without(pitch, roll);
        rollKeys = without(roll, Collections.emptyList());
    }

    private static List<String> without(List<String> keys, List<String> excluded) {
        Set<String> excludedSet = new HashSet<>(excluded);
        Set<String> result = new LinkedHashSet<>();
        for (String key : keys) {
            if (!excludedSet.contains(key)) {
                result.add(key);
            }
        }
        return new ArrayList<>(result);
    }

    @Override
    protected void renderModel(VehicleEntity entity, Weapon weapon, float tickDelta) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BBModelBridge.getTexture(id, 0));
        GlStateManager.disableLighting();

        RotationalManager manager = null;
        if (weapon instanceof RotaryCannon) {
            manager = ((RotaryCannon) weapon).getRotationalManager();
        } else if (weapon instanceof Telescope) {
            manager = ((Telescope) weapon).getRotationalManager();
        }

        if (manager == null || BBModelBridge.getPivot(id, "yaw") == null) {
            // fully static model
            for (String key : BBModelBridge.getSubtreeMeshKeys(id, BBModelBridge.ROOT_BONE)) {
                Mesh mesh = BBModelBridge.getMesh(id, key);
                if (mesh != null) {
                    AircraftEntityRenderer.renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        } else {
            updateKeys();

            // static base (everything not in the yaw chain)
            for (String key : baseKeys) {
                AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }

            float yaw = (float) (manager.getYaw(tickDelta) / Math.PI * 180.0f);
            float pitch = (float) (manager.getPitch(tickDelta) / Math.PI * 180.0f);
            float roll = (float) (manager.getRoll(tickDelta) / Math.PI * 180.0f);

            float[] yawPivot = BBModelBridge.getPivot(id, "yaw");
            float[] pitchPivot = BBModelBridge.getPivot(id, "pitch");
            float[] rollPivot = BBModelBridge.getPivot(id, "roll");

            GlStateManager.pushMatrix();
            rotateAround(yawPivot, -yaw, 0.0f, 1.0f, 0.0f);
            for (String key : yawKeys) {
                AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }

            GlStateManager.pushMatrix();
            rotateAround(pitchPivot, pitch, 1.0f, 0.0f, 0.0f);
            for (String key : pitchKeys) {
                AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }

            GlStateManager.pushMatrix();
            rotateAround(rollPivot, -roll, 0.0f, 0.0f, 1.0f);
            for (String key : rollKeys) {
                AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        GlStateManager.enableLighting();
    }

    private void rotateAround(float[] pivot, float degrees, float ax, float ay, float az) {
        if (pivot != null) {
            GlStateManager.translate(pivot[0], pivot[1], pivot[2]);
            GlStateManager.rotate(degrees, ax, ay, az);
            GlStateManager.translate(-pivot[0], -pivot[1], -pivot[2]);
        }
    }
}
