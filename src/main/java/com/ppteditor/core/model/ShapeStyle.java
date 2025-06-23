package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * 图形样式配置类
 * 包含图形的所有样式属性
 */
public class ShapeStyle implements ElementStyle, java.io.Serializable {
    
    @Serializable
    private Color fillColor;
    
    @Serializable
    private Color borderColor;
    
    @Serializable
    private float borderWidth;
    
    @Serializable
    private boolean hasFill;
    
    @Serializable
    private boolean hasBorder;
    
    @Serializable
    private int borderStyle; // 0=实线, 1=虚线, 2=点线
    
    @Serializable
    private double opacity; // 透明度 0-1
    
    public ShapeStyle() {
        this.fillColor = Color.LIGHT_GRAY;
        this.borderColor = Color.BLACK;
        this.borderWidth = 1.0f;
        this.hasFill = true;
        this.hasBorder = true;
        this.borderStyle = 0;
        this.opacity = 1.0;
    }
    
    @Override
    public ShapeStyle clone() {
        ShapeStyle cloned = new ShapeStyle();
        cloned.fillColor = this.fillColor;
        cloned.borderColor = this.borderColor;
        cloned.borderWidth = this.borderWidth;
        cloned.hasFill = this.hasFill;
        cloned.hasBorder = this.hasBorder;
        cloned.borderStyle = this.borderStyle;
        cloned.opacity = this.opacity;
        return cloned;
    }
    
    @Override
    public void applyColorTheme(ColorTheme colorTheme) {
        this.fillColor = colorTheme.getPrimaryColor();
        this.borderColor = colorTheme.getSecondaryColor();
    }
    
    public Stroke getBorderStroke() {
        switch (borderStyle) {
            case 1: // 虚线
                return new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0);
            case 2: // 点线
                return new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2}, 0);
            default: // 实线
                return new BasicStroke(borderWidth);
        }
    }
    
    public Color getEffectiveFillColor() {
        if (!hasFill) return null;
        if (opacity < 1.0) {
            int alpha = (int) (255 * opacity);
            return new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
        }
        return fillColor;
    }
    
    public Color getEffectiveBorderColor() {
        if (!hasBorder) return null;
        if (opacity < 1.0) {
            int alpha = (int) (255 * opacity);
            return new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha);
        }
        return borderColor;
    }
    
    // Getter和Setter方法
    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
    
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color borderColor) { this.borderColor = borderColor; }
    
    public float getBorderWidth() { return borderWidth; }
    public void setBorderWidth(float borderWidth) { this.borderWidth = borderWidth; }
    
    public boolean isHasFill() { return hasFill; }
    public void setHasFill(boolean hasFill) { this.hasFill = hasFill; }
    
    public boolean isHasBorder() { return hasBorder; }
    public void setHasBorder(boolean hasBorder) { this.hasBorder = hasBorder; }
    
    public int getBorderStyle() { return borderStyle; }
    public void setBorderStyle(int borderStyle) { this.borderStyle = borderStyle; }
    
    public double getOpacity() { return opacity; }
    public void setOpacity(double opacity) { this.opacity = opacity; }
} 