package com.ppteditor.core.command;

import com.ppteditor.core.model.Slide;
import com.ppteditor.core.model.SlideElement;
import java.util.List;
import java.util.ArrayList;

/**
 * 删除元素命令
 * 实现从幻灯片删除元素的可撤销操作
 */
public class DeleteElementsCommand implements Command {
    
    private final Slide slide;
    private final List<SlideElement<?>> elements;
    private final String description;
    
    public DeleteElementsCommand(Slide slide, List<SlideElement<?>> elements) {
        this.slide = slide;
        this.elements = new ArrayList<>(elements);
        this.description = "删除" + elements.size() + "个元素";
    }
    
    @Override
    public void execute() {
        if (slide != null && elements != null) {
            for (SlideElement<?> element : elements) {
                slide.removeElement(element);
            }
        }
    }
    
    @Override
    public void undo() {
        if (slide != null && elements != null) {
            for (SlideElement<?> element : elements) {
                slide.addElement(element);
            }
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public Slide getSlide() {
        return slide;
    }
    
    public List<SlideElement<?>> getElements() {
        return new ArrayList<>(elements);
    }
} 