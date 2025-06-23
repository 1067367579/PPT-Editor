package com.ppteditor.core.model;

import com.ppteditor.core.enums.ElementType;
import com.ppteditor.core.annotations.Serializable;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * 椭圆元素类
 * 继承自SlideElement并使用ShapeStyle作为样式类型
 * 支持在椭圆内显示文本
 */
public class EllipseElement extends SlideElement<ShapeStyle> implements java.io.Serializable {
    
    @Serializable
    private String text; // 椭圆内的文本
    
    @Serializable
    private TextStyle textStyle; // 文本样式
    
    public EllipseElement() {
        super(ElementType.ELLIPSE);
        this.style = createDefaultStyle();
        this.width = 100;
        this.height = 60;
        this.text = "";
        this.textStyle = createDefaultTextStyle();
    }
    
    public EllipseElement(double x, double y, double width, double height) {
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
        
        Ellipse2D ellipse = new Ellipse2D.Double(x, y, width, height);
        
        // 绘制填充
        Color fillColor = style.getEffectiveFillColor();
        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fill(ellipse);
        }
        
        // 绘制边框
        Color borderColor = style.getEffectiveBorderColor();
        if (borderColor != null) {
            g2d.setColor(borderColor);
            g2d.setStroke(style.getBorderStroke());
            g2d.draw(ellipse);
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
        Ellipse2D ellipse = new Ellipse2D.Double(x, y, width, height);
        return ellipse.contains(point);
    }
    
    // 静态工厂方法 - 创建特定样式的椭圆
    public static EllipseElement createCircle(double x, double y, double radius) {
        EllipseElement element = new EllipseElement(x, y, radius * 2, radius * 2);
        return element;
    }
    
    public static EllipseElement createOval(double x, double y, double width, double height) {
        EllipseElement element = new EllipseElement(x, y, width, height);
        element.style.setFillColor(new Color(255, 200, 200));
        element.style.setBorderColor(new Color(200, 100, 100));
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
    public EllipseElement clone() {
        EllipseElement cloned = (EllipseElement) super.clone();
        cloned.text = this.text;
        if (this.textStyle != null) {
            cloned.textStyle = this.textStyle.clone();
        }
        return cloned;
    }
} 