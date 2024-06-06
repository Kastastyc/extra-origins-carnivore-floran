/*
 * Copyright (c) MoriyaShiine. All Rights Reserved.
 */
package moriyashiine.extraorigins.common.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class CanStandOnPowderSnowPower extends Power {
	public CanStandOnPowderSnowPower(PowerType<?> type, LivingEntity entity) {
		super(type, entity);
	}
}
