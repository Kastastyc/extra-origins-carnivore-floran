/*
 * Copyright (c) MoriyaShiine. All Rights Reserved.
 */
package moriyashiine.extraorigins.client.event;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import moriyashiine.extraorigins.common.packet.ChangeRadialDirectionPacket;
import moriyashiine.extraorigins.common.power.RadialMenuPower;
import moriyashiine.extraorigins.common.util.RadialMenuDirection;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Direction;

import java.util.List;

public class RadialMenuEvents {
	public static boolean directionChanged = false;

	private static List<RadialMenuPower> activePowers;
	private static RadialMenuPower lastUsedPower;
	private static RadialMenuDirection targetDirection = null;
	private static boolean renderModeSwitch = false;
	private static int timer = 0;

	public static void init() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			activePowers = null;
			lastUsedPower = null;
			directionChanged = false;
			targetDirection = null;
			renderModeSwitch = false;
			timer = 0;
		});
		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
			@Override
			public void onEndTick(MinecraftClient client) {
				if (client.currentScreen != null) {
					return;
				}
				activePowers = PowerHolderComponent.getPowers(client.player, RadialMenuPower.class).stream().filter(power -> ApoliClient.idToKeyBindingMap.containsKey(power.getKey().key) && ApoliClient.idToKeyBindingMap.get(power.getKey().key).isPressed()).toList();
				if (!activePowers.isEmpty()) {
					client.mouse.unlockCursor();
					changeTargetMode(client);
					handleModeChange();
					renderModeSwitch = true;
				} else {
					client.mouse.lockCursor();
					lastUsedPower = null;
					directionChanged = false;
					targetDirection = null;
					renderModeSwitch = false;
					timer = 0;
				}
			}

			private void changeTargetMode(MinecraftClient client) {
				RadialMenuDirection direction = null;
				boolean arrowKey = false;
				if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_UP)) {
					direction = RadialMenuDirection.UP;
					arrowKey = true;
				} else if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_DOWN)) {
					direction = RadialMenuDirection.DOWN;
					arrowKey = true;
				} else if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT)) {
					direction = RadialMenuDirection.LEFT;
					arrowKey = true;
				} else if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_RIGHT)) {
					direction = RadialMenuDirection.RIGHT;
					arrowKey = true;
				}
				if (!arrowKey) {
					double x = client.mouse.getX() - (client.getWindow().getWidth() / 2F);
					double y = (client.getWindow().getHeight() / 2F) - client.mouse.getY();
					if (Math.abs(x) > 48 || Math.abs(y) > 48) {
						direction = switch (Direction.getFacing(x, y, 0)) {
							case UP -> RadialMenuDirection.UP;
							case DOWN -> RadialMenuDirection.DOWN;
							case WEST -> RadialMenuDirection.LEFT;
							case EAST -> RadialMenuDirection.RIGHT;
							default -> null;
						};
					}
				}
				if (targetDirection != direction) {
					targetDirection = direction;
					timer = 0;
				}
			}

			private void handleModeChange() {
				if (activePowers.get(0) != lastUsedPower) {
					timer = 0;
					lastUsedPower = activePowers.get(0);
				}
				if (targetDirection != null && lastUsedPower.getDirection() != targetDirection && lastUsedPower.getActionFromDirection(targetDirection) != null) {
					if (!directionChanged) {
						timer++;
					}
					if (timer == lastUsedPower.swapTime) {
						directionChanged = true;
						timer = 0;
						ChangeRadialDirectionPacket.send(targetDirection, lastUsedPower.getType());
					}
				}
			}
		});
		HudRenderCallback.EVENT.register(new HudRenderCallback() {
			@Override
			public void onHudRender(DrawContext drawContext, float tickDelta) {
				if (!renderModeSwitch) {
					return;
				}
				Window window = MinecraftClient.getInstance().getWindow();
				drawContext.drawTexture(lastUsedPower.spriteLocation, (window.getScaledWidth() / 2) - 64, (window.getScaledHeight() / 2) - 64, 0, 0, 128, 128, 320, 256);
				renderSection(RadialMenuDirection.UP, drawContext, window, -32, -80, 0);
				renderSection(RadialMenuDirection.DOWN, drawContext, window, -32, 16, 64);
				renderSection(RadialMenuDirection.LEFT, drawContext, window, -80, -32, 128);
				renderSection(RadialMenuDirection.RIGHT, drawContext, window, 16, -32, 192);
				if (timer > 0) {
					drawContext.drawTexture(lastUsedPower.spriteLocation, (window.getScaledWidth() / 2) - 13, (window.getScaledHeight() / 2) - 13, 48, 128, 26, 26, 320, 256);
					drawContext.drawTexture(lastUsedPower.spriteLocation, (window.getScaledWidth() / 2) - 12, (window.getScaledHeight() / 2) - 12, 24, 128, 24, 24, 320, 256);
					drawContext.drawTexture(lastUsedPower.spriteLocation, (window.getScaledWidth() / 2) - 12, (window.getScaledHeight() / 2) - 12, 0, 128, 24, (int) (24 - 24 * ((timer + 1) / (float) lastUsedPower.swapTime)), 320, 256);
				}
			}

			private void renderSection(RadialMenuDirection targetMode, DrawContext drawContext, Window window, int posXOffset, int posYOffset, int v) {
				if (lastUsedPower.getActionFromDirection(targetMode) == null) {
					return;
				}
				int u = 128;
				if (lastUsedPower.getDirection() == targetMode) {
					u += 128;
				} else if (targetDirection == targetMode) {
					u += 64;
				}
				drawContext.drawTexture(lastUsedPower.spriteLocation, (window.getScaledWidth() / 2) + posXOffset, (window.getScaledHeight() / 2) + posYOffset, u, v, 64, 64, 320, 256);
			}
		});
	}
}
