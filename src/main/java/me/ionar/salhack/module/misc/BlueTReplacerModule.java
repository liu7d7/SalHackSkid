package me.ionar.salhack.module.misc;

import java.util.ArrayList;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlueTReplacerModule extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);

    public BlueTReplacerModule()
    {
        super("BlueTReplacer", new String[] {"BTR"}, "Automatically replaces Blue Ts similar to how autototem works", "NONE", 0xB324DB, ModuleType.MISC);
    }

    private ArrayList<Item> Hotbar = new ArrayList<Item>();
    private Timer timer = new Timer();

    @Override
    public String getMetaData()
    {
        return "BTR";
    }

    /// Don't activate on startup
    @Override
    public void toggleNoSave()
    {

    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen != null)
            return;

        if (!timer.passed(Delay.getValue() * 1000))
            return;

        boolean needUpdate = true;
        int emptySlot = -1;
        for (int l_I = 0; l_I < 6; ++l_I) {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (!l_Stack.isEmpty()) {
                Item item = l_Stack.getItem();
                if (item == Items.DIAMOND_PICKAXE) {
                    needUpdate = false;
                }
            } else
            {
                emptySlot = l_I;
            }
        }

        if (!needUpdate)
            return;

        for (int l_I = 9; l_I < 36; ++l_I) {
            final ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty())
                continue;

            if (l_Stack.getItem() == Items.DIAMOND_PICKAXE) {
                if (emptySlot >= 0) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_I, 0,
                            ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.updateController();
                } else {

                }
                timer.reset();
                break;
            }
        }
    });

    private boolean SwitchSlotIfNeed(int p_Slot)
    {
        Item l_Item = Hotbar.get(p_Slot);

        if (l_Item == Items.AIR)
            return false;

        if (!mc.player.inventory.getStackInSlot(p_Slot).isEmpty() && mc.player.inventory.getStackInSlot(p_Slot).getItem() == l_Item)
            return false;

        int l_Slot = PlayerUtil.GetItemSlot(l_Item);

        if (l_Slot != -1 && l_Slot != 45)
        {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, p_Slot+36, 0, ClickType.PICKUP,
                    mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.updateController();

            return true;
        }

        return false;
    }

    private boolean RefillSlotIfNeed(int p_Slot)
    {
        ItemStack l_Stack = mc.player.inventory.getStackInSlot(p_Slot);

        if (l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR)
            return false;

        if (!l_Stack.isStackable())
            return false;

        if (l_Stack.getCount() >= l_Stack.getMaxStackSize())
            return false;

        /// We're going to search the entire inventory for the same stack, WITH THE SAME NAME, and use quick move.
        for (int l_I = 9; l_I < 36; ++l_I)
        {
            final ItemStack l_Item = mc.player.inventory.getStackInSlot(l_I);

            if (l_Item.isEmpty())
                continue;

            if (CanItemBeMergedWith(l_Stack, l_Item))
            {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_I, 0,
                        ClickType.QUICK_MOVE, mc.player);
                mc.playerController.updateController();

                /// Check again for more next available tick
                return true;
            }
        }

        return false;
    }

    private boolean CanItemBeMergedWith(ItemStack p_Source, ItemStack p_Target)
    {
        return p_Source.getItem() == p_Target.getItem() && p_Source.getDisplayName().equals(p_Target.getDisplayName());
    }
}
