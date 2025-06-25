package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * 幻灯片母版类
 * 定义幻灯片的通用样式和布局
 */
public class SlideMaster implements Cloneable {
    
    // 标准PPT页面尺寸（16:9比例）
    public static final int STANDARD_WIDTH = 1280;
    public static final int STANDARD_HEIGHT = 720;
    public static final Dimension STANDARD_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT);
    
    // 页眉页脚区域定义
    public static final int HEADER_HEIGHT = 40;
    public static final int FOOTER_HEIGHT = 40;
    public static final int MARGIN = 20;
    
    @Serializable(required = true)
    private String id;
    
    @Serializable
    private String name;
    
    @Serializable
    private Dimension slideSize; // 幻灯片尺寸
    
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
    
    // 页眉页脚设置
    @Serializable
    private boolean showHeader;
    
    @Serializable
    private String headerText;
    
    @Serializable
    private boolean showFooter;
    
    @Serializable
    private String footerText;
    
    @Serializable
    private boolean showPageNumber;
    
    @Serializable
    private boolean showDateTime;
    
    public SlideMaster() {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = "默认母版";
        this.slideSize = new Dimension(STANDARD_SIZE);
        this.backgroundColor = Color.WHITE;
        this.masterElements = new ArrayList<>();
        this.defaultTitleStyle = createDefaultTitleStyle();
        this.defaultBodyStyle = createDefaultBodyStyle();
        this.defaultShapeStyle = new ShapeStyle();
        
        // 默认页眉页脚设置
        this.showHeader = false;
        this.headerText = "";
        this.showFooter = true;
        this.footerText = "";
        this.showPageNumber = true;
        this.showDateTime = false;
        
        // 创建默认页眉页脚元素
        createDefaultHeaderFooter();
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
    
    /**
     * 创建默认的页眉页脚元素
     */
    private void createDefaultHeaderFooter() {
        // 创建页脚页码
        if (showPageNumber) {
            TextElement pageNumber = new TextElement("1");
            pageNumber.setBounds(slideSize.width - 60, slideSize.height - 30, 40, 20);
            pageNumber.setStyle(createPageNumberStyle());
            addMasterElement(pageNumber);
        }
    }
    
    /**
     * 更新页眉页脚元素
     */
    public void updateHeaderFooter() {
        // 清除现有的页眉页脚元素
        masterElements.removeIf(element -> 
            element instanceof TextElement && 
            (element.getBounds().y < HEADER_HEIGHT || 
             element.getBounds().y > slideSize.height - FOOTER_HEIGHT));
        
        // 重新创建页眉页脚
        createHeaderFooterElements();
    }
    
    /**
     * 创建页眉页脚元素
     */
    private void createHeaderFooterElements() {
        // 创建页眉
        if (showHeader && headerText != null && !headerText.trim().isEmpty()) {
            TextElement header = new TextElement(headerText);
            header.setBounds(MARGIN, MARGIN, slideSize.width - 2 * MARGIN, HEADER_HEIGHT - MARGIN);
            header.setStyle(createHeaderStyle());
            addMasterElement(header);
        }
        
        // 创建页脚文本
        if (showFooter && footerText != null && !footerText.trim().isEmpty()) {
            TextElement footer = new TextElement(footerText);
            footer.setBounds(MARGIN, slideSize.height - FOOTER_HEIGHT, 
                           slideSize.width / 2 - MARGIN, FOOTER_HEIGHT - MARGIN);
            footer.setStyle(createFooterStyle());
            addMasterElement(footer);
        }
        
        // 创建页码
        if (showPageNumber) {
            TextElement pageNumber = new TextElement("1");
            int x = slideSize.width - 80;
            int y = slideSize.height - 30;
            pageNumber.setBounds(x, y, 60, 20);
            pageNumber.setStyle(createPageNumberStyle());
            addMasterElement(pageNumber);
        }
        
        // 创建日期时间
        if (showDateTime) {
            TextElement dateTime = new TextElement(getCurrentDateTime());
            int x = slideSize.width / 2 - 60;
            int y = slideSize.height - 30;
            dateTime.setBounds(x, y, 120, 20);
            dateTime.setStyle(createDateTimeStyle());
            addMasterElement(dateTime);
        }
    }
    
    private TextStyle createHeaderStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(14)
                .bold(true)
                .alignment(0) // 左对齐
                .textColor(new Color(100, 100, 100))
                .build();
    }
    
    private TextStyle createFooterStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(12)
                .alignment(0) // 左对齐
                .textColor(new Color(120, 120, 120))
                .build();
    }
    
    private TextStyle createPageNumberStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(12)
                .alignment(2) // 右对齐
                .textColor(new Color(120, 120, 120))
                .build();
    }
    
    private TextStyle createDateTimeStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(10)
                .alignment(1) // 居中
                .textColor(new Color(120, 120, 120))
                .build();
    }
    
    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());
    }
    
    private TextStyle createTitlePlaceholderStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(56) // 更大的标题字号
                .bold(true)
                .alignment(1) // 居中对齐
                .textColor(new Color(33, 37, 41)) // 深灰色，更专业
                .lineSpacing(1.2) // 适当的行间距
                .build();
    }
    
    private TextStyle createSubtitlePlaceholderStyle() {
        return new TextStyle.Builder()
                .fontFamily("微软雅黑")
                .fontSize(20) // 适中的副标题字号
                .bold(false) // 副标题不加粗
                .alignment(1) // 居中对齐
                .textColor(new Color(108, 117, 125)) // 中等灰色
                .lineSpacing(1.3) // 稍大的行间距
                .build();
    }
    
    // 应用母版到幻灯片
    public void applyToSlide(Slide slide) {
        if (slide == null) return;
        
        // 首先清除之前的母版元素
        slide.removeMasterElements();
        
        // 设置幻灯片尺寸
        slide.setSize(slideSize);
        
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
        SlideMaster master = new SlideMaster("默认母版");
        master.showHeader = false;
        master.showFooter = false;
        master.showPageNumber = true;
        master.showDateTime = false;
        master.updateHeaderFooter();
        return master;
    }
    
    public static SlideMaster createBusinessMaster() {
        SlideMaster master = new SlideMaster("商务母版");
        master.backgroundColor = new Color(245, 245, 245);
        master.showHeader = true;
        master.headerText = "商务演示";
        master.showFooter = true;
        master.footerText = "© 公司名称";
        master.showPageNumber = true;
        master.showDateTime = true;
        master.updateHeaderFooter();
        return master;
    }
    
    public static SlideMaster createAcademicMaster() {
        SlideMaster master = new SlideMaster("学术母版");
        master.backgroundColor = Color.WHITE;
        master.showHeader = true;
        master.headerText = "学术报告";
        master.showFooter = true;
        master.footerText = "学术机构";
        master.showPageNumber = true;
        master.showDateTime = true;
        
        // 添加分隔线
        RectangleElement divider = new RectangleElement(MARGIN, 65, master.slideSize.width - 2 * MARGIN, 2);
        divider.getStyle().setFillColor(new Color(200, 200, 200));
        divider.getStyle().setBorderWidth(0);
        master.addMasterElement(divider);
        
        master.updateHeaderFooter();
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
        master.backgroundColor = Color.WHITE; // 纯白背景更简洁
        master.showHeader = false;
        master.showFooter = false;
        master.showPageNumber = false;
        master.showDateTime = false;
        
        // 主标题 - 完全居中
        int titleWidth = 1000;
        int titleHeight = 120;
        int titleX = (master.slideSize.width - titleWidth) / 2; // 水平居中
        int titleY = (master.slideSize.height - titleHeight) / 2 - 80; // 垂直居中偏上
        
        TextElement titlePlaceholder = new TextElement("演示文稿标题");
        titlePlaceholder.setBounds(titleX, titleY, titleWidth, titleHeight);
        titlePlaceholder.setStyle(master.createTitlePlaceholderStyle());
        master.addMasterElement(titlePlaceholder);
        
        // 副标题 - 在主标题正下方
        int subtitleWidth = 800;
        int subtitleHeight = 80;
        int subtitleX = (master.slideSize.width - subtitleWidth) / 2; // 水平居中
        int subtitleY = titleY + titleHeight + 20; // 紧跟主标题，间距20px
        
        TextElement subtitlePlaceholder = new TextElement("副标题或作者信息");
        subtitlePlaceholder.setBounds(subtitleX, subtitleY, subtitleWidth, subtitleHeight);
        subtitlePlaceholder.setStyle(master.createSubtitlePlaceholderStyle());
        master.addMasterElement(subtitlePlaceholder);
        
        // 简洁的装饰线 - 在副标题下方
        int lineWidth = 400;
        int lineX = (master.slideSize.width - lineWidth) / 2; // 居中
        int lineY = subtitleY + subtitleHeight + 30;
        
        RectangleElement decorLine = new RectangleElement(lineX, lineY, lineWidth, 2);
        decorLine.getStyle().setFillColor(new Color(100, 149, 237)); // 柔和的蓝色
        decorLine.getStyle().setBorderWidth(0);
        master.addMasterElement(decorLine);
        
        return master;
    }
    
    public static SlideMaster createContentSlideMaster() {
        SlideMaster master = new SlideMaster("内容页母版");
        master.backgroundColor = Color.WHITE;
        
        // 标题文本框 - 可编辑
        TextElement titlePlaceholder = new TextElement("点击编辑标题");
        titlePlaceholder.setBounds(60, 60, 600, 40);
        TextStyle titleStyle = master.createTitlePlaceholderStyle();
        titlePlaceholder.setStyle(titleStyle);
        master.addMasterElement(titlePlaceholder);
        
        // 主要内容文本框 - 可编辑
        TextElement contentPlaceholder = new TextElement("点击添加内容\n• 要点一\n• 要点二\n• 要点三");
        contentPlaceholder.setBounds(60, 130, 600, 400);
        TextStyle bodyStyle = master.createSubtitlePlaceholderStyle(); // 使用现有的副标题样式
        bodyStyle.setAlignment(TextStyle.ALIGN_LEFT); // 左对齐
        contentPlaceholder.setStyle(bodyStyle);
        master.addMasterElement(contentPlaceholder);
        
        // 副内容文本框 - 可编辑（右侧）
        TextElement sideContentPlaceholder = new TextElement("附加信息");
        sideContentPlaceholder.setBounds(680, 130, 200, 200);
        TextStyle sideStyle = master.createSubtitlePlaceholderStyle();
        sideStyle.setFontSize(14);
        sideStyle.setAlignment(TextStyle.ALIGN_LEFT);
        sideContentPlaceholder.setStyle(sideStyle);
        master.addMasterElement(sideContentPlaceholder);
        
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
    
    // 页面尺寸相关方法
    public Dimension getSlideSize() { return new Dimension(slideSize); }
    public void setSlideSize(Dimension slideSize) { 
        this.slideSize = new Dimension(slideSize);
        updateHeaderFooter(); // 尺寸改变时更新页眉页脚位置
    }
    
    // 页眉页脚相关方法
    public boolean isShowHeader() { return showHeader; }
    public void setShowHeader(boolean showHeader) { 
        this.showHeader = showHeader;
        updateHeaderFooter();
    }
    
    public String getHeaderText() { return headerText; }
    public void setHeaderText(String headerText) { 
        this.headerText = headerText;
        updateHeaderFooter();
    }
    
    public boolean isShowFooter() { return showFooter; }
    public void setShowFooter(boolean showFooter) { 
        this.showFooter = showFooter;
        updateHeaderFooter();
    }
    
    public String getFooterText() { return footerText; }
    public void setFooterText(String footerText) { 
        this.footerText = footerText;
        updateHeaderFooter();
    }
    
    public boolean isShowPageNumber() { return showPageNumber; }
    public void setShowPageNumber(boolean showPageNumber) { 
        this.showPageNumber = showPageNumber;
        updateHeaderFooter();
    }
    
    public boolean isShowDateTime() { return showDateTime; }
    public void setShowDateTime(boolean showDateTime) { 
        this.showDateTime = showDateTime;
        updateHeaderFooter();
    }
    
    /**
     * 设置统一背景
     */
    public void setUnifiedBackground(Color color) {
        this.backgroundColor = color;
        this.backgroundImagePath = null;
    }
    
    public void setUnifiedBackground(String imagePath) {
        this.backgroundImagePath = imagePath;
    }
    
    /**
     * 应用统一页面设置到所有使用此母版的幻灯片
     */
    public void applyToAllSlides(java.util.List<Slide> slides) {
        if (slides != null) {
            slides.forEach(this::applyToSlide);
        }
    }
} 