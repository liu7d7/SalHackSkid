package me.ionar.salhack.gui.hud.components;

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
        _autoTrap = (AutoTrap)ModuleManager.Get().GetMod(AutoTrap.class);
        _speed = (SpeedModule)ModuleManager.Get().GetMod(SpeedModule.class);
        _autoCrystalRewrite = (AutoCrystalRewrite)ModuleManager.Get().GetMod(AutoCrystalRewrite.class);
        _bedAura = (BedAuraModule)ModuleManager.Get().GetMod(BedAuraModule.class);
    }
    
    private KillAuraModule _killAura;
    private AutoCrystalModule _autoCrystal;
    private AutoTrap _autoTrap;
    private SpeedModule _speed;
    private AutoCrystalRewrite _autoCrystalRewrite;
    private BedAuraModule _bedAura;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String aura = ChatFormatting.GRAY + "KA " + ChatFormatting.WHITE + (_killAura.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String crystal = ChatFormatting.GRAY + "CA " + ChatFormatting.WHITE + ((_autoCrystal.isEnabled() || _autoCrystalRewrite.isEnabled()) ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String bedAura = ChatFormatting.GRAY + "BA " + ChatFormatting.WHITE + (_bedAura.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String autoTrap = ChatFormatting.GRAY + "AT " + ChatFormatting.WHITE + (_autoTrap.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String speed = ChatFormatting.GRAY + "S " + ChatFormatting.WHITE + (_speed.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        
        RenderUtil.drawStringWithShadow(aura, GetX(), GetY(), -1);
        RenderUtil.drawStringWithShadow(crystal, GetX(), GetY()+12, -1);
        RenderUtil.drawStringWithShadow(bedAura, GetX(), GetY()+24, -1);
        RenderUtil.drawStringWithShadow(speed, GetX(), GetY()+36, -1);
        RenderUtil.drawStringWithShadow(autoTrap, GetX(), GetY()+48, -1);

        SetWidth(RenderUtil.getStringWidth(aura));
        SetHeight(RenderUtil.getStringHeight(crystal)+RenderUtil.getStringHeight(aura)+RenderUtil.getStringHeight(autoTrap)+RenderUtil.getStringHeight(speed)+RenderUtil.getStringHeight(bedAura));
    }
}
