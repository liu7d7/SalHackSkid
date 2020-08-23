package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.EventRenderLayers;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;

public class LavaRemModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {""}, "Mode", Modes.LavaRem4wGuards);
    public final Value<BuildingModes> BuildingMode = new Value<BuildingModes>("BuildingMode", new String[] {""}, "Dynamic will update source block while walking, static keeps same position and resets on toggle", BuildingModes.Dynamic);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[] {"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"Delay"}, "Delay of the place", 0f, 0.0f, 1.0f, 0.1f);
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[] {"Render"}, "Visualizes where blocks are to be placed", true);

    public enum Modes
    {
        LavaRem4wGuards,
        LavaRem2x3,
        LavaRem3x3,
        LavaRem1x3,
    }

    public enum BuildingModes
    {
        Dynamic,
        Static,
    }

    public LavaRemModule()
    {
        super("AutoBuilder", new String[]
        { "AutoSwastika" }, "Builds cool things at your facing block", "NONE", 0x96DB24, ModuleType.WORLD);
    }
    
    private Vec3d Center = Vec3d.ZERO;
    private ICamera camera = new Frustum();
    private Timer timer = new Timer();
    private Timer NetherPortalTimer = new Timer();
    private BlockPos SourceBlock = null;

    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.player == null)
        {
            toggle();
            return;
        }
        
        timer.reset();
        SourceBlock = null;
        BlockArray.clear();
    }
    
    private float PitchHead = 0.0f;
    private boolean SentPacket = false;

    ArrayList<BlockPos> BlockArray = new ArrayList<BlockPos>();
    
    @Override
    public String getMetaData()
    {
        return Mode.getValue().toString() + " - " + BuildingMode.getValue().toString();
    }

    @EventHandler
    private Listener<EventRenderLayers> OnRender = new Listener<>(p_Event ->
    {
        if (p_Event.getEntityLivingBase() == mc.player)
            p_Event.SetHeadPitch(PitchHead == -420.0f ? mc.player.rotationPitch : PitchHead);
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (!timer.passed(Delay.getValue() * 1000f))
            return;
        
        timer.reset();
        
        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        BlockPos orignPos = new BlockPos(pos.x, pos.y+0.5f, pos.z);

        int lastSlot;
        Pair<Integer, Block> l_Pair = findStackHotbar();
        
        int slot = -1;
        double l_Offset = pos.y - orignPos.getY();
        
        if (l_Pair != null)
        {
            slot = l_Pair.getFirst();
            
            if (l_Pair.getSecond() instanceof BlockSlab)
            {
                if (l_Offset == 0.5f)
                {
                    orignPos = new BlockPos(pos.x, pos.y+0.5f, pos.z);
                }
            }
        }
        
        if (BuildingMode.getValue() == BuildingModes.Dynamic)
            BlockArray.clear();
        
        if (BlockArray.isEmpty())
            FillBlockArrayAsNeeded(pos, orignPos, l_Pair);
        
        boolean l_NeedPlace = false;

        float[] rotations = null;
        
        if (slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
                
                int l_BlocksPerTick = BlocksPerTick.getValue();

                for (BlockPos l_Pos : BlockArray)
                {
                    /*ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                    
                    if (l_Result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable())
                        continue;
                    
                    if (l_Result == ValidResult.NoNeighbors)
                        continue;*/
                    
                    PlaceResult l_Place = BlockInteractionHelper.place (l_Pos, 5.0f, false, l_Offset == -0.5f);
                    
                    if (l_Place != PlaceResult.Placed)
                        continue;
                    
                    l_NeedPlace = true;
                    rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos.getX(), l_Pos.getY(), l_Pos.getZ()));
                    if (--l_BlocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, l_Pair.getSecond()))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            }
        }
        
        if (!l_NeedPlace || rotations == null)
        {
            PitchHead = -420.0f;
            SentPacket = false;
            return;
        }
        
        p_Event.cancel();
        
        /// @todo: clean this up

        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity())
        {
            float l_Pitch = rotations[1];
            float l_Yaw = rotations[0];
            
            mc.player.rotationYawHead = l_Yaw;
            PitchHead = l_Pitch;
            
            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double l_YawDifference = (double)(l_Yaw - mc.player.lastReportedYaw);
            double l_RotationDifference = (double)(l_Pitch - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;

            if (mc.player.isRiding())
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
                l_MovedXYZ = false;
            }
            else if (l_MovedXYZ && l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (l_MovedXYZ)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            }
            else if (l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (mc.player.prevOnGround != mc.player.onGround)
            {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (l_MovedXYZ)
            {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (l_MovedRotation)
            {
                mc.player.lastReportedYaw = l_Yaw;
                mc.player.lastReportedPitch = l_Pitch;
            }

            SentPacket = true;
            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
    });
    
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (!Visualize.getValue())
            return;
        
        Iterator l_Itr = BlockArray.iterator();

        while (l_Itr.hasNext()) 
        {
            BlockPos l_Pos = (BlockPos) l_Itr.next();
            
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State != null && l_State.getBlock() != Blocks.AIR && l_State.getBlock() != Blocks.WATER)
                continue;
            
            final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                    l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    
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
    
                final double dist = mc.player.getDistance(l_Pos.getX() + 0.5f, l_Pos.getY() + 0.5f, l_Pos.getZ() + 0.5f)
                        * 0.75f;
    
                float alpha = MathUtil.clamp((float) (dist * 255.0f / 5.0f / 255.0f), 0.0f, 0.3f);

                //  public static void drawBoundingBox(AxisAlignedBB bb, float width, int color)
                
                
                int l_Color = 0x9000FFFF;
                
                RenderUtil.drawBoundingBox(bb, 1.0f, l_Color);
                RenderUtil.drawFilledBox(bb, l_Color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });
    
    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private void FillBlockArrayAsNeeded(final Vec3d pos, final BlockPos orignPos, final Pair<Integer, Block> p_Pair)
    {
        BlockPos interpPos = null;
        
        switch (Mode.getValue()) {
            case LavaRem4wGuards:
                switch (PlayerUtil.GetFacing()) {
                    case East:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().east());
                        BlockArray.add(orignPos.down().east().south());
                        BlockArray.add(orignPos.down().east().south(2).up());
                        BlockArray.add(orignPos.down().east().north());
                        BlockArray.add(orignPos.down().east().north(2));
                        BlockArray.add(orignPos.down().east().north(3).up());
                        break;
                    case North:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().south());
                        BlockArray.add(orignPos.down().south().east());
                        BlockArray.add(orignPos.down().south().east(2).up());
                        BlockArray.add(orignPos.down().south().west());
                        BlockArray.add(orignPos.down().south().west(2));
                        BlockArray.add(orignPos.down().south().west(3).up());
                        break;
                    case South:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().south());
                        BlockArray.add(orignPos.down().south().west());
                        BlockArray.add(orignPos.down().south().west(2).up());
                        BlockArray.add(orignPos.down().south().east());
                        BlockArray.add(orignPos.down().south().east(2));
                        BlockArray.add(orignPos.down().south().east(3).up());
                        break;
                    case West:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().east());
                        BlockArray.add(orignPos.down().east().north());
                        BlockArray.add(orignPos.down().east().north(2).up());
                        BlockArray.add(orignPos.down().east().south());
                        BlockArray.add(orignPos.down().east().south(2));
                        BlockArray.add(orignPos.down().east().south(3).up());
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private Pair<Integer, Block> findStackHotbar()
    {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)
            return new Pair<Integer, Block>(mc.player.inventory.currentItem, ((ItemBlock)mc.player.getHeldItemMainhand().getItem()).getBlock());
        
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();
                
                return new Pair<Integer, Block>(i, block.getBlock());
            }
        }
        return null;
    }

    public Vec3d GetCenter(double posX, double posY, double posZ)
    {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D ;
        
        return new Vec3d(x, y, z);
    }

    /// Verifies the array is all obsidian
    private boolean VerifyPortalFrame(ArrayList<BlockPos> p_Blocks)
    {
        for (BlockPos l_Pos : p_Blocks)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State == null || !(l_State.getBlock() instanceof BlockObsidian))
                return false;
        }
        
        return true;
    }
}
