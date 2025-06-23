package com.ppteditor.ui;

import com.ppteditor.core.enums.ElementType;
import com.ppteditor.core.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 模版库面板
 * 提供预定义的图形元素和模板供用户拖拽使用
 */
public class TemplatePanel extends JPanel {
    
    private JList<TemplateItem> templateList;
    private DefaultListModel<TemplateItem> listModel;
    private List<TemplateItem> templates;
    private JTabbedPane tabbedPane;
    private MainWindow mainWindow;
    
    public TemplatePanel() {
        initializeTemplates();
        initializeUI();
        setupDragAndDrop();
    }
    
    public TemplatePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializeTemplates();
        initializeUI();
        setupDragAndDrop();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("设计与模版"));
        setMinimumSize(new Dimension(180, 300));
        setPreferredSize(new Dimension(200, 400));
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 1. 图形模板选项卡
        JPanel templatePanel = createTemplatePanel();
        tabbedPane.addTab("图形", templatePanel);
        
        // 2. 配色方案选项卡
        JPanel colorThemePanel = createColorThemePanel();
        tabbedPane.addTab("配色", colorThemePanel);
        
        // 3. 幻灯片母版选项卡
        JPanel masterPanel = createMasterPanel();
        tabbedPane.addTab("母版", masterPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTemplatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        listModel = new DefaultListModel<>();
        templateList = new JList<>(listModel);
        templateList.setCellRenderer(new TemplateCellRenderer());
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setLayoutOrientation(JList.VERTICAL);
        templateList.setDragEnabled(true);
        
        // 添加模板到列表
        for (TemplateItem template : templates) {
            listModel.addElement(template);
        }
        
        JScrollPane scrollPane = new JScrollPane(templateList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加说明标签
        JLabel instructionLabel = new JLabel("<html><center>拖拽模板到画布<br/>创建元素</center></html>");
        instructionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        instructionLabel.setHorizontalAlignment(JLabel.CENTER);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(instructionLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createColorThemePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 获取所有预定义配色方案
        java.util.List<ColorTheme> themes = ColorTheme.getAllPredefinedThemes();
        
        JPanel themesPanel = new JPanel();
        themesPanel.setLayout(new BoxLayout(themesPanel, BoxLayout.Y_AXIS));
        
        for (ColorTheme theme : themes) {
            JPanel themeItem = createThemeItem(theme);
            themesPanel.add(themeItem);
            themesPanel.add(Box.createVerticalStrut(5));
        }
        
        // 添加随机配色按钮
        JButton randomButton = new JButton("随机配色");
        randomButton.addActionListener(e -> {
            if (mainWindow != null) {
                ColorTheme randomTheme = ColorTheme.generateRandomTheme();
                applyColorTheme(randomTheme);
            }
        });
        themesPanel.add(randomButton);
        
        JScrollPane scrollPane = new JScrollPane(themesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createThemeItem(ColorTheme theme) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // 主题名称
        JLabel nameLabel = new JLabel(theme.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        item.add(nameLabel, BorderLayout.NORTH);
        
        // 颜色预览
        JPanel colorPreview = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        Color[] colors = {theme.getPrimaryColor(), theme.getSecondaryColor(), 
                         theme.getAccentColor(), theme.getTextColor()};
        
        for (Color color : colors) {
            JPanel colorSquare = new JPanel();
            colorSquare.setBackground(color);
            colorSquare.setPreferredSize(new Dimension(15, 15));
            colorSquare.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            colorPreview.add(colorSquare);
        }
        
        item.add(colorPreview, BorderLayout.CENTER);
        
        // 点击应用主题
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                applyColorTheme(theme);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(240, 240, 240));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });
        
        return item;
    }
    
    private JPanel createMasterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 获取所有预定义母版
        java.util.List<SlideMaster> masters = SlideMaster.getAllPredefinedMasters();
        
        JPanel mastersPanel = new JPanel();
        mastersPanel.setLayout(new BoxLayout(mastersPanel, BoxLayout.Y_AXIS));
        
        for (SlideMaster master : masters) {
            JPanel masterItem = createMasterItem(master);
            mastersPanel.add(masterItem);
            mastersPanel.add(Box.createVerticalStrut(5));
        }
        
        JScrollPane scrollPane = new JScrollPane(mastersPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMasterItem(SlideMaster master) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // 母版名称和描述
        JLabel nameLabel = new JLabel(master.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        JLabel descLabel = new JLabel("<html>幻灯片母版样式</html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        descLabel.setForeground(Color.GRAY);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);
        
        item.add(textPanel, BorderLayout.CENTER);
        
        // 预览图标
        JPanel previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(40, 30));
        previewPanel.setBackground(master.getBackgroundColor());
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        item.add(previewPanel, BorderLayout.EAST);
        
        // 点击应用母版
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                applySlideMaster(master);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(240, 240, 240));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });
        
        return item;
    }
    
    private void applyColorTheme(ColorTheme theme) {
        if (mainWindow != null && mainWindow.getCurrentPresentation() != null) {
            Presentation presentation = mainWindow.getCurrentPresentation();
            
            // 输出配色详细信息
            System.out.println("=== 应用配色方案 ===");
            System.out.println("主题名称: " + theme.getName());
            System.out.println("主要颜色: " + colorToString(theme.getPrimaryColor()));
            System.out.println("次要颜色: " + colorToString(theme.getSecondaryColor()));
            System.out.println("背景颜色: " + colorToString(theme.getBackgroundColor()));
            System.out.println("文本颜色: " + colorToString(theme.getTextColor()));
            
            // 使用applyColorTheme方法，这会实际应用颜色到所有元素
            presentation.applyColorTheme(theme);
            
            // 统计应用的元素数量
            int totalElements = 0;
            for (Slide slide : presentation.getSlides()) {
                totalElements += slide.getElements().size();
            }
            
            // 强制刷新界面
            mainWindow.refreshCanvas();
            
            // 如果有属性面板，也需要刷新
            if (mainWindow.getSlideCanvas() != null) {
                mainWindow.getSlideCanvas().repaint();
                mainWindow.getSlideCanvas().revalidate();
            }
            
            String message = String.format("已应用配色方案: %s (影响 %d 个元素)", 
                theme.getName(), totalElements);
            System.out.println(message);
            mainWindow.updateStatus(message);
        }
    }
    
    /**
     * 将颜色转换为可读字符串
     */
    private String colorToString(Color color) {
        if (color == null) return "null";
        return String.format("RGB(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    private void applySlideMaster(SlideMaster master) {
        if (mainWindow != null && mainWindow.getCurrentPresentation() != null) {
            Slide currentSlide = mainWindow.getCurrentPresentation().getCurrentSlide();
            if (currentSlide != null) {
                // 应用母版到当前幻灯片（会自动清除之前的母版元素）
                master.applyToSlide(currentSlide);
                mainWindow.refreshCanvas();
                
                // 提供用户反馈
                String message = "已应用幻灯片母版: " + master.getName() + "（已清除之前的母版内容）";
                System.out.println(message);
                
                // 可选：显示状态提示
                if (mainWindow != null) {
                    mainWindow.updateStatus(message);
                }
            }
        }
    }
    
    private void initializeTemplates() {
        templates = new ArrayList<>();
        
        // 基本图形模板
        templates.add(new TemplateItem("矩形", ElementType.RECTANGLE, 
            "基本矩形图形", createRectangleTemplate()));
        templates.add(new TemplateItem("椭圆", ElementType.ELLIPSE, 
            "基本椭圆图形", createEllipseTemplate()));
        
        // 文本模板
        templates.add(new TemplateItem("标题文本", ElementType.TEXT, 
            "大号居中标题", createTitleTextTemplate()));
        templates.add(new TemplateItem("正文文本", ElementType.TEXT, 
            "左对齐段落文本", createBodyTextTemplate()));
        templates.add(new TemplateItem("普通文本", ElementType.TEXT, 
            "基础文本框", createTextTemplate()));
        
        // 样式化模板
        templates.add(new TemplateItem("彩色矩形", ElementType.RECTANGLE, 
            "带颜色的矩形", createColoredRectangleTemplate()));
        templates.add(new TemplateItem("彩色椭圆", ElementType.ELLIPSE, 
            "带颜色的椭圆", createColoredEllipseTemplate()));
            
        // 组合模板
        templates.add(new TemplateItem("文本框+边框", ElementType.TEXT, 
            "带边框的文本框", createBorderedTextTemplate()));
    }
    
    private SlideElement<?> createTextTemplate() {
        TextElement element = TextElement.createBodyText("文本内容");
        element.setPosition(50, 50);
        element.setSize(150, 40);
        return element;
    }
    
    private SlideElement<?> createRectangleTemplate() {
        RectangleElement element = new RectangleElement();
        element.setPosition(50, 50);
        element.setSize(100, 60);
        return element;
    }
    
    private SlideElement<?> createEllipseTemplate() {
        EllipseElement element = new EllipseElement();
        element.setPosition(50, 50);
        element.setSize(100, 60);
        return element;
    }
    
    private SlideElement<?> createTitleTextTemplate() {
        TextElement element = TextElement.createTitle("标题文本");
        element.setPosition(50, 50);
        element.setSize(200, 50);
        
        TextStyle style = element.getStyle();
        style.setFontSize(24);
        style.setTextColor(new Color(44, 62, 80));
        style.setBold(true);
        
        return element;
    }
    
    private SlideElement<?> createBodyTextTemplate() {
        TextElement element = TextElement.createBodyText("这是正文内容的示例文本，\n支持多行段落显示，\n左对齐排版，适合长文本展示。");
        element.setPosition(50, 50);
        element.setSize(300, 100);
        
        TextStyle style = element.getStyle();
        style.setFontSize(14);
        style.setTextColor(new Color(52, 73, 94));
        style.setLineSpacing(1.2); // 行间距稍大一些
        style.setLeftAlign(); // 确保左对齐
        
        return element;
    }
    
    private SlideElement<?> createColoredRectangleTemplate() {
        RectangleElement element = new RectangleElement();
        element.setPosition(50, 50);
        element.setSize(120, 80);
        
        ShapeStyle style = element.getStyle();
        style.setFillColor(new Color(52, 152, 219));
        style.setBorderColor(new Color(41, 128, 185));
        style.setBorderWidth(2);
        
        return element;
    }
    
    private SlideElement<?> createColoredEllipseTemplate() {
        EllipseElement element = new EllipseElement();
        element.setPosition(50, 50);
        element.setSize(120, 80);
        
        ShapeStyle style = element.getStyle();
        style.setFillColor(new Color(46, 204, 113));
        style.setBorderColor(new Color(39, 174, 96));
        style.setBorderWidth(2);
        
        return element;
    }
    
    private SlideElement<?> createBorderedTextTemplate() {
        TextElement element = TextElement.createBodyText("边框文本");
        element.setPosition(50, 50);
        element.setSize(120, 40);
        
        TextStyle style = element.getStyle();
        // 注意：TextStyle可能没有边框属性，这里只设置背景色
        style.setBackgroundColor(new Color(236, 240, 241));
        
        return element;
    }
    
    private void setupDragAndDrop() {
        templateList.setTransferHandler(new TemplateTransferHandler());
        templateList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = templateList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    templateList.setSelectedIndex(index);
                }
            }
        });
    }
    
    /**
     * 模板项数据类
     */
    public static class TemplateItem implements Serializable {
        private String name;
        private ElementType type;
        private String description;
        private SlideElement<?> templateElement;
        
        public TemplateItem(String name, ElementType type, String description, SlideElement<?> templateElement) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.templateElement = templateElement;
        }
        
        public String getName() { return name; }
        public ElementType getType() { return type; }
        public String getDescription() { return description; }
        public SlideElement<?> getTemplateElement() { return templateElement; }
        
        public SlideElement<?> createInstance() {
            return templateElement.clone();
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * 模板传输处理器，支持拖拽
     */
    private class TemplateTransferHandler extends TransferHandler {
        
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            JList<?> list = (JList<?>) c;
            TemplateItem selectedTemplate = (TemplateItem) list.getSelectedValue();
            if (selectedTemplate != null) {
                return new TemplateTransferable(selectedTemplate);
            }
            return null;
        }
    }
    
    /**
     * 模板可传输对象
     */
    public static class TemplateTransferable implements Transferable {
        
        public static final DataFlavor TEMPLATE_FLAVOR = 
            new DataFlavor(TemplateItem.class, "Template Item");
        
        private TemplateItem templateItem;
        
        public TemplateTransferable(TemplateItem templateItem) {
            this.templateItem = templateItem;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TEMPLATE_FLAVOR};
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return TEMPLATE_FLAVOR.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return templateItem;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    /**
     * 模板列表渲染器
     */
    private class TemplateCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof TemplateItem) {
                TemplateItem template = (TemplateItem) value;
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(true);
                panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                
                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                } else {
                    panel.setBackground(list.getBackground());
                }
                
                // 图标
                JLabel iconLabel = new JLabel(getTemplateIcon(template.getType()));
                iconLabel.setPreferredSize(new Dimension(24, 24));
                
                // 文本信息
                JPanel textPanel = new JPanel(new BorderLayout());
                textPanel.setOpaque(false);
                
                JLabel nameLabel = new JLabel(template.getName());
                nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
                nameLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                
                JLabel descLabel = new JLabel(template.getDescription());
                descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 9));
                descLabel.setForeground(isSelected ? 
                    list.getSelectionForeground() : Color.GRAY);
                
                textPanel.add(nameLabel, BorderLayout.NORTH);
                textPanel.add(descLabel, BorderLayout.CENTER);
                
                panel.add(iconLabel, BorderLayout.WEST);
                panel.add(textPanel, BorderLayout.CENTER);
                
                return panel;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        
        private Icon getTemplateIcon(ElementType type) {
            // 创建简单的图标
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    switch (type) {
                        case TEXT:
                            g2d.setColor(Color.BLUE);
                            g2d.setFont(new Font("Arial", Font.BOLD, 10));
                            g2d.drawString("T", x + 8, y + 15);
                            break;
                        case RECTANGLE:
                            g2d.setColor(Color.GREEN);
                            g2d.fillRect(x + 2, y + 2, 20, 15);
                            break;
                        case ELLIPSE:
                            g2d.setColor(Color.ORANGE);
                            g2d.fillOval(x + 2, y + 2, 20, 15);
                            break;
                        default:
                            g2d.setColor(Color.GRAY);
                            g2d.fillRect(x + 2, y + 2, 20, 15);
                            break;
                    }
                    g2d.dispose();
                }
                
                @Override
                public int getIconWidth() { return 24; }
                
                @Override
                public int getIconHeight() { return 18; }
            };
        }
    }
} 