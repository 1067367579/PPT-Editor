package com.ppteditor;

import com.ppteditor.core.model.*;
import com.ppteditor.ui.SlideMasterSettingsDialog;
import javax.swing.*;
import java.awt.*;

/**
 * 母版功能演示类
 * 展示如何使用新的母版功能
 */
public class TestMasterDemo {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建一个演示文稿
            Presentation presentation = new Presentation("母版演示");
            
            // 展示各种预定义母版
            demonstratePredefinedMasters();
            
            // 创建自定义母版
            demonstrateCustomMaster(presentation);
            
            // 显示页面尺寸信息
            demonstratePageSizes();
        });
    }
    
    private static void demonstratePredefinedMasters() {
        System.out.println("=== 预定义母版演示 ===");
        
        // 获取所有预定义母版
        var masters = SlideMaster.getAllPredefinedMasters();
        
        for (SlideMaster master : masters) {
            System.out.println("母版名称: " + master.getName());
            System.out.println("页面尺寸: " + master.getSlideSize().width + "x" + master.getSlideSize().height);
            System.out.println("显示页眉: " + master.isShowHeader());
            System.out.println("显示页脚: " + master.isShowFooter());
            System.out.println("显示页码: " + master.isShowPageNumber());
            System.out.println("显示日期: " + master.isShowDateTime());
            System.out.println("背景颜色: " + colorToString(master.getBackgroundColor()));
            System.out.println("---");
        }
    }
    
    private static void demonstrateCustomMaster(Presentation presentation) {
        System.out.println("=== 自定义母版演示 ===");
        
        // 创建自定义母版
        SlideMaster customMaster = new SlideMaster("自定义母版");
        
        // 设置页面尺寸为全高清16:9
        customMaster.setSlideSize(new Dimension(1920, 1080));
        
        // 设置页眉页脚
        customMaster.setShowHeader(true);
        customMaster.setHeaderText("公司机密文档");
        customMaster.setShowFooter(true);
        customMaster.setFooterText("© 2024 我的公司");
        customMaster.setShowPageNumber(true);
        customMaster.setShowDateTime(true);
        
        // 设置背景
        customMaster.setUnifiedBackground(new Color(248, 249, 251));
        
        System.out.println("自定义母版创建完成:");
        System.out.println("母版名称: " + customMaster.getName());
        System.out.println("页面尺寸: " + customMaster.getSlideSize().width + "x" + customMaster.getSlideSize().height);
        System.out.println("页眉文本: " + customMaster.getHeaderText());
        System.out.println("页脚文本: " + customMaster.getFooterText());
        
        // 应用到演示文稿
        presentation.setSlideMaster(customMaster);
        
        // 创建新幻灯片（会自动应用母版）
        Slide slide1 = new Slide("第一页");
        Slide slide2 = new Slide("第二页");
        presentation.addSlide(slide1);
        presentation.addSlide(slide2);
        
        System.out.println("母版已应用到演示文稿，共有 " + presentation.getTotalSlides() + " 张幻灯片");
    }
    
    private static void demonstratePageSizes() {
        System.out.println("=== 标准页面尺寸参考 ===");
        System.out.println("标准16:9 (推荐): " + SlideMaster.STANDARD_WIDTH + "x" + SlideMaster.STANDARD_HEIGHT);
        System.out.println("全高清16:9: 1920x1080");
        System.out.println("经典4:3: 1024x768");
        System.out.println("宽屏16:10: 1440x900");
        
        System.out.println("\n页眉页脚区域定义:");
        System.out.println("页眉高度: " + SlideMaster.HEADER_HEIGHT + "px");
        System.out.println("页脚高度: " + SlideMaster.FOOTER_HEIGHT + "px");
        System.out.println("边距: " + SlideMaster.MARGIN + "px");
    }
    
    private static String colorToString(Color color) {
        if (color == null) return "null";
        return String.format("RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * 测试母版设置对话框
     */
    public static void testMasterDialog() {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("母版设置测试");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(400, 300);
            testFrame.setLocationRelativeTo(null);
            
            JButton openDialogButton = new JButton("打开母版设置");
            openDialogButton.addActionListener(e -> {
                Presentation presentation = new Presentation("测试演示文稿");
                SlideMasterSettingsDialog dialog = new SlideMasterSettingsDialog(testFrame, presentation);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    SlideMaster master = dialog.getSlideMaster();
                    JOptionPane.showMessageDialog(testFrame, 
                        "母版设置完成:\n" +
                        "名称: " + master.getName() + "\n" +
                        "尺寸: " + master.getSlideSize().width + "x" + master.getSlideSize().height + "\n" +
                        "页眉: " + (master.isShowHeader() ? master.getHeaderText() : "无") + "\n" +
                        "页脚: " + (master.isShowFooter() ? master.getFooterText() : "无"),
                        "母版信息", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            testFrame.add(openDialogButton);
            testFrame.setVisible(true);
        });
    }
} 