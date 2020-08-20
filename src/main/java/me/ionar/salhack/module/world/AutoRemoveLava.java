// r333mo WIP doesn't function properly right now


package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.mixin.client.MixinBlockLiquid;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;


public class AutoRemoveLava extends Module {

    public final Value<Integer> PlaceRadius = new Value<Integer>("PlaceRadius", new String[]{""}, "PlaceRadius for lava remover to target.", 0, 0, 5, 1);


    public AutoRemoveLava() {
        super("AutoRemoveLava", new String[]{"LavaRemover"}, "Automatically places netherrack on lava source blocks", "NONE", 0xFFFB11, ModuleType.WORLD);
    }
}