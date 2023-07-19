package jagm.jagmkiwis;

import java.util.function.IntFunction;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class KiwiEntity extends Animal implements VariantHolder<KiwiEntity.Variant>, RangedAttackMob {

	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS,
			Items.PITCHER_POD);
	public static final ResourceLocation KIWI_LOOT_TABLE = new ResourceLocation(JagmKiwis.MODID, "entities/kiwi");
	private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(KiwiEntity.class, EntityDataSerializers.INT);
	private static final int CHANCE_OF_LASERS = 5;

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
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
		this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Cat.class, 8.0F, 1.0D, 1.0D));
		this.goalSelector.addGoal(6, this.digGoal);
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return this.isBaby() ? size.height : size.height * 0.8125F;
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
		KiwiEntity babyKiwi = JagmKiwis.KIWI.get().create(world);
		boolean isLaserVariant = this.getRandom().nextInt(100) < CHANCE_OF_LASERS;
		babyKiwi.setVariant(isLaserVariant ? Variant.LASER : Variant.NORMAL);
		return babyKiwi;
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

	@Override
	public void setVariant(Variant variant) {
		if (variant == Variant.LASER) {
			this.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
			this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 20, 40, 20.0F));
			this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
			this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
		}
		else {
			this.goalSelector.addGoal(1, new PanicGoal(this, 1.0D));
		}
		this.entityData.set(DATA_TYPE_ID, variant.id);
	}

	@Override
	public Variant getVariant() {
		return Variant.byId(this.entityData.get(DATA_TYPE_ID));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TYPE_ID, Variant.NORMAL.id);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("KiwiType", this.getVariant().id);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(Variant.byId(compoundTag.getInt("KiwiType")));
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData,
			@Nullable CompoundTag compoundTag) {
		boolean isLaserVariant = levelAccessor.getRandom().nextInt(100) < CHANCE_OF_LASERS;
		this.setVariant(isLaserVariant ? Variant.LASER : Variant.NORMAL);
		return super.finalizeSpawn(levelAccessor, difficulty, mobSpawnType, spawnGroupData, compoundTag);
	}
	
	@Override
	public void performRangedAttack(LivingEntity target, float p_33318_) {
		LaserBeamEntity laser = new LaserBeamEntity(this.level(), this);
		double d0 = target.getX() - this.getX();
		double d1 = target.getEyeY() - this.getEyeY();
		double d2 = target.getZ() - this.getZ();
		laser.setPos(new Vec3(this.getX(), this.getEyeY(), this.getZ()).add((new Vec3(d0, d1, d2)).normalize()));
		laser.shoot(d0, d1, d2, 1.5F, 0.0F);
		this.level().addFreshEntity(laser);
		if(!this.isSilent()) {
			this.playSound(JagmKiwis.LASER_SHOOT_SOUND.get());
		}
	}

	public static enum Variant implements StringRepresentable {
		NORMAL(0, "normal"), LASER(99, "laser");

		private static final IntFunction<Variant> BY_ID = ByIdMap.sparse(Variant::id, values(), NORMAL);
		final int id;
		private final String name;

		private Variant(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public int id() {
			return this.id;
		}

		public static Variant byId(int p_262665_) {
			return BY_ID.apply(p_262665_);
		}

	}

}
