package com.minenash.action_hunger.config;

public class Config extends TinyConfig {

    public enum Curve {DISABLED, LINEAR, QUADRATIC, EXPONENTIAL}

    public static int constantRegenRate = 80;
    public static float constantRegenAmount = 0F;

    public static int constantExhaustionRate = 80;
    public static float constantExhaustionAmount = 0F;

    public static int starvationDamageRate = 80;
    public static float starvationDamageAmount = 1F;

    public static int foodRegenRate = 80;
    public static float foodRegenHealthAmount = 1.0F;
    public static float foodRegenExhaustionAmount = 6.0F;
    public static int foodRegenMinimumHunger = 18;

    public static int hyperFoodRegenRate = 10;
    public static float hyperFoodRegenHealthMultiplier = 1.0F;
    public static float hyperFoodRegenExhaustionMultiplier = 1.0F;
    public static int hyperFoodRegenMinimumHunger = 20;

    public static Curve dynamicRegenRateCurve = Curve.DISABLED;
    public static float dynamicRegenRateMultiplier = 1.0F;

    public static boolean dynamicRegenOnConstantRegen = true;
    public static boolean dynamicRegenOnConstantExhaustion = true;
    public static boolean dynamicRegenOnFoodRegen = true;
    public static boolean dynamicRegenOnHyperFoodRegen = true;

    public static int spawnHealth = 20;
    public static int spawnHunger = 20;

    public static int foodLevelForSprint = 6;

    public static float jumpExhaustionAmount = 0.05F;
    public static float sprintJumpExhaustionAmount = 0.2F;
    public static float walkingExhaustionMultiplier = 0.0F;
    public static float sprintingExhaustionMultiplier = 0.1F;
    public static float crouchingExhaustionMultiplier = 0.0F;
    public static float swimmingExhaustionMultiplier = 0.1F;
    public static float walkingUnderwaterExhaustionMultiplier = 0.1F;
    public static float walkingOnWaterExhaustionMultiplier = 0.1F;

    public static float hungerFromFoodMultiplier = 1.0F;
    public static float saturationFromFoodMultiplier = 1.0F;

    public static boolean disableRegenWhenUsingShield = false;
    public static int shieldExhaustionRate = 80;
    public static float shieldExhaustionAmount = 0.0F;

    public static float sleepExhaustionAmount = 0.0F;
    public static float dynamicSleepExhaustionAmount = 0.0F;

    public static boolean debug = false;

    public static HealthEffect[] effects = new HealthEffect[0];
    public static SleepEffects[] sleepEffects = new SleepEffects[0];

}
