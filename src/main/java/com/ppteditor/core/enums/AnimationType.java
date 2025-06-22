package com.ppteditor.core.enums;

/**
 * 动画类型枚举
 * 定义幻灯片切换支持的动画效果
 */
public enum AnimationType {
    NONE("无动画"),
    FADE("淡入淡出"),
    SLIDE_LEFT("左滑"),
    SLIDE_RIGHT("右滑"),
    SLIDE_UP("上滑"),
    SLIDE_DOWN("下滑"),
    ZOOM_IN("放大"),
    ZOOM_OUT("缩小");
    
    private final String displayName;
    
    AnimationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 