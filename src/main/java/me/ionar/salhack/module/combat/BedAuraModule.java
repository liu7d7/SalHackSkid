//TODO r333mo implement placing / targeting at some point

package me.ionar.salhack.module.combat;

// Importing stuff
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.HotbarCacheModule;
import me.ionar.salhack.module.world.StashLoggerModule;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.util.EnumHand;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.util.BlockInteractionHelper;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import scala.Int;

import java.util.ArrayList;
import java.util.Comparator;


// Setting up class and objects
public class BedAuraModule extends Module {
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[]
            {"R"}, "Radius for right clicking beds", 4, 0, 5, 0);
    public final Value<Float> RequiredHealth = new Value<Float>("RequiredHealth", new String[]
            {"RH"}, "RequiredHealth for BedAura to function, must be above or equal to this amount.", 11.0f, 0.0f, 20.0f, 1.0f);
    public final Value<Boolean> ToggleHotbarCache = new Value<Boolean>("ToggleHotbarCache", new String[]
            {"THC"}, "Automatically toggles on HotbarCache if not already enabled", true);


    public BedAuraModule() {
        super("BedAura", new String[]{"BedAura"}, "Automatically right clicks beds.", "NONE", 0xFFFB11, Module.ModuleType.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (ToggleHotbarCache.getValue())
        {
            final Module mod = ModuleManager.Get().GetMod(HotbarCacheModule.class);

            if (!mod.isEnabled())
                mod.toggle();
        }
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;

        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                .filter(p_Pos -> IsValidBlockPos(p_Pos))
                .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                .orElse(null);

        if (l_ClosestPos != null)
        {
            boolean hasBed = mc.player.getHeldItemMainhand().getItem() == Items.BED;

            if (!hasBed && PlayerUtil.GetHealthWithAbsorption() <= RequiredHealth.getValue())
            {
                for (int i = 0; i < 9; ++i)
                {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty())
                        continue;

                    if (stack.getItem() == Items.BED)
                    {
                        hasBed = true;
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

            if (!hasBed)
                return;

            p_Event.cancel();

            final double l_Pos[] = EntityUtil.calculateLookAt(
                    l_ClosestPos.getX(),
                    l_ClosestPos.getY(),
                    l_ClosestPos.getZ(),
                    mc.player);

            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    l_ClosestPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F)
            );
        }
    });

    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockBed)
            return true;

        return false;
    }
}