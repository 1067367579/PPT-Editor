package com.ppteditor.ui;

import com.ppteditor.core.model.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 幻灯片母版设置对话框
 * 用于设置统一的页面大小、页眉页脚样式和背景
 */
public class SlideMasterSettingsDialog extends JDialog {
    
    private SlideMaster slideMaster;
    private Presentation presentation;
    private boolean confirmed = false;
    
    // 页面尺寸组件
    private JComboBox<String> sizeComboBox;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    
    // 背景设置组件
    private JPanel backgroundColorPanel;
    private Color selectedBackgroundColor;
    private JTextField backgroundImageField;
    
    // 页眉页脚组件
    private JCheckBox showHeaderCheckBox;
    private JTextField headerTextField;
    private JCheckBox showFooterCheckBox;
    private JTextField footerTextField;
    private JCheckBox showPageNumberCheckBox;
    private JCheckBox showDateTimeCheckBox;
    
    // 母版选择组件
    private JComboBox<SlideMaster> masterComboBox;
    
    public SlideMasterSettingsDialog(Frame parent, Presentation presentation) {
        super(parent, "母版设置", true);
        this.presentation = presentation;
        this.slideMaster = presentation.getSlideMaster() != null ? 
                          presentation.getSlideMaster() : SlideMaster.createDefaultMaster();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadCurrentSettings();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // 页面尺寸组件
        sizeComboBox = new JComboBox<>(new String[]{
            "标准16:9 (1280x720)",
            "全高清16:9 (1920x1080)", 
            "经典4:3 (1024x768)",
            "宽屏16:10 (1440x900)",
            "自定义"
        });
        
        widthSpinner = new JSpinner(new SpinnerNumberModel(1280, 400, 3840, 10));
        heightSpinner = new JSpinner(new SpinnerNumberModel(720, 300, 2160, 10));
        
        // 背景设置组件
        backgroundColorPanel = new JPanel();
        backgroundColorPanel.setPreferredSize(new Dimension(50, 30));
        backgroundColorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        backgroundColorPanel.setBackground(Color.WHITE);
        selectedBackgroundColor = Color.WHITE;
        
        backgroundImageField = new JTextField(20);
        
        // 页眉页脚组件
        showHeaderCheckBox = new JCheckBox("显示页眉");
        headerTextField = new JTextField(20);
        showFooterCheckBox = new JCheckBox("显示页脚");
        footerTextField = new JTextField(20);
        showPageNumberCheckBox = new JCheckBox("显示页码");
        showDateTimeCheckBox = new JCheckBox("显示日期时间");
        
        // 母版选择组件
        masterComboBox = new JComboBox<>();
        loadPredefinedMasters();
    }
    
