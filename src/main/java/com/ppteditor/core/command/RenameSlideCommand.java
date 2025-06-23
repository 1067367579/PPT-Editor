package com.ppteditor.core.command;

import com.ppteditor.core.model.Slide;

/**
 * 重命名幻灯片命令
 * 实现幻灯片名称修改的可撤销操作
 */
public class RenameSlideCommand implements Command {
    
    private final Slide slide;
    private final String oldName;
    private final String newName;
    private final String description;
    
    public RenameSlideCommand(Slide slide, String oldName, String newName) {
        this.slide = slide;
        this.oldName = oldName;
        this.newName = newName;
        this.description = "重命名幻灯片: " + oldName + " -> " + newName;
    }
    
    @Override
    public void execute() {
        if (slide != null) {
            slide.setName(newName);
        }
    }
    
    @Override
    public void undo() {
        if (slide != null) {
            slide.setName(oldName);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public Slide getSlide() {
        return slide;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
} 