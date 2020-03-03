package com.favouritedragon.apitest.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySunBolt extends EntityFireball {

    private int level;
    private float damage;

    public EntitySunBolt(World world) {
        super(world);
        this.setSize(0.3125F, 0.3125F);
    }

    public EntitySunBolt(World world, EntityLivingBase shooter, double accelX, double accelY, double accelZ, int level, float damage) {
        super(world, shooter, accelX * (1 + level / 10F), accelY * (1 + level / 10F), accelZ * (1 + level / 10F));
        this.setSize(0.3125F, 0.3125F);
        this.level = level;
        this.damage = damage;
    }

    public EntitySunBolt(World world, double x, double y, double z, double accelX, double accelY, double accelZ, int level, float damage) {
        super(world, x, y, z, accelX * (1 + level / 10F), accelY * (1 + level / 10F), accelZ * (1 + level / 10F));
        this.setSize(0.3125F, 0.3125F);
        this.level = level;
        this.damage = damage;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected boolean isFireballFiery() {
        return false;
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    public void onImpact(RayTraceResult result) {
        if (!this.worldObj.isRemote) {
            if (result.entityHit != null) {
                if (!result.entityHit.isImmuneToFire()) {
                    boolean flag = result.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), damage);

                    if (flag) {
                        this.applyEnchantments(this.shootingEntity, result.entityHit);
                        result.entityHit.setFire(3 + level * 2);
                    }
                }
            } else {
                boolean flag1 = true;

                if (this.shootingEntity != null && this.shootingEntity instanceof EntityLiving) {
                    flag1 = this.worldObj.getGameRules().getBoolean("mobGriefing");
                }

                if (flag1) {
                    BlockPos blockpos = result.getBlockPos().offset(result.sideHit);

                    if (this.worldObj.isAirBlock(blockpos)) {
                        this.worldObj.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
                    }
                }
            }

            this.setDead();
        }
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }
}
