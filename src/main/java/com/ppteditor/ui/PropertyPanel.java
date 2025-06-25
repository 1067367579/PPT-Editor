package com.ppteditor.ui;

import com.ppteditor.core.model.*;
import com.ppteditor.core.enums.AnimationType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * 属性编辑面板
 * 用于编辑选中元素的属性
 */
public class PropertyPanel extends JPanel {
    
    private SlideElement<?> currentElement;
    private Set<SlideElement<?>> selectedElements;
    private Slide currentSlideContext; // Explicitly hold the slide context
    
    // 通用属性组件
    private JSpinner xSpinner;
    private JSpinner ySpinner;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    
    // 文本属性组件
    private JPanel textPropertiesPanel;
    private JTextField textField;
    private JComboBox<String> fontFamilyCombo;
    private JSpinner fontSizeSpinner;
    private JToggleButton boldButton;
    private JToggleButton italicButton;
    private JToggleButton underlineButton;
    private JButton textColorButton;
    private JToggleButton[] textAlignButtons;
    
    // 形状属性组件
    private JPanel shapePropertiesPanel;
    private JButton fillColorButton;
    private JButton borderColorButton;
    private JSpinner borderWidthSpinner;
    
    // 超链接组件
    private JTextField hyperlinkField;
    private JButton setHyperlinkButton;
    private JButton clearHyperlinkButton;
    
    // 对齐按钮
    private JPanel alignmentPanel;
    
    private Color currentTextColor = Color.BLACK;
    private Color currentFillColor = Color.LIGHT_GRAY;
    private Color currentBorderColor = Color.BLACK;
    
    // 标志位，用于防止UI更新时触发事件监听器
    private boolean isUpdatingUI = false;
    
