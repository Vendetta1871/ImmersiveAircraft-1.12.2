package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

public class EnginePowerMessage extends Message {
    private float engineTarget;

    public EnginePowerMessage() {
    }

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    @Override
    protected void decode(PacketBuffer b) {
        engineTarget = b.readFloat();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeFloat(engineTarget);
    }

    @Override
    public void receive(EntityPlayer e) {
        // the value comes straight from the client
        if (!Float.isFinite(engineTarget)) {
            return;
        }
        if (e.getLowestRidingEntity() instanceof EngineAircraft) {
            EngineAircraft entity = (EngineAircraft) e.getLowestRidingEntity();
            entity.setEngineTarget(MathHelper.clamp(engineTarget, 0.0f, 1.0f));
        }
    }
}
