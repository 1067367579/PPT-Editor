package com.ppteditor.core.command;

import com.ppteditor.core.model.Slide;
import com.ppteditor.core.model.SlideElement;

/**
 * 添加元素命令
 * 实现向幻灯片添加元素的可撤销操作
 */
public class AddElementCommand implements Command {
    
    private final Slide slide;
    private final java.util.List<SlideElement<?>> elements;
    private final String description;
    
    public AddElementCommand(Slide slide, java.util.List<SlideElement<?>> elements) {
        this.slide = slide;
        this.elements = new java.util.ArrayList<>(elements);
        if (elements.size() == 1) {
            this.description = "添加" + elements.get(0).getType().getDisplayName();
        } else {
            this.description = "添加" + elements.size() + "个元素";
        }
    }
    
    public AddElementCommand(Slide slide, SlideElement<?> element) {
        this.slide = slide;
        this.elements = java.util.Collections.singletonList(element);
        this.description = "添加" + element.getType().getDisplayName();
    }
    
    @Override
    public void execute() {
        if (slide != null && elements != null) {
            elements.forEach(slide::addElement);
        }
    }
    
    @Override
    public void undo() {
        if (slide != null && elements != null) {
            elements.forEach(slide::removeElement);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public Slide getSlide() {
        return slide;
    }
    
    public java.util.List<SlideElement<?>> getElements() {
        return elements;
    }
} 