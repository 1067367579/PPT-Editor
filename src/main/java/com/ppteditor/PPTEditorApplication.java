package com.ppteditor;

import com.ppteditor.core.command.CommandManager;
import com.ppteditor.core.model.*;
import com.ppteditor.ui.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * PPT编辑器主应用程序类
 * 程序入口点，负责初始化和启动应用程序
 */
public class PPTEditorApplication {
    
    public static void main(String[] args) {
        // 设置系统外观
        setLookAndFeel();
        
        // 在EDT中启动应用
        SwingUtilities.invokeLater(() -> {
            try {
                // 初始化应用程序
                PPTEditorApplication app = new PPTEditorApplication();
                app.initialize();
                app.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "应用程序启动失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    private void initialize() {
        // 初始化命令管理器
        CommandManager commandManager = CommandManager.getInstance();
        commandManager.setStatusCallback(this::onStatusChanged);
        
        // 创建默认演示文稿
        createDefaultPresentation();
        
        System.out.println("PPT编辑器初始化完成");
    }
    
    private void start() {
        // 创建并显示主窗口
        MainWindow mainWindow = new MainWindow();
        mainWindow.setVisible(true);
        
        System.out.println("PPT编辑器启动成功");
    }
    
    private void createDefaultPresentation() {
        // 创建默认演示文稿
        Presentation presentation = new Presentation("我的演示文稿");
        
        // 获取第一张幻灯片
        Slide firstSlide = presentation.getCurrentSlide();
        if (firstSlide != null) {
            // 添加标题
            TextElement title = TextElement.createTitle("欢迎使用PPT编辑器");
            title.setBounds(100, 50, 600, 80);
            firstSlide.addElement(title);
            
            // 添加副标题
            TextElement subtitle = TextElement.createSubtitle("功能强大的幻灯片制作工具");
            subtitle.setBounds(100, 150, 600, 50);
            firstSlide.addElement(subtitle);
            
            // 添加装饰矩形
            RectangleElement decorRect = RectangleElement.createCard(50, 250, 700, 300);
            decorRect.getStyle().setFillColor(new Color(240, 248, 255));
            decorRect.getStyle().setBorderColor(new Color(70, 130, 180));
            firstSlide.addElement(decorRect);
            
            // 添加说明文本
            TextElement bodyText = TextElement.createBodyText(
                "主要功能：\n" +
                "• 创建和编辑文本、图形、图片\n" +
                "• 支持多种字体和样式设置\n" +
                "• 提供撤销/重做功能\n" +
                "• 支持多页面管理\n" +
                "• 可导出为图片和PDF格式\n" +
                "• 丰富的配色主题和母版"
            );
            bodyText.setBounds(80, 280, 640, 240);
            firstSlide.addElement(bodyText);
        }
        
        // 应用默认主题
        presentation.applyColorTheme(ColorTheme.createBlueTheme());
        
        System.out.println("默认演示文稿创建完成");
    }
    
    private void onStatusChanged(String status) {
        System.out.println("[状态] " + status);
    }
    
    private static void setLookAndFeel() {
        try {
            // 尝试使用系统外观
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            
            // 设置一些UI属性
            UIManager.put("Button.font", new Font("微软雅黑", Font.PLAIN, 12));
            UIManager.put("Label.font", new Font("微软雅黑", Font.PLAIN, 12));
            UIManager.put("Menu.font", new Font("微软雅黑", Font.PLAIN, 12));
            UIManager.put("MenuItem.font", new Font("微软雅黑", Font.PLAIN, 12));
            UIManager.put("TextField.font", new Font("微软雅黑", Font.PLAIN, 12));
            
        } catch (Exception e) {
            System.err.println("无法设置系统外观，使用默认外观: " + e.getMessage());
        }
    }
    
    /**
     * 获取应用程序信息
     */
    public static class AppInfo {
        public static final String NAME = "PPT编辑器";
        public static final String VERSION = "1.0.0";
        public static final String AUTHOR = "2022150206 陆景翔";
        
        public static String getFullName() {
            return NAME + " v" + VERSION;
        }
        
        public static String getAboutText() {
            return String.format(
                "%s\n版本: %s\n作者: %s\n\n",
                NAME, VERSION, AUTHOR
            );
        }
    }
} 