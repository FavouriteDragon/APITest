package com.favouritedragon.apitest.skills;

import com.favouritedragon.apitest.entity.EntitySunBolt;
import ibxm.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import swordskillsapi.api.core.PlayerSkills;
import swordskillsapi.api.core.SkillApiUtils;
import swordskillsapi.api.skill.IComboSkill;
import swordskillsapi.api.skill.SkillActive;
import swordskillsapi.api.skill.SkillGroup;

import java.util.List;

public class Sunbolt extends SkillActive {

    private int missTimer;

    /**
     * Counter incremented when next correct key in sequence pressed; reset when activated or if ticksTilFail timer reaches 0
     */
    @SideOnly(Side.CLIENT)
    private int keysPressed;

    /**
     * Only for double-tap activation: Current number of ticks remaining before skill will not activate
     */
    @SideOnly(Side.CLIENT)
    private int ticksTilFail;


    public Sunbolt(String translationKey) {
        super(translationKey);
    }

    private Sunbolt(Sunbolt skill) {
        super(skill);
    }

    @Override
    public Sunbolt newInstance() {
        return new Sunbolt(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(List<String> desc, EntityPlayer player) {
        desc.add(getDamageDisplay(getDamageFactor(player), false) + "%");
        desc.add(getRangeDisplay(12 + level));
        desc.add(new TextComponentTranslation(getTranslationKey() + ".info.health", String.format("%.1f", level * (1.5 / 20) * player.getMaxHealth()))
                .getUnformattedText());
        desc.add(getExhaustionDisplay(getExhaustion()));
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }

    @Override
    protected float getExhaustion() {
        return 2.0F - (0.1F * level);
    }

    /**
     * Number of ticks that skill will be considered active
     */
    private int getActiveTime() {
        return 9 + (level / 2);
    }

    /**
     * Number of ticks before player may attempt to use this skill again
     */
    private int getParryDelay() {
        return (5 - (level / 2));
    }

    /**
     * The maximum number of attacks that may be parried per use of the skill
     */
    private int getMaxParries() {
        return (1 + level) / 2;
    }

    /**
     * Returns the strength of the knockback effect when an attack is parried
     */
    public float getKnockbackStrength() {
        return 0.4F; // 0.5F is the base line per blocking with a shield
    }

    @Override
    public boolean canUse(EntityPlayer player) {
        return super.canUse(player) && !isActive()
                && !player.isHandActive()
                && player.getHeldItemMainhand() == null && checkHealth(player)
                && PlayerSkills.get(player).canAttack();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canExecute(EntityPlayer player) {
        return canUse(player) && keysPressed > 1 && ticksTilFail > 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player, boolean isLockedOn) {
        if (key == mc.gameSettings.keyBindBack) {
            ticksTilFail = 6;
            if (keysPressed < 2) {

                keysPressed++;
            }
        } else if (key == mc.gameSettings.keyBindUseItem) {
            boolean flag = (canExecute(player) && activate(player));
            ticksTilFail = 0;
            keysPressed = 0;
            return flag;
        } else {
            ticksTilFail = 0;
            keysPressed = 0;
        }
        return false;
    }

    @Override
    protected boolean onActivated(World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Base attack strength calculation from EntityPlayer#attackTargetEntityWithCurrentItem
            float str = player.getCooledAttackStrength(0.5F);
            float dmg = getDamage(player) * (0.2F + str * str * 0.8F);
            missTimer = 12 + level;
            SkillApiUtils.playSoundAtEntity(world, player, SoundEvents.ENTITY_ENDERDRAGON_SHOOT, SoundCategory.PLAYERS, 0.4F, 0.5F);
            EntitySunBolt bolt = new EntitySunBolt(world, player, 1, 1, 1, level, dmg);
           // beam.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, beam.getVelocity(), 1.0F);
            world.spawnEntityInWorld(bolt);
        } else {
           PlayerSkills.get(player).setAttackCooldown(20 - level);
        }
        return isActive();
    }

    @Override
    protected void onDeactivated(World world, EntityPlayer player) {
        missTimer = 0;
    }


    @Override
    public void onUpdate(EntityPlayer player) {
        if (missTimer > 0) {
            --missTimer;
            if (missTimer == 0 && !player.worldObj.isRemote) {
                IComboSkill combo = PlayerSkills.get(player).getComboSkill();
                if (combo != null && combo.isComboInProgress()) {
                    combo.getCombo().endCombo(player);
                }
            }
        }
    }

    /**
     * Call from {@link com.favouritedragon.apitest.entity.EntitySunBolt#onImpact(RayTraceResult)}  to allow handling of ICombo;
     * striking an entity sets the missTimer to zero
     *
     * @param hitBlock true if sword beam hit a block rather than an entity
     */
    public void onImpact(EntityPlayer player, boolean hitBlock) {
        missTimer = (hitBlock && missTimer > 0 ? 1 : 0);
    }

    /**
     * Returns true if players current health is within the allowed limit
     */
    private boolean checkHealth(EntityPlayer player) {
        return player.capabilities.isCreativeMode || player.getHealth() <= player.getMaxHealth() - level * ((1.5 / 20) * player.getMaxHealth());
    }

    //TODO: Adjust for being barehanded
    /**
     * The percent of base sword damage that should be inflicted, as an integer
     */
    private int getDamageFactor(EntityPlayer player) {
        return 30 + (level * 10);
    }

    /** Returns player's base damage (with sword) plus 1.0F per level */
    private float getDamage(EntityPlayer player) {
        return (float)((double)(getDamageFactor(player)) * 0.01D * player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
    }




}
