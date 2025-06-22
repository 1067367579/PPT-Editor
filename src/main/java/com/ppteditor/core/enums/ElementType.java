package com.ppteditor.core.enums;

/**
 * 元素类型枚举
 * 定义幻灯片中支持的所有元素类型
 */
public enum ElementType {
    TEXT("文本框"),
    RECTANGLE("矩形"),
    ELLIPSE("椭圆"),
    IMAGE("图片"),
    ICON("图标"),
    LINE("线条");
    
    private final String displayName;
    
    ElementType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 