package me.ionar.salhack.module.world;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ibxm.Player;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.BlockManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;

public class AutoTunnelModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {""}, "Mode", Modes.Tunnel1x2);
    public final Value<MiningModes> MiningMode = new Value<MiningModes>("MiningMode", new String[] {""}, "Mode of mining to use", MiningModes.Normal);
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[] {"Render"}, "Visualizes where blocks are to be destroyed", true);
    public final Value<Boolean> PauseAutoWalk = new Value<Boolean>("PauseAutoWalk", new String[] {"PauseAutoWalk"}, "Pauses autowalk if you are mining", true);
    public final Value<Float> TimerForAutoWalk = new Value<Float>("TimerForAutoWalk", new String[]{"TimerForAutoWalk"}, "Pauses to cancel packets", 0f, 0f, 6f, 0.1f);
    public final Value<Boolean> UseTimerForAutoWalk = new Value<Boolean>("UseTimerForAutoWalk", new String[]{"UseTimerForAutoWalk"}, "do we pause to cancel packets?", false);
    public enum Modes
    {
        Tunnel1x2,
        Tunnel2x2,
        Tunnel1x3,
        Tunnel2x3,
        Tunnel3x3,
        Tunnel4wGuards,
        Tunnel3wGuards,
        Tunnel2wGuards,
    }
    
    public enum MiningModes
    {
        Normal,
        Packet,
    }
    
    public AutoTunnelModule()
    {
        super("AutoTunnel", new String[] {""}, "Automatically mines different kind of 2d tunnels, in the direction you're facing", "NONE", -1, ModuleType.HIGHWAY);
    }
    
    private List<BlockPos> _blocksToDestroy = new CopyOnWriteArrayList<>();
    private ICamera camera = new Frustum();
    private boolean _needPause = false;
    private Timer pauseTimer = new Timer();
    private Timer pauseTimerTimer = new Timer();

    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdates = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE || event.isCancelled())
            return;
        
        _blocksToDestroy.clear();
        
        BlockPos playerPos = PlayerUtil.GetLocalPlayerPosFloored();
        
        switch (PlayerUtil.GetFacing())
        {
            case East:
                switch (Mode.getValue())
                {
                    case Tunnel1x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel2x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel1x3:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());

                            playerPos = new BlockPos(playerPos).east();
                        }
                    case Tunnel2x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up());
                            
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel3x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up());
                            _blocksToDestroy.add(playerPos.east().north().north());
                            _blocksToDestroy.add(playerPos.east().north().north().up());
                            _blocksToDestroy.add(playerPos.east().north().north().up().up());
                            
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel4wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up(2));
                            _blocksToDestroy.add(playerPos.east().up(3));
                            _blocksToDestroy.add(playerPos.east().south());
                            _blocksToDestroy.add(playerPos.east().south().up());
                            _blocksToDestroy.add(playerPos.east().south().up(2));
                            _blocksToDestroy.add(playerPos.east().south().up(3));
                            _blocksToDestroy.add(playerPos.east().south(2).up());
                            _blocksToDestroy.add(playerPos.east().south(2).up(2));
                            _blocksToDestroy.add(playerPos.east().south(2).up(3));
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up(2));
                            _blocksToDestroy.add(playerPos.east().north().up(3));
                            _blocksToDestroy.add(playerPos.east().north(2));
                            _blocksToDestroy.add(playerPos.east().north(2).up());
                            _blocksToDestroy.add(playerPos.east().north(2).up(2));
                            _blocksToDestroy.add(playerPos.east().north(2).up(3));
                            _blocksToDestroy.add(playerPos.east().north(3).up());
                            _blocksToDestroy.add(playerPos.east().north(3).up(2));
                            _blocksToDestroy.add(playerPos.east().north(3).up(3));
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel2wGuards:
                        for (int i = 0; i < 7; ++i) {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());
                            _blocksToDestroy.add(playerPos.east().up().up().up());
                            _blocksToDestroy.add(playerPos.east().south().up());
                            _blocksToDestroy.add(playerPos.east().south().up().up());
                            _blocksToDestroy.add(playerPos.east().south().up().up().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up().up());
                            _blocksToDestroy.add(playerPos.east().north().north().up());
                            _blocksToDestroy.add(playerPos.east().north().north().up().up());
                            _blocksToDestroy.add(playerPos.east().north().north().up().up().up());
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case Tunnel3wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up(2));
                            _blocksToDestroy.add(playerPos.east().up(3));
                            _blocksToDestroy.add(playerPos.east().south());
                            _blocksToDestroy.add(playerPos.east().south().up());
                            _blocksToDestroy.add(playerPos.east().south().up(2));
                            _blocksToDestroy.add(playerPos.east().south().up(3));
                            _blocksToDestroy.add(playerPos.east().south(2).up());
                            _blocksToDestroy.add(playerPos.east().south(2).up(2));
                            _blocksToDestroy.add(playerPos.east().south(2).up(3));
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up(2));
                            _blocksToDestroy.add(playerPos.east().north().up(3));
                            _blocksToDestroy.add(playerPos.east().north(2).up());
                            _blocksToDestroy.add(playerPos.east().north(2).up(2));
                            _blocksToDestroy.add(playerPos.east().north(2).up(3));
                            playerPos = new BlockPos(playerPos).east();
                        }
                    default:
                        break;
                }
                break;
            case North:
                switch (Mode.getValue())
                {
                    case Tunnel1x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel2x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel1x3:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());

                            playerPos = new BlockPos(playerPos).north();
                        }
                    case Tunnel2x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up());
                            
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel3x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up());
                            _blocksToDestroy.add(playerPos.north().east().east());
                            _blocksToDestroy.add(playerPos.north().east().east().up());
                            _blocksToDestroy.add(playerPos.north().east().east().up().up());
                            
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel4wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up(2));
                            _blocksToDestroy.add(playerPos.north().up(3));
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up(2));
                            _blocksToDestroy.add(playerPos.north().east().up(3));
                            _blocksToDestroy.add(playerPos.north().east(2).up());
                            _blocksToDestroy.add(playerPos.north().east(2).up(2));
                            _blocksToDestroy.add(playerPos.north().east(2).up(3));
                            _blocksToDestroy.add(playerPos.north().west());
                            _blocksToDestroy.add(playerPos.north().west().up());
                            _blocksToDestroy.add(playerPos.north().west().up(2));
                            _blocksToDestroy.add(playerPos.north().west().up(3));
                            _blocksToDestroy.add(playerPos.north().west(2));
                            _blocksToDestroy.add(playerPos.north().west(2).up());
                            _blocksToDestroy.add(playerPos.north().west(2).up(2));
                            _blocksToDestroy.add(playerPos.north().west(2).up(3));
                            _blocksToDestroy.add(playerPos.north().west(3).up());
                            _blocksToDestroy.add(playerPos.north().west(3).up(2));
                            _blocksToDestroy.add(playerPos.north().west(3).up(3));
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel2wGuards:
                        for (int i = 0; i < 7; ++i) {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());
                            _blocksToDestroy.add(playerPos.north().up().up().up());
                            _blocksToDestroy.add(playerPos.north().west().up());
                            _blocksToDestroy.add(playerPos.north().west().up().up());
                            _blocksToDestroy.add(playerPos.north().west().up().up().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up().up());
                            _blocksToDestroy.add(playerPos.north().east().north().up());
                            _blocksToDestroy.add(playerPos.north().east().north().up().up());
                            _blocksToDestroy.add(playerPos.north().east().north().up().up().up());
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case Tunnel3wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up(2));
                            _blocksToDestroy.add(playerPos.north().up(3));
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up(2));
                            _blocksToDestroy.add(playerPos.north().east().up(3));
                            _blocksToDestroy.add(playerPos.north().east(2).up());
                            _blocksToDestroy.add(playerPos.north().east(2).up(2));
                            _blocksToDestroy.add(playerPos.north().east(2).up(3));
                            _blocksToDestroy.add(playerPos.north().west());
                            _blocksToDestroy.add(playerPos.north().west().up());
                            _blocksToDestroy.add(playerPos.north().west().up(2));
                            _blocksToDestroy.add(playerPos.north().west().up(3));
                            _blocksToDestroy.add(playerPos.north().west(2).up());
                            _blocksToDestroy.add(playerPos.north().west(2).up(2));
                            _blocksToDestroy.add(playerPos.north().west(2).up(3));
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case South:
                switch (Mode.getValue())
                {
                    case Tunnel1x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel2x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel1x3:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());

                            playerPos = new BlockPos(playerPos).south();
                        }
                    case Tunnel2x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up());
                            
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel3x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up());
                            _blocksToDestroy.add(playerPos.south().west().west());
                            _blocksToDestroy.add(playerPos.south().west().west().up());
                            _blocksToDestroy.add(playerPos.south().west().west().up().up());
                            
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel4wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up(2));
                            _blocksToDestroy.add(playerPos.south().up(3));
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up(2));
                            _blocksToDestroy.add(playerPos.south().west().up(3));
                            _blocksToDestroy.add(playerPos.south().west(2).up());
                            _blocksToDestroy.add(playerPos.south().west(2).up(2));
                            _blocksToDestroy.add(playerPos.south().west(2).up(3));
                            _blocksToDestroy.add(playerPos.south().east());
                            _blocksToDestroy.add(playerPos.south().east().up());
                            _blocksToDestroy.add(playerPos.south().east().up(2));
                            _blocksToDestroy.add(playerPos.south().east().up(3));
                            _blocksToDestroy.add(playerPos.south().east(2));
                            _blocksToDestroy.add(playerPos.south().east(2).up());
                            _blocksToDestroy.add(playerPos.south().east(2).up(2));
                            _blocksToDestroy.add(playerPos.south().east(2).up(3));
                            _blocksToDestroy.add(playerPos.south().east(3).up());
                            _blocksToDestroy.add(playerPos.south().east(3).up(2));
                            _blocksToDestroy.add(playerPos.south().east(3).up(3));
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel2wGuards:
                        for (int i = 0; i < 7; ++i) {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());
                            _blocksToDestroy.add(playerPos.south().up().up().up());
                            _blocksToDestroy.add(playerPos.south().east().up());
                            _blocksToDestroy.add(playerPos.south().east().up().up());
                            _blocksToDestroy.add(playerPos.south().east().up().up().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up().up());
                            _blocksToDestroy.add(playerPos.south().west().west().up());
                            _blocksToDestroy.add(playerPos.south().west().west().up().up());
                            _blocksToDestroy.add(playerPos.south().west().west().up().up().up());
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case Tunnel3wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up(2));
                            _blocksToDestroy.add(playerPos.south().up(3));
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up(2));
                            _blocksToDestroy.add(playerPos.south().west().up(3));
                            _blocksToDestroy.add(playerPos.south().west(2).up());
                            _blocksToDestroy.add(playerPos.south().west(2).up(2));
                            _blocksToDestroy.add(playerPos.south().west(2).up(3));
                            _blocksToDestroy.add(playerPos.south().east());
                            _blocksToDestroy.add(playerPos.south().east().up());
                            _blocksToDestroy.add(playerPos.south().east().up(2));
                            _blocksToDestroy.add(playerPos.south().east().up(3));
                            _blocksToDestroy.add(playerPos.south().east(2).up());
                            _blocksToDestroy.add(playerPos.south().east(2).up(2));
                            _blocksToDestroy.add(playerPos.south().east(2).up(3));
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case West:
                switch (Mode.getValue())
                {
                    case Tunnel1x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel2x2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            
                            playerPos = new BlockPos(playerPos).west();
                        }
                    case Tunnel1x3:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());

                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel2x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up());
                            
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel3x3:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up());
                            _blocksToDestroy.add(playerPos.west().south().south());
                            _blocksToDestroy.add(playerPos.west().south().south().up());
                            _blocksToDestroy.add(playerPos.west().south().south().up().up());
                            
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel4wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up(2));
                            _blocksToDestroy.add(playerPos.west().up(3));
                            _blocksToDestroy.add(playerPos.west().north());
                            _blocksToDestroy.add(playerPos.west().north().up());
                            _blocksToDestroy.add(playerPos.west().north().up(2));
                            _blocksToDestroy.add(playerPos.west().north().up(3));
                            _blocksToDestroy.add(playerPos.west().north(2).up());
                            _blocksToDestroy.add(playerPos.west().north(2).up(2));
                            _blocksToDestroy.add(playerPos.west().north(2).up(3));
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up(2));
                            _blocksToDestroy.add(playerPos.west().south().up(3));
                            _blocksToDestroy.add(playerPos.west().south(2));
                            _blocksToDestroy.add(playerPos.west().south(2).up());
                            _blocksToDestroy.add(playerPos.west().south(2).up(2));
                            _blocksToDestroy.add(playerPos.west().south(2).up(3));
                            _blocksToDestroy.add(playerPos.west().south(3).up());
                            _blocksToDestroy.add(playerPos.west().south(3).up(2));
                            _blocksToDestroy.add(playerPos.west().south(3).up(3));
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel2wGuards:
                        for (int i = 0; i < 7; ++i) {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());
                            _blocksToDestroy.add(playerPos.west().up().up().up());
                            _blocksToDestroy.add(playerPos.west().north().up());
                            _blocksToDestroy.add(playerPos.west().north().up().up());
                            _blocksToDestroy.add(playerPos.west().north().up().up().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up().up());
                            _blocksToDestroy.add(playerPos.west().south().south().up());
                            _blocksToDestroy.add(playerPos.west().south().south().up().up());
                            _blocksToDestroy.add(playerPos.west().south().south().up().up().up());
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case Tunnel3wGuards:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up(2));
                            _blocksToDestroy.add(playerPos.west().up(3));
                            _blocksToDestroy.add(playerPos.west().north());
                            _blocksToDestroy.add(playerPos.west().north().up());
                            _blocksToDestroy.add(playerPos.west().north().up(2));
                            _blocksToDestroy.add(playerPos.west().north().up(3));
                            _blocksToDestroy.add(playerPos.west().north(2).up());
                            _blocksToDestroy.add(playerPos.west().north(2).up(2));
                            _blocksToDestroy.add(playerPos.west().north(2).up(3));
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up(2));
                            _blocksToDestroy.add(playerPos.west().south().up(3));
                            _blocksToDestroy.add(playerPos.west().south(2).up());
                            _blocksToDestroy.add(playerPos.west().south(2).up(2));
                            _blocksToDestroy.add(playerPos.west().south(2).up(3));
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        
        BlockPos toDestroy = null;
        
        for (BlockPos pos : _blocksToDestroy)
        {
            IBlockState state = mc.world.getBlockState(pos);
            
            if (state.getBlock() == Blocks.AIR || state.getBlock() instanceof BlockDynamicLiquid || state.getBlock() instanceof BlockStaticLiquid || state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.NETHERRACK)
                continue;
            
            toDestroy = pos;
            break;
        }
        
        if (toDestroy != null)
        {
            event.cancel();
            
            float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(toDestroy.getX(), toDestroy.getY(), toDestroy.getZ()));
            
            PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
            
            switch (MiningMode.getValue())
            {
                case Normal:
                    if (BlockManager.GetCurrBlock() == null)
                        BlockManager.SetCurrentBlock(toDestroy);
                    
                    BlockManager.Update(5.0f, true);
                    break;
                case Packet:
                    IBlockState l2_State = mc.world.getBlockState(PlayerUtil.GetLocalPlayerPosFloored().up().west().west().west());
                    if (l2_State != Blocks.NETHERRACK) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.START_DESTROY_BLOCK, toDestroy, EnumFacing.UP));
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                toDestroy, EnumFacing.UP));
                    }
                    break;
                default:
                    break;
            }
            
            _needPause = true;
        }
        else
            _needPause = false;
    });
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (!Visualize.getValue())
            return;
        
        _blocksToDestroy.forEach(pos ->
        {
            IBlockState l_State = mc.world.getBlockState(pos);
            
            if (l_State != null && l_State.getBlock() != Blocks.AIR && l_State.getBlock() != Blocks.BEDROCK && !(l_State.getBlock() instanceof BlockDynamicLiquid) && !(l_State.getBlock() instanceof BlockStaticLiquid))
            {
                final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                        pos.getY() - mc.getRenderManager().viewerPosY,
                        pos.getZ() - mc.getRenderManager().viewerPosZ,
                        pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                        pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                        pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
        
                camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                        mc.getRenderViewEntity().posZ);
        
                if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ)))
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    glEnable(GL_LINE_SMOOTH);
                    glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                    glLineWidth(1.5f);
        
                    RenderUtil.drawBoundingBox(bb, 1.0f, 0x50FF0000);
                    RenderUtil.drawFilledBox(bb, 0x50FF0000);
                    glDisable(GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        });
    });

    @EventHandler
    private Listener<EventClientTick> onTick = new Listener<>(event -> {
        if (UseTimerForAutoWalk.getValue()) {
            if (!_needPause && pauseTimer.passed(TimerForAutoWalk.getValue() * 1000)) {
                _needPause = true;
                pauseTimer.reset();
            }
            if (_needPause && pauseTimer.passed(5500)) {
                _needPause = false;
                pauseTimer.reset();
            }
        }
    });
    
    public boolean PauseAutoWalk()
    {
        return _needPause && PauseAutoWalk.getValue();
    }
}
