package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.client.Request;
import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.combat.*;
import me.ionar.salhack.module.movement.SpeedModule;
import me.ionar.salhack.util.render.RenderUtil;

public class PvPInfoComponent extends HudComponentItem
{
    public PvPInfoComponent()
    {
        super("PvPInfo", 2, 290);
        
        _killAura = (KillAuraModule)ModuleManager.Get().GetMod(KillAuraModule.class);
        _autoCrystal = (AutoCrystalModule)ModuleManager.Get().GetMod(AutoCrystalModule.class);
        _bedAura = (BedAuraModule)ModuleManager.Get().GetMod(BedAuraModule.class);
        _autoTrap = (AutoTrap)ModuleManager.Get().GetMod(AutoTrap.class);
        _surround = (SurroundModule)ModuleManager.Get().GetMod(SurroundModule.class);
        _autoCrystalRewrite = (AutoCrystalRewrite)ModuleManager.Get().GetMod(AutoCrystalRewrite.class);
    }
    
    private KillAuraModule _killAura;
    private AutoCrystalModule _autoCrystal;
    private BedAuraModule _bedAura;
    private AutoTrap _autoTrap;
    private SurroundModule _surround;
    private AutoCrystalRewrite _autoCrystalRewrite;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String aura = ChatFormatting.GRAY + "KILL AURA " + ChatFormatting.WHITE + (_killAura.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String crystal = ChatFormatting.GRAY + "CRYSTAL A " + ChatFormatting.WHITE + ((_autoCrystal.isEnabled() || _autoCrystalRewrite.isEnabled()) ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String bed = ChatFormatting.GRAY + "BED AURA " + ChatFormatting.WHITE + (_bedAura.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String autoTrap = ChatFormatting.GRAY + "AUTO TRAP " + ChatFormatting.WHITE + (_autoTrap.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String speed = ChatFormatting.GRAY + "SURROUND " + ChatFormatting.WHITE + (_surround.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        
        RenderUtil.drawStringWithShadow(aura, GetX(), GetY(), -1);
        RenderUtil.drawStringWithShadow(crystal, GetX(), GetY()+12, -1);
        RenderUtil.drawStringWithShadow(bed, GetX(), GetY()+24, -1);
        RenderUtil.drawStringWithShadow(autoTrap, GetX(), GetY()+36, -1);
        RenderUtil.drawStringWithShadow(speed, GetX(), GetY()+48, -1);

        SetWidth(RenderUtil.getStringWidth(aura));
        SetHeight(RenderUtil.getStringHeight(crystal)+RenderUtil.getStringHeight(aura)+RenderUtil.getStringHeight(autoTrap)+RenderUtil.getStringHeight(speed));
    }
}
