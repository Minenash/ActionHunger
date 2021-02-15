package com.minenash.action_hunger.config;

public class Config extends TinyConfig {

    public enum DynamicRegenRateCurve {DISABLED, LINEAR, QUADRATIC, EXPONENTIAL}

    public static int constantRegenRate = 80;
    public static float constantRegenAmount = 0F;

    public static int constantHungerRate = 80;
    public static float constantHungerAmount = 0F;

    public static int foodRegenRate = 80;
    public static float foodRegenHealthMultiplier = 1.0F;
    public static float foodRegenExhaustionMultiplier = 1.0F;

    public static DynamicRegenRateCurve dynamicRegenRateCurve = DynamicRegenRateCurve.DISABLED;
    public static float dynamicRegenRateMultiplier = 1.0F;

}
