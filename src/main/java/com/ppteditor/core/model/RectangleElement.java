package com.ppteditor.core.model;

import com.ppteditor.core.enums.ElementType;
import com.ppteditor.core.annotations.Serializable;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 矩形元素类
 * 继承自SlideElement并使用ShapeStyle作为样式类型
 * 支持在矩形内显示文本
 */
public class RectangleElement extends SlideElement<ShapeStyle> implements java.io.Serializable {
    @Serializable
    private String text; // 矩形内的文本
    @Serializable
    private TextStyle textStyle; // 文本样式
    public RectangleElement() {
        super(ElementType.RECTANGLE);
        this.style = createDefaultStyle();
        this.width = 100;
        this.height = 60;
        this.text = "";
        this.textStyle = createDefaultTextStyle();
    }
    public RectangleElement(double x, double y, double width, double height) {
        this();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    @Override
    public void render(Graphics2D g2d) {
        if (style == null) {
            style = createDefaultStyle();
        }
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        // 绘制填充
        Color fillColor = style.getEffectiveFillColor();
        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fill(rect);
        }
        // 绘制边框
        Color borderColor = style.getEffectiveBorderColor();
        if (borderColor != null) {
            g2d.setColor(borderColor);
            g2d.setStroke(style.getBorderStroke());
            g2d.draw(rect);
        }
        // 绘制文本
        if (text != null && !text.trim().isEmpty() && textStyle != null) {
            drawText(g2d);
        }
    }
    
    @Override
    public ShapeStyle createDefaultStyle() {
        return new ShapeStyle();
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
    
    // 静态工厂方法 - 创建特定样式的矩形
    public static RectangleElement createCard(double x, double y, double width, double height) {
        RectangleElement element = new RectangleElement(x, y, width, height);
        element.style.setFillColor(Color.WHITE);
        element.style.setBorderColor(Color.LIGHT_GRAY);
        element.style.setBorderWidth(1.0f);
        return element;
    }
    
    public static RectangleElement createButton(double x, double y, double width, double height) {
        RectangleElement element = new RectangleElement(x, y, width, height);
        element.style.setFillColor(new Color(0, 123, 255));
        element.style.setBorderColor(new Color(0, 86, 179));
        element.style.setBorderWidth(2.0f);
        return element;
    }
    
    /**
     * 绘制文本
     */
    private void drawText(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(textStyle.getFont());
        g2d.setColor(textStyle.getTextColor());
        
        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = text.split("\n");
        
        // 计算文本位置（居中）
        double totalHeight = lines.length * fm.getHeight() * textStyle.getLineSpacing();
        double startY = y + (height - totalHeight) / 2 + fm.getAscent();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineWidth = fm.stringWidth(line);
            double lineX;
            
            // 根据对齐方式计算X位置
            switch (textStyle.getAlignment()) {
                case TextStyle.ALIGN_CENTER:
                    lineX = x + (width - lineWidth) / 2;
                    break;
                case TextStyle.ALIGN_RIGHT:
                    lineX = x + width - lineWidth - 5;
                    break;
                default: // 左对齐
                    lineX = x + 5;
                    break;
            }
            
            double lineY = startY + i * fm.getHeight() * textStyle.getLineSpacing();
            g2d.drawString(line, (int)lineX, (int)lineY);
        }
    }
    
    /**
     * 创建默认文本样式
     */
    private TextStyle createDefaultTextStyle() {
        return new TextStyle.Builder()
                .fontSize(12)
                .textColor(Color.BLACK)
                .alignment(TextStyle.ALIGN_CENTER)
                .build();
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public TextStyle getTextStyle() { return textStyle; }
    public void setTextStyle(TextStyle textStyle) { this.textStyle = textStyle; }
    
    @Override
    public RectangleElement clone() {
        RectangleElement cloned = (RectangleElement) super.clone();
        cloned.text = this.text;
        if (this.textStyle != null) {
            cloned.textStyle = this.textStyle.clone();
        }
        return cloned;
    }
} 