package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpeedComponent extends HudComponentItem
{
    final DecimalFormat Formatter = new DecimalFormat("#.0");
    
    public SpeedComponent()
    {
        super("Speed", 2, 80);
    }
    
    private double PrevPosX;
    private double PrevPosZ;
    private Timer timer = new Timer();
    

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (timer.passed(1000))
        {
            PrevPosX = mc.player.prevPosX;
            PrevPosZ = mc.player.prevPosZ;
        }
        
        final double deltaX = mc.player.posX - PrevPosX;
        final double deltaZ = mc.player.posZ - PrevPosZ;
        
        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double l_BPS = l_Distance * 20;
        
        String l_Formatter = Formatter.format(l_BPS);

//        if (!l_Formatter.contains("."))
//            l_Formatter += ".0";
        
        final String bps = ChatFormatting.GRAY + "Speed " + ChatFormatting.WHITE + l_Formatter + " BPS";

        SetWidth(RenderUtil.getStringWidth(bps));
        SetHeight(RenderUtil.getStringHeight(bps)+1);

        RenderUtil.drawStringWithShadow(bps, GetX(), GetY(), -1);
    }
}
