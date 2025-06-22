package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.ppteditor.core.enums.ElementType;
import java.awt.*;
import java.util.UUID;

/**
 * 抽象幻灯片元素基类
 * 使用泛型支持不同类型的样式配置
 * 
 * @param <T> 样式配置类型
 */
public abstract class SlideElement<T extends ElementStyle> implements Cloneable {
    
    @Serializable(required = true)
    protected String id;
    
    @Serializable(required = true) 
    protected ElementType type;
    
    @Serializable
    protected double x, y, width, height;
    
    @Serializable
    protected double rotation; // 旋转角度
    
    @Serializable
    protected boolean visible;
    
    @Serializable
    protected int zIndex; // 层级
    
    @Serializable
    protected T style;
    
    @Serializable
    protected String hyperlink; // 超链接
    
    protected boolean selected;
    protected boolean locked;
    
    public SlideElement(ElementType type) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.visible = true;
        this.selected = false;
        this.locked = false;
        this.rotation = 0;
        this.zIndex = 0;
    }
    
    // 抽象方法 - 子类必须实现
    public abstract void render(Graphics2D g2d);
    public abstract T createDefaultStyle();
    public abstract Rectangle getBounds();
    public abstract boolean contains(Point point);
    
    // 模板方法模式 - 渲染流程
    public final void draw(Graphics2D g2d) {
        if (!visible) return;
        
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        
        // 应用变换
        applyTransform(g2dCopy);
        
        // 渲染元素
        render(g2dCopy);
        
        // 绘制选中状态
        if (selected) {
            drawSelectionIndicator(g2dCopy);
        }
        
        g2dCopy.dispose();
    }
    
    protected void applyTransform(Graphics2D g2d) {
        if (rotation != 0) {
            double centerX = x + width / 2;
            double centerY = y + height / 2;
            g2d.rotate(Math.toRadians(rotation), centerX, centerY);
        }
    }
    
    protected void drawSelectionIndicator(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
        g2d.drawRect((int)x - 2, (int)y - 2, (int)width + 4, (int)height + 4);
        
        // 绘制控制点
        drawControlPoints(g2d);
    }
    
    protected void drawControlPoints(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        g2d.fillRect((int)x - 4, (int)y - 4, 8, 8);
        g2d.fillRect((int)(x + width) - 4, (int)y - 4, 8, 8);
        g2d.fillRect((int)x - 4, (int)(y + height) - 4, 8, 8);
        g2d.fillRect((int)(x + width) - 4, (int)(y + height) - 4, 8, 8);
    }
    
    // 克隆方法
    @Override
    public SlideElement<T> clone() {
        try {
            @SuppressWarnings("unchecked")
            SlideElement<T> cloned = (SlideElement<T>) super.clone();
            cloned.id = UUID.randomUUID().toString();
            cloned.selected = false;
            if (style != null) {
                cloned.style = (T) style.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆失败", e);
        }
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    public ElementType getType() { return type; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    
    public int getZIndex() { return zIndex; }
    public void setZIndex(int zIndex) { this.zIndex = zIndex; }
    
    public T getStyle() { return style; }
    public void setStyle(T style) { this.style = style; }
    
    public String getHyperlink() { return hyperlink; }
    public void setHyperlink(String hyperlink) { this.hyperlink = hyperlink; }
    
    // 位置和大小操作方法 - 方法重载
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setPosition(Point point) {
        this.x = point.x;
        this.y = point.y;
    }
    
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    public void setSize(Dimension size) {
        this.width = size.width;
        this.height = size.height;
    }
    
    public void setBounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void move(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }
    
    public void resize(double deltaWidth, double deltaHeight) {
        this.width += deltaWidth;
        this.height += deltaHeight;
        if (this.width < 1) this.width = 1;
        if (this.height < 1) this.height = 1;
    }
} 