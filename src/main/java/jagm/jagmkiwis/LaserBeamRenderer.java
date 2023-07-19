package jagm.jagmkiwis;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class LaserBeamRenderer extends ArrowRenderer<LaserBeamEntity>{
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(JagmKiwis.MODID, "textures/entity/laser_beam.png");

	public LaserBeamRenderer(Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(LaserBeamEntity p_114482_) {
		return TEXTURE;
	}

}
