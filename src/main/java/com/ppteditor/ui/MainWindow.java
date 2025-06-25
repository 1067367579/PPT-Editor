package com.ppteditor.ui;

import com.ppteditor.PPTEditorApplication;
import com.ppteditor.core.command.CommandManager;
import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.io.PresentationFileManager;
import com.ppteditor.core.io.PresentationExporter;
import com.ppteditor.core.model.Slide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * 主窗口类
 * 提供PPT编辑器的主要用户界面
 */
public class MainWindow extends JFrame {
    
    private static final int DEFAULT_WIDTH = 1400;
    private static final int DEFAULT_HEIGHT = 900;
    
    // UI组件
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel contentPanel;
    private JLabel statusLabel;
    
    // 核心组件
    private CommandManager commandManager;
    private SlideCanvas slideCanvas;
    private SlidePanel slidePanel;
    private PropertyPanel propertyPanel;
    private TemplatePanel templatePanel;
    private Presentation currentPresentation;
    private PresentationFileManager fileManager;
    private PresentationExporter exporter;
    
    public MainWindow() {
        super(PPTEditorApplication.AppInfo.getFullName());
        
        this.commandManager = CommandManager.getInstance();
        this.exporter = new PresentationExporter();
        
        initializeUI();
        setupEventHandlers();
        
        // 设置窗口属性
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null); // 居中显示
        
        // 设置最小尺寸
        setMinimumSize(new Dimension(1200, 800));
        
