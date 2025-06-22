package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 幻灯片母版类
 * 定义幻灯片的通用样式和布局
 */
public class SlideMaster implements Cloneable {
    
    @Serializable(required = true)
    private String id;
    
    @Serializable
    private String name;
    
    @Serializable
    private Color backgroundColor;
    
    @Serializable
    private String backgroundImagePath;
    
    @Serializable
    private List<SlideElement<?>> masterElements; // 母版元素（如页眉、页脚）
    
    @Serializable
    private TextStyle defaultTitleStyle;
    
    @Serializable
    private TextStyle defaultBodyStyle;
    
    @Serializable
    private ShapeStyle defaultShapeStyle;
    
    public SlideMaster() {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = "默认母版";
        this.backgroundColor = Color.WHITE;
        this.masterElements = new ArrayList<>();
        this.defaultTitleStyle = createDefaultTitleStyle();
        this.defaultBodyStyle = createDefaultBodyStyle();
        this.defaultShapeStyle = new ShapeStyle();
    }
    
    public SlideMaster(String name) {
        this();
        this.name = name;
    }
    
    private TextStyle createDefaultTitleStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(28)
                .bold(true)
                .alignment(1) // 居中
                .textColor(new Color(68, 68, 68))
                .build();
    }
    
    private TextStyle createDefaultBodyStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(16)
                .alignment(0) // 左对齐
                .textColor(new Color(68, 68, 68))
                .lineSpacing(1.2)
                .build();
    }
    
    // 应用母版到幻灯片
    public void applyToSlide(Slide slide) {
        if (slide == null) return;
        
        // 设置背景
        slide.setBackgroundColor(backgroundColor);
        slide.setBackgroundImagePath(backgroundImagePath);
        
        // 添加母版元素
        masterElements.forEach(element -> {
            SlideElement<?> cloned = element.clone();
            cloned.setLocked(true); // 锁定母版元素
            slide.addElement(cloned);
        });
    }
    
    // 添加母版元素（如页眉、页脚）
    public void addMasterElement(SlideElement<?> element) {
        if (element != null) {
            masterElements.add(element);
        }
    }
    
    public void removeMasterElement(SlideElement<?> element) {
        masterElements.remove(element);
    }
    
    // 静态工厂方法 - 预定义母版
    public static SlideMaster createDefaultMaster() {
        return new SlideMaster("默认母版");
    }
    
    public static SlideMaster createBusinessMaster() {
        SlideMaster master = new SlideMaster("商务母版");
        master.backgroundColor = new Color(245, 245, 245);
        
        // 添加页脚
        TextElement footer = new TextElement("© 公司名称");
        footer.setBounds(50, 680, 200, 20);
        footer.getStyle().setFontSize(10);
        footer.getStyle().setTextColor(Color.GRAY);
        master.addMasterElement(footer);
        
        return master;
    }
    
    @Override
    public SlideMaster clone() {
        try {
            SlideMaster cloned = (SlideMaster) super.clone();
            cloned.id = java.util.UUID.randomUUID().toString();
            cloned.masterElements = new ArrayList<>();
            
            // 克隆母版元素
            this.masterElements.forEach(element -> 
                cloned.masterElements.add(element.clone()));
            
            // 克隆样式
            cloned.defaultTitleStyle = this.defaultTitleStyle.clone();
            cloned.defaultBodyStyle = this.defaultBodyStyle.clone();
            cloned.defaultShapeStyle = this.defaultShapeStyle.clone();
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆失败", e);
        }
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public String getBackgroundImagePath() { return backgroundImagePath; }
    public void setBackgroundImagePath(String backgroundImagePath) { this.backgroundImagePath = backgroundImagePath; }
    
    public List<SlideElement<?>> getMasterElements() { return new ArrayList<>(masterElements); }
    public void setMasterElements(List<SlideElement<?>> masterElements) { 
        this.masterElements = new ArrayList<>(masterElements); 
    }
    
    public TextStyle getDefaultTitleStyle() { return defaultTitleStyle; }
    public void setDefaultTitleStyle(TextStyle defaultTitleStyle) { this.defaultTitleStyle = defaultTitleStyle; }
    
    public TextStyle getDefaultBodyStyle() { return defaultBodyStyle; }
    public void setDefaultBodyStyle(TextStyle defaultBodyStyle) { this.defaultBodyStyle = defaultBodyStyle; }
    
    public ShapeStyle getDefaultShapeStyle() { return defaultShapeStyle; }
    public void setDefaultShapeStyle(ShapeStyle defaultShapeStyle) { this.defaultShapeStyle = defaultShapeStyle; }
} 