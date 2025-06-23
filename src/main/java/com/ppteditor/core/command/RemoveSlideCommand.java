package com.ppteditor.core.command;

import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.model.Slide;

/**
 * 删除幻灯片命令
 * 实现从演示文档删除幻灯片的可撤销操作
 */
public class RemoveSlideCommand implements Command {
    
    private final Presentation presentation;
    private final Slide slide;
    private final int originalIndex;
    private final String description;
    
    public RemoveSlideCommand(Presentation presentation, Slide slide, int originalIndex) {
        this.presentation = presentation;
        this.slide = slide;
        this.originalIndex = originalIndex;
        this.description = "删除幻灯片: " + slide.getName();
    }
    
    @Override
    public void execute() {
        if (presentation != null && slide != null) {
            presentation.removeSlide(slide);
        }
    }
    
    @Override
    public void undo() {
        if (presentation != null && slide != null) {
            presentation.insertSlide(originalIndex, slide);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public Presentation getPresentation() {
        return presentation;
    }
    
    public Slide getSlide() {
        return slide;
    }
    
    public int getOriginalIndex() {
        return originalIndex;
    }
} 