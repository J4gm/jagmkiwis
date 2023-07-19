package jagm.jagmkiwis;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KiwiRenderer extends MobRenderer<KiwiEntity, KiwiModel<KiwiEntity>> {

	private static final ResourceLocation NORMAL_KIWI = new ResourceLocation(JagmKiwis.MODID, "textures/entity/kiwi.png");
	private static final ResourceLocation LASER_KIWI = new ResourceLocation(JagmKiwis.MODID, "textures/entity/laser_kiwi.png");

	public KiwiRenderer(Context context) {
		super(context, new KiwiModel<KiwiEntity>(context.bakeLayer(KiwiModel.KIWI_LAYER)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(KiwiEntity kiwi) {
		return kiwi.getVariant() == KiwiEntity.Variant.LASER ? LASER_KIWI : NORMAL_KIWI;
	}

}
