package olejka.meteorplus.mixin.meteorclient;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import olejka.meteorplus.utils.RaycastUtils;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static baritone.api.utils.Helper.mc;

@Mixin(Freecam.class)
public class FreecamMixin {
	private final Freecam freecam = (Freecam)(Object) this;

	private final SettingGroup freecamMeteorPlusSetting = freecam.settings.createGroup("Meteor Plus");

	private final Setting<Boolean> baritoneControl = freecamMeteorPlusSetting.add(new BoolSetting.Builder()
		.name("baritone-control")
		.description("Left-click to set the destination on the selected block. Right-click to cancel.")
		.build()
	);

	private final Setting<Boolean> smartBaritoneControl = freecamMeteorPlusSetting.add(new BoolSetting.Builder()
		.name("smart-baritone-control")
		.description("For the baritone task, consider the notes in your hand.")
		.visible(baritoneControl::get)
		.build()
	);

	@Unique
	@EventHandler
	private void onMouseButtonEvent(MouseButtonEvent event) {
		if (!baritoneControl.get()) return;
		if (event.action != KeyAction.Press) return;
		if (mc.currentScreen != null) return;
		if (mc.player == null) return;

		ItemStack mainhand = mc.player.getMainHandStack();
		ItemStack offhand = mc.player.getOffHandStack();

		if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			BlockPos blockPos = null;
			Vec3d yaw = RaycastUtils.getRotationVector((float) freecam.getPitch(mc.getTickDelta()), (float) freecam.getYaw(mc.getTickDelta()));
			Vec3d pos = new Vec3d(freecam.pos.x, freecam.pos.y, freecam.pos.z);
			HitResult result = RaycastUtils.raycast(pos, yaw, 64 * 4, mc.getTickDelta(), true);
			if (result.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult) result;
				blockPos = blockHitResult.getBlockPos();
			}

			if (blockPos == null) return;

			if (mc.world == null) return;

			if (mc.world.getBlockState(blockPos).isAir()) return;

			Block mineBlock = mc.world.getBlockState(blockPos).getBlock();

			if (smartBaritoneControl.get()) {
				if (mainhand == null || mainhand.getItem() == Items.AIR) {
					GoalBlock goal = new GoalBlock(blockPos.up());
					BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
					event.cancel();
				}
				if (mineBlock instanceof CropBlock || mineBlock instanceof SugarCaneBlock) {
					BaritoneAPI.getProvider().getPrimaryBaritone().getFarmProcess().farm((int) mc.player.getPos().distanceTo(blockPos.toCenterPos()) + 1);
					event.cancel();
				} else if (mainhand != null && (mainhand.getItem() instanceof PickaxeItem || mainhand.getItem() instanceof AxeItem || mainhand.getItem() instanceof ShovelItem)) {
					BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().mine(mineBlock);
					event.cancel();
				} else {
					GoalBlock goal = new GoalBlock(blockPos.up());
					BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
					event.cancel();
				}
			} else {
				GoalBlock goal = new GoalBlock(blockPos.up());
				BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
				event.cancel();
			}
		}

		if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			if (smartBaritoneControl.get()) {
				if ((offhand == null || !(offhand.getItem() instanceof BlockItem)) && (mainhand == null || !(mainhand.getItem() instanceof BlockItem))) {
					if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()) {
						BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(null);
						event.cancel();
					}
				}
			}
			else {
				BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(null);
				event.cancel();
			}
		}
	}
}
