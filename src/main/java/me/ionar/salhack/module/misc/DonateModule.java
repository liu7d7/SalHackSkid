package me.ionar.salhack.module.misc;

import java.awt.Desktop;
import java.net.URI;

import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public final class DonateModule extends Module
{
    public DonateModule()
    {
        super("Donate!", new String[]
                { "Build" }, "Plz give moneyz i need pls", "NONE", 0xDB24C4, ModuleType.DONATE);
    }

    @Override
    public void onEnable()
    {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://www.patreon.com/BobGreg"));
            }
        } catch (Exception e) {e.printStackTrace();}
        ModuleManager.Get().GetMod(DonateModule.class).setEnabled(false);
    }

}
