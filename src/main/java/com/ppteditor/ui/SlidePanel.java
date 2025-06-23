package com.ppteditor.ui;

import com.ppteditor.core.model.*;
import com.ppteditor.core.command.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 幻灯片缩略图面板
 * 显示所有幻灯片的缩略图，支持选择、拖拽排序等操作
 */
public class SlidePanel extends JPanel {
    
    private static final int THUMBNAIL_WIDTH = 120;
    private static final int THUMBNAIL_HEIGHT = 68;
    private static final int ITEM_HEIGHT = 90;
    
    private Presentation presentation;
    private JList<Slide> slideList;
    private DefaultListModel<Slide> listModel;
    private Runnable onSlideSelected;
    private CommandManager commandManager;
    
    // 拖拽状态
    private int draggedIndex = -1;
    private boolean isDragging = false;
    private Point dragStartPoint;
    
    public SlidePanel() {
        this.commandManager = CommandManager.getInstance();
        this.listModel = new DefaultListModel<>();
        
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(180, 250));
        setPreferredSize(new Dimension(200, 350));
        setBorder(BorderFactory.createTitledBorder("幻灯片"));
        
        // 创建幻灯片列表
        slideList = new JList<>(listModel);
        slideList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        slideList.setCellRenderer(new SlideCellRenderer());
        slideList.setFixedCellHeight(ITEM_HEIGHT);
        
        // 创建工具栏
        JToolBar toolBar = createToolBar();
        
