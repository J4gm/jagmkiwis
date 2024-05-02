package jagm.jagmkiwis;

import net.minecraft.world.entity.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(JagmKiwis.MODID)
public class JagmKiwis {

	public static final String MODID = "jagmkiwis";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

	public static final RegistryObject<EntityType<KiwiEntity>> KIWI = ENTITIES.register("kiwi",
			() -> EntityType.Builder.of(KiwiEntity::new, MobCategory.CREATURE).clientTrackingRange(8).setShouldReceiveVelocityUpdates(false)
					.sized(0.5F, 0.5F).build("kiwi"));
	public static final RegistryObject<EntityType<LaserBeamEntity>> LASER_BEAM = ENTITIES.register("laser_beam", () -> EntityType.Builder
			.of((EntityType.EntityFactory<LaserBeamEntity>) LaserBeamEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F).build("laser_beam"));

	public static final RegistryObject<Item> KIWI_SPAWN_EGG = ITEMS.register("kiwi_spawn_egg", () -> new ForgeSpawnEggItem(KIWI, 0x97784A, 0xBEE000, new Item.Properties()));
	public static final RegistryObject<Item> KIWI_FRUIT = ITEMS.register("kiwi_fruit", () -> new Item((new Item.Properties()).food(Foods.APPLE)));
	public static final RegistryObject<Item> KIWI_EGG = ITEMS.register("kiwi_egg", () -> new KiwiEggItem((new Item.Properties()).stacksTo(16)));
	public static final RegistryObject<Item> PAVLOVA = ITEMS.register("pavlova",
			() -> new Item((new Item.Properties()).food((new FoodProperties.Builder()).nutrition(10).saturationModifier(0.6F).build())));

	public static final RegistryObject<SoundEvent> KIWI_AMBIENT_SOUND = SOUNDS.register("kiwi_ambient",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kiwi_ambient")));
	public static final RegistryObject<SoundEvent> KIWI_HURT_SOUND = SOUNDS.register("kiwi_hurt",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kiwi_hurt")));
	public static final RegistryObject<SoundEvent> KIWI_DEATH_SOUND = SOUNDS.register("kiwi_death",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kiwi_death")));
	public static final RegistryObject<SoundEvent> KIWI_DIG = SOUNDS.register("kiwi_dig", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kiwi_dig")));
	public static final RegistryObject<SoundEvent> KIWI_LAY_EGG = SOUNDS.register("kiwi_lay_egg",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "kiwi_lay_egg")));
	public static final RegistryObject<SoundEvent> LASER_SHOOT_SOUND = SOUNDS.register("laser_shoot",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "laser_shoot")));

	public JagmKiwis() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		ITEMS.register(modEventBus);
		ENTITIES.register(modEventBus);
		SOUNDS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(JagmKiwis.class);
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public class ModSetup {

		@SubscribeEvent
		public static void onAttributeCreate(EntityAttributeCreationEvent event) {
			event.put(KIWI.get(), KiwiEntity.prepareAttributes().build());
		}

		@SubscribeEvent
		public static void onRegisterSpawnPlacements(SpawnPlacementRegisterEvent event) {
			event.register(KIWI.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
					SpawnPlacementRegisterEvent.Operation.REPLACE);
		}

		@SubscribeEvent
		public static void onFillCreativeTabs(BuildCreativeModeTabContentsEvent event) {

			if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
				event.accept(KIWI_FRUIT);
				event.accept(PAVLOVA);
			}

			if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
				event.accept(KIWI_SPAWN_EGG);
			}

			if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
				event.accept(KIWI_EGG);
			}

			if (event.getTabKey() == CreativeModeTabs.COMBAT) {
				event.accept(KIWI_EGG);
			}

		}

	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			EntityRenderers.register(KIWI.get(), KiwiRenderer::new);
			EntityRenderers.register(LASER_BEAM.get(), LaserBeamRenderer::new);
		}

		@SubscribeEvent
		public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
			event.registerLayerDefinition(KiwiModel.KIWI_LAYER, KiwiModel::createBodyLayer);
		}

	}

	@SubscribeEvent
	public static void onJoinLevel(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof Cat cat) {
            if (!cat.level().isClientSide) {
				cat.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(cat, KiwiEntity.class, false));
			}
		}
	}

}
