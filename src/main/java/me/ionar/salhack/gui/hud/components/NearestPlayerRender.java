package me.ionar.salhack.gui.hud.components;

import java.util.Comparator;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;

public class NearestPlayerRender extends HudComponentItem
{
    public final Value<Boolean> Players = new Value<>("Players", new String[]{"P"}, "Displays players", true);
    public final Value<Boolean> Friends = new Value<>("Friends", new String[]{"F"}, "Displays Friends", false);

    public final Value<Integer> Scale = new Value<>("Scale", new String[] {""}, "Scale for rendering", 30, 10, 100, 5);
    public final Value<Integer> xPos = new Value<>("xPos", new String[] {""}, "xPos for rendering", 100, 10, 100, 5);
    public final Value<Integer> yPos = new Value<>("yPos", new String[] {""}, "yPos for rendering", 0, 10, 100, 5);

    public NearestPlayerRender()
    {
        super("NearestPlayerRender", 400, 2);
    }

    String PlayerType = "";

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x990C0C0C);

        EntityLivingBase l_Entity = mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidEntity(entity))
                .map(entity -> (EntityLivingBase) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);

        if (l_Entity == null)
            return;

        if (FriendManager.Get().IsFriend(l_Entity))
        {
            PlayerType = ChatFormatting.AQUA + "Friend";
        }
        else if (l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA)
        {
            PlayerType = ChatFormatting.GOLD + "Wasp";
        }
        else if(l_Entity.getHeldItemMainhand().getItem() == Items.END_CRYSTAL
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.DIAMOND_HELMET
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.CHAINMAIL_HELMET
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.DIAMOND_CHESTPLATE
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.CHAINMAIL_CHESTPLATE
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.DIAMOND_LEGGINGS
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.CHAINMAIL_LEGGINGS
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.DIAMOND_BOOTS
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.CHAINMAIL_BOOTS)
        {
            PlayerType = ChatFormatting.RED + "Crystal PvPer";
        }
        else if(l_Entity.getHeldItemMainhand().getItem() == Items.BED
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.DIAMOND_HELMET
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.CHAINMAIL_HELMET
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.DIAMOND_CHESTPLATE
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.CHAINMAIL_CHESTPLATE
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.DIAMOND_LEGGINGS
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.CHAINMAIL_LEGGINGS
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.DIAMOND_BOOTS
                || l_Entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.CHAINMAIL_BOOTS)
        {
            PlayerType = ChatFormatting.RED + "Bed PvPer";
        }
        else if (l_Entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.AIR
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.AIR
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.AIR
                && l_Entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.AIR)
        {
            PlayerType = ChatFormatting.GREEN + "New Friend";
        }
        /*else {
            PlayerType = ChatFormatting.GOLD + "Unknown";
        }*/

        //String armour = Objects.toString(l_Entity.getArmorInventoryList());

        //float l_HealthPct = ((l_Entity.getHealth()+l_Entity.getAbsorptionAmount())/l_Entity.getMaxHealth())*100.0f ;
        //float l_HealthBarPct = Math.min(l_HealthPct, 100.0f);

        //DecimalFormat l_Format = new DecimalFormat("#.#");

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) GetX()+xPos.getValue(), (int)GetY()-yPos.getValue(), Scale.getValue(), l_Entity.rotationYaw, -l_Entity.rotationPitch, l_Entity);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderUtil.drawStringWithShadow(l_Entity.getName(), GetX()+4, GetY()+4, 0xFFFFFF);
        //RenderUtil.drawGradientRect(GetX(), GetY(), GetX()+l_HealthBarPct, GetY()+11, 0x999FF365, 0x9913FF00);
        //RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", l_Format.format(l_HealthPct) + "%", l_Format.format(l_Entity.getHealth()+l_Entity.getAbsorptionAmount()), l_Format.format(l_Entity.getMaxHealth())), GetX()+20, GetY()+11, 0xFFFFFF);
        RenderUtil.drawStringWithShadow("Threat: " + PlayerType, GetX()+4, GetY()+12, -1);
        //RenderUtil.drawStringWithShadow("armour: " + armour, GetX()+4, GetY()+22, -1);

        this.SetWidth(120);
        this.SetHeight(22);
    }

    private boolean IsValidEntity(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityPlayer))
            return false;

        if (p_Entity == mc.player)
            return false;

        if (!Players.getValue())
            return false;

        return true;
    }
}