    private void loadPredefinedMasters() {
        List<SlideMaster> masters = SlideMaster.getAllPredefinedMasters();
        for (SlideMaster master : masters) {
            masterComboBox.addItem(master);
        }
        masterComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SlideMaster) {
                    setText(((SlideMaster) value).getName());
                }
                return this;
            }
        });
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 母版选择区域
        JPanel masterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        masterPanel.setBorder(new TitledBorder("选择预定义母版"));
        masterPanel.add(new JLabel("母版模板："));
        masterPanel.add(masterComboBox);
        JButton applyMasterButton = new JButton("应用模板");
        masterPanel.add(applyMasterButton);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(masterPanel, gbc);
        
        // 页面尺寸设置区域
        JPanel sizePanel = new JPanel(new GridBagLayout());
        sizePanel.setBorder(new TitledBorder("页面尺寸设置"));
        GridBagConstraints sizeGbc = new GridBagConstraints();
        sizeGbc.insets = new Insets(3, 3, 3, 3);
        
        sizeGbc.gridx = 0; sizeGbc.gridy = 0;
        sizePanel.add(new JLabel("标准尺寸："), sizeGbc);
        sizeGbc.gridx = 1; sizeGbc.gridwidth = 2;
        sizePanel.add(sizeComboBox, sizeGbc);
        
        sizeGbc.gridx = 0; sizeGbc.gridy = 1; sizeGbc.gridwidth = 1;
        sizePanel.add(new JLabel("宽度："), sizeGbc);
        sizeGbc.gridx = 1;
        sizePanel.add(widthSpinner, sizeGbc);
        sizeGbc.gridx = 2;
        sizePanel.add(new JLabel("像素"), sizeGbc);
        
        sizeGbc.gridx = 0; sizeGbc.gridy = 2;
        sizePanel.add(new JLabel("高度："), sizeGbc);
        sizeGbc.gridx = 1;
        sizePanel.add(heightSpinner, sizeGbc);
        sizeGbc.gridx = 2;
        sizePanel.add(new JLabel("像素"), sizeGbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(sizePanel, gbc);
        
        // 背景设置区域
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBorder(new TitledBorder("背景设置"));
        GridBagConstraints bgGbc = new GridBagConstraints();
        bgGbc.insets = new Insets(3, 3, 3, 3);
        
        bgGbc.gridx = 0; bgGbc.gridy = 0;
        backgroundPanel.add(new JLabel("背景颜色："), bgGbc);
        bgGbc.gridx = 1;
        backgroundPanel.add(backgroundColorPanel, bgGbc);
        bgGbc.gridx = 2;
        JButton colorButton = new JButton("选择颜色");
        backgroundPanel.add(colorButton, bgGbc);
        
        bgGbc.gridx = 0; bgGbc.gridy = 1;
        backgroundPanel.add(new JLabel("背景图片："), bgGbc);
        bgGbc.gridx = 1; bgGbc.gridwidth = 2; bgGbc.fill = GridBagConstraints.HORIZONTAL;
        backgroundPanel.add(backgroundImageField, bgGbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(backgroundPanel, gbc);
        
        // 页眉页脚设置区域
        JPanel headerFooterPanel = new JPanel(new GridBagLayout());
        headerFooterPanel.setBorder(new TitledBorder("页眉页脚设置"));
        GridBagConstraints hfGbc = new GridBagConstraints();
        hfGbc.insets = new Insets(3, 3, 3, 3);
        hfGbc.anchor = GridBagConstraints.WEST;
        
        hfGbc.gridx = 0; hfGbc.gridy = 0;
        headerFooterPanel.add(showHeaderCheckBox, hfGbc);
        hfGbc.gridx = 1; hfGbc.fill = GridBagConstraints.HORIZONTAL; hfGbc.weightx = 1.0;
        headerFooterPanel.add(headerTextField, hfGbc);
        
        hfGbc.gridx = 0; hfGbc.gridy = 1; hfGbc.fill = GridBagConstraints.NONE; hfGbc.weightx = 0;
        headerFooterPanel.add(showFooterCheckBox, hfGbc);
        hfGbc.gridx = 1; hfGbc.fill = GridBagConstraints.HORIZONTAL; hfGbc.weightx = 1.0;
        headerFooterPanel.add(footerTextField, hfGbc);
        
        hfGbc.gridx = 0; hfGbc.gridy = 2; hfGbc.fill = GridBagConstraints.NONE; hfGbc.weightx = 0;
        headerFooterPanel.add(showPageNumberCheckBox, hfGbc);
        hfGbc.gridx = 1;
        headerFooterPanel.add(showDateTimeCheckBox, hfGbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(headerFooterPanel, gbc);
        
        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        JButton previewButton = new JButton("预览效果");
        
        buttonPanel.add(previewButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置事件处理
        applyMasterButton.addActionListener(e -> applySelectedMaster());
        colorButton.addActionListener(e -> chooseBackgroundColor());
        okButton.addActionListener(e -> confirmSettings());
        cancelButton.addActionListener(e -> dispose());
        previewButton.addActionListener(e -> previewMaster());
    }
    
    private void setupEventHandlers() {
        sizeComboBox.addActionListener(e -> {
            String selected = (String) sizeComboBox.getSelectedItem();
            if (selected.contains("1280x720")) {
                widthSpinner.setValue(1280);
                heightSpinner.setValue(720);
            } else if (selected.contains("1920x1080")) {
                widthSpinner.setValue(1920);
                heightSpinner.setValue(1080);
            } else if (selected.contains("1024x768")) {
                widthSpinner.setValue(1024);
                heightSpinner.setValue(768);
            } else if (selected.contains("1440x900")) {
                widthSpinner.setValue(1440);
                heightSpinner.setValue(900);
            }
        });
        
        showHeaderCheckBox.addActionListener(e -> headerTextField.setEnabled(showHeaderCheckBox.isSelected()));
        showFooterCheckBox.addActionListener(e -> footerTextField.setEnabled(showFooterCheckBox.isSelected()));
    }
    
    private void loadCurrentSettings() {
        // 加载当前母版设置
        Dimension size = slideMaster.getSlideSize();
        widthSpinner.setValue(size.width);
        heightSpinner.setValue(size.height);
        
        selectedBackgroundColor = slideMaster.getBackgroundColor();
        backgroundColorPanel.setBackground(selectedBackgroundColor);
        
        if (slideMaster.getBackgroundImagePath() != null) {
            backgroundImageField.setText(slideMaster.getBackgroundImagePath());
        }
        
        showHeaderCheckBox.setSelected(slideMaster.isShowHeader());
        headerTextField.setText(slideMaster.getHeaderText() != null ? slideMaster.getHeaderText() : "");
        headerTextField.setEnabled(slideMaster.isShowHeader());
        
        showFooterCheckBox.setSelected(slideMaster.isShowFooter());
        footerTextField.setText(slideMaster.getFooterText() != null ? slideMaster.getFooterText() : "");
        footerTextField.setEnabled(slideMaster.isShowFooter());
        
        showPageNumberCheckBox.setSelected(slideMaster.isShowPageNumber());
        showDateTimeCheckBox.setSelected(slideMaster.isShowDateTime());
    }
    
    private void applySelectedMaster() {
        SlideMaster selected = (SlideMaster) masterComboBox.getSelectedItem();
        if (selected != null) {
            this.slideMaster = selected.clone();
            loadCurrentSettings();
        }
    }
    
    private void chooseBackgroundColor() {
        Color color = JColorChooser.showDialog(this, "选择背景颜色", selectedBackgroundColor);
        if (color != null) {
            selectedBackgroundColor = color;
            backgroundColorPanel.setBackground(color);
        }
    }
    
    private void previewMaster() {
        // 应用当前设置到临时母版
        applySettingsToMaster();
        
        // 显示预览对话框
        PreviewDialog dialog = new PreviewDialog(this, slideMaster);
        dialog.setVisible(true);
    }
    
    private void confirmSettings() {
        applySettingsToMaster();
        confirmed = true;
        dispose();
    }
    
    private void applySettingsToMaster() {
        // 应用页面尺寸
        int width = (Integer) widthSpinner.getValue();
        int height = (Integer) heightSpinner.getValue();
        slideMaster.setSlideSize(new Dimension(width, height));
        
        // 应用背景设置
        slideMaster.setUnifiedBackground(selectedBackgroundColor);
        String imagePath = backgroundImageField.getText().trim();
        if (!imagePath.isEmpty()) {
            slideMaster.setUnifiedBackground(imagePath);
        }
        
        // 应用页眉页脚设置
        slideMaster.setShowHeader(showHeaderCheckBox.isSelected());
        slideMaster.setHeaderText(headerTextField.getText());
        slideMaster.setShowFooter(showFooterCheckBox.isSelected());
        slideMaster.setFooterText(footerTextField.getText());
        slideMaster.setShowPageNumber(showPageNumberCheckBox.isSelected());
        slideMaster.setShowDateTime(showDateTimeCheckBox.isSelected());
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public SlideMaster getSlideMaster() {
        return slideMaster;
    }
    
    // 预览对话框
    private class PreviewDialog extends JDialog {
        public PreviewDialog(Dialog parent, SlideMaster master) {
            super(parent, "母版预览", true);
            
            JPanel previewPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // 绘制背景
                    g2d.setColor(master.getBackgroundColor());
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // 简单的母版预览
                    g2d.setColor(Color.BLACK);
                    g2d.drawString("母版预览 - " + master.getName(), 20, 30);
                    
                    if (master.isShowHeader() && master.getHeaderText() != null) {
                        g2d.drawString("页眉: " + master.getHeaderText(), 20, 60);
                    }
                    
                    if (master.isShowFooter() && master.getFooterText() != null) {
                        g2d.drawString("页脚: " + master.getFooterText(), 20, getHeight() - 40);
                    }
                    
                    if (master.isShowPageNumber()) {
                        g2d.drawString("页码: 1", getWidth() - 50, getHeight() - 20);
                    }
                    
                    g2d.dispose();
                }
            };
            
            previewPanel.setPreferredSize(new Dimension(400, 300));
            previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
            
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dispose());
            
            setLayout(new BorderLayout());
            add(previewPanel, BorderLayout.CENTER);
            add(closeButton, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(parent);
        }
    }
} 