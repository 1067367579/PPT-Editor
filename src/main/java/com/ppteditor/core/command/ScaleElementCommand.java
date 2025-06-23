package com.ppteditor.core.command;

import com.ppteditor.core.model.SlideElement;

/**
 * 元素缩放命令
 * 支持撤销和重做操作
 */
public class ScaleElementCommand implements Command {
    
    private SlideElement<?> element;
    // 使用基本类型避免序列化问题
    private double originalX, originalY, originalWidth, originalHeight;
    private double newX, newY, newWidth, newHeight;
    private double originalRotation;
    private double newRotation;
    
    public ScaleElementCommand(SlideElement<?> element, java.awt.Rectangle originalBounds, java.awt.Rectangle newBounds) {
        this.element = element;
        this.originalX = originalBounds.x;
        this.originalY = originalBounds.y;
        this.originalWidth = originalBounds.width;
        this.originalHeight = originalBounds.height;
        this.newX = newBounds.x;
        this.newY = newBounds.y;
        this.newWidth = newBounds.width;
        this.newHeight = newBounds.height;
        this.originalRotation = element.getRotation();
        this.newRotation = element.getRotation();
    }
    
    public ScaleElementCommand(SlideElement<?> element, java.awt.Rectangle originalBounds, java.awt.Rectangle newBounds, 
                              double originalRotation, double newRotation) {
        this.element = element;
        this.originalX = originalBounds.x;
        this.originalY = originalBounds.y;
        this.originalWidth = originalBounds.width;
        this.originalHeight = originalBounds.height;
        this.newX = newBounds.x;
        this.newY = newBounds.y;
        this.newWidth = newBounds.width;
        this.newHeight = newBounds.height;
        this.originalRotation = originalRotation;
        this.newRotation = newRotation;
    }
    
    @Override
    public void execute() {
        element.setBounds(newX, newY, newWidth, newHeight);
        element.setRotation(newRotation);
    }
    
    @Override
    public void undo() {
        element.setBounds(originalX, originalY, originalWidth, originalHeight);
        element.setRotation(originalRotation);
    }
    
    @Override
    public String getDescription() {
        return "缩放元素";
    }
    
    public SlideElement<?> getElement() {
        return element;
    }
    
    public java.awt.Rectangle getOriginalBounds() {
        return new java.awt.Rectangle((int)originalX, (int)originalY, (int)originalWidth, (int)originalHeight);
    }
    
    public java.awt.Rectangle getNewBounds() {
        return new java.awt.Rectangle((int)newX, (int)newY, (int)newWidth, (int)newHeight);
    }
} 