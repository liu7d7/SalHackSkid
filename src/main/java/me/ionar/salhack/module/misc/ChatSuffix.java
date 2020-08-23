package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerSendChatMessage;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;

public final class ChatSuffix extends Module
{
    public final Value<Modes> mode = new Value<Modes>("Mode", new String[] {"M"}, "The retard chat mode", Modes.Spongebob);

    public enum Modes
    {
        on_top_doe,
        powered_by,
    }

    public ChatSuffix()
    {
        super("ChatSuffix", new String[]
                        { "" }, "Makes your chat have a string appended on the end", "NONE",
                0xDB2485, ModuleType.MISC);
    }

    @Override
    public String getMetaData()
    {
        return mode.getValue().toString();
    }

    @EventHandler
    private Listener<EventPlayerSendChatMessage> OnSendChatMsg = new Listener<>(p_Event ->
    {
        if (p_Event.Message.startsWith("/"))
            return;

        String l_Message = "";

        switch (mode.getValue())
        {
            case on_top_doe:
            {
                boolean l_Flag = false;

                l_Message = ">> SalHackSkid_skid do be on top doe";

                break;
            }

            case powered_by:
            {
                boolean l_Flag = false;

                l_Message = ">> powered by SalHackSkid_skid";

                break;
            }
        }

        p_Event.cancel();
        mc.getConnection().sendPacket(new CPacketChatMessage(p_Event.Message + l_Message));
    });
}
