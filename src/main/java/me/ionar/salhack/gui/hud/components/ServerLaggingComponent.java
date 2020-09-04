package me.ionar.salhack.gui.hud.components;

import com.google.common.base.Stopwatch;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.core.jmx.Server;
import org.newdawn.slick.tests.xml.Entity;
import net.minecraft.util.math.MathHelper;

public class ServerLaggingComponent extends HudComponentItem
{
    public ServerLaggingComponent()
    {
        super("ServerLagNotifier", 2, 230);
    }

    String message = "";
    private long prevTime = 0;
    private double tps = 20;
    private long lastPacket = 0;
    private long time = 0;
    private long timeOffset = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (IsServerLagging()) {
                message = ChatFormatting.BLUE + "Server lagging for: " + ChatFormatting.WHITE + (time - lastPacket) + ChatFormatting.BLUE + "ms";

                SetWidth(RenderUtil.getStringWidth(message));
                SetHeight(RenderUtil.getStringHeight(message));
                RenderUtil.drawStringWithShadow(message, GetX(), GetY(), -1);
        }
        else
        {
            return;
        }
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> OnPacketEvent = new Listener<>(p_Event ->
    {
        lastPacket = System.currentTimeMillis();

        if (p_Event.getPacket() instanceof TickEvent.WorldTickEvent)
        {
            long time = System.currentTimeMillis();
            if (time < 500)
                return;
            long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
            tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
            prevTime = time;
        }
    });

    private boolean IsServerLagging()
    {
        if (time - lastPacket > 100)
        {
            return true;
        }
        return false;
    }
}