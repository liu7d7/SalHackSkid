// A bit buggy atm. Sometimes the render goes green or red. Not sure what causes this.

package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.module.Value;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public class PlayerRender extends HudComponentItem
{
    public final Value<Integer> Scale = new Value<Integer>("Scale", new String[] {""}, "Scale for rendering", 50, 10, 100, 5);


    public PlayerRender()
    {
        super("PlayerRender", 200, 2);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        DecimalFormat l_Format = new DecimalFormat("#.#");

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Actual render part
        GuiInventory.drawEntityOnScreen((int) GetX()+64, (int)GetY()+110, Scale.getValue(), -(int)mc.player.lastReportedYaw, -(int)mc.player.lastReportedPitch, mc.player);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // Need to set width of square even though its transparent, wouldnt be able to move it around otherwise
        this.SetWidth(128);
        this.SetHeight(128);
    }

}
