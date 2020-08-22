
package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.util.entity.PlayerUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import com.google.common.eventbus.Subscribe;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.world.NukerBypassModule;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.item.Items;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket.Mode;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class BleachElytraFlyModule extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "Mode of elytrafly", Modes.Normal);
    public final Value<Float> Speed = new Value<Float>("Speed", new String[] {"Speed"}, "Speed", 3.0f, 0.1f, 10.0f, 1.0f);
    public enum Modes {
        Normal,
        Control,
        BrMomentum,
    }

    public BleachElytraFlyModule() {
        super("BleachElytraFly", new String[] {"BleachElytraFly"}, "For the guy that wanted this for some reason idek why", "NONE", -1, ModuleType.MOVEMENT);
    }

    BlockPos _lastPlayerPos = null;

    @EventHandler
    private Listener<EventPlayerMotionUpdate> onPlayerUpdate = new Listener<>(event -> {
        /* Cancel the retarded auto elytra movement */
        if (Mode.getValue() == Modes.Normal && !mc.player.onGround) {
            if (!mc.gameSettings.keyBindJump.isPressed() && !mc.gameSettings.keyBindSneak.isPressed()) {
                _lastPlayerPos = PlayerUtil.GetLocalPlayerPosFloored();
                mc.player.connection.sendPacket(new CPacketPlayer.Position(_lastPlayerPos.getX(), _lastPlayerPos.getY(), _lastPlayerPos.getZ(), true));
                mc.player.setPosition(_lastPlayerPos.getX(), _lastPlayerPos.getY(), _lastPlayerPos.getZ());
            }

            if (!mc.gameSettings.keyBindBack.isPressed() && !mc.gameSettings.keyBindLeft.isPressed()
                    && !mc.gameSettings.keyBindRight.isPressed() && !mc.gameSettings.keyBindForward.isPressed()) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(_lastPlayerPos.getX(), _lastPlayerPos.getY(), _lastPlayerPos.getZ(), true));
                mc.player.setPosition(_lastPlayerPos.getX(), _lastPlayerPos.getY(), _lastPlayerPos.getZ());
            }
        }
    });

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event -> {
        Vec3d vec3d = new Vec3d(0,0,Speed.getValue())
                .rotateX(getSetting(0).asMode().mode == 1 ? 0 : -(float) Math.toRadians(mc.player.rotationPitch))
                .rotateY(-(float) Math.toRadians(mc.player.yaw));

        //if (getSetting(0).toMode().mode == 1) vec3d = new Vec3d(vec3d.x, 0, vec3d.z);

        if (mc.player.isFallFlying()) {
            if (getSetting(0).asMode().mode == 0 && mc.options.keyForward.isPressed()) {
                mc.player.setVelocity(
                        mc.player.().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
                        mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
                        mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
            } else if (getSetting(0).asMode().mode == 1) {
                if (mc.options.keyBack.isPressed()) vec3d = vec3d.multiply(-1);
                if (mc.options.keyLeft.isPressed()) vec3d = vec3d.rotateY((float) Math.toRadians(90));
                if (mc.options.keyRight.isPressed()) vec3d = vec3d.rotateY(-(float) Math.toRadians(90));
                if (mc.options.keyJump.isPressed()) vec3d = vec3d.add(0, getSetting(1).asSlider().getValue(), 0);
                if (mc.options.keySneak.isPressed()) vec3d = vec3d.add(0, -getSetting(1).asSlider().getValue(), 0);
                if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
                        && !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()
                        && !mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed()) vec3d = Vec3d.ZERO;
                mc.player.setVelocity(vec3d.multiply(2));
            }
        } else if (getSetting(0).asMode().mode == 2 && !mc.player.onGround
                && mc.player.inventory.getArmorStack(2).getItem() == Items.ELYTRA && mc.player.fallDistance > 0.5) {
            /* I tried packet mode and got whatever the fuck **i mean frick** this is */
            if (mc.options.keySneak.isPressed()) return;
            mc.player.setVelocity(vec3d);
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
        }
    });
}
