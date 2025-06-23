package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import java.awt.Color;

/**
 * 文本片段类
 * 用于支持部分文字的超链接和样式
 */
public class TextSegment implements java.io.Serializable {
    
    @Serializable(required = true)
    private String text; // 片段文本
    
    @Serializable
    private String hyperlink; // 超链接URL
    
    @Serializable
    private boolean isHyperlink; // 是否为超链接
    
    @Serializable
    private Color textColor; // 文本颜色（可覆盖默认颜色）
    
    @Serializable
    private boolean bold; // 是否粗体
    
    @Serializable
    private boolean italic; // 是否斜体
    
    @Serializable
    private boolean underline; // 是否下划线
    
    public TextSegment() {
        this.text = "";
        this.isHyperlink = false;
        this.bold = false;
        this.italic = false;
        this.underline = false;
    }
    
    public TextSegment(String text) {
        this();
        this.text = text;
    }
    
    public TextSegment(String text, String hyperlink) {
        this(text);
        this.hyperlink = hyperlink;
        this.isHyperlink = hyperlink != null && !hyperlink.trim().isEmpty();
        if (this.isHyperlink) {
            this.textColor = new Color(0, 102, 204); // 蓝色
            this.underline = true;
        }
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getHyperlink() { return hyperlink; }
    public void setHyperlink(String hyperlink) { 
        this.hyperlink = hyperlink;
        this.isHyperlink = hyperlink != null && !hyperlink.trim().isEmpty();
        if (this.isHyperlink) {
            this.textColor = new Color(0, 102, 204); // 蓝色
            this.underline = true;
        }
    }
    
    public boolean isHyperlink() { return isHyperlink; }
    public void setHyperlink(boolean isHyperlink) { this.isHyperlink = isHyperlink; }
    
    public Color getTextColor() { return textColor; }
    public void setTextColor(Color textColor) { this.textColor = textColor; }
    
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    
    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }
    
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    
    /**
     * 克隆文本片段
     */
    public TextSegment clone() {
        TextSegment cloned = new TextSegment();
        cloned.text = this.text;
        cloned.hyperlink = this.hyperlink;
        cloned.isHyperlink = this.isHyperlink;
        cloned.textColor = this.textColor;
        cloned.bold = this.bold;
        cloned.italic = this.italic;
        cloned.underline = this.underline;
        return cloned;
    }
    
    @Override
    public String toString() {
        return text;
    }
} 