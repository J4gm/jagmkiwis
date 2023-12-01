package jagm.jagmkiwis;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class LaserBeamEntity extends AbstractArrow {

	private double baseDamage = 4.0D;

	protected LaserBeamEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	protected LaserBeamEntity(Level level, LivingEntity shooter) {
		super(JagmKiwis.LASER_BEAM.get(), shooter, level);
	}

	@Override
	protected ItemStack getPickupItem() {
		return null;
	}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		BlockState blockstate = this.level().getBlockState(hitResult.getBlockPos());
		blockstate.onProjectileHit(this.level(), blockstate, hitResult, this);
		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		Entity target = hitResult.getEntity();
		float f = (float) this.getDeltaMovement().length();
		int i = Mth.ceil(Mth.clamp((double) f * this.baseDamage, 0.0D, (double) Integer.MAX_VALUE));
		if (this.isCritArrow()) {
			long j = (long) this.random.nextInt(i / 2 + 2);
			i = (int) Math.min(j + (long) i, 2147483647L);
		}
		Entity shooter = this.getOwner();
		DamageSource damagesource;
		if (shooter == null) {
			damagesource = this.damageSources().arrow(this, this);
		} else {
			damagesource = this.damageSources().arrow(this, shooter);
			if (shooter instanceof LivingEntity) {
				((LivingEntity) shooter).setLastHurtMob(target);
			}
		}
		boolean flag = target.getType() == EntityType.ENDERMAN;
		if (target.hurt(damagesource, (float) i)) {
			if (flag) {
				return;
			}
			if (target instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity) target;
				Level level = this.level();
				if (!level.isClientSide && shooter instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects(livingentity, shooter);
					EnchantmentHelper.doPostDamageEffects((LivingEntity) shooter, livingentity);
				}
				this.doPostHurtEffects(livingentity);
			}
		}
		this.discard();
	}
	
	public void tick() {
		super.tick();
		if(this.getDeltaMovement().length() < 1.0D) {
			this.discard();
		}
	}

}
