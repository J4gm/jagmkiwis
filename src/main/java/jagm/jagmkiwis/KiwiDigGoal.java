package jagm.jagmkiwis;

import java.util.EnumSet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class KiwiDigGoal extends Goal {

	private final Mob mob;
	public static final ResourceLocation DIGGING_LOOT = new ResourceLocation(JagmKiwis.MODID, "entities/kiwi_diggables");
	private int digAnimationTick;

	public KiwiDigGoal(Mob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.mob.isBaby() || this.mob.getRandom().nextInt(3000) != 0) {
			return false;
		} else {
			return this.mob.level().getBlockState(this.mob.blockPosition().below()).is(BlockTags.DIRT);
		}
	}

	@Override
	public void start() {
		this.digAnimationTick = this.adjustedTickDelay(40);
		this.mob.level().broadcastEntityEvent(this.mob, (byte) 10);
		this.mob.getNavigation().stop();
	}

	@Override
	public void stop() {
		this.digAnimationTick = 0;
	}

	@Override
	public boolean canContinueToUse() {
		return this.digAnimationTick > 0;
	}

	@Override
	public void tick() {
		this.digAnimationTick = Math.max(this.digAnimationTick - 1, 0);
		if (this.digAnimationTick == this.adjustedTickDelay(4)) {
			Level level = this.mob.level();
			if (!level.isClientSide && this.mob.isAlive()) {
				this.mob.playSound(JagmKiwis.KIWI_DIG.get(), 1.0F, (this.mob.getRandom().nextFloat() - this.mob.getRandom().nextFloat()) * 0.2F + 1.0F);
				LootTable diggingLoot = level.getServer().getLootData().getLootTable(DIGGING_LOOT);
				LootParams.Builder lootParams$builder = new LootParams.Builder((ServerLevel) level);
				LootParams lootParams = lootParams$builder.create(LootContextParamSets.EMPTY);
				diggingLoot.getRandomItems(lootParams, this.mob.getLootTableSeed(), this.mob::spawnAtLocation);
				this.mob.gameEvent(GameEvent.ENTITY_PLACE);
			}

		}
	}

	public int getDigAnimationTick() {
		return this.digAnimationTick;
	}

}
