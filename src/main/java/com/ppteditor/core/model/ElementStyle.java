package com.ppteditor.core.model;

/**
 * 元素样式接口
 * 所有样式配置类的基接口
 */
public interface ElementStyle extends Cloneable {
    
    /**
     * 克隆样式对象
     * @return 克隆的样式对象
     */
    ElementStyle clone();
    
    /**
     * 应用主题配色
     * @param colorTheme 配色主题
     */
    void applyColorTheme(ColorTheme colorTheme);
} 