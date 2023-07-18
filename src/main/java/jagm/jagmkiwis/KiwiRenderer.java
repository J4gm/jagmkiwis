package jagm.jagmkiwis;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KiwiRenderer extends MobRenderer<KiwiEntity, KiwiModel<KiwiEntity>> {

	private static final ResourceLocation KIWI_LOCATION = new ResourceLocation(JagmKiwis.MODID, "textures/entity/kiwi.png");

	public KiwiRenderer(Context context) {
		super(context, new KiwiModel<KiwiEntity>(context.bakeLayer(KiwiModel.KIWI_LAYER)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(KiwiEntity p_114482_) {
		return KIWI_LOCATION;
	}

}
