package com.ppteditor.core.model;

import java.awt.Color;
import java.util.*;
import java.util.List;

/**
 * 配色主题类
 * 定义幻灯片的颜色主题
 */
public class ColorTheme {
    
    private String name;
    private Color primaryColor;
    private Color secondaryColor;
    private Color accentColor;
    private Color backgroundColor;
    private Color textColor;
    private Map<String, Color> customColors;
    
    // 无参构造函数，供Jackson反序列化使用
    public ColorTheme() {
        this.customColors = new HashMap<>();
    }
    
    public ColorTheme(String name) {
        this.name = name;
        this.customColors = new HashMap<>();
    }
    
    // 静态工厂方法 - 预定义主题
    public static ColorTheme createDefaultTheme() {
        ColorTheme theme = new ColorTheme("默认主题");
        theme.primaryColor = new Color(0, 123, 255);
        theme.secondaryColor = new Color(108, 117, 125);
        theme.accentColor = new Color(255, 193, 7);
        theme.backgroundColor = Color.WHITE;
        theme.textColor = Color.BLACK;
        return theme;
    }
    
    public static ColorTheme createBlueTheme() {
        ColorTheme theme = new ColorTheme("蓝色主题");
        theme.primaryColor = new Color(0, 86, 179);
        theme.secondaryColor = new Color(173, 216, 230);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(240, 248, 255);
        theme.textColor = new Color(25, 25, 112);
        return theme;
    }
    
    public static ColorTheme createGreenTheme() {
        ColorTheme theme = new ColorTheme("绿色主题");
        theme.primaryColor = new Color(34, 139, 34);
        theme.secondaryColor = new Color(144, 238, 144);
        theme.accentColor = new Color(255, 140, 0);
        theme.backgroundColor = new Color(240, 255, 240);
        theme.textColor = new Color(0, 100, 0);
        return theme;
    }
    
    public static ColorTheme createRedTheme() {
        ColorTheme theme = new ColorTheme("红色主题");
        theme.primaryColor = new Color(220, 20, 60);
        theme.secondaryColor = new Color(255, 182, 193);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(255, 250, 250);
        theme.textColor = new Color(139, 0, 0);
        return theme;
    }
    
    public static ColorTheme createPurpleTheme() {
        ColorTheme theme = new ColorTheme("紫色主题");
        theme.primaryColor = new Color(138, 43, 226);
        theme.secondaryColor = new Color(221, 160, 221);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(248, 248, 255);
        theme.textColor = new Color(75, 0, 130);
        return theme;
    }
    
    public static ColorTheme createOrangeTheme() {
        ColorTheme theme = new ColorTheme("橙色主题");
        theme.primaryColor = new Color(255, 140, 0);
        theme.secondaryColor = new Color(255, 218, 185);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(255, 250, 240);
        theme.textColor = new Color(139, 69, 19);
        return theme;
    }
    
    public static ColorTheme createDarkTheme() {
        ColorTheme theme = new ColorTheme("深色主题");
        theme.primaryColor = new Color(100, 149, 237);
        theme.secondaryColor = new Color(119, 136, 153);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(47, 79, 79);
        theme.textColor = Color.WHITE;
        return theme;
    }
    
    public static ColorTheme createBusinessTheme() {
        ColorTheme theme = new ColorTheme("商务主题");
        theme.primaryColor = new Color(25, 25, 112);
        theme.secondaryColor = new Color(176, 196, 222);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(248, 248, 255);
        theme.textColor = new Color(25, 25, 112);
        return theme;
    }
    
    public static ColorTheme createNatureTheme() {
        ColorTheme theme = new ColorTheme("自然主题");
        theme.primaryColor = new Color(107, 142, 35);
        theme.secondaryColor = new Color(189, 183, 107);
        theme.accentColor = new Color(255, 140, 0);
        theme.backgroundColor = new Color(245, 245, 220);
        theme.textColor = new Color(85, 107, 47);
        return theme;
    }
    
    // 新增专业配色主题
    public static ColorTheme createOfficeTheme() {
        ColorTheme theme = new ColorTheme("Office经典");
        theme.primaryColor = new Color(68, 114, 196);  // Office蓝
        theme.secondaryColor = new Color(217, 225, 242);
        theme.accentColor = new Color(255, 192, 0);
        theme.backgroundColor = Color.WHITE;
        theme.textColor = new Color(68, 68, 68);
        return theme;
    }
    
    public static ColorTheme createModernTheme() {
        ColorTheme theme = new ColorTheme("现代简约");
        theme.primaryColor = new Color(91, 155, 213);
        theme.secondaryColor = new Color(217, 225, 242);
        theme.accentColor = new Color(255, 192, 0);
        theme.backgroundColor = new Color(248, 249, 250);
        theme.textColor = new Color(44, 62, 80);
        return theme;
    }
    
    public static ColorTheme createElegantTheme() {
        ColorTheme theme = new ColorTheme("优雅灰");
        theme.primaryColor = new Color(112, 173, 71);
        theme.secondaryColor = new Color(198, 224, 180);
        theme.accentColor = new Color(255, 192, 0);
        theme.backgroundColor = new Color(242, 242, 242);
        theme.textColor = new Color(89, 89, 89);
        return theme;
    }
    
    public static ColorTheme createTechTheme() {
        ColorTheme theme = new ColorTheme("科技蓝");
        theme.primaryColor = new Color(0, 176, 240);
        theme.secondaryColor = new Color(191, 243, 255);
        theme.accentColor = new Color(255, 217, 102);
        theme.backgroundColor = new Color(247, 252, 255);
        theme.textColor = new Color(31, 73, 125);
        return theme;
    }
    
