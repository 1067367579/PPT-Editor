package com.ppteditor.core.model;

import com.ppteditor.core.enums.ElementType;
import java.awt.*;
import java.awt.geom.*;

/**
 * 图标元素类
 * 支持各种预定义的图标形状
 */
public class IconElement extends SlideElement<ShapeStyle> {
    
    public enum IconType {
        ARROW_RIGHT("右箭头"),
        ARROW_LEFT("左箭头"),
        ARROW_UP("上箭头"), 
        ARROW_DOWN("下箭头"),
        STAR("星形"),
        HEART("心形"),
        TRIANGLE("三角形"),
        DIAMOND("菱形"),
        PENTAGON("五边形"),
        HEXAGON("六边形"),
        CHECK("对勾"),
        CROSS("叉号"),
        PLUS("加号"),
        MINUS("减号"),
        CIRCLE_DOT("圆点"),
        SQUARE_DOT("方点");
        
        private final String displayName;
        
        IconType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private IconType iconType;
    
    // 无参构造函数
    public IconElement() {
        super(ElementType.ICON);
        this.iconType = IconType.STAR;
        this.setBounds(0, 0, 50, 50);
        this.style = createDefaultStyle();
    }
    
    public IconElement(IconType iconType, double x, double y, double width, double height) {
        super(ElementType.ICON);
        this.iconType = iconType;
        this.setBounds(x, y, width, height);
        this.style = createDefaultStyle();
    }
    
    public static IconElement createStar(int x, int y, int size) {
        return new IconElement(IconType.STAR, x, y, size, size);
    }
    
    public static IconElement createArrow(int x, int y, int width, int height) {
        return new IconElement(IconType.ARROW_RIGHT, x, y, width, height);
    }
    
    public static IconElement createHeart(int x, int y, int size) {
        return new IconElement(IconType.HEART, x, y, size, size);
    }
    
    @Override
    public ElementType getType() {
        return ElementType.ICON;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Shape iconShape = createIconShape();
        
        // 填充
        if (style != null && style.getEffectiveFillColor() != null) {
            g2d.setColor(style.getEffectiveFillColor());
            g2d.fill(iconShape);
        }
        
        // 边框
        if (style != null && style.getEffectiveBorderColor() != null) {
            g2d.setColor(style.getEffectiveBorderColor());
            g2d.setStroke(style.getBorderStroke());
            g2d.draw(iconShape);
        }
    }
    
    @Override
    public ShapeStyle createDefaultStyle() {
        ShapeStyle defaultStyle = new ShapeStyle();
        defaultStyle.setFillColor(Color.BLUE);
        defaultStyle.setBorderColor(Color.BLACK);
        defaultStyle.setBorderWidth(1.0f);
        return defaultStyle;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, (int)width, (int)height);
    }
    
    @Override
    public boolean contains(Point point) {
        return getBounds().contains(point);
    }
    
    private Shape createIconShape() {
        Rectangle bounds = getBounds();
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;
        
        switch (iconType) {
            case ARROW_RIGHT:
                return createArrowRight(x, y, w, h);
            case ARROW_LEFT:
                return createArrowLeft(x, y, w, h);
            case ARROW_UP:
                return createArrowUp(x, y, w, h);
            case ARROW_DOWN:
                return createArrowDown(x, y, w, h);
            case STAR:
                return createStar(x, y, w, h);
            case HEART:
                return createHeart(x, y, w, h);
            case TRIANGLE:
                return createTriangle(x, y, w, h);
            case DIAMOND:
                return createDiamond(x, y, w, h);
            case PENTAGON:
                return createPentagon(x, y, w, h);
            case HEXAGON:
                return createHexagon(x, y, w, h);
            case CHECK:
                return createCheck(x, y, w, h);
            case CROSS:
                return createCross(x, y, w, h);
            case PLUS:
                return createPlus(x, y, w, h);
            case MINUS:
                return createMinus(x, y, w, h);
            case CIRCLE_DOT:
                return new Ellipse2D.Double(x, y, w, h);
            case SQUARE_DOT:
                return new Rectangle2D.Double(x, y, w, h);
            default:
                return new Rectangle2D.Double(x, y, w, h);
        }
    }
    
