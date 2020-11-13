package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.Listener;
import me.zero.alpine.fork.listener.EventHandler;
import me.ionar.salhack.module.Value;
/**
 * Credits to 0x22 for this.
 */

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInput;

public class EntitySpeedModule extends Module
{
    public final Value<Float> EntitySpeed = new Value<Float>("Speed", new String[]{""}, "Make entity speedyer", 1.0f, 0.0f, 10.0f, 0.01f);

    public EntitySpeedModule()
    {
        super("EntitySpeed", new String[]{""},"Espeed", "NONE", -1, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if(isEnabled()) {
            if (mc.player.getRidingEntity() != null) {
                MovementInput movementInput = mc.player.movementInput;
                double forward = movementInput.moveForward;
                double strafe = movementInput.moveStrafe;
                float yaw = mc.player.rotationYaw;
                if ((forward == 0.0D) && (strafe == 0.0D)) {
                    mc.player.getRidingEntity().motionX = 0.0D;
                    mc.player.getRidingEntity().motionZ = 0.0D;
                }
                else{
                    if (forward != 0.0D) {
                        if (strafe > 0.0D) {
                            yaw += (forward > 0.0D ? -45 : 45);
                        }else if (strafe < 0.0D) {
                            yaw += (forward > 0.0D ? 45 : -45);
                        }
                        strafe = 0.0D;
                        if (forward > 0.0D) {
                            forward = 1.0D;
                        }else if (forward < 0.0D) {
                            forward = -1.0D;
                        }
                    }
                    double sin = Math.sin(Math.toRadians(yaw + 90.0F));
                    double cos = Math.cos(Math.toRadians(yaw + 90.0F));
                    mc.player.getRidingEntity().motionX = (forward * EntitySpeed.getValue() * cos + strafe * EntitySpeed.getValue() * sin);
                    mc.player.getRidingEntity().motionZ = (forward * EntitySpeed.getValue() * sin - strafe * EntitySpeed.getValue() * cos);
                }
            }
        }
    });
}