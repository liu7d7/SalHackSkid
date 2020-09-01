package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;

import java.util.Calendar;

public class WelcomeComponent extends HudComponentItem
{
    public WelcomeComponent()
    {
        super("WelcomeComponent", 200, 2);
    }

    String l_welcome = "";
    Calendar c = Calendar.getInstance();
    // THIS IS A MINOR BUG!!! Time doesn't refresh (so it only checks on method call)
    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (timeOfDay >= 6 && timeOfDay < 12)
        {
            l_welcome = ChatFormatting.BLUE + "Good Morning, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 12 && timeOfDay < 17)
        {
            l_welcome = ChatFormatting.BLUE + "Good Afternoon, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 17 && timeOfDay < 22)
        {
            l_welcome = ChatFormatting.BLUE + "Good Evening, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 22 || timeOfDay < 6)
        {
            l_welcome = ChatFormatting.BLUE + "Good Night, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else
        {
            l_welcome = ChatFormatting.BLUE + "Hello, " + ChatFormatting.WHITE + mc.getSession().getUsername() + ".. psst! something went wrong!";
        }

        SetWidth(RenderUtil.getStringWidth(l_welcome));
        SetHeight(RenderUtil.getStringHeight(l_welcome) + 1);

        RenderUtil.drawStringWithShadow(l_welcome, GetX(), GetY(), -1);
    }
}