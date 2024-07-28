package com.minenash.action_hunger.config;

import net.minecraft.entity.effect.StatusEffect;

import com.minenash.action_hunger.config.Config.AmplifierCurveSource;
import com.minenash.action_hunger.config.Config.Curve;
import com.minenash.action_hunger.config.Config.RequiredBounds;

public class HealthEffect {



    public String effect;
    public StatusEffect statusEffect;
    public int healthLowBound = 0;
    public int healthHighBound = Integer.MAX_VALUE;
    public int hungerLowBound = 0;
    public int hungerHighBound = Integer.MAX_VALUE;
    public RequiredBounds requiredBounds = RequiredBounds.BOTH;
    public int amplifier = 0;
    public Curve amplifierCurve = Curve.DISABLED;
    public float amplifierCurveMultiplier = 1;
    public AmplifierCurveSource amplifierCurveSource = AmplifierCurveSource.HEALTH;

    public boolean onSleep = false;
    public int onSleepDuration = 60;

}