        // 布局
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(slideList), BorderLayout.CENTER);
    }
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);
        
        JButton addButton = new JButton("＋");
        addButton.setToolTipText("新建幻灯片");
        addButton.addActionListener(e -> addNewSlide());
        
        JButton deleteButton = new JButton("－");
        deleteButton.setToolTipText("删除幻灯片");
        deleteButton.addActionListener(e -> deleteSelectedSlide());
        
        JButton duplicateButton = new JButton("📋");
        duplicateButton.setToolTipText("复制幻灯片");
        duplicateButton.addActionListener(e -> duplicateSelectedSlide());
        
        toolBar.add(addButton);
        toolBar.add(deleteButton);
        toolBar.add(duplicateButton);
        
        return toolBar;
    }
    
    private void setupEventHandlers() {
        // 选择事件
        slideList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                notifySlideSelected();
            }
        });
        
        // 鼠标事件 - 支持拖拽排序
        slideList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = slideList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        slideList.setSelectedIndex(index);
                        showContextMenu(e.getPoint());
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    int index = slideList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        slideList.setSelectedIndex(index);
                        draggedIndex = index;
                        dragStartPoint = e.getPoint();
                        isDragging = false;
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && draggedIndex >= 0) {
                    int dropIndex = slideList.locationToIndex(e.getPoint());
                    if (dropIndex >= 0 && dropIndex != draggedIndex) {
                        moveSlide(draggedIndex, dropIndex);
                    }
                }
                
                // 恢复正常状态
                draggedIndex = -1;
                isDragging = false;
                dragStartPoint = null;
                slideList.setCursor(Cursor.getDefaultCursor());
                slideList.repaint();
            }
        });
        
        slideList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedIndex >= 0 && dragStartPoint != null) {
                    int distance = Math.abs(e.getY() - dragStartPoint.y);
                    if (distance > 5) { // 设置合适的拖拽阈值
                        isDragging = true;
                        slideList.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        
                        // 自动滚动支持
                        Rectangle visible = slideList.getVisibleRect();
                        if (e.getY() < visible.y + 20) {
                            // 向上滚动
                            slideList.scrollRectToVisible(new Rectangle(0, e.getY() - 50, 1, 1));
                        } else if (e.getY() > visible.y + visible.height - 20) {
                            // 向下滚动
                            slideList.scrollRectToVisible(new Rectangle(0, e.getY() + 50, 1, 1));
                        }
                        
                        slideList.repaint();
                    }
                }
            }
        });
    }
    
    private void showContextMenu(Point point) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem addItem = new JMenuItem("新建幻灯片");
        addItem.addActionListener(e -> addNewSlide());
        
        JMenuItem duplicateItem = new JMenuItem("复制幻灯片");
        duplicateItem.addActionListener(e -> duplicateSelectedSlide());
        
        JMenuItem deleteItem = new JMenuItem("删除幻灯片");
        deleteItem.addActionListener(e -> deleteSelectedSlide());
        
        JMenuItem renameItem = new JMenuItem("重命名");
        renameItem.addActionListener(e -> renameSelectedSlide());
        
        menu.add(addItem);
        menu.add(duplicateItem);
        menu.addSeparator();
        menu.add(renameItem);
        menu.addSeparator();
        menu.add(deleteItem);
        
        menu.show(slideList, point.x, point.y);
    }
    
    public void setPresentation(Presentation presentation) {
        this.presentation = presentation;
        updateSlideList();
    }
    
    public void updateSlideList() {
        listModel.clear();
        if (presentation != null) {
            List<Slide> slides = presentation.getSlides();
            for (Slide slide : slides) {
                listModel.addElement(slide);
            }
            
            // 选中当前幻灯片
            int currentIndex = presentation.getCurrentSlideIndex();
            if (currentIndex >= 0 && currentIndex < listModel.size()) {
                slideList.setSelectedIndex(currentIndex);
            }
        }
    }
    
    public void setOnSlideSelected(Runnable callback) {
        this.onSlideSelected = callback;
    }
    
    public Slide getSelectedSlide() {
        return slideList.getSelectedValue();
    }
    
    public int getSelectedIndex() {
        return slideList.getSelectedIndex();
    }
    
    private void addNewSlide() {
        if (presentation == null) return;
        
        Slide newSlide = new Slide("幻灯片 " + (presentation.getTotalSlides() + 1));
        
        AddSlideCommand command = new AddSlideCommand(presentation, newSlide);
        commandManager.executeCommand(command);
        
        updateSlideList();
        slideList.setSelectedIndex(presentation.getTotalSlides() - 1);
        notifySlideSelected();
    }
    
    private void deleteSelectedSlide() {
        if (presentation == null || slideList.getSelectedIndex() < 0) return;
        
        if (presentation.getTotalSlides() <= 1) {
            JOptionPane.showMessageDialog(this, "至少需要保留一张幻灯片", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要删除选中的幻灯片吗？", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            int selectedIndex = slideList.getSelectedIndex();
            Slide selectedSlide = slideList.getSelectedValue();
            
            RemoveSlideCommand command = new RemoveSlideCommand(presentation, selectedSlide, selectedIndex);
            commandManager.executeCommand(command);
            
            updateSlideList();
            
            // 选择合适的幻灯片
            int newIndex = Math.min(selectedIndex, presentation.getTotalSlides() - 1);
            slideList.setSelectedIndex(newIndex);
            notifySlideSelected();
        }
    }
    
    private void duplicateSelectedSlide() {
        if (presentation == null || slideList.getSelectedIndex() < 0) return;
        
        Slide selectedSlide = slideList.getSelectedValue();
        Slide duplicatedSlide = selectedSlide.clone();
        
        int insertIndex = slideList.getSelectedIndex() + 1;
        AddSlideCommand command = new AddSlideCommand(presentation, duplicatedSlide, insertIndex);
        commandManager.executeCommand(command);
        
        updateSlideList();
        slideList.setSelectedIndex(insertIndex);
        notifySlideSelected();
    }
    
    private void renameSelectedSlide() {
        if (presentation == null || slideList.getSelectedIndex() < 0) return;
        
        Slide selectedSlide = slideList.getSelectedValue();
        String newName = JOptionPane.showInputDialog(this, "重命名幻灯片:", selectedSlide.getName());
        
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(selectedSlide.getName())) {
            RenameSlideCommand command = new RenameSlideCommand(selectedSlide, selectedSlide.getName(), newName.trim());
            commandManager.executeCommand(command);
            
            slideList.repaint();
        }
    }
    
    private void notifySlideSelected() {
        if (presentation != null && slideList.getSelectedIndex() >= 0) {
            presentation.goToSlide(slideList.getSelectedIndex());
        }
        
        if (onSlideSelected != null) {
            onSlideSelected.run();
        }
    }
    
    private void moveSlide(int fromIndex, int toIndex) {
        if (presentation == null || fromIndex < 0 || toIndex < 0) return;
        if (fromIndex >= presentation.getTotalSlides() || toIndex >= presentation.getTotalSlides()) return;
        if (fromIndex == toIndex) return;
        
        // 使用Presentation的moveSlide方法，这样会正确处理内部状态
        presentation.moveSlide(fromIndex, toIndex);
        
        // 更新显示
        updateSlideList();
        slideList.setSelectedIndex(toIndex);
        presentation.goToSlide(toIndex);
        notifySlideSelected();
        
        System.out.println("移动幻灯片: " + fromIndex + " -> " + toIndex);
    }
    
    /**
     * 自定义列表渲染器，显示幻灯片缩略图
     */
    private class SlideCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof Slide) {
                Slide slide = (Slide) value;
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(true);
                
                // 拖拽状态的视觉反馈
                boolean isDraggedItem = isDragging && index == draggedIndex;
                
                if (isDraggedItem) {
                    // 拖拽中的项目使用半透明效果
                    panel.setBackground(new Color(200, 200, 255, 150));
                    panel.setBorder(BorderFactory.createDashedBorder(new Color(100, 100, 255), 2, 3, 3, true));
                } else if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    panel.setBorder(BorderFactory.createLineBorder(list.getSelectionBackground().darker(), 2));
                } else {
                    panel.setBackground(list.getBackground());
                    panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                }
                
                // 创建缩略图
                BufferedImage thumbnail = createThumbnail(slide);
                JLabel imageLabel = new JLabel(new ImageIcon(thumbnail));
                
                // 根据状态设置边框
                if (isDraggedItem) {
                    imageLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 255), 2));
                } else {
                    imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }
                
                // 创建文字标签
                JLabel textLabel = new JLabel((index + 1) + ". " + slide.getName());
                if (isDraggedItem) {
                    textLabel.setForeground(new Color(100, 100, 255));
                } else {
                    textLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                }
                textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
                textLabel.setHorizontalAlignment(JLabel.CENTER);
                
                panel.add(imageLabel, BorderLayout.CENTER);
                panel.add(textLabel, BorderLayout.SOUTH);
                
                return panel;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        
        private BufferedImage createThumbnail(Slide slide) {
            BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 计算缩放比例
                double scaleX = (double) THUMBNAIL_WIDTH / SlideCanvas.CANVAS_WIDTH;
                double scaleY = (double) THUMBNAIL_HEIGHT / SlideCanvas.CANVAS_HEIGHT;
                g2d.scale(scaleX, scaleY);
                
                // 渲染幻灯片
                Dimension slideSize = new Dimension(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT);
                slide.render(g2d, slideSize);
                
            } finally {
                g2d.dispose();
            }
            
            return image;
        }
    }
} 