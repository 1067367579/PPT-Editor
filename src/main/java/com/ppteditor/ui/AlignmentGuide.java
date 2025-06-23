package com.ppteditor.ui;

import com.ppteditor.core.model.SlideElement;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * 对齐辅助类
 * 提供智能对齐线显示和自动吸附功能，类似WPS/Office的对齐辅助
 */
public class AlignmentGuide {
    
    // 对齐容差（像素）
    private static final int SNAP_TOLERANCE = 8;
    private static final int GUIDE_TOLERANCE = 5;
    
    // 对齐线样式
    private static final Color GUIDE_COLOR = new Color(255, 0, 0, 150);
    private static final BasicStroke GUIDE_STROKE = new BasicStroke(1.0f, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 3}, 0);
    
    // 当前显示的对齐线
    private Set<AlignmentLine> activeGuideLines;
    private Rectangle canvasArea;
    
    public AlignmentGuide(Rectangle canvasArea) {
        this.canvasArea = canvasArea;
        this.activeGuideLines = new HashSet<>();
    }
    
    /**
     * 计算元素拖拽时的对齐位置和显示的对齐线
     */
    public AlignmentResult calculateAlignment(SlideElement<?> draggedElement, 
                                            double newX, double newY, 
                                            Collection<SlideElement<?>> otherElements) {
        
        Rectangle draggedBounds = new Rectangle((int)newX, (int)newY, 
                                              (int)draggedElement.getWidth(), 
                                              (int)draggedElement.getHeight());
        
        double adjustedX = newX;
        double adjustedY = newY;
        Set<AlignmentLine> guideLines = new HashSet<>();
        
        // 与画布边界对齐
        AlignmentResult canvasAlignment = alignToCanvas(draggedBounds);
        if (canvasAlignment.hasAlignment()) {
            adjustedX = canvasAlignment.getAdjustedX();
            adjustedY = canvasAlignment.getAdjustedY();
            guideLines.addAll(canvasAlignment.getGuideLines());
        }
        
        // 与其他元素对齐
        AlignmentResult elementAlignment = alignToElements(draggedBounds, otherElements);
        if (elementAlignment.hasAlignment()) {
            adjustedX = elementAlignment.getAdjustedX();
            adjustedY = elementAlignment.getAdjustedY();
            guideLines.addAll(elementAlignment.getGuideLines());
        }
        
        // 网格对齐（如果启用）
        // 这里可以添加网格对齐逻辑
        
        this.activeGuideLines = guideLines;
        return new AlignmentResult(adjustedX, adjustedY, guideLines);
    }
    
    /**
     * 与画布边界对齐
     */
    private AlignmentResult alignToCanvas(Rectangle bounds) {
        double adjustedX = bounds.x;
        double adjustedY = bounds.y;
        Set<AlignmentLine> guideLines = new HashSet<>();
        
        // 左边界对齐
        if (Math.abs(bounds.x - canvasArea.x) <= SNAP_TOLERANCE) {
            adjustedX = canvasArea.x;
            guideLines.add(new AlignmentLine(canvasArea.x, canvasArea.y, 
                                           canvasArea.x, canvasArea.y + canvasArea.height, 
                                           AlignmentType.VERTICAL));
        }
        
        // 右边界对齐
        if (Math.abs(bounds.x + bounds.width - (canvasArea.x + canvasArea.width)) <= SNAP_TOLERANCE) {
            adjustedX = canvasArea.x + canvasArea.width - bounds.width;
            guideLines.add(new AlignmentLine(canvasArea.x + canvasArea.width, canvasArea.y,
                                           canvasArea.x + canvasArea.width, canvasArea.y + canvasArea.height,
                                           AlignmentType.VERTICAL));
        }
        
        // 水平居中对齐
        double canvasCenterX = canvasArea.x + canvasArea.width / 2.0;
        double elementCenterX = bounds.x + bounds.width / 2.0;
        if (Math.abs(elementCenterX - canvasCenterX) <= SNAP_TOLERANCE) {
            adjustedX = canvasCenterX - bounds.width / 2.0;
            guideLines.add(new AlignmentLine(canvasCenterX, canvasArea.y,
                                           canvasCenterX, canvasArea.y + canvasArea.height,
                                           AlignmentType.VERTICAL));
        }
        
        // 顶边界对齐
        if (Math.abs(bounds.y - canvasArea.y) <= SNAP_TOLERANCE) {
            adjustedY = canvasArea.y;
            guideLines.add(new AlignmentLine(canvasArea.x, canvasArea.y,
                                           canvasArea.x + canvasArea.width, canvasArea.y,
                                           AlignmentType.HORIZONTAL));
        }
        
        // 底边界对齐
        if (Math.abs(bounds.y + bounds.height - (canvasArea.y + canvasArea.height)) <= SNAP_TOLERANCE) {
            adjustedY = canvasArea.y + canvasArea.height - bounds.height;
            guideLines.add(new AlignmentLine(canvasArea.x, canvasArea.y + canvasArea.height,
                                           canvasArea.x + canvasArea.width, canvasArea.y + canvasArea.height,
                                           AlignmentType.HORIZONTAL));
        }
        
        // 垂直居中对齐
        double canvasCenterY = canvasArea.y + canvasArea.height / 2.0;
        double elementCenterY = bounds.y + bounds.height / 2.0;
        if (Math.abs(elementCenterY - canvasCenterY) <= SNAP_TOLERANCE) {
            adjustedY = canvasCenterY - bounds.height / 2.0;
            guideLines.add(new AlignmentLine(canvasArea.x, canvasCenterY,
                                           canvasArea.x + canvasArea.width, canvasCenterY,
                                           AlignmentType.HORIZONTAL));
        }
        
        return new AlignmentResult(adjustedX, adjustedY, guideLines);
    }
    
    /**
     * 与其他元素对齐
     */
    private AlignmentResult alignToElements(Rectangle bounds, Collection<SlideElement<?>> otherElements) {
        double adjustedX = bounds.x;
        double adjustedY = bounds.y;
        Set<AlignmentLine> guideLines = new HashSet<>();
        
        for (SlideElement<?> element : otherElements) {
            Rectangle otherBounds = element.getBounds();
            
            // 垂直对齐检查
            // 左边缘对齐
            if (Math.abs(bounds.x - otherBounds.x) <= SNAP_TOLERANCE) {
                adjustedX = otherBounds.x;
                guideLines.add(createVerticalGuideLine(otherBounds.x, bounds, otherBounds));
            }
            
            // 右边缘对齐
            if (Math.abs(bounds.x + bounds.width - (otherBounds.x + otherBounds.width)) <= SNAP_TOLERANCE) {
                adjustedX = otherBounds.x + otherBounds.width - bounds.width;
                guideLines.add(createVerticalGuideLine(otherBounds.x + otherBounds.width, bounds, otherBounds));
            }
            
            // 水平中心对齐
            double otherCenterX = otherBounds.x + otherBounds.width / 2.0;
            double elementCenterX = bounds.x + bounds.width / 2.0;
            if (Math.abs(elementCenterX - otherCenterX) <= SNAP_TOLERANCE) {
                adjustedX = otherCenterX - bounds.width / 2.0;
                guideLines.add(createVerticalGuideLine(otherCenterX, bounds, otherBounds));
            }
            
            // 水平对齐检查
            // 顶边缘对齐
            if (Math.abs(bounds.y - otherBounds.y) <= SNAP_TOLERANCE) {
                adjustedY = otherBounds.y;
                guideLines.add(createHorizontalGuideLine(otherBounds.y, bounds, otherBounds));
            }
            
            // 底边缘对齐
            if (Math.abs(bounds.y + bounds.height - (otherBounds.y + otherBounds.height)) <= SNAP_TOLERANCE) {
                adjustedY = otherBounds.y + otherBounds.height - bounds.height;
                guideLines.add(createHorizontalGuideLine(otherBounds.y + otherBounds.height, bounds, otherBounds));
            }
            
            // 垂直中心对齐
            double otherCenterY = otherBounds.y + otherBounds.height / 2.0;
            double elementCenterY = bounds.y + bounds.height / 2.0;
            if (Math.abs(elementCenterY - otherCenterY) <= SNAP_TOLERANCE) {
                adjustedY = otherCenterY - bounds.height / 2.0;
                guideLines.add(createHorizontalGuideLine(otherCenterY, bounds, otherBounds));
            }
        }
        
        return new AlignmentResult(adjustedX, adjustedY, guideLines);
    }
    
    private AlignmentLine createVerticalGuideLine(double x, Rectangle bounds1, Rectangle bounds2) {
        double minY = Math.min(Math.min(bounds1.y, bounds2.y), canvasArea.y);
        double maxY = Math.max(Math.max(bounds1.y + bounds1.height, bounds2.y + bounds2.height), 
                              canvasArea.y + canvasArea.height);
        return new AlignmentLine(x, minY, x, maxY, AlignmentType.VERTICAL);
    }
    
    private AlignmentLine createHorizontalGuideLine(double y, Rectangle bounds1, Rectangle bounds2) {
        double minX = Math.min(Math.min(bounds1.x, bounds2.x), canvasArea.x);
        double maxX = Math.max(Math.max(bounds1.x + bounds1.width, bounds2.x + bounds2.width), 
                              canvasArea.x + canvasArea.width);
        return new AlignmentLine(minX, y, maxX, y, AlignmentType.HORIZONTAL);
    }
    
    /**
     * 绘制对齐线
     */
    public void renderGuideLines(Graphics2D g2d) {
        if (activeGuideLines.isEmpty()) return;
        
        g2d.setColor(GUIDE_COLOR);
        g2d.setStroke(GUIDE_STROKE);
        
        for (AlignmentLine line : activeGuideLines) {
            g2d.draw(new Line2D.Double(line.x1, line.y1, line.x2, line.y2));
        }
    }
    
    /**
     * 清除对齐线
     */
    public void clearGuideLines() {
        activeGuideLines.clear();
    }
    
    /**
     * 对齐线数据类
     */
    public static class AlignmentLine {
        public final double x1, y1, x2, y2;
        public final AlignmentType type;
        
        public AlignmentLine(double x1, double y1, double x2, double y2, AlignmentType type) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.type = type;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof AlignmentLine)) return false;
            AlignmentLine other = (AlignmentLine) obj;
            return Double.compare(x1, other.x1) == 0 &&
                   Double.compare(y1, other.y1) == 0 &&
                   Double.compare(x2, other.x2) == 0 &&
                   Double.compare(y2, other.y2) == 0 &&
                   type == other.type;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x1, y1, x2, y2, type);
        }
    }
    
    /**
     * 对齐结果类
     */
    public static class AlignmentResult {
        private final double adjustedX, adjustedY;
        private final Set<AlignmentLine> guideLines;
        
        public AlignmentResult(double adjustedX, double adjustedY, Set<AlignmentLine> guideLines) {
            this.adjustedX = adjustedX;
            this.adjustedY = adjustedY;
            this.guideLines = new HashSet<>(guideLines);
        }
        
        public double getAdjustedX() { return adjustedX; }
        public double getAdjustedY() { return adjustedY; }
        public Set<AlignmentLine> getGuideLines() { return new HashSet<>(guideLines); }
        public boolean hasAlignment() { return !guideLines.isEmpty(); }
    }
    
    /**
     * 对齐类型枚举
     */
    public enum AlignmentType {
        HORIZONTAL, VERTICAL
    }
} 