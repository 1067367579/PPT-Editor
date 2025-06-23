package com.ppteditor.ui;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 选择控制点类
 * 用于元素的缩放和旋转操作
 */
public class SelectionHandle {
    
    public enum HandleType {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
        ROTATION
    }
    
    private static final int HANDLE_SIZE = 8;
    private static final int ROTATION_OFFSET = 20;
    
    private HandleType type;
    private Rectangle2D bounds;
    private Cursor cursor;
    
    public SelectionHandle(HandleType type, Rectangle elementBounds) {
        this.type = type;
        this.bounds = calculateHandleBounds(type, elementBounds);
        this.cursor = getCursorForHandle(type);
    }
    
    private Rectangle2D calculateHandleBounds(HandleType type, Rectangle elementBounds) {
        double x = elementBounds.x;
        double y = elementBounds.y;
        double w = elementBounds.width;
        double h = elementBounds.height;
        double half = HANDLE_SIZE / 2.0;
        
        switch (type) {
            case TOP_LEFT:
                return new Rectangle2D.Double(x - half, y - half, HANDLE_SIZE, HANDLE_SIZE);
            case TOP_CENTER:
                return new Rectangle2D.Double(x + w/2 - half, y - half, HANDLE_SIZE, HANDLE_SIZE);
            case TOP_RIGHT:
                return new Rectangle2D.Double(x + w - half, y - half, HANDLE_SIZE, HANDLE_SIZE);
            case MIDDLE_LEFT:
                return new Rectangle2D.Double(x - half, y + h/2 - half, HANDLE_SIZE, HANDLE_SIZE);
            case MIDDLE_RIGHT:
                return new Rectangle2D.Double(x + w - half, y + h/2 - half, HANDLE_SIZE, HANDLE_SIZE);
            case BOTTOM_LEFT:
                return new Rectangle2D.Double(x - half, y + h - half, HANDLE_SIZE, HANDLE_SIZE);
            case BOTTOM_CENTER:
                return new Rectangle2D.Double(x + w/2 - half, y + h - half, HANDLE_SIZE, HANDLE_SIZE);
            case BOTTOM_RIGHT:
                return new Rectangle2D.Double(x + w - half, y + h - half, HANDLE_SIZE, HANDLE_SIZE);
            case ROTATION:
                return new Rectangle2D.Double(x + w/2 - half, y - ROTATION_OFFSET - half, HANDLE_SIZE, HANDLE_SIZE);
            default:
                return new Rectangle2D.Double(0, 0, HANDLE_SIZE, HANDLE_SIZE);
        }
    }
    
    private Cursor getCursorForHandle(HandleType type) {
        switch (type) {
            case TOP_LEFT:
            case BOTTOM_RIGHT:
                return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            case TOP_RIGHT:
            case BOTTOM_LEFT:
                return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            case TOP_CENTER:
            case BOTTOM_CENTER:
                return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case MIDDLE_LEFT:
            case MIDDLE_RIGHT:
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case ROTATION:
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            default:
                return Cursor.getDefaultCursor();
        }
    }
    
    public void render(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fill(bounds);
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(bounds);
        
        // 旋转控制点特殊显示
        if (type == HandleType.ROTATION) {
            g2d.setColor(Color.GREEN);
            g2d.fill(bounds);
            g2d.setColor(new Color(0, 100, 0));
            g2d.draw(bounds);
        }
    }
    
    public boolean contains(Point point) {
        return bounds.contains(point);
    }
    
    public HandleType getType() {
        return type;
    }
    
    public Cursor getCursor() {
        return cursor;
    }
    
    public Rectangle2D getBounds() {
        return bounds;
    }
    
    /**
     * 计算缩放操作
     */
    public static Rectangle calculateResizedBounds(Rectangle originalBounds, HandleType handleType, 
                                                  Point startPoint, Point currentPoint) {
        int dx = currentPoint.x - startPoint.x;
        int dy = currentPoint.y - startPoint.y;
        
        int newX = originalBounds.x;
        int newY = originalBounds.y;
        int newWidth = originalBounds.width;
        int newHeight = originalBounds.height;
        
        switch (handleType) {
            case TOP_LEFT:
                newX += dx;
                newY += dy;
                newWidth -= dx;
                newHeight -= dy;
                break;
            case TOP_CENTER:
                newY += dy;
                newHeight -= dy;
                break;
            case TOP_RIGHT:
                newY += dy;
                newWidth += dx;
                newHeight -= dy;
                break;
            case MIDDLE_LEFT:
                newX += dx;
                newWidth -= dx;
                break;
            case MIDDLE_RIGHT:
                newWidth += dx;
                break;
            case BOTTOM_LEFT:
                newX += dx;
                newWidth -= dx;
                newHeight += dy;
                break;
            case BOTTOM_CENTER:
                newHeight += dy;
                break;
            case BOTTOM_RIGHT:
                newWidth += dx;
                newHeight += dy;
                break;
        }
        
        // 确保最小尺寸
        newWidth = Math.max(10, newWidth);
        newHeight = Math.max(10, newHeight);
        
        return new Rectangle(newX, newY, newWidth, newHeight);
    }
    
    /**
     * 计算旋转角度
     */
    public static double calculateRotationAngle(Rectangle elementBounds, Point startPoint, Point currentPoint) {
        Point center = new Point(
            elementBounds.x + elementBounds.width / 2,
            elementBounds.y + elementBounds.height / 2
        );
        
        double startAngle = Math.atan2(startPoint.y - center.y, startPoint.x - center.x);
        double currentAngle = Math.atan2(currentPoint.y - center.y, currentPoint.x - center.x);
        
        return currentAngle - startAngle;
    }
} 