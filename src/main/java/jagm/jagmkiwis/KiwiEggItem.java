package jagm.jagmkiwis;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class KiwiEggItem extends EggItem {

	public KiwiEggItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (!level.isClientSide) {
			ThrownEgg thrownegg = new ThrownEgg(level, player) {

				@Override
				protected void onHit(HitResult hitResult) {
					Level level = this.level();
					HitResult.Type hitresult$type = hitResult.getType();
					if (hitresult$type == HitResult.Type.ENTITY) {
						this.onHitEntity((EntityHitResult) hitResult);
						level.gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
					} else if (hitresult$type == HitResult.Type.BLOCK) {
						BlockHitResult blockhitresult = (BlockHitResult) hitResult;
						this.onHitBlock(blockhitresult);
						BlockPos blockpos = blockhitresult.getBlockPos();
						level.gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, level.getBlockState(blockpos)));
					}
					if (!level.isClientSide) {
						if (this.random.nextInt(8) == 0) {
							int i = 1;
							if (this.random.nextInt(32) == 0) {
								i = 4;
							}

							for (int j = 0; j < i; ++j) {
								KiwiEntity kiwi = JagmKiwis.KIWI.get().create(level);
								if (kiwi != null) {
									kiwi.setAge(-24000);
									kiwi.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
									level.addFreshEntity(kiwi);
								}
							}
						}

						level.broadcastEntityEvent(this, (byte) 3);
						this.discard();
					}

				}

			};
			thrownegg.setItem(itemstack);
			thrownegg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.25F, 1.0F);
			level.addFreshEntity(thrownegg);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.getAbilities().instabuild) {
			itemstack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}

}
