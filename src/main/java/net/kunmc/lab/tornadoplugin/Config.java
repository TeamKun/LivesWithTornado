package net.kunmc.lab.tornadoplugin;

public class Config {
    public static int changeTargetInterval = 600;
    public static double riseCoef = 0.0625;
    public static double centrifugalCoef = 0.0625;
    public static String metadataKey = "TornadoPluginEntity";
    public static boolean exceptCreatives = true;
    public static boolean exceptSpectators = true;
    public static boolean exceptFlowing = true;
    public static boolean exceptSource = false;
    public static boolean exceptOtherTornado = true;
    public static int limitInvolvedEntity = 0;
    public static double involveBlockProbability = 1.0;
    public static double involveEntityProbability = 1.0;
    public static double followingSpeed = 0.3;
}
