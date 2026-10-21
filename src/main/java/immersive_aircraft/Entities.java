package immersive_aircraft;

import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BambooHopperEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.CargoAirshipEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.entity.WarshipEntity;
import net.minecraftforge.fml.common.registry.EntityRegistry;

/**
 * 1.12.2 port: entities are registered directly with EntityRegistry during preInit
 * (1.12.2 has no entity RegistryEvent worth using here). Hitbox sizes and fire
 * immunity move into the entity constructors (Entity.setSize / isImmuneToFire).
 */
public class Entities {
    private static int nextId = 0;

    public static void bootstrap() {
        // tracking ranges (blocks) and update intervals follow the 1.20.1 clientTrackingRange (chunks)
        register("gyrodyne", GyrodyneEntity.class, 192, 3);
        register("biplane", BiplaneEntity.class, 192, 3);
        register("airship", AirshipEntity.class, 192, 3);
        register("cargo_airship", CargoAirshipEntity.class, 192, 3);
        register("quadrocopter", QuadrocopterEntity.class, 160, 3);
        register("warship", WarshipEntity.class, 256, 3);
        register("bamboo_hopper", BambooHopperEntity.class, 224, 3);
        register("bullet", immersive_aircraft.entity.bullet.BulletEntity.class, 160, 10);
        register("tiny_tnt", immersive_aircraft.entity.bullet.TinyTNT.class, 160, 10);
    }

    private static void register(String name, Class<? extends net.minecraft.entity.Entity> entityClass, int trackingRange, int updateFrequency) {
        EntityRegistry.registerModEntity(Main.locate(name), entityClass, Main.MOD_ID + "." + name, nextId++, Main.instance, trackingRange, updateFrequency, true);
    }
}
