package olejka.meteorplus.modules.spider;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.client.MinecraftClient;
import olejka.meteorplus.MeteorPlus;

public class SpiderMode {
	protected final MinecraftClient mc;
	protected final SpiderPlus settings;
	private final SpiderModes type;

	public SpiderMode(SpiderModes type) {
		this.settings = MeteorPlus.getInstance().spiderPlus;
		this.mc = MinecraftClient.getInstance();
		this.type = type;
	}

	public void onSendPacket(PacketEvent.Send event) {}
	public void onSentPacket(PacketEvent.Sent event) {}

	public void onTickEventPre(TickEvent.Pre event) {}
	public void onTickEventPost(TickEvent.Post event) {}

	public void onActivate() {}
	public void onDeactivate() {}
}