    public static ColorTheme createWarmTheme() {
        ColorTheme theme = new ColorTheme("暖色调");
        theme.primaryColor = new Color(237, 125, 49);
        theme.secondaryColor = new Color(248, 203, 173);
        theme.accentColor = new Color(255, 217, 102);
        theme.backgroundColor = new Color(255, 248, 240);
        theme.textColor = new Color(152, 72, 7);
        return theme;
    }
    
    public static ColorTheme createCoolTheme() {
        ColorTheme theme = new ColorTheme("冷色调");
        theme.primaryColor = new Color(70, 130, 180);
        theme.secondaryColor = new Color(176, 196, 222);
        theme.accentColor = new Color(255, 215, 0);
        theme.backgroundColor = new Color(240, 248, 255);
        theme.textColor = new Color(25, 25, 112);
        return theme;
    }
    
    public static ColorTheme createMinimalTheme() {
        ColorTheme theme = new ColorTheme("极简黑白");
        theme.primaryColor = new Color(64, 64, 64);
        theme.secondaryColor = new Color(160, 160, 160);
        theme.accentColor = new Color(255, 87, 87);
        theme.backgroundColor = Color.WHITE;
        theme.textColor = new Color(33, 33, 33);
        return theme;
    }
    
    public static ColorTheme createVibrantTheme() {
        ColorTheme theme = new ColorTheme("活力彩虹");
        theme.primaryColor = new Color(255, 87, 87);
        theme.secondaryColor = new Color(255, 183, 77);
        theme.accentColor = new Color(72, 207, 173);
        theme.backgroundColor = new Color(255, 255, 255);
        theme.textColor = new Color(45, 52, 54);
        return theme;
    }

    /**
     * 获取所有预定义主题
     */
    public static List<ColorTheme> getAllPredefinedThemes() {
        return Arrays.asList(
            createDefaultTheme(),
            createOfficeTheme(),
            createModernTheme(),
            createElegantTheme(),
            createTechTheme(),
            createBlueTheme(),
            createGreenTheme(),
            createRedTheme(),
            createPurpleTheme(),
            createOrangeTheme(),
            createWarmTheme(),
            createCoolTheme(),
            createDarkTheme(),
            createBusinessTheme(),
            createNatureTheme(),
            createMinimalTheme(),
            createVibrantTheme()
        );
    }
    
    /**
     * 随机生成配色主题
     */
    public static ColorTheme generateRandomTheme() {
        Random random = new Random();
        ColorTheme theme = new ColorTheme("随机主题 " + System.currentTimeMillis());
        
        // 随机生成主色调
        float hue = random.nextFloat();
        theme.primaryColor = Color.getHSBColor(hue, 0.7f, 0.9f);
        
        // 生成次要色（主色调的邻近色）
        float secondaryHue = (hue + 0.1f + random.nextFloat() * 0.1f) % 1.0f;
        theme.secondaryColor = Color.getHSBColor(secondaryHue, 0.4f, 0.9f);
        
        // 生成强调色（主色调的对比色）
        float accentHue = (hue + 0.4f + random.nextFloat() * 0.2f) % 1.0f;
        theme.accentColor = Color.getHSBColor(accentHue, 0.8f, 1.0f);
        
        // 背景色（浅色）
        theme.backgroundColor = Color.getHSBColor(hue, 0.1f, 0.98f);
        
        // 文本色（深色）
        theme.textColor = Color.getHSBColor(hue, 0.3f, 0.2f);
        
        return theme;
    }
    
    /**
     * 基于给定颜色生成协调的配色方案
     */
    public static ColorTheme generateThemeFromColor(Color baseColor, String themeName) {
        ColorTheme theme = new ColorTheme(themeName);
        
        // 将RGB转换为HSB
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        float baseHue = hsb[0];
        float baseSaturation = hsb[1];
        float baseBrightness = hsb[2];
        
        theme.primaryColor = baseColor;
        
        // 次要色：降低饱和度和亮度
        theme.secondaryColor = Color.getHSBColor(baseHue, baseSaturation * 0.6f, Math.min(1.0f, baseBrightness + 0.2f));
        
        // 强调色：对比色
        float accentHue = (baseHue + 0.5f) % 1.0f;
        theme.accentColor = Color.getHSBColor(accentHue, baseSaturation, baseBrightness);
        
        // 背景色：非常浅的主色调
        theme.backgroundColor = Color.getHSBColor(baseHue, baseSaturation * 0.1f, 0.98f);
        
        // 文本色：深色版本
        theme.textColor = Color.getHSBColor(baseHue, baseSaturation * 0.8f, Math.max(0.1f, baseBrightness * 0.3f));
        
        return theme;
    }
    
    // Getter和Setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Color getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(Color primaryColor) { this.primaryColor = primaryColor; }
    
    public Color getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(Color secondaryColor) { this.secondaryColor = secondaryColor; }
    
    public Color getAccentColor() { return accentColor; }
    public void setAccentColor(Color accentColor) { this.accentColor = accentColor; }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public Color getTextColor() { return textColor; }
    public void setTextColor(Color textColor) { this.textColor = textColor; }
    
    public Map<String, Color> getCustomColors() { return customColors; }
    public void setCustomColors(Map<String, Color> customColors) { this.customColors = customColors; }
    
    public void addCustomColor(String key, Color color) {
        customColors.put(key, color);
    }
    
    public Color getCustomColor(String key) {
        return customColors.get(key);
    }
} 