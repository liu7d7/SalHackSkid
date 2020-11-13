package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerSendChatMessage;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.client.model.ICustomModelLoader;

public final class ChatSuffixModule extends Module
{
    public final Value<Modes> mode = new Value<Modes>("Mode", new String[] {"M"}, "The retard chat mode", Modes.standard);

    public enum Modes
    {
        standard,
        //custom,
    }

    public ChatSuffixModule()
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

        switch (mode.getValue()) {
            case standard: {
                l_Message = " \u00b7 \ua731\u1d00\u029f\u029c\u1d00\u1d04\u1d0b\ua731\u1d0b\u026a\u1d05";
                //  (dot) SalHackSkid
                break;
            }
            /*case custom: {
                l_Message = "";

                break;
            }*/
        }

        p_Event.cancel();
        mc.getConnection().sendPacket(new CPacketChatMessage(p_Event.Message + l_Message));
    });
}
