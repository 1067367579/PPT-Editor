package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.ppteditor.core.enums.ElementType;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 文本元素类
 * 继承自SlideElement并使用TextStyle作为样式类型
 */
public class TextElement extends SlideElement<TextStyle> {
    
    @Serializable(required = true)
    private String text;
    
    @Serializable
    private boolean autoSize; // 自动调整大小
    
    public TextElement() {
        super(ElementType.TEXT);
        this.text = "文本";
        this.autoSize = false;
        this.style = createDefaultStyle();
        this.width = 100;
        this.height = 30;
    }
    
    public TextElement(String text) {
        this();
        this.text = text;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (style == null) {
            style = createDefaultStyle();
        }
        
        // 设置渲染提示以获得更好的文本质量
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制背景
        if (style.getBackgroundColor() != null) {
            g2d.setColor(style.getBackgroundColor());
            g2d.fillRect((int)x, (int)y, (int)width, (int)height);
        }
        
        // 绘制文本
        g2d.setFont(style.getFont());
        g2d.setColor(style.getTextColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        
        // 如果启用自动调整大小
        if (autoSize) {
            Rectangle2D textBounds = fm.getStringBounds(text, g2d);
            this.width = textBounds.getWidth() + 10; // 添加一些边距
            this.height = textBounds.getHeight() + 5;
        }
        
        // 计算文本位置
        int textX = calculateTextX(fm);
        int textY = calculateTextY(fm);
        
        // 绘制文本
        drawText(g2d, textX, textY);
        
        // 绘制下划线
        if (style.isUnderline()) {
            drawUnderline(g2d, textX, textY, fm);
        }
    }
    
    private int calculateTextX(FontMetrics fm) {
        switch (style.getAlignment()) {
            case 1: // 居中
                return (int)(x + (width - fm.stringWidth(text)) / 2);
            case 2: // 右对齐
                return (int)(x + width - fm.stringWidth(text) - 5);
            default: // 左对齐
                return (int)x + 5;
        }
    }
    
    private int calculateTextY(FontMetrics fm) {
        return (int)(y + (height + fm.getAscent() - fm.getDescent()) / 2);
    }
    
    private void drawText(Graphics2D g2d, int textX, int textY) {
        // 处理多行文本
        String[] lines = text.split("\n");
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = (int)(fm.getHeight() * style.getLineSpacing());
        
        for (int i = 0; i < lines.length; i++) {
            int currentY = textY + i * lineHeight;
            if (currentY <= y + height) { // 确保文本在边界内
                g2d.drawString(lines[i], textX, currentY);
            }
        }
    }
    
    private void drawUnderline(Graphics2D g2d, int textX, int textY, FontMetrics fm) {
        String[] lines = text.split("\n");
        int lineHeight = (int)(fm.getHeight() * style.getLineSpacing());
        
        for (int i = 0; i < lines.length; i++) {
            int currentY = textY + i * lineHeight;
            if (currentY <= y + height) {
                int lineWidth = fm.stringWidth(lines[i]);
                g2d.drawLine(textX, currentY + 2, textX + lineWidth, currentY + 2);
            }
        }
    }
    
    @Override
    public TextStyle createDefaultStyle() {
        return new TextStyle();
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, (int)width, (int)height);
    }
    
    @Override
    public boolean contains(Point point) {
        return point.x >= x && point.x <= x + width && 
               point.y >= y && point.y <= y + height;
    }
    
    // 静态工厂方法
    public static TextElement createTitle(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(24)
                .bold(true)
                .alignment(1) // 居中
                .build();
        return element;
    }
    
    public static TextElement createSubtitle(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(18)
                .italic(true)
                .alignment(1) // 居中
                .build();
        return element;
    }
    
    public static TextElement createBodyText(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(14)
                .alignment(0) // 左对齐
                .build();
        return element;
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public boolean isAutoSize() { return autoSize; }
    public void setAutoSize(boolean autoSize) { this.autoSize = autoSize; }
    
    @Override
    public TextElement clone() {
        TextElement cloned = (TextElement) super.clone();
        cloned.text = this.text;
        cloned.autoSize = this.autoSize;
        return cloned;
    }
} 