package com.ppteditor.ui;

import com.ppteditor.PPTEditorApplication;
import com.ppteditor.core.command.CommandManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * 主窗口类
 * 提供PPT编辑器的主要用户界面
 */
public class MainWindow extends JFrame {
    
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    
    // UI组件
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel contentPanel;
    private JLabel statusLabel;
    
    // 核心组件
    private CommandManager commandManager;
    
    public MainWindow() {
        super(PPTEditorApplication.AppInfo.getFullName());
        
        this.commandManager = CommandManager.getInstance();
        
        initializeUI();
        setupEventHandlers();
        
        // 设置窗口属性
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null); // 居中显示
        
        // 设置最小尺寸
        setMinimumSize(new Dimension(800, 600));
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
        contentPanel.setBackground(Color.LIGHT_GRAY);
        
        // 临时添加一些内容
        JLabel label = new JLabel("PPT编辑器界面", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 24));
        label.setForeground(Color.DARK_GRAY);
        contentPanel.add(label, BorderLayout.CENTER);
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
        updateStatus("创建新演示文稿");
        // TODO: 实现新建功能
    }
    
    private void openPresentation() {
        updateStatus("打开演示文稿");
        // TODO: 实现打开功能
    }
    
    private void savePresentation() {
        updateStatus("保存演示文稿");
        // TODO: 实现保存功能
    }
    
    private void saveAsPresentation() {
        updateStatus("另存为演示文稿");
        // TODO: 实现另存为功能
    }
    
    private void exportAsImage() {
        updateStatus("导出为图片");
        // TODO: 实现导出图片功能
    }
    
    private void exportAsPDF() {
        updateStatus("导出为PDF");
        // TODO: 实现导出PDF功能
    }
    
    private void undo() {
        if (commandManager.undo()) {
            updateStatus("撤销完成");
        } else {
            updateStatus("无法撤销");
        }
    }
    
    private void redo() {
        if (commandManager.redo()) {
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
        updateStatus("删除");
        // TODO: 实现删除功能
    }
    
    private void insertTextBox() {
        updateStatus("插入文本框");
        // TODO: 实现插入文本框功能
    }
    
    private void insertRectangle() {
        updateStatus("插入矩形");
        // TODO: 实现插入矩形功能
    }
    
    private void insertEllipse() {
        updateStatus("插入椭圆");
        // TODO: 实现插入椭圆功能
    }
    
    private void insertImage() {
        updateStatus("插入图片");
        // TODO: 实现插入图片功能
    }
    
    private void zoomIn() {
        updateStatus("放大");
        // TODO: 实现放大功能
    }
    
    private void zoomOut() {
        updateStatus("缩小");
        // TODO: 实现缩小功能
    }
    
    private void fitToWindow() {
        updateStatus("适合窗口");
        // TODO: 实现适合窗口功能
    }
    
    private void startPresentationMode() {
        updateStatus("开始演示模式");
        // TODO: 实现演示模式功能
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
} 