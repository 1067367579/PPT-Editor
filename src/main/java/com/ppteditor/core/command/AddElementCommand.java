package com.ppteditor.core.command;

import com.ppteditor.core.model.Slide;
import com.ppteditor.core.model.SlideElement;

/**
 * 添加元素命令
 * 实现向幻灯片添加元素的可撤销操作
 */
public class AddElementCommand implements Command {
    
    private final Slide slide;
    private final SlideElement<?> element;
    private final String description;
    
    public AddElementCommand(Slide slide, SlideElement<?> element) {
        this.slide = slide;
        this.element = element;
        this.description = "添加" + element.getType().getDisplayName();
    }
    
    @Override
    public void execute() {
        if (slide != null && element != null) {
            slide.addElement(element);
        }
    }
    
    @Override
    public void undo() {
        if (slide != null && element != null) {
            slide.removeElement(element);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public Slide getSlide() {
        return slide;
    }
    
    public SlideElement<?> getElement() {
        return element;
    }
} 