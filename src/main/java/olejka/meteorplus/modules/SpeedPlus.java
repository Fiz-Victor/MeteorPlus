package olejka.meteorplus.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import olejka.meteorplus.MeteorPlus;

public class SpeedPlus extends Module {
	public SpeedPlus() {
		super(MeteorPlus.CATEGORY, "speed-plus", "Bypasses speed.");
	}

	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
		.name("Mode")
		.description("Speed mode.")
		.defaultValue(Mode.MatrixExploit)
		.build()
	);

	public enum Mode
	{
		MatrixExploit,
	}

	private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
		.name("Speed")
		.description("Speed.")
		.defaultValue(4)
		.build()
	);

	@Override
	public void onActivate() {
		FindItemResult elytra = InvUtils.find(Items.ELYTRA);
		if (!elytra.found()) {
			error("Elytra not found");
			toggle();
		}
		else {
			tick = 0;
		}
	}

	public void startFly() {
		mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
	}
	int tick = 0;

	@EventHandler
	public void onTickPre(TickEvent.Pre event) {
		FindItemResult elytra = InvUtils.find(Items.ELYTRA);
		if (elytra.found()) {
			if (tick == 0) {
				InvUtils.move().from(elytra.slot()).toArmor(2);
				startFly();
				startFly();
				InvUtils.move().fromArmor(2).to(elytra.slot());
				tick = 21;
			}
			else {
				tick--;
			}

			float yaw = mc.player.getYaw();
			Vec3d forward = Vec3d.fromPolar(0, yaw);
			Vec3d right = Vec3d.fromPolar(0, yaw + 90);
			double velX = 0;
			double velZ = 0;
			double s = speed.get();
			double speedValue = 0.01;
			if (mc.options.forwardKey.isPressed()) {
				velX += forward.x * s * speedValue;
				velZ += forward.z * s * speedValue;
			}
			if (mc.options.backKey.isPressed()) {
				velX -= forward.x * s * speedValue;
				velZ -= forward.z * s * speedValue;
			}

			if (mc.options.rightKey.isPressed()) {
				velX += right.x * s * speedValue;
				velZ += right.z * s * speedValue;
			}
			if (mc.options.leftKey.isPressed()) {
				velX -= right.x * s * speedValue;
				velZ -= right.z * s * speedValue;
			}
			double y = mc.player.getVelocity().y;
			((IVec3d) mc.player.getVelocity()).set(velX, y, velZ);
		}
	}
}
