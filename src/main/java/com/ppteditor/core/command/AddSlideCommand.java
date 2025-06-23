package com.ppteditor.core.command;

import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.model.Slide;

/**
 * 添加幻灯片命令
 * 实现向演示文档添加新幻灯片的可撤销操作
 */
public class AddSlideCommand implements Command {
    
    private final Presentation presentation;
    private final Slide slide;
    private final int insertIndex;
    private final String description;
    
    public AddSlideCommand(Presentation presentation, Slide slide) {
        this(presentation, slide, -1); // -1 表示添加到末尾
    }
    
    public AddSlideCommand(Presentation presentation, Slide slide, int insertIndex) {
        this.presentation = presentation;
        this.slide = slide;
        this.insertIndex = insertIndex;
        this.description = "添加幻灯片: " + slide.getName();
    }
    
    @Override
    public void execute() {
        if (presentation != null && slide != null) {
            if (insertIndex < 0) {
                presentation.addSlide(slide);
            } else {
                presentation.insertSlide(insertIndex, slide);
            }
        }
    }
    
    @Override
    public void undo() {
        if (presentation != null && slide != null) {
            presentation.removeSlide(slide);
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
    
    public int getInsertIndex() {
        return insertIndex;
    }
} 