    private Shape createArrowRight(int x, int y, int w, int h) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y + h/4);
        path.lineTo(x + w*3/4, y + h/4);
        path.lineTo(x + w*3/4, y);
        path.lineTo(x + w, y + h/2);
        path.lineTo(x + w*3/4, y + h);
        path.lineTo(x + w*3/4, y + h*3/4);
        path.lineTo(x, y + h*3/4);
        path.closePath();
        return path;
    }
    
    private Shape createArrowLeft(int x, int y, int w, int h) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + w, y + h/4);
        path.lineTo(x + w/4, y + h/4);
        path.lineTo(x + w/4, y);
        path.lineTo(x, y + h/2);
        path.lineTo(x + w/4, y + h);
        path.lineTo(x + w/4, y + h*3/4);
        path.lineTo(x + w, y + h*3/4);
        path.closePath();
        return path;
    }
    
    private Shape createArrowUp(int x, int y, int w, int h) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + w/4, y + h);
        path.lineTo(x + w/4, y + h/4);
        path.lineTo(x, y + h/4);
        path.lineTo(x + w/2, y);
        path.lineTo(x + w, y + h/4);
        path.lineTo(x + w*3/4, y + h/4);
        path.lineTo(x + w*3/4, y + h);
        path.closePath();
        return path;
    }
    
    private Shape createArrowDown(int x, int y, int w, int h) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + w/4, y);
        path.lineTo(x + w/4, y + h*3/4);
        path.lineTo(x, y + h*3/4);
        path.lineTo(x + w/2, y + h);
        path.lineTo(x + w, y + h*3/4);
        path.lineTo(x + w*3/4, y + h*3/4);
        path.lineTo(x + w*3/4, y);
        path.closePath();
        return path;
    }
    
    private Shape createStar(int x, int y, int w, int h) {
        GeneralPath star = new GeneralPath();
        double centerX = x + w/2.0;
        double centerY = y + h/2.0;
        double outerRadius = Math.min(w, h) / 2.0 * 0.9;
        double innerRadius = outerRadius * 0.4;
        
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5.0 - Math.PI/2;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double px = centerX + radius * Math.cos(angle);
            double py = centerY + radius * Math.sin(angle);
            
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        return star;
    }
    
    private Shape createHeart(int x, int y, int w, int h) {
        GeneralPath heart = new GeneralPath();
        double centerX = x + w/2.0;
        double topY = y + h*0.2;
        double bottomY = y + h*0.9;
        
        // 左半心
        heart.moveTo(centerX, bottomY);
        heart.curveTo(x + w*0.1, y + h*0.6, x + w*0.1, topY, x + w*0.3, topY);
        heart.curveTo(x + w*0.45, topY, centerX, y + h*0.4, centerX, y + h*0.4);
        
        // 右半心
        heart.curveTo(centerX, y + h*0.4, x + w*0.55, topY, x + w*0.7, topY);
        heart.curveTo(x + w*0.9, topY, x + w*0.9, y + h*0.6, centerX, bottomY);
        
        heart.closePath();
        return heart;
    }
    
    private Shape createTriangle(int x, int y, int w, int h) {
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(x + w/2, y);
        triangle.lineTo(x + w, y + h);
        triangle.lineTo(x, y + h);
        triangle.closePath();
        return triangle;
    }
    
    private Shape createDiamond(int x, int y, int w, int h) {
        GeneralPath diamond = new GeneralPath();
        diamond.moveTo(x + w/2, y);
        diamond.lineTo(x + w, y + h/2);
        diamond.lineTo(x + w/2, y + h);
        diamond.lineTo(x, y + h/2);
        diamond.closePath();
        return diamond;
    }
    
    private Shape createPentagon(int x, int y, int w, int h) {
        GeneralPath pentagon = new GeneralPath();
        double centerX = x + w/2.0;
        double centerY = y + h/2.0;
        double radius = Math.min(w, h) / 2.0 * 0.9;
        
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI * 2 * i / 5.0 - Math.PI/2;
            double px = centerX + radius * Math.cos(angle);
            double py = centerY + radius * Math.sin(angle);
            
            if (i == 0) {
                pentagon.moveTo(px, py);
            } else {
                pentagon.lineTo(px, py);
            }
        }
        pentagon.closePath();
        return pentagon;
    }
    
    private Shape createHexagon(int x, int y, int w, int h) {
        GeneralPath hexagon = new GeneralPath();
        double centerX = x + w/2.0;
        double centerY = y + h/2.0;
        double radius = Math.min(w, h) / 2.0 * 0.9;
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * i / 3.0;
            double px = centerX + radius * Math.cos(angle);
            double py = centerY + radius * Math.sin(angle);
            
            if (i == 0) {
                hexagon.moveTo(px, py);
            } else {
                hexagon.lineTo(px, py);
            }
        }
        hexagon.closePath();
        return hexagon;
    }
    
    private Shape createCheck(int x, int y, int w, int h) {
        GeneralPath check = new GeneralPath();
        check.moveTo(x + w*0.2, y + h*0.5);
        check.lineTo(x + w*0.4, y + h*0.7);
        check.lineTo(x + w*0.8, y + h*0.3);
        return check;
    }
    
    private Shape createCross(int x, int y, int w, int h) {
        GeneralPath cross = new GeneralPath();
        cross.moveTo(x + w*0.2, y + h*0.2);
        cross.lineTo(x + w*0.8, y + h*0.8);
        cross.moveTo(x + w*0.8, y + h*0.2);
        cross.lineTo(x + w*0.2, y + h*0.8);
        return cross;
    }
    
    private Shape createPlus(int x, int y, int w, int h) {
        GeneralPath plus = new GeneralPath();
        plus.moveTo(x + w/2, y + h*0.2);
        plus.lineTo(x + w/2, y + h*0.8);
        plus.moveTo(x + w*0.2, y + h/2);
        plus.lineTo(x + w*0.8, y + h/2);
        return plus;
    }
    
    private Shape createMinus(int x, int y, int w, int h) {
        GeneralPath minus = new GeneralPath();
        minus.moveTo(x + w*0.2, y + h/2);
        minus.lineTo(x + w*0.8, y + h/2);
        return minus;
    }
    
    // Getters and Setters
    public IconType getIconType() {
        return iconType;
    }
    
    public void setIconType(IconType iconType) {
        this.iconType = iconType;
    }
    
    public Color getFillColor() {
        return style != null ? style.getFillColor() : null;
    }
    
    public void setFillColor(Color fillColor) {
        if (style != null) {
            style.setFillColor(fillColor);
        }
    }
    
    public Color getBorderColor() {
        return style != null ? style.getBorderColor() : null;
    }
    
    public void setBorderColor(Color borderColor) {
        if (style != null) {
            style.setBorderColor(borderColor);
        }
    }
    
    public float getBorderWidth() {
        return style != null ? style.getBorderWidth() : 0;
    }
    
    public void setBorderWidth(float borderWidth) {
        if (style != null) {
            style.setBorderWidth(borderWidth);
        }
    }
    
    @Override
    public IconElement clone() {
        IconElement copy = new IconElement(iconType, getX(), getY(), getWidth(), getHeight());
        copy.setRotation(getRotation());
        copy.setVisible(isVisible());
        copy.setZIndex(getZIndex());
        copy.setHyperlink(getHyperlink());
        if (style != null) {
            copy.setStyle(style.clone());
        }
        return copy;
    }
} 