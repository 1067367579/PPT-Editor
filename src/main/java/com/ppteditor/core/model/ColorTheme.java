package com.ppteditor.core.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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