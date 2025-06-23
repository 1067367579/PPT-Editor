package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.ppteditor.core.enums.AnimationType;

/**
 * 幻灯片动画类
 * 定义幻灯片切换时的动画效果
 */
public class SlideAnimation {
    
    @Serializable
    private AnimationType type;
    
    @Serializable
    private int duration; // 动画持续时间（毫秒）
    
    @Serializable
    private String direction; // 动画方向（如：left, right, up, down）
    
    @Serializable
    private boolean enabled; // 是否启用动画
    
    public SlideAnimation() {
        this.type = AnimationType.NONE;
        this.duration = 500;
        this.direction = "right";
        this.enabled = false;
    }
    
    public SlideAnimation(AnimationType type) {
        this();
        this.type = type;
        this.enabled = true;
    }
    
    public SlideAnimation(AnimationType type, int duration, String direction) {
        this.type = type;
        this.duration = duration;
        this.direction = direction;
        this.enabled = true;
    }
    
    // 预定义动画
    public static SlideAnimation fadeIn() {
        return new SlideAnimation(AnimationType.FADE, 500, "in");
    }
    
    public static SlideAnimation slideLeft() {
        return new SlideAnimation(AnimationType.SLIDE, 500, "left");
    }
    
    public static SlideAnimation slideRight() {
        return new SlideAnimation(AnimationType.SLIDE, 500, "right");
    }
    
    public static SlideAnimation slideUp() {
        return new SlideAnimation(AnimationType.SLIDE, 500, "up");
    }
    
    public static SlideAnimation slideDown() {
        return new SlideAnimation(AnimationType.SLIDE, 500, "down");
    }
    
    public static SlideAnimation zoom() {
        return new SlideAnimation(AnimationType.ZOOM, 400, "in");
    }
    
    public static SlideAnimation flip() {
        return new SlideAnimation(AnimationType.FLIP, 600, "horizontal");
    }
    
    public static SlideAnimation dissolve() {
        return new SlideAnimation(AnimationType.DISSOLVE, 800, "random");
    }
    
    // Getter和Setter方法
    public AnimationType getType() { return type; }
    public void setType(AnimationType type) { this.type = type; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = Math.max(100, Math.min(5000, duration)); }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    @Override
    public String toString() {
        if (!enabled || type == AnimationType.NONE) {
            return "无动画";
        }
        return type.getDisplayName() + " (" + duration + "ms)";
    }
} 