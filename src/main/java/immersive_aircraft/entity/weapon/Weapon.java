package immersive_aircraft.entity.weapon;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.item.ItemStack;

public abstract class Weapon {
    private final VehicleEntity entity;
    private final ItemStack stack;
    private final WeaponMount mount;
    private final int slot;
    private int gunnerOffset;
    private long lastServerFire = -1000;

    public Weapon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        this.entity = entity;
        this.stack = stack;
        this.mount = mount;
        this.slot = slot;
    }

    public VehicleEntity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public WeaponMount getMount() {
        return mount;
    }

    public int getSlot() {
        return slot;
    }

    public int getGunnerOffset() {
        return gunnerOffset;
    }

    public void setGunnerOffset(int gunnerOffset) {
        this.gunnerOffset = gunnerOffset;
    }

    public abstract void tick();

    /**
     * Minimum ticks between two accepted fire requests on the server. The cooldowns in
     * {@link #clientFire(int)} only exist on the client, so this keeps a modified client from
     * firing faster; it is a bit below the client rate to tolerate network jitter.
     */
    protected int getMinFireInterval() {
        return 0;
    }

    /**
     * Server entry point for a fire request sent by the client.
     */
    public void serverFire(net.minecraft.util.math.Vec3d direction) {
        long time = entity.world.getTotalWorldTime();
        if (time - lastServerFire < getMinFireInterval()) {
            return;
        }
        lastServerFire = time;
        fire(direction);
    }

    public abstract void fire(net.minecraft.util.math.Vec3d direction);

    public abstract void clientFire(int index);
}
