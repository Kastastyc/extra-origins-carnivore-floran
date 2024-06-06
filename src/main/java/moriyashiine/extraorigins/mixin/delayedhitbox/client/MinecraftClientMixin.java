/*
 * Copyright (c) MoriyaShiine. All Rights Reserved.
 */
package moriyashiine.extraorigins.mixin.delayedhitbox.client;

import moriyashiine.extraorigins.client.event.DelayedHitboxEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Nullable
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	@Nullable
	public ClientPlayerEntity player;

	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void enchancement$coyoteBite(CallbackInfoReturnable<Boolean> cir) {
		if (DelayedHitboxEvent.target != null) {
			interactionManager.attackEntity(player, DelayedHitboxEvent.target);
			player.swingHand(Hand.MAIN_HAND);
			cir.setReturnValue(true);
		}
	}
}
