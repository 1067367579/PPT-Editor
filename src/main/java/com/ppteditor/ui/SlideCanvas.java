package com.ppteditor.ui;

import com.ppteditor.core.command.*;
import com.ppteditor.core.model.*;
import com.ppteditor.core.enums.ElementType;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 幻灯片画布组件
 * 负责显示和编辑幻灯片内容
 */
public class SlideCanvas extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    
    // 画布尺寸（标准16:9比例）
    public static final int CANVAS_WIDTH = 800;
    public static final int CANVAS_HEIGHT = 450;
    private static final int GRID_SIZE = 10;
    
    private Slide currentSlide;
    private double zoomLevel = 1.0;
    private boolean showGrid = true;
    private boolean snapToGrid = true;
    
    // 交互状态
    private SlideElement<?> draggedElement;
    private Point dragStartPoint;
    private Point elementStartPos;
    private boolean isDragging = false;
    private Rectangle selectionRect;
    private Point selectionStart;
    
    // 缩放和旋转状态
    private SelectionHandle.HandleType activeHandle;
    private SelectionHandle activeSelectionHandle;
    private Rectangle originalBounds;
    private double originalRotation;
    private java.util.List<SelectionHandle> selectionHandles;
    
    // 复制粘贴支持
    private java.util.List<SlideElement<?>> clipboard;
    
    // 回调接口
    private Runnable onSelectionChanged;
    private Runnable onContentChanged;
    private CommandManager commandManager;
    
    public SlideCanvas() {
        this.commandManager = CommandManager.getInstance();
        this.selectionHandles = new ArrayList<>();
        this.clipboard = new ArrayList<>();
        
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        
        // 设置边框
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        // 设置拖拽支持
        setTransferHandler(new CanvasTransferHandler());
    }
    
    public void setSlide(Slide slide) {
        this.currentSlide = slide;
        repaint();
    }
    
    public Slide getCurrentSlide() {
        return currentSlide;
    }
    
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }
    
    public void setOnContentChanged(Runnable callback) {
        this.onContentChanged = callback;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 应用缩放
        g2d.scale(zoomLevel, zoomLevel);
        
        // 绘制网格
        if (showGrid) {
            drawGrid(g2d);
        }
        
        // 绘制幻灯片内容
        if (currentSlide != null) {
            Dimension slideSize = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
            currentSlide.render(g2d, slideSize);
        }
        
        // 绘制选择框
        if (selectionRect != null) {
            g2d.setColor(new Color(100, 150, 255, 100));
            g2d.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            g2d.setColor(new Color(100, 150, 255));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
        
        // 绘制选择控制点
        drawSelectionHandles(g2d);
        
        g2d.dispose();
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 200, 100));
        g2d.setStroke(new BasicStroke(0.5f));
        
        // 绘制垂直线
        for (int x = 0; x <= CANVAS_WIDTH; x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, CANVAS_HEIGHT);
        }
        
        // 绘制水平线
        for (int y = 0; y <= CANVAS_HEIGHT; y += GRID_SIZE) {
            g2d.drawLine(0, y, CANVAS_WIDTH, y);
        }
    }
    
    // 添加元素的方法
    public void addTextElement(String text) {
        if (currentSlide == null) return;
        
        TextElement element = new TextElement(text);
        element.setBounds(50, 50, 200, 50);
        
        // 应用当前配色主题
        applyCurrentColorTheme(element);
        
        AddElementCommand command = new AddElementCommand(currentSlide, element);
        commandManager.executeCommand(command);
        
        // 选中新创建的元素
        currentSlide.selectElement(element);
        notifySelectionChanged();
        notifyContentChanged();
        repaint();
    }
    
    public void addRectangleElement() {
        if (currentSlide == null) return;
        
        RectangleElement element = new RectangleElement(100, 100, 150, 100);
        
        // 应用当前配色主题
        applyCurrentColorTheme(element);
        
        AddElementCommand command = new AddElementCommand(currentSlide, element);
        commandManager.executeCommand(command);
        
        currentSlide.selectElement(element);
        notifySelectionChanged();
        notifyContentChanged();
        repaint();
    }
    
    public void addEllipseElement() {
        if (currentSlide == null) return;
        
        EllipseElement element = new EllipseElement(100, 100, 150, 100);
        
        // 应用当前配色主题
        applyCurrentColorTheme(element);
        
        AddElementCommand command = new AddElementCommand(currentSlide, element);
        commandManager.executeCommand(command);
        
        currentSlide.selectElement(element);
        notifySelectionChanged();
        notifyContentChanged();
        repaint();
    }
    
    /**
     * 为新创建的元素应用当前配色主题
     */
    private void applyCurrentColorTheme(SlideElement<?> element) {
        // 需要从MainWindow获取当前演示文稿的配色主题
        Component parent = this.getParent();
        while (parent != null && !(parent instanceof MainWindow)) {
            parent = parent.getParent();
        }
        
        if (parent instanceof MainWindow) {
            MainWindow mainWindow = (MainWindow) parent;
            Presentation presentation = mainWindow.getCurrentPresentation();
            if (presentation != null && presentation.getColorTheme() != null) {
                ColorTheme theme = presentation.getColorTheme();
                if (element.getStyle() != null) {
                    element.getStyle().applyColorTheme(theme);
                    System.out.println("已为新元素应用配色主题: " + theme.getName());
                }
            }
        }
    }
    
    public void addImageElement() {
        if (currentSlide == null) return;
        
        // 打开文件选择对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择图片文件");
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || ImageElement.isSupportedImageFile(f);
            }
            
            @Override
            public String getDescription() {
                return "图片文件 (*.jpg, *.png, *.gif, *.bmp)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ImageElement element = new ImageElement(selectedFile);
                element.setPosition(100, 100);
                
                // 应用当前配色主题（虽然图片元素可能没有颜色样式，但保持一致性）
                applyCurrentColorTheme(element);
                
                AddElementCommand command = new AddElementCommand(currentSlide, element);
                commandManager.executeCommand(command);
                
                currentSlide.selectElement(element);
                notifySelectionChanged();
                notifyContentChanged();
                repaint();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "无法加载图片文件: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 删除选中元素
    public void deleteSelectedElements() {
        if (currentSlide == null || !currentSlide.hasSelection()) return;
        
        DeleteElementsCommand command = new DeleteElementsCommand(currentSlide, 
            new ArrayList<>(currentSlide.getSelectedElements()));
        commandManager.executeCommand(command);
        
        notifySelectionChanged();
        notifyContentChanged();
        repaint();
    }
    
    // 复制选中元素到剪贴板
    public void copySelectedElements() {
        if (currentSlide == null || !currentSlide.hasSelection()) return;
        
        clipboard.clear();
        for (SlideElement<?> element : currentSlide.getSelectedElements()) {
            clipboard.add(element.clone());
        }
    }
    
    // 粘贴剪贴板元素
    public void pasteElements() {
        if (currentSlide == null || clipboard.isEmpty()) return;
        
        List<SlideElement<?>> pastedElements = new ArrayList<>();
        for (SlideElement<?> original : clipboard) {
            SlideElement<?> cloned = original.clone();
            cloned.move(20, 20); // 粘贴时偏移
            pastedElements.add(cloned);
        }
        
        AddElementCommand command = new AddElementCommand(currentSlide, pastedElements);
        commandManager.executeCommand(command);
        
        currentSlide.clearSelection();
        pastedElements.forEach(currentSlide::addToSelection);
        
        notifySelectionChanged();
        notifyContentChanged();
    }
    
    // 对齐功能
    public void alignLeft() {
        if (currentSlide != null) {
            currentSlide.alignLeft();
            repaint();
        }
    }
    
    public void alignRight() {
        if (currentSlide != null) {
            currentSlide.alignRight();
            repaint();
        }
    }
    
    public void alignTop() {
        if (currentSlide != null) {
            currentSlide.alignTop();
            repaint();
        }
    }
    
    public void alignBottom() {
        if (currentSlide != null) {
            currentSlide.alignBottom();
            repaint();
        }
    }
    
    public void alignCenterHorizontal() {
        if (currentSlide != null) {
            currentSlide.alignCenterHorizontal();
            repaint();
        }
    }
    
    public void alignCenterVertical() {
        if (currentSlide != null) {
            currentSlide.alignCenterVertical();
            repaint();
        }
    }
    
    // 鼠标事件处理
    @Override
    public void mousePressed(MouseEvent e) {
        if (currentSlide == null) return;
        requestFocusInWindow();

        Point p = scalePoint(e.getPoint());
        SelectionHandle handle = getHandleAt(p);

        if (handle != null) {
            // Clicked on a resize/rotate handle
            activeHandle = handle.getType();
            isDragging = true;
            dragStartPoint = p;
            originalBounds = calculateBoundingBox(currentSlide.getSelectedElements());
            if (!currentSlide.getSelectedElements().isEmpty()) {
                originalRotation = currentSlide.getSelectedElements().iterator().next().getRotation();
            }
            return;
        }
        
        draggedElement = findTopmostElementAt(p);
        
        if (draggedElement != null) {
            isDragging = true;
            dragStartPoint = p;
            elementStartPos = new Point((int)draggedElement.getX(), (int)draggedElement.getY());

            if (!e.isControlDown() && !currentSlide.getSelectedElements().contains(draggedElement)) {
                currentSlide.selectElement(draggedElement);
            } else if (e.isControlDown() && currentSlide.getSelectedElements().contains(draggedElement)) {
                // Ctrl-clicking a selected element deselects it
                currentSlide.removeFromSelection(draggedElement);
                draggedElement = null; // Don't drag it
                isDragging = false;
            } else if (e.isControlDown()) {
                currentSlide.addToSelection(draggedElement);
            }
            notifySelectionChanged();
        } else {
            // Clicked on empty canvas space
            currentSlide.clearSelection();
            notifySelectionChanged();
            selectionStart = p;
            selectionRect = new Rectangle(p.x, p.y, 0, 0);
        }
        repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentSlide == null) return;
        
        Point point = scalePoint(e.getPoint());
        
        if (activeHandle != null && activeSelectionHandle != null) {
            // 处理缩放和旋转操作
            handleScaleRotateOperation(point);
            repaint();
            
        } else if (draggedElement != null && dragStartPoint != null) {
            // 拖拽元素
            isDragging = true;
            
            int deltaX = point.x - dragStartPoint.x;
            int deltaY = point.y - dragStartPoint.y;
            
            double newX = elementStartPos.x + deltaX;
            double newY = elementStartPos.y + deltaY;
            
            // 网格吸附和自动对齐
            if (snapToGrid) {
                newX = Math.round(newX / GRID_SIZE) * GRID_SIZE;
                newY = Math.round(newY / GRID_SIZE) * GRID_SIZE;
            } else {
                // 自动吸附到其他元素
                Point alignedPos = findAlignmentPosition(newX, newY, draggedElement);
                newX = alignedPos.x;
                newY = alignedPos.y;
            }
            
            // 如果是多选，移动所有选中元素
            Set<SlideElement<?>> selected = currentSlide.getSelectedElements();
            if (selected.size() > 1) {
                double moveX = newX - draggedElement.getX();
                double moveY = newY - draggedElement.getY();
                for (SlideElement<?> element : selected) {
                    if (element != draggedElement) {
                        element.move(moveX, moveY);
                    }
                }
            }
            
            draggedElement.setPosition(newX, newY);
            repaint();
            
        } else if (selectionStart != null) {
            // 框选
            int x = Math.min(selectionStart.x, point.x);
            int y = Math.min(selectionStart.y, point.y);
            int width = Math.abs(point.x - selectionStart.x);
            int height = Math.abs(point.y - selectionStart.y);
            
            selectionRect = new Rectangle(x, y, width, height);
            
            // 选择矩形内的元素
            List<SlideElement<?>> elementsInRect = currentSlide.findElementsInArea(selectionRect);
            
            if (!e.isControlDown()) {
                currentSlide.clearSelection();
            }
            
            for (SlideElement<?> element : elementsInRect) {
                currentSlide.addToSelection(element);
            }
            
            repaint();
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (activeHandle != null) {
            // 缩放/旋转操作完成，创建命令
            Set<SlideElement<?>> selected = currentSlide.getSelectedElements();
            if (!selected.isEmpty() && originalBounds != null) {
                SlideElement<?> element = selected.iterator().next();
                Rectangle newBounds = element.getBounds();
                double newRotation = element.getRotation();
                
                // 只有当实际发生变化时才创建命令
                if (!originalBounds.equals(newBounds) || originalRotation != newRotation) {
                    ScaleElementCommand command = new ScaleElementCommand(
                        element, originalBounds, newBounds, originalRotation, newRotation);
                    commandManager.executeCommand(command);
                }
            }
            
            activeHandle = null;
            activeSelectionHandle = null;
            originalBounds = null;
            setCursor(Cursor.getDefaultCursor());
            
        } else if (isDragging && draggedElement != null) {
            // 创建移动命令
            Point currentPos = new Point((int)draggedElement.getX(), (int)draggedElement.getY());
            if (!currentPos.equals(elementStartPos)) {
                MoveElementCommand command = new MoveElementCommand(
                    draggedElement, elementStartPos, currentPos);
                commandManager.executeCommand(command);
            }
        }
        
        // 清理状态
        draggedElement = null;
        dragStartPoint = null;
        elementStartPos = null;
        isDragging = false;
        selectionStart = null;
        selectionRect = null;
        
        notifyContentChanged();
        repaint();
    }
    
    // 内联文本编辑相关字段
    private TextElement editingTextElement;
    private JTextField inlineTextEditor;
    private Point editStartPoint;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentSlide == null) return;
        
        Point point = scalePoint(e.getPoint());
        List<SlideElement<?>> elements = currentSlide.findElementsAt(point);
        
        if (!elements.isEmpty()) {
            SlideElement<?> clickedElement = elements.get(0);
            
            // 单击处理超链接
            if (e.getClickCount() == 1) {
                String hyperlink = null;
                
                // 对于文本元素，检查文本片段超链接
                if (clickedElement instanceof TextElement) {
                    TextElement textElement = (TextElement) clickedElement;
                    hyperlink = textElement.getHyperlinkAtPoint(point);
                }
                
                // 如果没有文本片段超链接，使用元素级超链接
                if (hyperlink == null) {
                    hyperlink = clickedElement.getHyperlink();
                }
                
                if (hyperlink != null && !hyperlink.trim().isEmpty()) {
                    openHyperlink(hyperlink);
                    return; // 不继续处理其他事件
                }
            }
            
            // 双击编辑文本 - 使用内联编辑
            if (e.getClickCount() == 2 && clickedElement instanceof TextElement) {
                TextElement textElement = (TextElement) clickedElement;
                startInlineTextEditing(textElement, e.getPoint());
            }
        } else {
            // 点击空白区域，结束内联编辑
            finishInlineTextEditing();
        }
    }
    
    /**
     * 开始内联文本编辑
     */
    private void startInlineTextEditing(TextElement textElement, Point clickPoint) {
        // 如果已经在编辑其他文本，先结束
        finishInlineTextEditing();
        
        editingTextElement = textElement;
        editStartPoint = clickPoint;
        
        // 创建内联编辑器
        inlineTextEditor = new JTextField(textElement.getText());
        inlineTextEditor.setFont(textElement.getStyle().getFont());
        inlineTextEditor.setForeground(textElement.getStyle().getTextColor());
        inlineTextEditor.setBackground(new Color(255, 255, 255, 200)); // 半透明背景
        inlineTextEditor.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        
        // 设置编辑器位置和大小
        Rectangle bounds = textElement.getBounds();
        int x = (int) (bounds.x * zoomLevel);
        int y = (int) (bounds.y * zoomLevel);
        int width = Math.max(100, (int) (bounds.width * zoomLevel));
        int height = (int) (bounds.height * zoomLevel);
        
        inlineTextEditor.setBounds(x, y, width, height);
        
        // 添加事件监听器
        inlineTextEditor.addActionListener(e -> finishInlineTextEditing());
        inlineTextEditor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                finishInlineTextEditing();
            }
        });
        
        inlineTextEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    cancelInlineTextEditing();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    finishInlineTextEditing();
                }
            }
        });
        
        // 添加到画布并获得焦点
        add(inlineTextEditor);
        inlineTextEditor.selectAll();
        inlineTextEditor.requestFocus();
        
        revalidate();
        repaint();
    }
    
    /**
     * 完成内联文本编辑
     */
    private void finishInlineTextEditing() {
        if (inlineTextEditor != null && editingTextElement != null) {
            String newText = inlineTextEditor.getText();
            String oldText = editingTextElement.getText();
            
            if (!newText.equals(oldText)) {
                // 执行编辑命令
                EditTextCommand command = new EditTextCommand(editingTextElement, oldText, newText);
                commandManager.executeCommand(command);
            }
            
            // 清理编辑器
            remove(inlineTextEditor);
            inlineTextEditor = null;
            editingTextElement = null;
            editStartPoint = null;
            
            notifyContentChanged();
            revalidate();
            repaint();
        }
    }
    
    /**
     * 取消内联文本编辑
     */
    private void cancelInlineTextEditing() {
        if (inlineTextEditor != null) {
            remove(inlineTextEditor);
            inlineTextEditor = null;
            editingTextElement = null;
            editStartPoint = null;
            
            notifyContentChanged();
            revalidate();
            repaint();
        }
    }
    
    private void editTextElement(TextElement textElement) {
        // 创建内联编辑对话框
        createInlineTextEditor(textElement);
    }
    
    private void createInlineTextEditor(TextElement textElement) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "编辑文本", true);
        dialog.setLayout(new BorderLayout());
        
        // 创建文本编辑区域
        JTextArea textArea = new JTextArea(textElement.getText());
        textArea.setFont(textElement.getStyle().getFont());
        textArea.setForeground(textElement.getStyle().getTextColor());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretColor(Color.BLACK);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        okButton.addActionListener(e -> {
            String newText = textArea.getText();
            if (!newText.equals(textElement.getText())) {
                EditTextCommand command = new EditTextCommand(textElement, textElement.getText(), newText);
                commandManager.executeCommand(command);
                notifyContentChanged();
            }
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置快捷键
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        textArea.getActionMap().put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                                 if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) { // Shift+Enter 换行，Enter确认
                    okButton.doClick();
                }
            }
        });
        
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        textArea.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });
        
        // 设置对话框属性
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        // 自动选中所有文本并聚焦
        SwingUtilities.invokeLater(() -> {
            textArea.selectAll();
            textArea.requestFocus();
        });
        
        dialog.setVisible(true);
    }
    
    /**
     * 打开超链接
     */
    private void openHyperlink(String hyperlink) {
        try {
            String url = hyperlink.trim();
            
            if (url.startsWith("slide:")) {
                // 页面跳转
                handleSlideNavigation(url.substring(6));
                return;
            } else if (url.startsWith("mailto:")) {
                // 邮箱链接
                handleMailtoLink(url);
                return;
            } else if (url.startsWith("file://")) {
                // 文件链接
                handleFileLink(url);
                return;
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                // 如果没有协议前缀，默认添加 http://
                url = "http://" + url;
            }
            
            // 网页链接
            handleWebLink(url, hyperlink);
            
        } catch (Exception ex) {
            showHyperlinkStatus("打开超链接失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void handleSlideNavigation(String target) {
        try {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof MainWindow) {
                MainWindow mainWindow = (MainWindow) parentWindow;
                
                if (target.equals("first")) {
                    // 跳转到第一页
                    mainWindow.goToSlide(0);
                    showHyperlinkStatus("已跳转到第一页");
                } else if (target.equals("last")) {
                    // 跳转到最后一页
                    mainWindow.goToLastSlide();
                    showHyperlinkStatus("已跳转到最后一页");
                } else {
                    // 跳转到指定页面
                    int slideNumber = Integer.parseInt(target);
                    mainWindow.goToSlide(slideNumber - 1); // 转换为0基索引
                    showHyperlinkStatus("已跳转到第" + slideNumber + "页");
                }
            } else {
                showHyperlinkStatus("无法在当前环境中执行页面跳转");
            }
        } catch (NumberFormatException e) {
            showHyperlinkStatus("无效的页面编号: " + target);
        } catch (Exception e) {
            showHyperlinkStatus("页面跳转失败: " + e.getMessage());
        }
    }
    
    private void handleMailtoLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    desktop.mail(new java.net.URI(url));
                    showHyperlinkStatus("已打开邮箱客户端");
                } else {
                    showHyperlinkStatus("系统不支持邮箱功能");
                }
            } else {
                showHyperlinkStatus("系统不支持Desktop功能");
            }
        } catch (Exception e) {
            showHyperlinkStatus("打开邮箱失败: " + e.getMessage());
        }
    }
    
    private void handleFileLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    java.io.File file = new java.io.File(new java.net.URI(url));
                    if (file.exists()) {
                        desktop.open(file);
                        showHyperlinkStatus("已打开文件: " + file.getName());
                    } else {
                        showHyperlinkStatus("文件不存在: " + file.getPath());
                    }
                } else {
                    showHyperlinkStatus("系统不支持文件打开功能");
                }
            } else {
                showHyperlinkStatus("系统不支持Desktop功能");
            }
        } catch (Exception e) {
            showHyperlinkStatus("打开文件失败: " + e.getMessage());
        }
    }
    
    private void handleWebLink(String url, String originalHyperlink) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                    showHyperlinkStatus("已打开超链接: " + originalHyperlink);
                } else {
                    showHyperlinkStatus("系统不支持打开浏览器");
                }
            } else {
                showHyperlinkStatus("系统不支持Desktop功能");
            }
        } catch (Exception e) {
            showHyperlinkStatus("打开网页失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示超链接状态信息
     */
    private void showHyperlinkStatus(String message) {
        // 查找主窗口并更新状态栏
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof MainWindow) {
            MainWindow mainWindow = (MainWindow) parentWindow;
            // 可以通过MainWindow的状态栏显示信息
            System.out.println("状态: " + message);
        } else {
            System.out.println("状态: " + message);
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        // 更新鼠标样式
        if (currentSlide != null) {
            Point point = scalePoint(e.getPoint());
            List<SlideElement<?>> elements = currentSlide.findElementsAt(point);
            
            if (!elements.isEmpty()) {
                SlideElement<?> element = elements.get(0);
                boolean hasHyperlink = false;
                
                // 检查文本片段超链接
                if (element instanceof TextElement) {
                    TextElement textElement = (TextElement) element;
                    String segmentHyperlink = textElement.getHyperlinkAtPoint(point);
                    if (segmentHyperlink != null && !segmentHyperlink.trim().isEmpty()) {
                        hasHyperlink = true;
                    }
                }
                
                // 检查元素级超链接
                if (!hasHyperlink && element.getHyperlink() != null && !element.getHyperlink().trim().isEmpty()) {
                    hasHyperlink = true;
                }
                
                // 设置光标
                if (hasHyperlink) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    // 键盘事件处理
    @Override
    public void keyPressed(KeyEvent e) {
        if (currentSlide == null) return;
        
        int keyCode = e.getKeyCode();
        boolean ctrlPressed = e.isControlDown();
        
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
            deleteSelectedElements();
            
        } else if (ctrlPressed && keyCode == KeyEvent.VK_C) {
            // 复制
            copySelectedElements();
            
        } else if (ctrlPressed && keyCode == KeyEvent.VK_V) {
            // 粘贴
            pasteElements();
            
        } else if (ctrlPressed && keyCode == KeyEvent.VK_Z) {
            commandManager.undo();
            notifySelectionChanged();
            notifyContentChanged();
            repaint();
            
        } else if (ctrlPressed && keyCode == KeyEvent.VK_Y) {
            commandManager.redo();
            notifySelectionChanged();
            notifyContentChanged();
            repaint();
            
        } else if (currentSlide.hasSelection()) {
            // 方向键微调
            int deltaX = 0, deltaY = 0;
            
            switch (keyCode) {
                case KeyEvent.VK_LEFT: deltaX = -1; break;
                case KeyEvent.VK_RIGHT: deltaX = 1; break;
                case KeyEvent.VK_UP: deltaY = -1; break;
                case KeyEvent.VK_DOWN: deltaY = 1; break;
            }
            
            if (deltaX != 0 || deltaY != 0) {
                // 如果按住Shift，移动距离更大
                if (e.isShiftDown()) {
                    deltaX *= GRID_SIZE;
                    deltaY *= GRID_SIZE;
                }
                
                for (SlideElement<?> element : currentSlide.getSelectedElements()) {
                    element.move(deltaX, deltaY);
                }
                notifyContentChanged();
                repaint();
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 工具方法
    private Point scalePoint(Point p) {
        return new Point((int)(p.x / zoomLevel), (int)(p.y / zoomLevel));
    }
    
    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }
    
    private void notifyContentChanged() {
        if (onContentChanged != null) {
            onContentChanged.run();
        }
        repaint();
    }
    
    // Getter和Setter
    public double getZoomLevel() { return zoomLevel; }
    
    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = Math.max(0.1, Math.min(5.0, zoomLevel));
        repaint();
    }
    
    public void zoomIn() {
        setZoomLevel(zoomLevel * 1.2);
    }
    
    public void zoomOut() {
        setZoomLevel(zoomLevel / 1.2);
    }
    
    public void resetZoom() {
        setZoomLevel(1.0);
    }
    
    public void fitToWindow() {
        if (getWidth() > 0 && getHeight() > 0) {
            double widthRatio = (double) getWidth() / CANVAS_WIDTH;
            double heightRatio = (double) getHeight() / CANVAS_HEIGHT;
            setZoomLevel(Math.min(widthRatio, heightRatio));
        }
    }
    
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { 
        this.showGrid = showGrid; 
        repaint();
    }
    
    public boolean isSnapToGrid() { return snapToGrid; }
    public void setSnapToGrid(boolean snapToGrid) { this.snapToGrid = snapToGrid; }
    
    public Set<SlideElement<?>> getSelectedElements() {
        if (currentSlide != null) {
            return currentSlide.getSelectedElements();
        }
        return new HashSet<>();
    }
    
    // 绘制选择控制点
    private void drawSelectionHandles(Graphics2D g2d) {
        if (currentSlide == null || !currentSlide.hasSelection()) {
            selectionHandles.clear();
            return;
        }
        
        Set<SlideElement<?>> selected = currentSlide.getSelectedElements();
        if (selected.size() == 1) {
            // 单选：显示完整的缩放旋转控制点
            SlideElement<?> element = selected.iterator().next();
            Rectangle bounds = element.getBounds();
            
            selectionHandles.clear();
            
            // 添加8个缩放控制点
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.TOP_LEFT, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.TOP_CENTER, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.TOP_RIGHT, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.MIDDLE_LEFT, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.MIDDLE_RIGHT, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.BOTTOM_LEFT, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.BOTTOM_CENTER, bounds));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.BOTTOM_RIGHT, bounds));
            
            // 添加旋转控制点
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.ROTATION, bounds));
            
            // 绘制所有控制点
            for (SelectionHandle handle : selectionHandles) {
                handle.render(g2d);
            }
        } else if (selected.size() > 1) {
            // 多选：只显示包围框
            Rectangle boundingBox = calculateBoundingBox(selected);
            g2d.setColor(new Color(100, 150, 255));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            g2d.drawRect(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
            
            // 显示四角控制点用于多选缩放
            selectionHandles.clear();
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.TOP_LEFT, boundingBox));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.TOP_RIGHT, boundingBox));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.BOTTOM_LEFT, boundingBox));
            selectionHandles.add(new SelectionHandle(SelectionHandle.HandleType.BOTTOM_RIGHT, boundingBox));
            
            for (SelectionHandle handle : selectionHandles) {
                handle.render(g2d);
            }
        }
    }
    
    // 计算多个元素的包围框
    private Rectangle calculateBoundingBox(Set<SlideElement<?>> elements) {
        if (elements.isEmpty()) return new Rectangle();
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for (SlideElement<?> element : elements) {
            Rectangle bounds = element.getBounds();
            minX = Math.min(minX, bounds.x);
            minY = Math.min(minY, bounds.y);
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
    
    // 检查鼠标是否在选择控制点上
    private SelectionHandle getHandleAt(Point point) {
        for (SelectionHandle handle : selectionHandles) {
            if (handle.contains(point)) {
                return handle;
            }
        }
        return null;
    }
    
    // 处理缩放和旋转操作
    private void handleScaleRotateOperation(Point currentPoint) {
        Set<SlideElement<?>> selected = currentSlide.getSelectedElements();
        if (selected.isEmpty() || originalBounds == null) return;
        
        SlideElement<?> element = selected.iterator().next();
        
        if (activeHandle == SelectionHandle.HandleType.ROTATION) {
            // 旋转操作
            double angle = SelectionHandle.calculateRotationAngle(originalBounds, dragStartPoint, currentPoint);
            element.setRotation(originalRotation + Math.toDegrees(angle));
            
        } else {
            // 缩放操作
            Rectangle newBounds = SelectionHandle.calculateResizedBounds(
                originalBounds, activeHandle, dragStartPoint, currentPoint);
                
            element.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
        }
    }
    
    // 自动对齐和吸附功能
    private Point findAlignmentPosition(double newX, double newY, SlideElement<?> draggedElement) {
        final double SNAP_DISTANCE = 5; // 吸附距离阈值
        
        double alignedX = newX;
        double alignedY = newY;
        
        // 获取当前元素的边界
        double elementLeft = newX;
        double elementRight = newX + draggedElement.getWidth();
        double elementTop = newY;
        double elementBottom = newY + draggedElement.getHeight();
        double elementCenterX = newX + draggedElement.getWidth() / 2;
        double elementCenterY = newY + draggedElement.getHeight() / 2;
        
        // 检查与其他元素的对齐
        for (SlideElement<?> element : currentSlide.getElements()) {
            if (element == draggedElement || element.isSelected()) continue;
            
            double otherLeft = element.getX();
            double otherRight = element.getX() + element.getWidth();
            double otherTop = element.getY();
            double otherBottom = element.getY() + element.getHeight();
            double otherCenterX = element.getX() + element.getWidth() / 2;
            double otherCenterY = element.getY() + element.getHeight() / 2;
            
            // 检查水平对齐
            if (Math.abs(elementLeft - otherLeft) <= SNAP_DISTANCE) {
                alignedX = otherLeft; // 左边对齐
            } else if (Math.abs(elementRight - otherRight) <= SNAP_DISTANCE) {
                alignedX = otherRight - draggedElement.getWidth(); // 右边对齐
            } else if (Math.abs(elementCenterX - otherCenterX) <= SNAP_DISTANCE) {
                alignedX = otherCenterX - draggedElement.getWidth() / 2; // 中心对齐
            } else if (Math.abs(elementLeft - otherRight) <= SNAP_DISTANCE) {
                alignedX = otherRight; // 左边贴右边
            } else if (Math.abs(elementRight - otherLeft) <= SNAP_DISTANCE) {
                alignedX = otherLeft - draggedElement.getWidth(); // 右边贴左边
            }
            
            // 检查垂直对齐
            if (Math.abs(elementTop - otherTop) <= SNAP_DISTANCE) {
                alignedY = otherTop; // 顶部对齐
            } else if (Math.abs(elementBottom - otherBottom) <= SNAP_DISTANCE) {
                alignedY = otherBottom - draggedElement.getHeight(); // 底部对齐
            } else if (Math.abs(elementCenterY - otherCenterY) <= SNAP_DISTANCE) {
                alignedY = otherCenterY - draggedElement.getHeight() / 2; // 中心对齐
            } else if (Math.abs(elementTop - otherBottom) <= SNAP_DISTANCE) {
                alignedY = otherBottom; // 顶部贴底部
            } else if (Math.abs(elementBottom - otherTop) <= SNAP_DISTANCE) {
                alignedY = otherTop - draggedElement.getHeight(); // 底部贴顶部
            }
        }
        
        // 检查与画布边界的对齐
        if (Math.abs(elementLeft) <= SNAP_DISTANCE) {
            alignedX = 0; // 左边界对齐
        } else if (Math.abs(elementRight - CANVAS_WIDTH) <= SNAP_DISTANCE) {
            alignedX = CANVAS_WIDTH - draggedElement.getWidth(); // 右边界对齐
        } else if (Math.abs(elementCenterX - CANVAS_WIDTH / 2) <= SNAP_DISTANCE) {
            alignedX = (CANVAS_WIDTH - draggedElement.getWidth()) / 2; // 画布中心对齐
        }
        
        if (Math.abs(elementTop) <= SNAP_DISTANCE) {
            alignedY = 0; // 顶部边界对齐
        } else if (Math.abs(elementBottom - CANVAS_HEIGHT) <= SNAP_DISTANCE) {
            alignedY = CANVAS_HEIGHT - draggedElement.getHeight(); // 底部边界对齐
        } else if (Math.abs(elementCenterY - CANVAS_HEIGHT / 2) <= SNAP_DISTANCE) {
            alignedY = (CANVAS_HEIGHT - draggedElement.getHeight()) / 2; // 画布中心对齐
        }
        
        return new Point((int)alignedX, (int)alignedY);
    }
    
    /**
     * 画布传输处理器，处理从模板面板的拖拽
     */
    private class CanvasTransferHandler extends TransferHandler {
        
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(TemplatePanel.TemplateTransferable.TEMPLATE_FLAVOR);
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support) || currentSlide == null) {
                return false;
            }
            
            try {
                TemplatePanel.TemplateItem templateItem = 
                    (TemplatePanel.TemplateItem) support.getTransferable().getTransferData(
                        TemplatePanel.TemplateTransferable.TEMPLATE_FLAVOR);
                
                // 创建模板的实例
                SlideElement<?> newElement = templateItem.createInstance();
                
                // 设置位置为拖拽位置
                Point dropPoint = support.getDropLocation().getDropPoint();
                Point scaledPoint = scalePoint(dropPoint);
                newElement.setPosition(scaledPoint.x - newElement.getWidth() / 2, 
                                     scaledPoint.y - newElement.getHeight() / 2);
                
                // 添加到幻灯片
                AddElementCommand command = new AddElementCommand(currentSlide, newElement);
                commandManager.executeCommand(command);
                
                // 选中新创建的元素
                currentSlide.selectElement(newElement);
                notifySelectionChanged();
                notifyContentChanged();
                repaint();
                
                return true;
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    private SlideElement<?> findTopmostElementAt(Point p) {
        if (currentSlide == null) return null;
        return currentSlide.findElementsAt(p).stream().findFirst().orElse(null);
    }
} 