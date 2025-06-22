package com.ppteditor.core.command;

/**
 * 命令接口
 * 实现命令模式，支持撤销和重做功能
 */
public interface Command {
    
    /**
     * 执行命令
     */
    void execute();
    
    /**
     * 撤销命令
     */
    void undo();
    
    /**
     * 重做命令
     */
    default void redo() {
        execute();
    }
    
    /**
     * 获取命令描述
     * @return 命令的可读描述
     */
    String getDescription();
    
    /**
     * 判断命令是否可以撤销
     * @return true如果可以撤销
     */
    default boolean canUndo() {
        return true;
    }
    
    /**
     * 判断命令是否可以重做
     * @return true如果可以重做
     */
    default boolean canRedo() {
        return true;
    }
} 