package me.ionar.salhack.module.world;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class HasteModule extends Module {
    public final Value<Integer> HasteLVL = new Value<Integer>("HasteLevel", new String[] {"Levelhaste"}, "Gives haste", 1, 1, 3, 1);

    public HasteModule() {
        super ("Haste", new String[] {""}, "gives you haste", "NONE", -1, ModuleType.HIGHWAY);
    }

    public void onDisable() {
        mc.player.removeActivePotionEffect(Potion.REGISTRY.getObjectById(3));
    }

    @EventHandler
    private Listener<EventClientTick> onTick = new Listener<>(event -> {
        mc.player.addPotionEffect(new PotionEffect(Potion.REGISTRY.getObjectById(3), 4206969, HasteLVL.getValue()));
    });
}
