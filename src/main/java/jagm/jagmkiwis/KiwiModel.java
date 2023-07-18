package jagm.jagmkiwis;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KiwiModel<T extends Entity> extends AgeableListModel<T> {

	public static final ModelLayerLocation KIWI_LAYER = new ModelLayerLocation(new ResourceLocation(JagmKiwis.MODID, "kiwi"), "main");
	private final ModelPart head;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart body;

	private float headXRot;

	public KiwiModel(ModelPart root) {
		super(false, 2.0F, 0.5F);
		this.head = root.getChild("head");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
		this.body = root.getChild("body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -1.5F, -3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(12, 10)
				.addBox(-1.0F, -0.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 17.5F, -1.0F));
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(15, 0).addBox(-1.5F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(23, 0)
				.addBox(-1.0F, 1.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(21, 3).addBox(-2.0F, 3.5F, -2.5F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 20.5F, 2.0F));
		partdefinition.addOrReplaceChild("right_leg",
				CubeListBuilder.create().texOffs(15, 0).addBox(-1.5F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(23, 0)
						.addBox(-1.0F, 1.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(21, 3)
						.addBox(-2.0F, 3.5F, -2.5F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 20.5F, 2.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 19.5F, 1.5F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void prepareMobModel(T p_103687_, float p_103688_, float p_103689_, float p_103690_) {
		super.prepareMobModel(p_103687_, p_103688_, p_103689_, p_103690_);
		KiwiEntity kiwi = (KiwiEntity) p_103687_;
		this.head.y = 17.5F + kiwi.getHeadEatPositionScale(p_103690_);
		this.headXRot = kiwi.getHeadEatAngleScale(p_103690_);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.leftLeg, this.rightLeg);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.xRot = this.headXRot;
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
	}

}
