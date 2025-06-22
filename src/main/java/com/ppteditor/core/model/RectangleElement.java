package com.ppteditor.core.model;

import com.ppteditor.core.enums.ElementType;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 矩形元素类
 * 继承自SlideElement并使用ShapeStyle作为样式类型
 */
public class RectangleElement extends SlideElement<ShapeStyle> {
    
    public RectangleElement() {
        super(ElementType.RECTANGLE);
        this.style = createDefaultStyle();
        this.width = 100;
        this.height = 60;
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
    
    @Override
    public RectangleElement clone() {
        RectangleElement cloned = (RectangleElement) super.clone();
        return cloned;
    }
} 