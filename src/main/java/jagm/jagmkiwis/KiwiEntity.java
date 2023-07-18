package jagm.jagmkiwis;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkHooks;

public class KiwiEntity extends Animal {

	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS,
			Items.PITCHER_POD);
	public static final ResourceLocation KIWI_LOOT_TABLE = new ResourceLocation(JagmKiwis.MODID, "entities/kiwi");

	private KiwiDigGoal digGoal;
	private int digAnimationTick;
	public int eggTime = this.random.nextInt(12000) + 12000;

	protected KiwiEntity(EntityType<? extends Animal> entityType, Level world) {
		super(entityType, world);
	}

	@Override
	protected void registerGoals() {
		this.digGoal = new KiwiDigGoal(this);
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
		this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0D, 1.0D));
		this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Cat.class, 8.0F, 1.0D, 1.0D));
		this.goalSelector.addGoal(6, this.digGoal);
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return this.isBaby() ? size.height * 0.85F : size.height * 0.92F;
	}

	@Override
	protected void customServerAiStep() {
		this.digAnimationTick = this.digGoal.getDigAnimationTick();
		super.customServerAiStep();
	}

	@Override
	public void aiStep() {
		Level level = this.level();
		if (level.isClientSide) {
			this.digAnimationTick = Math.max(0, this.digAnimationTick - 1);
		}
		if (!level.isClientSide && this.isAlive() && !this.isBaby() && --this.eggTime <= 0) {
			this.playSound(JagmKiwis.KIWI_LAY_EGG.get(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			this.spawnAtLocation(JagmKiwis.KIWI_EGG.get());
			this.gameEvent(GameEvent.ENTITY_PLACE);
			this.eggTime = this.random.nextInt(12000) + 12000;
		}

		super.aiStep();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return FOOD_ITEMS.test(stack);
	}

	@Override
	public void handleEntityEvent(byte p_29814_) {
		if (p_29814_ == 10) {
			this.digAnimationTick = 40;
		} else {
			super.handleEntityEvent(p_29814_);
		}
	}

	public float getHeadEatPositionScale(float p_29881_) {
		if (this.digAnimationTick <= 0) {
			return 0.0F;
		} else if (this.digAnimationTick >= 4 && this.digAnimationTick <= 36) {
			return 1.0F;
		} else {
			return this.digAnimationTick < 4 ? ((float) this.digAnimationTick - p_29881_) / 4.0F : -((float) (this.digAnimationTick - 40) - p_29881_) / 4.0F;
		}
	}

	public float getHeadEatAngleScale(float p_29883_) {
		if (this.digAnimationTick > 4 && this.digAnimationTick <= 36) {
			float f = ((float) (this.digAnimationTick - 4) - p_29883_) / 32.0F;
			return ((float) Math.PI / 5F) + 0.21991149F * Mth.sin(f * 28.7F);
		} else {
			return this.digAnimationTick > 0 ? ((float) Math.PI / 5F) : this.getXRot() * ((float) Math.PI / 180F);
		}
	}

	@Override
	public KiwiEntity getBreedOffspring(ServerLevel world, AgeableMob kiwi) {
		return new KiwiEntity(JagmKiwis.KIWI.get(), world);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public static AttributeSupplier.Builder prepareAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.MAX_HEALTH, 5.0D).add(Attributes.MOVEMENT_SPEED, 0.35D)
				.add(Attributes.FOLLOW_RANGE, 40.0D);
	}

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return KIWI_LOOT_TABLE;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return JagmKiwis.KIWI_AMBIENT_SOUND.get();
	}

	@Override
	public void playAmbientSound() {
		if (this.level().dimensionType().hasFixedTime() || this.level().isNight()) {
			super.playAmbientSound();
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return JagmKiwis.KIWI_HURT_SOUND.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return JagmKiwis.KIWI_DEATH_SOUND.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockState) {
		this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
	}

	@Override
	protected float getSoundVolume() {
		return 0.3F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 160;
	}

}