        // 延迟调整布局（确保组件都已创建）
        SwingUtilities.invokeLater(() -> {
            adjustLayoutOnResize();
        });
    }
    
    private void initializeUI() {
        // 创建菜单栏
        createMenuBar();
        
        // 创建工具栏
        createToolBar();
        
        // 创建内容面板
        createContentPanel();
        
        // 创建状态栏
        createStatusBar();
        
        // 布局
        setJMenuBar(menuBar);
        add(toolBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        addMenuItem(fileMenu, "新建", KeyEvent.VK_N, 
                   KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK),
                   e -> newPresentation());
        
        addMenuItem(fileMenu, "打开", KeyEvent.VK_O,
                   KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
                   e -> openPresentation());
        
        fileMenu.addSeparator();
        
        addMenuItem(fileMenu, "保存", KeyEvent.VK_S,
                   KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
                   e -> savePresentation());
        
        addMenuItem(fileMenu, "另存为", KeyEvent.VK_A,
                   KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK),
                   e -> saveAsPresentation());
        
        fileMenu.addSeparator();
        
        addMenuItem(fileMenu, "导出为图片", 0, null, e -> exportAsImage());
        addMenuItem(fileMenu, "导出为PDF", 0, null, e -> exportAsPDF());
        
        fileMenu.addSeparator();
        
        addMenuItem(fileMenu, "退出", KeyEvent.VK_X,
                   KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK),
                   e -> exitApplication());
        
        menuBar.add(fileMenu);
        
        // 编辑菜单
        JMenu editMenu = new JMenu("编辑(E)");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        addMenuItem(editMenu, "撤销", KeyEvent.VK_U,
                   KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK),
                   e -> undo());
        
        addMenuItem(editMenu, "重做", KeyEvent.VK_R,
                   KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK),
                   e -> redo());
        
        editMenu.addSeparator();
        
        addMenuItem(editMenu, "复制", KeyEvent.VK_C,
                   KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
                   e -> copy());
        
        addMenuItem(editMenu, "粘贴", KeyEvent.VK_V,
                   KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK),
                   e -> paste());
        
        addMenuItem(editMenu, "删除", KeyEvent.VK_D,
                   KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                   e -> delete());
        
        menuBar.add(editMenu);
        
        // 设计菜单
        JMenu designMenu = new JMenu("设计(D)");
        designMenu.setMnemonic(KeyEvent.VK_D);
        
        addMenuItem(designMenu, "设置过渡动画...", 0, null, e -> openTransitionSettings());
        
        menuBar.add(designMenu);
        
        // 插入菜单
        JMenu insertMenu = new JMenu("插入(I)");
        insertMenu.setMnemonic(KeyEvent.VK_I);
        
        addMenuItem(insertMenu, "文本框", 0, null, e -> insertTextBox());
        addMenuItem(insertMenu, "矩形", 0, null, e -> insertRectangle());
        addMenuItem(insertMenu, "椭圆", 0, null, e -> insertEllipse());
        addMenuItem(insertMenu, "图片", 0, null, e -> insertImage());
        
        menuBar.add(insertMenu);
        
        // 视图菜单
        JMenu viewMenu = new JMenu("视图(V)");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        addMenuItem(viewMenu, "放大", 0,
                   KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK),
                   e -> zoomIn());
        
        addMenuItem(viewMenu, "缩小", 0,
                   KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK),
                   e -> zoomOut());
        
        addMenuItem(viewMenu, "适合窗口", 0,
                   KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK),
                   e -> fitToWindow());
        
        viewMenu.addSeparator();
        
        addMenuItem(viewMenu, "演示模式", KeyEvent.VK_F5,
                   KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                   e -> startPresentationMode());
        
        menuBar.add(viewMenu);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        addMenuItem(helpMenu, "关于", 0, null, e -> showAbout());
        
        menuBar.add(helpMenu);
    }
    
    private void addMenuItem(JMenu menu, String text, int mnemonic, 
                           KeyStroke accelerator, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        
        if (mnemonic != 0) {
            item.setMnemonic(mnemonic);
        }
        
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        
        if (action != null) {
            item.addActionListener(action);
        }
        
        menu.add(item);
    }
    
    private void createToolBar() {
        toolBar = new JToolBar("工具栏");
        toolBar.setFloatable(false);
        
        // 文件操作按钮
        addToolBarButton("新建", "创建新演示文稿", e -> newPresentation());
        addToolBarButton("打开", "打开演示文稿", e -> openPresentation());
        addToolBarButton("保存", "保存演示文稿", e -> savePresentation());
        
        toolBar.addSeparator();
        
        // 编辑操作按钮
        addToolBarButton("撤销", "撤销操作", e -> undo());
        addToolBarButton("重做", "重做操作", e -> redo());
        
        toolBar.addSeparator();
        
        // 插入按钮
        addToolBarButton("文本", "插入文本框", e -> insertTextBox());
        addToolBarButton("矩形", "插入矩形", e -> insertRectangle());
        addToolBarButton("椭圆", "插入椭圆", e -> insertEllipse());
        addToolBarButton("图片", "插入图片", e -> insertImage());
        
        toolBar.addSeparator();
        
        // 视图按钮
        addToolBarButton("演示", "开始演示", e -> startPresentationMode());
    }
    
    private void addToolBarButton(String text, String tooltip, ActionListener action) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        
        if (action != null) {
            button.addActionListener(action);
        }
        
        toolBar.add(button);
    }
    
    private void createContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        
        // 初始化核心组件
        slideCanvas = new SlideCanvas();
        slidePanel = new SlidePanel();
        propertyPanel = new PropertyPanel();
        templatePanel = new TemplatePanel(this);
        
        // 设置组件的最小尺寸，确保可见性
        slidePanel.setMinimumSize(new Dimension(180, 250));
        templatePanel.setMinimumSize(new Dimension(180, 200));
        propertyPanel.setMinimumSize(new Dimension(250, 400));
        slideCanvas.setMinimumSize(new Dimension(600, 400));
        
        // 设置首选尺寸
        slidePanel.setPreferredSize(new Dimension(200, 350));
        templatePanel.setPreferredSize(new Dimension(200, 250));
        propertyPanel.setPreferredSize(new Dimension(280, 600));
        
        // 创建中央画布区域（带滚动条）
        JScrollPane canvasScrollPane = new JScrollPane(slideCanvas);
        canvasScrollPane.setMinimumSize(new Dimension(600, 400));
        canvasScrollPane.setPreferredSize(new Dimension(800, 450));
        canvasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        canvasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 创建左侧面板（幻灯片面板 + 模板面板）
        JSplitPane leftSidePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, slidePanel, templatePanel);
        leftSidePanel.setResizeWeight(0.6); // 幻灯片面板占60%空间
        leftSidePanel.setDividerSize(4);
        leftSidePanel.setDividerLocation(350);
        leftSidePanel.setOneTouchExpandable(true);
        leftSidePanel.setContinuousLayout(true);
        
        // 创建左侧分割面板（左侧面板 + 画布）
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSidePanel, canvasScrollPane);
        leftSplitPane.setResizeWeight(0.0); // 左侧固定比例
        leftSplitPane.setDividerSize(6);
        leftSplitPane.setDividerLocation(220);
        leftSplitPane.setOneTouchExpandable(true);
        leftSplitPane.setContinuousLayout(true);
        
        // 创建主分割面板（左侧+画布 vs 属性面板）
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, propertyPanel);
        mainSplitPane.setResizeWeight(0.8); // 左侧占80%，属性面板占20%
        mainSplitPane.setDividerSize(6);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);
        
        // 根据窗口大小动态设置分割位置
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adjustLayoutOnResize();
            }
        });
        
        contentPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        // 设置事件处理
        setupComponentInteractions();
    }
    
    private void setupComponentInteractions() {
        // 当画布中的选择变化时，更新属性面板
        slideCanvas.setOnSelectionChanged(() -> {
            propertyPanel.setSelectedElements(slideCanvas.getSelectedElements());
        });

        // 当画布内容变化时，更新幻灯片缩略图列表
        slideCanvas.setOnContentChanged(() -> {
            if (slidePanel != null) {
                slidePanel.updateSlideList();
            }
        });

        // 当幻灯片列表中选择的幻灯片变化时，更新所有相关组件
        slidePanel.setOnSlideSelected(index -> {
            if (index >= 0 && index < currentPresentation.getSlides().size()) {
                currentPresentation.setCurrentSlideIndex(index);
                Slide newSlide = currentPresentation.getCurrentSlide();
                
                slideCanvas.setSlide(newSlide);
                propertyPanel.setSlideContext(newSlide);
                
                updateStatus("切换到: " + newSlide.getName());
            }
        });
        
        // 属性修改时刷新画布
        propertyPanel.setOnElementChanged(() -> {
            slideCanvas.repaint();
            slidePanel.updateSlideList();
        });
        
        // 设置对齐回调
        propertyPanel.setAlignmentCallbacks(
            () -> slideCanvas.alignLeft(),
            () -> slideCanvas.alignRight(),
            () -> slideCanvas.alignTop(),
            () -> slideCanvas.alignBottom(),
            () -> slideCanvas.alignCenterHorizontal(),
            () -> slideCanvas.alignCenterVertical()
        );
    }
    
    private void createStatusBar() {
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusLabel.setPreferredSize(new Dimension(0, 25));
        
        // 设置状态回调
        commandManager.setStatusCallback(this::updateStatus);
    }
    
    private void setupEventHandlers() {
        // 窗口关闭事件
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    // 菜单动作方法
    private void newPresentation() {
        currentPresentation = new Presentation("新建演示文稿");
        slidePanel.setPresentation(currentPresentation);
        slideCanvas.setSlide(currentPresentation.getCurrentSlide());
        updateStatus("创建新演示文稿");
    }
    
    private void openPresentation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("打开演示文稿");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || PresentationFileManager.isSupportedFile(f);
            }
            
            @Override
            public String getDescription() {
                return "PPT编辑器文件 (*" + PresentationFileManager.getRecommendedExtension() + ")";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentPresentation = PresentationFileManager.loadPresentation(selectedFile);
                slidePanel.setPresentation(currentPresentation);
                slideCanvas.setSlide(currentPresentation.getCurrentSlide());
                updateStatus("打开演示文稿: " + selectedFile.getName());
                setTitle(PPTEditorApplication.AppInfo.getFullName() + " - " + selectedFile.getName());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "无法打开文件: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                updateStatus("打开文件失败");
            }
        }
    }
    
    private void savePresentation() {
        if (currentPresentation == null) {
            newPresentation();
            return;
        }
        
        String filePath = currentPresentation.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            saveAsPresentation();
            return;
        }
        
        try {
            PresentationFileManager.savePresentation(currentPresentation, new File(filePath));
            updateStatus("保存成功: " + new File(filePath).getName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "保存失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            updateStatus("保存失败");
        }
    }
    
    private void saveAsPresentation() {
        if (currentPresentation == null) {
            newPresentation();
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("另存为");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || PresentationFileManager.isSupportedFile(f);
            }
            
            @Override
            public String getDescription() {
                return "PPT编辑器文件 (*" + PresentationFileManager.getRecommendedExtension() + ")";
            }
        });
        
        // 设置默认文件名
        String defaultName = currentPresentation.getTitle();
        if (defaultName == null || defaultName.isEmpty()) {
            defaultName = "新建演示文稿";
        }
        fileChooser.setSelectedFile(new File(defaultName + PresentationFileManager.getRecommendedExtension()));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                PresentationFileManager.savePresentation(currentPresentation, selectedFile);
                updateStatus("另存为成功: " + selectedFile.getName());
                setTitle(PPTEditorApplication.AppInfo.getFullName() + " - " + selectedFile.getName());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "另存为失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                updateStatus("另存为失败");
            }
        }
    }
    
    private void exportAsImage() {
        if (currentPresentation == null) {
            JOptionPane.showMessageDialog(this, "没有可导出的演示文稿", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择导出目录");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            
            // 选择图片格式
            String[] formats = exporter.getSupportedImageFormats();
            String format = (String) JOptionPane.showInputDialog(this,
                "选择图片格式:", 
                "导出格式", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                formats, 
                formats[0]);
            
            if (format != null) {
                try {
                    exporter.exportAllSlidesAsImages(currentPresentation, selectedDir.getAbsolutePath(), format);
                    updateStatus("图片导出成功: " + selectedDir.getAbsolutePath());
                    
                    JOptionPane.showMessageDialog(this,
                        "导出完成！文件保存到: " + selectedDir.getAbsolutePath(),
                        "导出成功",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "导出失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    updateStatus("图片导出失败");
                }
            }
        }
    }
    
    private void exportAsPDF() {
        if (currentPresentation == null) {
            JOptionPane.showMessageDialog(this, "没有可导出的演示文稿", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出为PDF");
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
            }
            
            @Override
            public String getDescription() {
                return "PDF文件 (*.pdf)";
            }
        });
        
        // 设置默认文件名
        String defaultName = currentPresentation.getTitle();
        if (defaultName == null || defaultName.isEmpty()) {
            defaultName = "演示文稿";
        }
        chooser.setSelectedFile(new File(defaultName + ".pdf"));
        
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            
            try {
                exporter.exportAsPDF(currentPresentation, selectedFile.getAbsolutePath());
                updateStatus("PDF导出成功: " + selectedFile.getName());
                
                JOptionPane.showMessageDialog(this,
                    "导出完成！文件保存为: " + selectedFile.getName(),
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "导出失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
                updateStatus("PDF导出失败");
            }
        }
    }
    
    private void undo() {
        if (commandManager.undo()) {
            // 刷新界面显示
            if (slideCanvas != null) {
                slideCanvas.repaint();
            }
            if (slidePanel != null) {
                slidePanel.updateSlideList();
            }
            updateStatus("撤销完成");
        } else {
            updateStatus("无法撤销");
        }
    }
    
    private void redo() {
        if (commandManager.redo()) {
            // 刷新界面显示
            if (slideCanvas != null) {
                slideCanvas.repaint();
            }
            if (slidePanel != null) {
                slidePanel.updateSlideList();
            }
            updateStatus("重做完成");
        } else {
            updateStatus("无法重做");
        }
    }
    
    private void copy() {
        updateStatus("复制");
        // TODO: 实现复制功能
    }
    
    private void paste() {
        updateStatus("粘贴");
        // TODO: 实现粘贴功能
    }
    
    private void delete() {
        if (slideCanvas != null) {
            slideCanvas.deleteSelectedElements();
            updateStatus("删除选中元素");
        }
    }
    
    private void insertTextBox() {
        if (slideCanvas != null) {
            slideCanvas.addTextElement("点击编辑文本");
            updateStatus("插入文本框");
        }
    }
    
    private void insertRectangle() {
        if (slideCanvas != null) {
            slideCanvas.addRectangleElement();
            updateStatus("插入矩形");
        }
    }
    
    private void insertEllipse() {
        if (slideCanvas != null) {
            slideCanvas.addEllipseElement();
            updateStatus("插入椭圆");
        }
    }
    
    private void insertImage() {
        if (slideCanvas != null) {
            slideCanvas.addImageElement();
            updateStatus("插入图片");
        }
    }
    
    private void zoomIn() {
        if (slideCanvas != null) {
            slideCanvas.zoomIn();
            updateStatus("放大到 " + Math.round(slideCanvas.getZoomLevel() * 100) + "%");
        }
    }
    
    private void zoomOut() {
        if (slideCanvas != null) {
            slideCanvas.zoomOut();
            updateStatus("缩小到 " + Math.round(slideCanvas.getZoomLevel() * 100) + "%");
        }
    }
    
    private void fitToWindow() {
        slideCanvas.fitToWindow();
        updateStatus("视图已缩放至适合窗口大小");
    }
    
    private void openTransitionSettings() {
        if (currentPresentation != null) {
            TransitionSettingsDialog dialog = new TransitionSettingsDialog(this, currentPresentation);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "请先打开或创建一个演示文稿。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void startPresentationMode() {
        if (currentPresentation != null && !currentPresentation.getSlides().isEmpty()) {
            int selectedIndex = slidePanel.getSelectedIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
            
            PresentationPlayerWindow player = new PresentationPlayerWindow(currentPresentation, selectedIndex);
            player.start();
            updateStatus("已进入演示模式。按ESC键退出。");
        } else {
            JOptionPane.showMessageDialog(this, "没有可供演示的幻灯片。", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            PPTEditorApplication.AppInfo.getAboutText(),
            "关于 " + PPTEditorApplication.AppInfo.NAME,
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitApplication() {
        // TODO: 检查是否有未保存的更改
        int result = JOptionPane.showConfirmDialog(this,
            "确定要退出吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    /**
     * 根据窗口大小动态调整布局
     */
    private void adjustLayoutOnResize() {
        SwingUtilities.invokeLater(() -> {
            if (contentPanel != null) {
                // 获取主分割面板
                Component[] components = contentPanel.getComponents();
                if (components.length > 0 && components[0] instanceof JSplitPane) {
                    JSplitPane mainSplitPane = (JSplitPane) components[0];
                    
                    // 计算动态分割位置
                    int windowWidth = getWidth();
                    int propertyPanelWidth = Math.max(250, Math.min(350, windowWidth / 5)); // 属性面板占1/5宽度，最小250px，最大350px
                    int mainDividerLocation = windowWidth - propertyPanelWidth - 20; // 留些边距
                    
                    // 设置主分割面板位置
                    mainSplitPane.setDividerLocation(mainDividerLocation);
                    
                    // 获取左侧分割面板
                    Component leftComponent = mainSplitPane.getLeftComponent();
                    if (leftComponent instanceof JSplitPane) {
                        JSplitPane leftSplitPane = (JSplitPane) leftComponent;
                        
                        // 左侧面板宽度固定为200-250px
                        int leftPanelWidth = Math.max(200, Math.min(250, windowWidth / 6));
                        leftSplitPane.setDividerLocation(leftPanelWidth);
                        
                        // 获取左侧垂直分割面板
                        Component leftSideComponent = leftSplitPane.getLeftComponent();
                        if (leftSideComponent instanceof JSplitPane) {
                            JSplitPane leftSidePanel = (JSplitPane) leftSideComponent;
                            
                            // 动态调整幻灯片面板和模板面板的比例
                            int windowHeight = getHeight();
                            int availableHeight = windowHeight - 100; // 减去菜单栏、工具栏、状态栏的高度
                            int slidePanelHeight = (int)(availableHeight * 0.6); // 60%给幻灯片面板
                            
                            leftSidePanel.setDividerLocation(slidePanelHeight);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * 跳转到指定页面
     */
    public void goToSlide(int index) {
        if (currentPresentation != null && slidePanel != null) {
            currentPresentation.goToSlide(index);
            slidePanel.updateSlideList();
            slideCanvas.repaint();
            updateStatus("已跳转到第" + (index + 1) + "页");
        }
    }
    
    /**
     * 跳转到最后一页
     */
    public void goToLastSlide() {
        if (currentPresentation != null && !currentPresentation.getSlides().isEmpty()) {
            int lastIndex = currentPresentation.getSlides().size() - 1;
            goToSlide(lastIndex);
        }
    }
    
    // 公共访问方法
    public Presentation getCurrentPresentation() {
        return currentPresentation;
    }
    
    public SlideCanvas getSlideCanvas() {
        return slideCanvas;
    }
    
    public void refreshCanvas() {
        slideCanvas.repaint();
        slidePanel.repaint();
    }
} 