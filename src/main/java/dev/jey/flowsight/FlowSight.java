package dev.jey.flowsight;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Press O to see flowing water: every water block that is not a source gets a tinted box
 * (warm = weak, about to dry up; cool = strong, right next to a source; violet = falling)
 * and an arrow pointing where it flows. Works underwater, where flowing and source water
 * normally look the same.
 */
public class FlowSight implements ClientModInitializer {
	/** Blocks around the player in each direction. 16 → a 33×33×33 cube, about 36k lookups per tick. */
	private static final int RADIUS = 16;

	private static boolean enabled;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("flowsight", "main"));
		KeyMapping toggle = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.flowsight.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, category));

		// Runs inside the client tick's gizmo collection, so these gizmos are drawn until the next tick.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggle.consumeClick()) {
				enabled = !enabled;
				if (client.player != null)
					client.player.sendOverlayMessage(Component.translatable(enabled ? "flowsight.on" : "flowsight.off"));
			}
			if (enabled && client.player != null && client.level != null) emit(client);
		});
	}

	private static void emit(Minecraft client) {
		Level level = client.level;
		BlockPos center = client.player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
			FluidState fluid = level.getFluidState(pos);
			if (fluid.isEmpty() || fluid.isSource() || !fluid.is(FluidTags.WATER)) continue;

			boolean falling = fluid.hasProperty(FlowingFluid.FALLING) && fluid.getValue(FlowingFluid.FALLING);
			float height = fluid.getHeight(level, pos);
			int rgb = falling ? 0xB060FF : strengthColor(fluid.getAmount());
			Gizmos.cuboid(
					new AABB(pos.getX() + 0.02, pos.getY() + 0.02, pos.getZ() + 0.02,
							pos.getX() + 0.98, pos.getY() + Math.max(0.1, height), pos.getZ() + 0.98),
					GizmoStyle.strokeAndFill(0x60000000 | rgb, 1f, 0x1C000000 | rgb));

			Vec3 flow = fluid.getFlow(level, pos);
			Vec3 from = new Vec3(pos.getX() + 0.5, pos.getY() + Math.max(0.15, height - 0.1), pos.getZ() + 0.5);
			if (falling) Gizmos.arrow(from, from.add(0, -0.6, 0), 0x90FFFFFF, 1.5f);
			else if (flow.lengthSqr() > 1e-4) Gizmos.arrow(from, from.add(flow.normalize().scale(0.4)), 0x90FFFFFF, 1.5f);
		}
	}

	/** Amount 7 (next to a source) → cyan, amount 1 (edge of the spread) → orange. */
	static int strengthColor(int amount) {
		float t = Math.clamp((amount - 1) / 6f, 0f, 1f);
		int r = (int) (255 + (40 - 255) * t);
		int g = (int) (150 + (220 - 150) * t);
		int b = (int) (40 + (255 - 40) * t);
		return r << 16 | g << 8 | b;
	}
}
