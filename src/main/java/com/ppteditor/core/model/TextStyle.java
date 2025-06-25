package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Color;
import java.awt.Font;

/**
 * 文本样式配置类
 * 包含文本的所有样式属性
 */
public class TextStyle implements ElementStyle, java.io.Serializable {
    
    // 文本对齐常量
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_JUSTIFY = 3;
    
    @Serializable
    private String fontFamily;
    
    @Serializable
    private int fontSize;
    
    @Serializable
    private boolean bold;
    
    @Serializable
    private boolean italic;
    
    @Serializable
    private boolean underline;
    
    @Serializable
    private Color textColor;
    
    @Serializable
    private Color backgroundColor;
    
    @Serializable
    private int alignment; // 0=左对齐, 1=居中, 2=右对齐
    
    @Serializable
    private double lineSpacing;
    
    public TextStyle() {
        this.fontFamily = "宋体";
        this.fontSize = 16;
        this.bold = false;
        this.italic = false;
        this.underline = false;
        this.textColor = Color.BLACK;
        this.backgroundColor = Color.WHITE;
        this.alignment = 0;
        this.lineSpacing = 1.0;
    }
    
    // Builder模式
    public static class Builder {
        private TextStyle style = new TextStyle();
        
        public Builder fontFamily(String fontFamily) {
            style.fontFamily = fontFamily;
            return this;
        }
        
        public Builder fontSize(int fontSize) {
            style.fontSize = fontSize;
            return this;
        }
        
        public Builder bold(boolean bold) {
            style.bold = bold;
            return this;
        }
        
        public Builder italic(boolean italic) {
            style.italic = italic;
            return this;
        }
        
        public Builder underline(boolean underline) {
            style.underline = underline;
            return this;
        }
        
        public Builder textColor(Color textColor) {
            style.textColor = textColor;
            return this;
        }
        
        public Builder backgroundColor(Color backgroundColor) {
            style.backgroundColor = backgroundColor;
            return this;
        }
        
        public Builder alignment(int alignment) {
            style.alignment = alignment;
            return this;
        }
        
        public Builder lineSpacing(double lineSpacing) {
            style.lineSpacing = lineSpacing;
            return this;
        }
        
        public TextStyle build() {
            return style;
        }
    }
    
    @Override
    public TextStyle clone() {
        TextStyle cloned = new TextStyle();
        cloned.fontFamily = this.fontFamily;
        cloned.fontSize = this.fontSize;
        cloned.bold = this.bold;
        cloned.italic = this.italic;
        cloned.underline = this.underline;
        cloned.textColor = this.textColor;
        cloned.backgroundColor = this.backgroundColor;
        cloned.alignment = this.alignment;
        cloned.lineSpacing = this.lineSpacing;
        return cloned;
    }
    
    @Override
    public void applyColorTheme(ColorTheme colorTheme) {
        // 只应用文本颜色，不改变文本框的背景色
        this.textColor = colorTheme.getTextColor();
        // 文本框背景保持透明或白色，不使用主题背景色
        // this.backgroundColor = colorTheme.getBackgroundColor(); // 注释掉这行
    }
    
    @JsonIgnore
    public Font getFont() {
        int style = Font.PLAIN;
        if (bold) style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;
        return new Font(fontFamily, style, fontSize);
    }
    
    // Getter和Setter方法
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }
    
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    
    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }
    
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    
    public Color getTextColor() { return textColor; }
    public void setTextColor(Color textColor) { this.textColor = textColor; }
    
    public Color getBackgroundColor() { return backgroundColor; }
    
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public int getAlignment() { return alignment; }
    public void setAlignment(int alignment) { this.alignment = alignment; }
    
    public double getLineSpacing() { return lineSpacing; }
    public void setLineSpacing(double lineSpacing) { this.lineSpacing = lineSpacing; }
    
    // 便捷的对齐方法
    public boolean isLeftAligned() { return alignment == ALIGN_LEFT; }
    public boolean isCenterAligned() { return alignment == ALIGN_CENTER; }
    public boolean isRightAligned() { return alignment == ALIGN_RIGHT; }
    public boolean isJustifyAligned() { return alignment == ALIGN_JUSTIFY; }
    
    public void setLeftAlign() { this.alignment = ALIGN_LEFT; }
    public void setCenterAlign() { this.alignment = ALIGN_CENTER; }
    public void setRightAlign() { this.alignment = ALIGN_RIGHT; }
    public void setJustifyAlign() { this.alignment = ALIGN_JUSTIFY; }
} 