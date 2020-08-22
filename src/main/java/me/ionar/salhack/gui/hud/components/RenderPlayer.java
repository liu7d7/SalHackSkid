package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

public class RenderPlayer extends HudComponentItem
{
    public RenderPlayer()
    {
        super("RenderPlayer", 200, 2);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);



        //RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x990C0C0C);

        DecimalFormat l_Format = new DecimalFormat("#.#");

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) GetX()+64, (int)GetY()+110, 50, -(int)mc.player.lastReportedYaw, -(int)mc.player.lastReportedPitch, mc.player);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        this.SetWidth(128);
        this.SetHeight(128);
    }

}
