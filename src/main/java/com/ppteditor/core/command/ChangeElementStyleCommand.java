package com.ppteditor.core.command;

import com.ppteditor.core.model.ElementStyle;
import com.ppteditor.core.model.IconElement;
import com.ppteditor.core.model.SlideElement;

import java.awt.*;
import java.util.function.Consumer;

/**
 * 通用样式修改命令
 * @param <T> 元素类型
 * @param <S> 样式值类型
 */
public class ChangeElementStyleCommand<T extends SlideElement<?>, S> implements Command {

    private final T element;
    private final S oldValue;
    private final S newValue;
    private final Consumer<S> styleSetter;
    private final String description;

    public ChangeElementStyleCommand(T element, S oldValue, S newValue,
                                     Consumer<S> styleSetter, String description) {
        this.element = element;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.styleSetter = styleSetter;
        this.description = description;
    }

    @Override
    public void execute() {
        styleSetter.accept(newValue);
    }

    @Override
    public void undo() {
        styleSetter.accept(oldValue);
    }

    @Override
    public String getDescription() {
        return description;
    }
    
} 