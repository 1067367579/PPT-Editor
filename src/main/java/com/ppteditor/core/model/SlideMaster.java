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
        
        // 首先清除之前的母版元素
        slide.removeMasterElements();
        
        // 设置背景
        slide.setBackgroundColor(backgroundColor);
        slide.setBackgroundImagePath(backgroundImagePath);
        
        // 添加新的母版元素
        masterElements.forEach(element -> {
            SlideElement<?> cloned = element.clone();
            cloned.setLocked(true); // 锁定母版元素，标识为母版元素
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
    
    public static SlideMaster createAcademicMaster() {
        SlideMaster master = new SlideMaster("学术母版");
        master.backgroundColor = Color.WHITE;
        
        // 添加页眉
        TextElement header = new TextElement("学术报告");
        header.setBounds(650, 30, 150, 25);
        header.getStyle().setFontSize(12);
        header.getStyle().setTextColor(new Color(100, 100, 100));
        header.getStyle().setAlignment(2); // 右对齐
        master.addMasterElement(header);
        
        // 添加页脚
        TextElement footer = new TextElement("第 1 页");
        footer.setBounds(750, 680, 50, 20);
        footer.getStyle().setFontSize(10);
        footer.getStyle().setTextColor(Color.GRAY);
        footer.getStyle().setAlignment(2); // 右对齐
        master.addMasterElement(footer);
        
        // 添加分隔线
        RectangleElement divider = new RectangleElement(50, 65, 700, 2);
        divider.getStyle().setFillColor(new Color(200, 200, 200));
        divider.getStyle().setBorderWidth(0);
        master.addMasterElement(divider);
        
        return master;
    }
    
    public static SlideMaster createCreativeMaster() {
        SlideMaster master = new SlideMaster("创意母版");
        master.backgroundColor = new Color(248, 249, 250);
        
        // 添加装饰性元素
        EllipseElement decoration1 = new EllipseElement(750, 50, 80, 80);
        decoration1.getStyle().setFillColor(new Color(255, 193, 7, 100));
        decoration1.getStyle().setBorderWidth(0);
        master.addMasterElement(decoration1);
        
        EllipseElement decoration2 = new EllipseElement(-20, 600, 60, 60);
        decoration2.getStyle().setFillColor(new Color(0, 123, 255, 100));
        decoration2.getStyle().setBorderWidth(0);
        master.addMasterElement(decoration2);
        
        // 添加品牌标识区域
        RectangleElement brandArea = new RectangleElement(600, 650, 180, 40);
        brandArea.getStyle().setFillColor(new Color(33, 37, 41, 20));
        brandArea.getStyle().setBorderWidth(1);
        brandArea.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(brandArea);
        
        return master;
    }
    
    public static SlideMaster createMinimalMaster() {
        SlideMaster master = new SlideMaster("简约母版");
        master.backgroundColor = Color.WHITE;
        
        // 只添加一个简单的页码
        TextElement pageNumber = new TextElement("1");
        pageNumber.setBounds(390, 680, 20, 20);
        pageNumber.getStyle().setFontSize(12);
        pageNumber.getStyle().setTextColor(new Color(150, 150, 150));
        pageNumber.getStyle().setAlignment(1); // 居中
        master.addMasterElement(pageNumber);
        
        return master;
    }
    
    public static SlideMaster createTitleSlideMaster() {
        SlideMaster master = new SlideMaster("标题页母版");
        master.backgroundColor = new Color(248, 249, 250);
        
        // 大标题区域指示
        RectangleElement titleArea = new RectangleElement(100, 150, 600, 80);
        titleArea.getStyle().setFillColor(new Color(0, 0, 0, 0)); // 透明
        titleArea.getStyle().setBorderWidth(2);
        titleArea.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(titleArea);
        
        // 副标题区域指示
        RectangleElement subtitleArea = new RectangleElement(100, 250, 600, 40);
        subtitleArea.getStyle().setFillColor(new Color(0, 0, 0, 0)); // 透明
        subtitleArea.getStyle().setBorderWidth(1);
        subtitleArea.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(subtitleArea);
        
        // 装饰线
        RectangleElement decorLine = new RectangleElement(100, 320, 600, 3);
        decorLine.getStyle().setFillColor(new Color(0, 123, 255));
        decorLine.getStyle().setBorderWidth(0);
        master.addMasterElement(decorLine);
        
        return master;
    }
    
    public static SlideMaster createContentSlideMaster() {
        SlideMaster master = new SlideMaster("内容页母版");
        master.backgroundColor = Color.WHITE;
        
        // 标题区域
        RectangleElement titleBar = new RectangleElement(50, 50, 700, 50);
        titleBar.getStyle().setFillColor(new Color(245, 245, 245));
        titleBar.getStyle().setBorderWidth(0);
        master.addMasterElement(titleBar);
        
        // 内容区域边框
        RectangleElement contentFrame = new RectangleElement(50, 120, 700, 500);
        contentFrame.getStyle().setFillColor(new Color(0, 0, 0, 0)); // 透明
        contentFrame.getStyle().setBorderWidth(1);
        contentFrame.getStyle().setBorderColor(new Color(220, 220, 220));
        master.addMasterElement(contentFrame);
        
        // 页脚信息
        TextElement footer = new TextElement("演示文稿");
        footer.setBounds(50, 680, 200, 20);
        footer.getStyle().setFontSize(10);
        footer.getStyle().setTextColor(new Color(128, 128, 128));
        master.addMasterElement(footer);
        
        return master;
    }
    
    public static SlideMaster createTwoColumnMaster() {
        SlideMaster master = new SlideMaster("双栏母版");
        master.backgroundColor = Color.WHITE;
        
        // 左栏
        RectangleElement leftColumn = new RectangleElement(50, 120, 320, 500);
        leftColumn.getStyle().setFillColor(new Color(0, 0, 0, 0)); // 透明
        leftColumn.getStyle().setBorderWidth(1);
        leftColumn.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(leftColumn);
        
        // 右栏
        RectangleElement rightColumn = new RectangleElement(390, 120, 320, 500);
        rightColumn.getStyle().setFillColor(new Color(0, 0, 0, 0)); // 透明
        rightColumn.getStyle().setBorderWidth(1);
        rightColumn.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(rightColumn);
        
        // 分隔线
        RectangleElement separator = new RectangleElement(375, 120, 2, 500);
        separator.getStyle().setFillColor(new Color(220, 220, 220));
        separator.getStyle().setBorderWidth(0);
        master.addMasterElement(separator);
        
        return master;
    }
    
    public static SlideMaster createProfessionalMaster() {
        SlideMaster master = new SlideMaster("专业母版");
        master.backgroundColor = new Color(252, 252, 252);
        
        // 顶部装饰条
        RectangleElement topBar = new RectangleElement(0, 0, 800, 8);
        topBar.getStyle().setFillColor(new Color(68, 114, 196));
        topBar.getStyle().setBorderWidth(0);
        master.addMasterElement(topBar);
        
        // 公司LOGO区域
        RectangleElement logoArea = new RectangleElement(650, 20, 120, 40);
        logoArea.getStyle().setFillColor(new Color(240, 240, 240));
        logoArea.getStyle().setBorderWidth(1);
        logoArea.getStyle().setBorderColor(new Color(200, 200, 200));
        master.addMasterElement(logoArea);
        
        TextElement logoText = new TextElement("LOGO");
        logoText.setBounds(680, 30, 60, 20);
        logoText.getStyle().setFontSize(10);
        logoText.getStyle().setTextColor(new Color(150, 150, 150));
        logoText.getStyle().setAlignment(1); // 居中
        master.addMasterElement(logoText);
        
        // 底部信息条
        RectangleElement bottomBar = new RectangleElement(0, 680, 800, 30);
        bottomBar.getStyle().setFillColor(new Color(245, 245, 245));
        bottomBar.getStyle().setBorderWidth(0);
        master.addMasterElement(bottomBar);
        
        // 日期
        TextElement dateText = new TextElement("2024年");
        dateText.setBounds(50, 685, 100, 20);
        dateText.getStyle().setFontSize(10);
        dateText.getStyle().setTextColor(new Color(100, 100, 100));
        master.addMasterElement(dateText);
        
        // 页码
        TextElement pageNum = new TextElement("1");
        pageNum.setBounds(750, 685, 30, 20);
        pageNum.getStyle().setFontSize(10);
        pageNum.getStyle().setTextColor(new Color(100, 100, 100));
        pageNum.getStyle().setAlignment(2); // 右对齐
        master.addMasterElement(pageNum);
        
        return master;
    }
    
    public static SlideMaster createModernFlatMaster() {
        SlideMaster master = new SlideMaster("现代扁平");
        master.backgroundColor = Color.WHITE;
        
        // 左侧色彩条
        RectangleElement colorBar = new RectangleElement(0, 0, 8, 720);
        colorBar.getStyle().setFillColor(new Color(91, 155, 213));
        colorBar.getStyle().setBorderWidth(0);
        master.addMasterElement(colorBar);
        
        // 标题下划线（动态元素，会根据内容调整）
        RectangleElement titleUnderline = new RectangleElement(50, 95, 200, 3);
        titleUnderline.getStyle().setFillColor(new Color(255, 192, 0));
        titleUnderline.getStyle().setBorderWidth(0);
        master.addMasterElement(titleUnderline);
        
        return master;
    }

    /**
     * 获取所有预定义母版
     */
    public static List<SlideMaster> getAllPredefinedMasters() {
        return java.util.Arrays.asList(
            createDefaultMaster(),
            createTitleSlideMaster(),
            createContentSlideMaster(),
            createTwoColumnMaster(),
            createBusinessMaster(),
            createProfessionalMaster(),
            createAcademicMaster(),
            createCreativeMaster(),
            createModernFlatMaster(),
            createMinimalMaster()
        );
    }
    
    /**
     * 应用配色主题到母版
     */
    public void applyColorTheme(ColorTheme theme) {
        if (theme == null) return;
        
        // 应用背景色
        this.backgroundColor = theme.getBackgroundColor();
        
        // 更新默认样式
        if (defaultTitleStyle != null) {
            defaultTitleStyle.setTextColor(theme.getTextColor());
        }
        
        if (defaultBodyStyle != null) {
            defaultBodyStyle.setTextColor(theme.getTextColor());
        }
        
        if (defaultShapeStyle != null) {
            defaultShapeStyle.setFillColor(theme.getSecondaryColor());
            defaultShapeStyle.setBorderColor(theme.getPrimaryColor());
        }
        
        // 更新母版元素颜色
        for (SlideElement<?> element : masterElements) {
            if (element instanceof TextElement) {
                TextElement textElement = (TextElement) element;
                textElement.getStyle().setTextColor(theme.getTextColor());
            } else if (element.getStyle() instanceof ShapeStyle) {
                ShapeStyle shapeStyle = (ShapeStyle) element.getStyle();
                shapeStyle.setFillColor(theme.getSecondaryColor());
                shapeStyle.setBorderColor(theme.getPrimaryColor());
            }
        }
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