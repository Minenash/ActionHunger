package com.minenash.action_hunger.config;

public class Config extends TinyConfig {

    public enum DynamicRegenRateCurve {DISABLED, LINEAR, QUADRATIC, EXPONENTIAL}

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

    public static DynamicRegenRateCurve dynamicRegenRateCurve = DynamicRegenRateCurve.DISABLED;
    public static float dynamicRegenRateMultiplier = 1.0F;

    public static boolean dynamicRegenOnConstantRegen = true;
    public static boolean dynamicRegenOnConstantExhaustion = true;
    public static boolean dynamicRegenOnFoodRegen = true;
    public static boolean dynamicRegenOnHyperFoodRegen = true;

    public static int spawnHealth = 20;
    public static int spawnHunger = 20;

    public static boolean debug = false;

}
