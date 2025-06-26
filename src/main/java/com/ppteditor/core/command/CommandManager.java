package com.ppteditor.core.command;

import java.util.Stack;
import java.util.function.Consumer;

/**
 * 命令管理器
 * 使用单例模式管理命令的执行、撤销和重做
 */
public class CommandManager {
    
    private static volatile CommandManager instance;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final int maxHistorySize;
    
    // 回调函数 - 使用Lambda表达式
    private Consumer<String> statusCallback;
    
    private CommandManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.maxHistorySize = 100; // 最大历史记录数
    }
    
    // 单例模式 - 双重检查锁定
    public static CommandManager getInstance() {
        if (instance == null) {
            synchronized (CommandManager.class) {
                if (instance == null) {
                    instance = new CommandManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 执行命令并添加到撤销栈
     */
    public void executeCommand(Command command) {
        if (command == null) return;
        try {
            command.execute();
            // 清空重做栈
            redoStack.clear();
            // 添加到撤销栈
            undoStack.push(command);
            // 限制历史记录大小
            if (undoStack.size() > maxHistorySize) {
                // 移除最旧的命令
                Stack<Command> temp = new Stack<>();
                for (int i = 0; i < maxHistorySize - 1; i++) {
                    temp.push(undoStack.pop());
                }
                undoStack.clear();
                while (!temp.isEmpty()) {
                    undoStack.push(temp.pop());
                }
            }
            notifyStatusChange("执行: " + command.getDescription());
        } catch (Exception e) {
            notifyStatusChange("执行失败: " + command.getDescription());
            throw new RuntimeException("命令执行失败", e);
        }
    }
    
    //撤销上一个命令
    public boolean undo() {
        if (!canUndo()) return false;
        Command command = undoStack.pop();
        try {
            if (command.canUndo()) {
                command.undo();
                redoStack.push(command);
                notifyStatusChange("撤销: " + command.getDescription());
                return true;
            }
        } catch (Exception e) {
            // 如果撤销失败，重新加入撤销栈
            undoStack.push(command);
            notifyStatusChange("撤销失败: " + command.getDescription());
            throw new RuntimeException("撤销失败", e);
        }
        return false;
    }
    
    //重做下一个命令
    public boolean redo() {
        if (!canRedo()) return false;
        Command command = redoStack.pop();
        try {
            if (command.canRedo()) {
                command.redo();
                undoStack.push(command);
                notifyStatusChange("重做: " + command.getDescription());
                return true;
            }
        } catch (Exception e) {
            // 如果重做失败，重新加入重做栈
            redoStack.push(command);
            notifyStatusChange("重做失败: " + command.getDescription());
            throw new RuntimeException("重做失败", e);
        }
        return false;
    }
    
    /**
     * 检查是否可以撤销
     */
    public boolean canUndo() {
        return !undoStack.isEmpty() && undoStack.peek().canUndo();
    }
    
    /**
     * 检查是否可以重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty() && redoStack.peek().canRedo();
    }
    
    /**
     * 获取下一个可撤销命令的描述
     */
    public String getUndoDescription() {
        if (canUndo()) {
            return undoStack.peek().getDescription();
        }
        return null;
    }
    
    /**
     * 获取下一个可重做命令的描述
     */
    public String getRedoDescription() {
        if (canRedo()) {
            return redoStack.peek().getDescription();
        }
        return null;
    }
    
    /**
     * 清空所有历史记录
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        notifyStatusChange("历史记录已清空");
    }
    
    /**
     * 获取撤销栈大小
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }
    
    /**
     * 获取重做栈大小
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }
    
    /**
     * 设置状态回调函数
     */
    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }
    
    private void notifyStatusChange(String message) {
        if (statusCallback != null) {
            statusCallback.accept(message);
        }
    }
    
    /**
     * 批量执行命令（作为一个整体进行撤销/重做）
     */
    public void executeBatch(String batchDescription, Command... commands) {
        if (commands == null || commands.length == 0) {
            return;
        }
        
        if (commands.length == 1) {
            executeCommand(commands[0]);
            return;
        }
        
        // 创建批量命令
        BatchCommand batchCommand = new BatchCommand(batchDescription, commands);
        executeCommand(batchCommand);
    }
    
    /**
     * 批量命令实现
     */
    private static class BatchCommand implements Command {
        private final String description;
        private final Command[] commands;
        
        public BatchCommand(String description, Command... commands) {
            this.description = description;
            this.commands = commands.clone();
        }
        
        @Override
        public void execute() {
            for (Command command : commands) {
                command.execute();
            }
        }
        
        @Override
        public void undo() {
            // 逆序撤销
            for (int i = commands.length - 1; i >= 0; i--) {
                commands[i].undo();
            }
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public boolean canUndo() {
            // 所有命令都可以撤销才能撤销批量命令
            for (Command command : commands) {
                if (!command.canUndo()) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public boolean canRedo() {
            // 所有命令都可以重做才能重做批量命令
            for (Command command : commands) {
                if (!command.canRedo()) {
                    return false;
                }
            }
            return true;
        }
    }
} 