    public PropertyPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(250, 400));
        setPreferredSize(new Dimension(280, 600));
        setBorder(BorderFactory.createTitledBorder("属性"));
        
        JPanel propertiesPanel = createPropertiesPanel();
        JScrollPane scrollPane = new JScrollPane(propertiesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createPropertiesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // 位置和大小
        panel.add(createPositionSizePanel());
        panel.add(Box.createVerticalStrut(10));
        
        // 文本属性
        textPropertiesPanel = createTextPropertiesPanel();
        panel.add(textPropertiesPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 形状属性
        shapePropertiesPanel = createShapePropertiesPanel();
        panel.add(shapePropertiesPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 对齐工具
        alignmentPanel = createAlignmentPanel();
        panel.add(alignmentPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 超链接面板
        panel.add(createHyperlinkPanel());
        panel.add(Box.createVerticalStrut(10));
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createPositionSizePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("位置和大小"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        
        // X坐标
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("X:"), gbc);
        gbc.gridx = 1;
        xSpinner = new JSpinner(new SpinnerNumberModel(0, -9999, 9999, 1));
        xSpinner.addChangeListener(e -> updateElementPosition());
        panel.add(xSpinner, gbc);
        
        // Y坐标
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 3;
        ySpinner = new JSpinner(new SpinnerNumberModel(0, -9999, 9999, 1));
        ySpinner.addChangeListener(e -> updateElementPosition());
        panel.add(ySpinner, gbc);
        
        // 宽度
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("宽:"), gbc);
        gbc.gridx = 1;
        widthSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 9999, 1));
        widthSpinner.addChangeListener(e -> updateElementSize());
        panel.add(widthSpinner, gbc);
        
        // 高度
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("高:"), gbc);
        gbc.gridx = 3;
        heightSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 9999, 1));
        heightSpinner.addChangeListener(e -> updateElementSize());
        panel.add(heightSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createTextPropertiesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("文本属性"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 文本内容
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panel.add(new JLabel("文本:"), gbc);
        
        gbc.gridy = 1;
        textField = new JTextField();
        textField.addActionListener(e -> updateTextContent());
        panel.add(textField, gbc);
        
        // 字体
        gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(new JLabel("字体:"), gbc);
        gbc.gridx = 2;
        fontFamilyCombo = new JComboBox<>(new String[]{"微软雅黑", "宋体", "黑体", "Times New Roman", "Arial"});
        fontFamilyCombo.addActionListener(e -> updateTextStyle());
        panel.add(fontFamilyCombo, gbc);
        
        // 字号
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("字号:"), gbc);
        gbc.gridx = 1;
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 6, 72, 1));
        fontSizeSpinner.addChangeListener(e -> updateTextStyle());
        panel.add(fontSizeSpinner, gbc);
        
        // 样式按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        
        boldButton = new JToggleButton("B");
        boldButton.setFont(boldButton.getFont().deriveFont(Font.BOLD));
        boldButton.addActionListener(e -> updateTextStyle());
        stylePanel.add(boldButton);
        
        italicButton = new JToggleButton("I");
        italicButton.setFont(italicButton.getFont().deriveFont(Font.ITALIC));
        italicButton.addActionListener(e -> updateTextStyle());
        stylePanel.add(italicButton);
        
        underlineButton = new JToggleButton("U");
        underlineButton.addActionListener(e -> updateTextStyle());
        stylePanel.add(underlineButton);
        
        textColorButton = new JButton("■");
        textColorButton.setForeground(currentTextColor);
        textColorButton.addActionListener(e -> chooseTextColor());
        stylePanel.add(textColorButton);
        
        panel.add(stylePanel, gbc);
        
        // 文本对齐按钮
        gbc.gridy = 5; gbc.gridwidth = 4;
        panel.add(new JLabel("文本对齐:"), gbc);
        
        gbc.gridy = 6;
        JPanel textAlignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        
        JToggleButton leftAlignButton = new JToggleButton("左");
        leftAlignButton.setToolTipText("左对齐");
        leftAlignButton.addActionListener(e -> setTextAlignment(TextStyle.ALIGN_LEFT, leftAlignButton));
        textAlignPanel.add(leftAlignButton);
        
        JToggleButton centerAlignButton = new JToggleButton("中");
        centerAlignButton.setToolTipText("居中对齐");
        centerAlignButton.addActionListener(e -> setTextAlignment(TextStyle.ALIGN_CENTER, centerAlignButton));
        textAlignPanel.add(centerAlignButton);
        
        JToggleButton rightAlignButton = new JToggleButton("右");
        rightAlignButton.setToolTipText("右对齐");
        rightAlignButton.addActionListener(e -> setTextAlignment(TextStyle.ALIGN_RIGHT, rightAlignButton));
        textAlignPanel.add(rightAlignButton);
        
        // 将对齐按钮保存为实例变量以便更新UI
        this.textAlignButtons = new JToggleButton[]{leftAlignButton, centerAlignButton, rightAlignButton};
        
        panel.add(textAlignPanel, gbc);
        
        return panel;
    }
    
    private JPanel createShapePropertiesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("形状属性"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 填充颜色
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("填充:"), gbc);
        gbc.gridx = 1;
        fillColorButton = new JButton("■");
        fillColorButton.setForeground(currentFillColor);
        fillColorButton.addActionListener(e -> chooseFillColor());
        panel.add(fillColorButton, gbc);
        
        // 边框颜色
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("边框:"), gbc);
        gbc.gridx = 1;
        borderColorButton = new JButton("■");
        borderColorButton.setForeground(currentBorderColor);
        borderColorButton.addActionListener(e -> chooseBorderColor());
        panel.add(borderColorButton, gbc);
        
        // 边框宽度
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("宽度:"), gbc);
        gbc.gridx = 1;
        borderWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 10.0, 0.5));
        borderWidthSpinner.addChangeListener(e -> updateShapeStyle());
        panel.add(borderWidthSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createAlignmentPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 2, 2));
        panel.setBorder(new TitledBorder("对齐"));
        
        JButton leftButton = new JButton("左对齐");
        leftButton.addActionListener(e -> alignLeft());
        panel.add(leftButton);
        
        JButton centerHButton = new JButton("水平居中");
        centerHButton.addActionListener(e -> alignCenterHorizontal());
        panel.add(centerHButton);
        
        JButton rightButton = new JButton("右对齐");
        rightButton.addActionListener(e -> alignRight());
        panel.add(rightButton);
        
        JButton topButton = new JButton("顶对齐");
        topButton.addActionListener(e -> alignTop());
        panel.add(topButton);
        
        JButton centerVButton = new JButton("垂直居中");
        centerVButton.addActionListener(e -> alignCenterVertical());
        panel.add(centerVButton);
        
        JButton bottomButton = new JButton("底对齐");
        bottomButton.addActionListener(e -> alignBottom());
        panel.add(bottomButton);
        
        return panel;
    }
    
    private JPanel createHyperlinkPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("超链接"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 超链接类型选择
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("链接类型:"), gbc);
        
        gbc.gridy = 1;
        JComboBox<String> linkTypeCombo = new JComboBox<>(new String[]{
            "网页链接", "文件链接", "页面跳转", "邮箱链接"
        });
        linkTypeCombo.addActionListener(e -> updateHyperlinkPlaceholder(linkTypeCombo));
        panel.add(linkTypeCombo, gbc);
        
        // 超链接输入框
        gbc.gridy = 2;
        panel.add(new JLabel("链接地址:"), gbc);
        
        gbc.gridy = 3;
        hyperlinkField = new JTextField();
        hyperlinkField.setToolTipText("输入超链接地址");
        panel.add(hyperlinkField, gbc);
        
        // 快捷按钮面板
        gbc.gridy = 4; gbc.gridwidth = 1;
        JButton browseFileButton = new JButton("浏览文件");
        browseFileButton.addActionListener(e -> browseForFile());
        panel.add(browseFileButton, gbc);
        
        gbc.gridx = 1;
        JButton selectSlideButton = new JButton("选择页面");
        selectSlideButton.addActionListener(e -> selectSlideForLink());
        panel.add(selectSlideButton, gbc);
        
        // 设置和清除按钮
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 1;
        setHyperlinkButton = new JButton("设置");
        setHyperlinkButton.addActionListener(e -> setHyperlink());
        panel.add(setHyperlinkButton, gbc);
        
        gbc.gridx = 1;
        clearHyperlinkButton = new JButton("清除");
        clearHyperlinkButton.addActionListener(e -> clearHyperlink());
        panel.add(clearHyperlinkButton, gbc);
        
        // 添加文本选择超链接按钮
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton selectTextButton = new JButton("为选中文字设置超链接");
        selectTextButton.addActionListener(e -> openTextSelectionDialog());
        panel.add(selectTextButton, gbc);
        
        // 初始化提示文本
        updateHyperlinkPlaceholder(linkTypeCombo);
        
        return panel;
    }
    
    private void updateHyperlinkPlaceholder(JComboBox<String> linkTypeCombo) {
        String selectedType = (String) linkTypeCombo.getSelectedItem();
        switch (selectedType) {
            case "网页链接":
                hyperlinkField.setToolTipText("例如: http://www.example.com 或 https://www.baidu.com");
                break;
            case "文件链接":
                hyperlinkField.setToolTipText("例如: file:///Users/Documents/file.pdf 或 C:\\Documents\\file.pdf");
                break;
            case "页面跳转":
                hyperlinkField.setToolTipText("例如: slide:3 (跳转到第3页) 或 slide:first, slide:last");
                break;
            case "邮箱链接":
                hyperlinkField.setToolTipText("例如: mailto:example@email.com");
                break;
        }
    }
    
    private void browseForFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择文件");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            hyperlinkField.setText("file://" + filePath);
        }
    }
    
    private void selectSlideForLink() {
        // 这里需要获取当前演示文稿的幻灯片列表
        // 简化实现：让用户输入页面编号
        String input = JOptionPane.showInputDialog(this, 
            "请输入要跳转的页面编号\n(1-N 或 'first'/'last'):", 
            "选择页面", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            input = input.trim().toLowerCase();
            if (input.equals("first") || input.equals("last")) {
                hyperlinkField.setText("slide:" + input);
            } else {
                try {
                    int slideNumber = Integer.parseInt(input);
                    if (slideNumber > 0) {
                        hyperlinkField.setText("slide:" + slideNumber);
                    } else {
                        JOptionPane.showMessageDialog(this, "页面编号必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "请输入有效的页面编号", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public void setSlideContext(Slide slide) {
        this.currentSlideContext = slide;
        // If no element is selected, update the UI for the new slide context
        if (selectedElements == null || selectedElements.isEmpty()) {
            updateUIForSlide();
        }
    }
    
    public void setSelectedElements(Set<SlideElement<?>> elements) {
        // Guard clause: Do not proceed if UI is not yet initialized
        if (xSpinner == null) return;

        this.selectedElements = elements;
        if (elements == null || elements.isEmpty()) {
            // No elements selected, show properties for the current slide
            updateUIForSlide();
        } else if (elements.size() == 1) {
            // Single element selected
            this.currentElement = elements.iterator().next();
            updatePropertyUI();
        } else {
            // Multiple elements selected
            this.currentElement = null;
            updateUIForMultipleSelection();
        }
    }
    
    private void updateUIForSlide() {
        isUpdatingUI = true;
        setComponentsEnabled(false);
        textPropertiesPanel.setVisible(false);
        shapePropertiesPanel.setVisible(false);
        isUpdatingUI = false;
    }
    
    private void updatePropertyUI() {
        if (currentElement == null) {
            setComponentsEnabled(false);
            return;
        }
        
        // 设置标志位，防止更新UI时触发回调
        isUpdatingUI = true;
        
        // 更新位置和大小
        xSpinner.setValue((int) currentElement.getX());
        ySpinner.setValue((int) currentElement.getY());
        widthSpinner.setValue((int) currentElement.getWidth());
        heightSpinner.setValue((int) currentElement.getHeight());
        
        // 显示/隐藏特定属性面板
        textPropertiesPanel.setVisible(currentElement instanceof TextElement);
        shapePropertiesPanel.setVisible(currentElement instanceof RectangleElement || 
                                       currentElement instanceof EllipseElement);
        
        // 更新文本属性
        if (currentElement instanceof TextElement) {
            TextElement textElement = (TextElement) currentElement;
            textField.setText(textElement.getText());
            
            TextStyle style = textElement.getStyle();
            if (style != null) {
                fontFamilyCombo.setSelectedItem(style.getFontFamily());
                fontSizeSpinner.setValue(style.getFontSize());
                boldButton.setSelected(style.isBold());
                italicButton.setSelected(style.isItalic());
                underlineButton.setSelected(style.isUnderline());
                
                currentTextColor = style.getTextColor();
                if (currentTextColor == null) currentTextColor = Color.BLACK;
                textColorButton.setForeground(currentTextColor);
                
                // 更新文本对齐按钮状态
                if (textAlignButtons != null) {
                    int alignment = style.getAlignment();
                    for (int i = 0; i < textAlignButtons.length; i++) {
                        textAlignButtons[i].setSelected(i == alignment);
                    }
                }
            }
        }
        
        // 更新超链接
        if (hyperlinkField != null) {
            String hyperlink = currentElement.getHyperlink();
            hyperlinkField.setText(hyperlink != null ? hyperlink : "");
        }
        
        // 更新形状属性
        if (currentElement.getStyle() instanceof ShapeStyle) {
            ShapeStyle style = (ShapeStyle) currentElement.getStyle();
            
            currentFillColor = style.getEffectiveFillColor();
            fillColorButton.setForeground(currentFillColor);
            
            currentBorderColor = style.getEffectiveBorderColor();
            borderColorButton.setForeground(currentBorderColor);
            
            borderWidthSpinner.setValue(style.getBorderWidth());
        }
        
        revalidate();
        repaint();
        
        // 清除标志位
        isUpdatingUI = false;
    }
    
    private void updateUIForMultipleSelection() {
        // 多选时显示通用属性
        textPropertiesPanel.setVisible(false);
        shapePropertiesPanel.setVisible(false);
        
        // 设置标志位，防止更新UI时触发回调
        isUpdatingUI = true;
        
        // 计算多个元素的平均位置和大小
        if (selectedElements != null && !selectedElements.isEmpty()) {
            double avgX = selectedElements.stream().mapToDouble(SlideElement::getX).average().orElse(0);
            double avgY = selectedElements.stream().mapToDouble(SlideElement::getY).average().orElse(0);
            double avgWidth = selectedElements.stream().mapToDouble(SlideElement::getWidth).average().orElse(0);
            double avgHeight = selectedElements.stream().mapToDouble(SlideElement::getHeight).average().orElse(0);
            
            xSpinner.setValue((int) avgX);
            ySpinner.setValue((int) avgY);
            widthSpinner.setValue((int) avgWidth);
            heightSpinner.setValue((int) avgHeight);
        }
        
        revalidate();
        repaint();
        
        // 清除标志位
        isUpdatingUI = false;
    }
    
    // 回调接口 - 父组件设置
    private Runnable onElementChanged;
    
    public void setOnElementChanged(Runnable callback) {
        this.onElementChanged = callback;
    }
    
    private void notifyElementChanged() {
        if (onElementChanged != null) {
            onElementChanged.run();
        }
    }
    
    // 更新方法
    private void updateElementPosition() {
        if (isUpdatingUI) return; // 防止循环更新
        
        if (currentElement != null) {
            // 单选元素
            double x = (Integer) xSpinner.getValue();
            double y = (Integer) ySpinner.getValue();
            currentElement.setPosition(x, y);
            notifyElementChanged();
        } else if (selectedElements != null && !selectedElements.isEmpty()) {
            // 多选元素：相对移动
            double newX = (Integer) xSpinner.getValue();
            double newY = (Integer) ySpinner.getValue();
            
            // 计算当前平均位置
            double currentAvgX = selectedElements.stream().mapToDouble(SlideElement::getX).average().orElse(0);
            double currentAvgY = selectedElements.stream().mapToDouble(SlideElement::getY).average().orElse(0);
            
            // 计算偏移量
            double deltaX = newX - currentAvgX;
            double deltaY = newY - currentAvgY;
            
            // 应用到所有选中元素
            for (SlideElement<?> element : selectedElements) {
                element.move(deltaX, deltaY);
            }
            notifyElementChanged();
        }
    }
    
    private void updateElementSize() {
        if (isUpdatingUI) return; // 防止循环更新
        
        if (currentElement != null) {
            // 单选元素
            double width = (Integer) widthSpinner.getValue();
            double height = (Integer) heightSpinner.getValue();
            currentElement.setSize(width, height);
            notifyElementChanged();
        } else if (selectedElements != null && !selectedElements.isEmpty()) {
            // 多选元素：按比例缩放
            double newWidth = (Integer) widthSpinner.getValue();
            double newHeight = (Integer) heightSpinner.getValue();
            
            // 计算当前平均大小
            double currentAvgWidth = selectedElements.stream().mapToDouble(SlideElement::getWidth).average().orElse(1);
            double currentAvgHeight = selectedElements.stream().mapToDouble(SlideElement::getHeight).average().orElse(1);
            
            // 计算缩放比例
            double scaleX = newWidth / currentAvgWidth;
            double scaleY = newHeight / currentAvgHeight;
            
            // 应用到所有选中元素
            for (SlideElement<?> element : selectedElements) {
                element.setSize(element.getWidth() * scaleX, element.getHeight() * scaleY);
            }
            notifyElementChanged();
        }
    }
    
    private void updateTextContent() {
        if (currentElement instanceof TextElement) {
            TextElement textElement = (TextElement) currentElement;
            textElement.setText(textField.getText());
            notifyElementChanged();
        }
    }
    
    private void updateTextStyle() {
        if (currentElement instanceof TextElement) {
            TextElement textElement = (TextElement) currentElement;
            TextStyle style = textElement.getStyle();
            
            style.setFontFamily((String) fontFamilyCombo.getSelectedItem());
            style.setFontSize((Integer) fontSizeSpinner.getValue());
            style.setBold(boldButton.isSelected());
            style.setItalic(italicButton.isSelected());
            style.setUnderline(underlineButton.isSelected());
            style.setTextColor(currentTextColor);
            
            notifyElementChanged();
        }
    }
    
    private void setTextAlignment(int alignment, JToggleButton sourceButton) {
        if (currentElement instanceof TextElement) {
            TextElement textElement = (TextElement) currentElement;
            TextStyle style = textElement.getStyle();
            
            // 更新样式中的对齐方式
            style.setAlignment(alignment);
            
            // 更新按钮状态 - 只有被点击的按钮保持选中状态
            for (JToggleButton button : textAlignButtons) {
                button.setSelected(button == sourceButton);
            }
            
            notifyElementChanged();
        }
    }
    
    private void updateShapeStyle() {
        if (currentElement != null && currentElement.getStyle() instanceof ShapeStyle) {
            ShapeStyle style = (ShapeStyle) currentElement.getStyle();
            
            style.setFillColor(currentFillColor);
            style.setBorderColor(currentBorderColor);
            style.setBorderWidth((Float) borderWidthSpinner.getValue());
            
            notifyElementChanged();
        }
    }
    
    private void chooseTextColor() {
        Color color = JColorChooser.showDialog(this, "选择文本颜色", currentTextColor);
        if (color != null) {
            currentTextColor = color;
            textColorButton.setForeground(color);
            updateTextStyle();
        }
    }
    
    private void chooseFillColor() {
        Color color = JColorChooser.showDialog(this, "选择填充颜色", currentFillColor);
        if (color != null) {
            currentFillColor = color;
            fillColorButton.setForeground(color);
            updateShapeStyle();
        }
    }
    
    private void chooseBorderColor() {
        Color color = JColorChooser.showDialog(this, "选择边框颜色", currentBorderColor);
        if (color != null) {
            currentBorderColor = color;
            borderColorButton.setForeground(color);
            updateShapeStyle();
        }
    }
    
    private void setHyperlink() {
        if (currentElement != null) {
            String hyperlink = hyperlinkField.getText().trim();
            if (!hyperlink.isEmpty()) {
                // 简单的URL格式验证
                if (!hyperlink.startsWith("http://") && !hyperlink.startsWith("https://") && 
                    !hyperlink.startsWith("mailto:") && !hyperlink.startsWith("file://")) {
                    hyperlink = "http://" + hyperlink;
                }
                currentElement.setHyperlink(hyperlink);
                notifyElementChanged();
                updateStatus("超链接已设置: " + hyperlink);
            }
        }
    }
    
    private void clearHyperlink() {
        if (currentElement != null) {
            currentElement.setHyperlink(null);
            hyperlinkField.setText("");
            notifyElementChanged();
            updateStatus("超链接已清除");
        }
    }
    
    private void updateStatus(String message) {
        // 这个方法可以发送状态到主窗口
        System.out.println("状态: " + message);
    }
    
    // 对齐操作回调接口
    private Runnable onAlignLeft, onAlignRight, onAlignTop, onAlignBottom, 
                     onAlignCenterHorizontal, onAlignCenterVertical;
    
    public void setAlignmentCallbacks(Runnable left, Runnable right, Runnable top, 
                                     Runnable bottom, Runnable centerH, Runnable centerV) {
        this.onAlignLeft = left;
        this.onAlignRight = right;
        this.onAlignTop = top;
        this.onAlignBottom = bottom;
        this.onAlignCenterHorizontal = centerH;
        this.onAlignCenterVertical = centerV;
    }
    
    private void alignLeft() { if (onAlignLeft != null) onAlignLeft.run(); }
    private void alignRight() { if (onAlignRight != null) onAlignRight.run(); }
    private void alignTop() { if (onAlignTop != null) onAlignTop.run(); }
    private void alignBottom() { if (onAlignBottom != null) onAlignBottom.run(); }
    private void alignCenterHorizontal() { if (onAlignCenterHorizontal != null) onAlignCenterHorizontal.run(); }
    private void alignCenterVertical() { if (onAlignCenterVertical != null) onAlignCenterVertical.run(); }
    
    private void setComponentsEnabled(boolean enabled) {
        // Position and size are always available
        xSpinner.setEnabled(enabled);
        ySpinner.setEnabled(enabled);
        widthSpinner.setEnabled(enabled);
        heightSpinner.setEnabled(enabled);

        // Check if other panels are initialized before using them
        if (textPropertiesPanel != null) {
            textField.setEnabled(enabled);
            fontFamilyCombo.setEnabled(enabled);
            fontSizeSpinner.setEnabled(enabled);
            boldButton.setEnabled(enabled);
            italicButton.setEnabled(enabled);
            underlineButton.setEnabled(enabled);
            textColorButton.setEnabled(enabled);
            for (JToggleButton button : textAlignButtons) {
                button.setEnabled(enabled);
            }
        }
        
        if (shapePropertiesPanel != null) {
            fillColorButton.setEnabled(enabled);
            borderColorButton.setEnabled(enabled);
            borderWidthSpinner.setEnabled(enabled);
        }

        if (hyperlinkField != null) {
            hyperlinkField.setEnabled(enabled);
            setHyperlinkButton.setEnabled(enabled);
            clearHyperlinkButton.setEnabled(enabled);
        }

        if (alignmentPanel != null) {
             for (Component comp : alignmentPanel.getComponents()) {
                 if (comp instanceof JButton) {
                     comp.setEnabled(enabled);
                 }
             }
        }
    }
    
    /**
     * 打开文本选择对话框，为部分文字设置超链接
     */
    private void openTextSelectionDialog() {
        if (!(currentElement instanceof TextElement)) {
            JOptionPane.showMessageDialog(this, "请先选择一个文本元素", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        TextElement textElement = (TextElement) currentElement;
        String text = textElement.getText();
        
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "文本元素为空", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 创建对话框
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "设置文字超链接", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        // 文本显示区域
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("选择要设置超链接的文字"));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // 控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 选择范围输入
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("起始位置:"), gbc);
        
        gbc.gridx = 1;
        JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(0, 0, text.length(), 1));
        startSpinner.setPreferredSize(new Dimension(80, 25));
        controlPanel.add(startSpinner, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(new JLabel("结束位置:"), gbc);
        
        gbc.gridx = 3;
        JSpinner endSpinner = new JSpinner(new SpinnerNumberModel(text.length(), 0, text.length(), 1));
        endSpinner.setPreferredSize(new Dimension(80, 25));
        controlPanel.add(endSpinner, gbc);
        
        // 超链接输入
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("超链接:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField linkField = new JTextField();
        linkField.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(linkField, gbc);
        
        // 按钮面板
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            try {
                int start = (Integer) startSpinner.getValue();
                int end = (Integer) endSpinner.getValue();
                String link = linkField.getText().trim();
                
                if (start >= end) {
                    JOptionPane.showMessageDialog(dialog, "起始位置必须小于结束位置", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (link.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请输入超链接地址", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 设置部分文字超链接
                textElement.setHyperlinkForSelection(start, end, link);
                
                updateStatus("已为文字 \"" + text.substring(start, end) + "\" 设置超链接: " + link);
                notifyElementChanged();
                dialog.dispose();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "设置失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        controlPanel.add(buttonPanel, gbc);
        
        dialog.add(controlPanel, BorderLayout.SOUTH);
        
        // 添加文本选择功能
        textArea.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();
                if (start != end) {
                    startSpinner.setValue(start);
                    endSpinner.setValue(end);
                }
            }
        });
        
        dialog.setVisible(true);
    }
} 