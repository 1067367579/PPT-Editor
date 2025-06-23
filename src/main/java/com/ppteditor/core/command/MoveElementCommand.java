package com.ppteditor.core.command;

import com.ppteditor.core.model.SlideElement;
import java.awt.Point;

/**
 * 移动元素命令
 * 实现元素位置变化的可撤销操作
 */
public class MoveElementCommand implements Command {
    
    private final SlideElement<?> element;
    private final Point oldPosition;
    private final Point newPosition;
    private final String description;
    
    public MoveElementCommand(SlideElement<?> element, Point oldPosition, Point newPosition) {
        this.element = element;
        this.oldPosition = new Point(oldPosition);
        this.newPosition = new Point(newPosition);
        this.description = "移动" + element.getType().getDisplayName();
    }
    
    @Override
    public void execute() {
        if (element != null) {
            element.setPosition(newPosition.x, newPosition.y);
        }
    }
    
    @Override
    public void undo() {
        if (element != null) {
            element.setPosition(oldPosition.x, oldPosition.y);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public SlideElement<?> getElement() {
        return element;
    }
    
    public Point getOldPosition() {
        return new Point(oldPosition);
    }
    
    public Point getNewPosition() {
        return new Point(newPosition);
    }
} 