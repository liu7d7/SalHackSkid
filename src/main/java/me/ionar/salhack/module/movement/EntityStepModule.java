package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.Wrapper;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import org.lwjgl.input.Keyboard;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import net.minecraft.client.entity.EntityPlayerSP;

public class EntityStepModule extends Module
{

    public final Value<Integer> StepHeight = new Value<Integer>("StepHeight", new String[]{""}, "Step Higher", 1, 1, 3, 1);

    public EntityStepModule()
    {
        super("EntityStep", new String[]
                { "" }, "EntityStep", "NONE", 0xDB2468, ModuleType.MOVEMENT);
    }
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->{
        if(isEnabled()) {
            if(mc.player.getRidingEntity() != null && mc.player.getRidingEntity().stepHeight != StepHeight.getValue()) {
                mc.player.getRidingEntity().stepHeight = StepHeight.getValue();
            }
        }
    });

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            if(Wrapper.GetPlayer().getRidingEntity() != null) {
                Wrapper.GetPlayer().getRidingEntity().stepHeight = 1;
            }
        }catch(Exception ex){}
    }
}