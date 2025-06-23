package com.ppteditor.core.command;

import com.ppteditor.core.model.TextElement;

/**
 * 编辑文本命令
 * 实现文本内容修改的可撤销操作
 */
public class EditTextCommand implements Command {
    
    private final TextElement textElement;
    private final String oldText;
    private final String newText;
    private final String description;
    
    public EditTextCommand(TextElement textElement, String oldText, String newText) {
        this.textElement = textElement;
        this.oldText = oldText;
        this.newText = newText;
        this.description = "编辑文本";
    }
    
    @Override
    public void execute() {
        if (textElement != null) {
            textElement.setText(newText);
        }
    }
    
    @Override
    public void undo() {
        if (textElement != null) {
            textElement.setText(oldText);
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    public TextElement getTextElement() {
        return textElement;
    }
    
    public String getOldText() {
        return oldText;
    }
    
    public String getNewText() {
        return newText;
    }
} 