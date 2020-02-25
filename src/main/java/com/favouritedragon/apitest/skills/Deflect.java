package com.favouritedragon.apitest.skills;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import swordskillsapi.api.skill.SkillActive;
import swordskillsapi.api.skill.SkillGroup;

import java.util.List;

public class Deflect extends SkillActive {

	/**
	 * Timer during which player is considered actively parrying
	 */
	private int parryTimer;

	/**
	 * Number of attacks parried this activation cycle
	 */
	private int attacksParried;

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

	/**
	 * Notification to play miss sound; set to true when activated and false when attack parried
	 */
	private boolean playMissSound;

	public Deflect(String translationKey) {
		super(translationKey);
	}

	private Deflect(Deflect skill) {
		super(skill);
	}

	@Override
	public Deflect newInstance() {
		return new Deflect(this);
	}

	@Override
	public boolean displayInGroup(SkillGroup group) {
		return super.displayInGroup(group) || group == Skills.UNARMED;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(new TextComponentTranslation(getTranslationKey() + ".info.chance", (int) (getDisarmChance(player, null) * 100)).getUnformattedText());
		desc.add(new TextComponentTranslation(getTranslationKey() + ".info.bonus", (int) (2.5F * (getActiveTime() - getParryDelay()))).getUnformattedText());
		desc.add(new TextComponentTranslation(getTranslationKey() + ".info.max", getMaxParries()).getUnformattedText());
		desc.add(getTimeLimitDisplay(getActiveTime() - getParryDelay()));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	public boolean isActive() {
		return (parryTimer > 0);
	}

	@Override
	protected float getExhaustion() {
		return 0.3F - (0.02F * level);
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
	 * Returns player's chance to disarm an attacker
	 *
	 * @param attacker if the attacker is an EntityPlayer, their Parry score will decrease their chance of being disarmed
	 */
	private float getDisarmChance(EntityPlayer player, EntityLivingBase attacker) {
		float penalty = 0.05F * attacksParried;
		float bonus = (parryTimer > 0 ? (parryTimer - getParryDelay()) : 0);
		if (attacker instanceof EntityPlayer) {
			penalty += this.getLevel();
		}
		return ((level * 0.1F) - penalty + bonus);
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
				&& player.getHeldItemMainhand() == null;
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
		parryTimer = getActiveTime();
		attacksParried = 0;
		playMissSound = true;
		player.swingArm(EnumHand.MAIN_HAND);
		player.resetCooldown();
		return isActive();
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		parryTimer = 0;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			if (--parryTimer <= getParryDelay() && playMissSound) {
				playMissSound = false;
				player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 0.4F, 0.5F);
			}
		} else if (player.getEntityWorld().isRemote && ticksTilFail > 0) {
			--ticksTilFail;
			if (ticksTilFail < 1) {
				keysPressed = 0;
			}
		}
	}

	@Override
	public boolean onBeingAttacked(EntityPlayer player, DamageSource source) {
		if (source.getSourceOfDamage() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) source.getSourceOfDamage();
			if (attacksParried < getMaxParries() && parryTimer > getParryDelay() && player.getHeldItemMainhand() == null) {
				if (!(source instanceof EntityDamageSourceIndirect)) {
					++attacksParried; // increment after disarm check
					player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 0.4F, 0.5F);
					playMissSound = false;
					Vec3d vel = player.getLookVec().scale(getKnockbackStrength());
					attacker.addVelocity(vel.xCoord, vel.yCoord + 0.15, vel.zCoord);
					return true;
				} // don't deactivate early, as there is a delay between uses
			}
		}
		if (source.getSourceOfDamage() instanceof EntityArrow || source.getSourceOfDamage() instanceof EntityThrowable || source.getSourceOfDamage() instanceof EntityFireball) {
			if (attacksParried < getMaxParries() && parryTimer > getParryDelay() && player.getHeldItemMainhand() == null) {
				if (!(source instanceof EntityDamageSourceIndirect)) {
					++attacksParried; // increment after disarm check
					player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 0.4F, 0.5F);
					playMissSound = false;
					Vec3d vel = player.getLookVec().scale(getKnockbackStrength() * 10);
					source.getSourceOfDamage().addVelocity(vel.xCoord, vel.yCoord + 0.15, vel.zCoord);
					return true;
				} // don't d
			}
		}
		return false;
	}
}
