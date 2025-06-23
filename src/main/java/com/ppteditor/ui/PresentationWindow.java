package com.ppteditor.ui;

import com.ppteditor.core.enums.AnimationType;
import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.model.Slide;
import com.ppteditor.core.model.SlideElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 演示模式窗口
 * 支持全屏展示、切换动画、键盘控制
 */
public class PresentationWindow extends JWindow implements KeyListener {
    
    private Presentation presentation;
    private int currentSlideIndex = 0;
    private Slide currentSlide;
    
    // UI组件
    private JPanel slidePanel;
    private JLabel slideNumberLabel;
    private Timer animationTimer;
    
    // 动画相关
    private boolean isAnimating = false;
    private BufferedImage previousSlideImage;
    private BufferedImage currentSlideImage;
    private double animationProgress = 0.0;
    private AnimationType currentAnimation = AnimationType.NONE;
    
    public PresentationWindow(Presentation presentation) {
        this.presentation = presentation;
        this.currentSlideIndex = 0;
        this.currentSlide = presentation.getSlides().isEmpty() ? 
            null : presentation.getSlides().get(0);
            
        initializeUI();
        loadCurrentSlide();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        
        // 主要幻灯片显示区域
        slidePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintSlide((Graphics2D) g);
            }
        };
        slidePanel.setBackground(Color.BLACK);
        slidePanel.setFocusable(true);
        slidePanel.addKeyListener(this);
        
        add(slidePanel, BorderLayout.CENTER);
        
        // 状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(0, 0, 0, 150));
        statusPanel.setOpaque(true);
        
        // 左侧：幻灯片编号
        slideNumberLabel = new JLabel();
        slideNumberLabel.setForeground(Color.WHITE);
        slideNumberLabel.setFont(new Font("Arial", Font.BOLD, 14));
        slideNumberLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        updateSlideNumber();
        
        // 中间：控制按钮
        JPanel controlPanel = createControlPanel();
        
        // 右侧：退出按钮
        JButton exitButton = createStyledButton("× 退出", new Color(220, 53, 69));
        exitButton.addActionListener(e -> exitPresentation());
        
        statusPanel.add(slideNumberLabel, BorderLayout.WEST);
        statusPanel.add(controlPanel, BorderLayout.CENTER);
        statusPanel.add(exitButton, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        // 添加悬浮控制面板
        JPanel floatingControls = createFloatingControls();
        add(floatingControls, BorderLayout.NORTH);
        
        // 设置键盘监听器 - 使用InputMap和ActionMap确保键盘事件能被捕获
        setupKeyboardHandling();
        
        // 设置全屏
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        }
        
        setVisible(true);
        
        // 强制获得键盘焦点 - 多重保障
        SwingUtilities.invokeLater(() -> {
            this.requestFocus();
            this.toFront();
            slidePanel.requestFocus();
            System.out.println("演示模式：键盘焦点已设置");
        });
        
        // 添加全局鼠标监听器
        slidePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                slidePanel.requestFocus();
                PresentationWindow.this.requestFocus();
                System.out.println("演示模式：鼠标点击，重新获取焦点");
            }
        });
        
        // 添加窗口焦点监听器
        this.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                slidePanel.requestFocus();
                System.out.println("演示模式：窗口获得焦点");
            }
        });
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setOpaque(false);
        
        // 上一页按钮
        JButton prevButton = createStyledButton("◄ 上一页", new Color(108, 117, 125));
        prevButton.addActionListener(e -> {
            previousSlide();
            slidePanel.requestFocus(); // 确保焦点回到主面板
        });
        
        // 下一页按钮
        JButton nextButton = createStyledButton("下一页 ►", new Color(40, 167, 69));
        nextButton.addActionListener(e -> {
            nextSlide();
            slidePanel.requestFocus(); // 确保焦点回到主面板
        });
        
        // 从头开始按钮
        JButton restartButton = createStyledButton("⟲ 从头开始", new Color(23, 162, 184));
        restartButton.addActionListener(e -> {
            goToSlide(0);
            slidePanel.requestFocus(); // 确保焦点回到主面板
        });
        
        // 帮助文本
        JLabel helpLabel = new JLabel("ESC:退出 | ←→:翻页 | F5:从头");
        helpLabel.setForeground(new Color(200, 200, 200));
        helpLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        panel.add(prevButton);
        panel.add(nextButton);
        panel.add(restartButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(helpLabel);
        
        return panel;
    }
    
    private JPanel createFloatingControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);
        
        // 快速跳转按钮
        JButton firstButton = createSmallButton("⟪");
        firstButton.setToolTipText("跳到第一页");
        firstButton.addActionListener(e -> {
            goToSlide(0);
            slidePanel.requestFocus();
        });
        
        JButton lastButton = createSmallButton("⟫");
        lastButton.setToolTipText("跳到最后一页");
        lastButton.addActionListener(e -> {
            goToSlide(presentation.getSlides().size() - 1);
            slidePanel.requestFocus();
        });
        
        panel.add(firstButton);
        panel.add(lastButton);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 鼠标悬停效果
        Color hoverColor = bgColor.darker();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(52, 58, 64));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(35, 30));
        
        Color hoverColor = new Color(108, 117, 125);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 58, 64));
            }
        });
        
        return button;
    }
    
    private void setupKeyboardHandling() {
        // 设置键盘监听器到窗口级别和面板级别
        this.addKeyListener(this);
        this.setFocusable(true);
        
        // 使用InputMap和ActionMap确保键盘事件能被正确处理
        InputMap inputMap = slidePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = slidePanel.getActionMap();
        
        // ESC键
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        actionMap.put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：ESC键被按下，退出演示");
                exitPresentation();
            }
        });
        
        // 右箭头键和空格键 - 下一页
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "next");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "next");
        actionMap.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：下一页键被按下");
                nextSlide();
            }
        });
        
        // 左箭头键 - 上一页
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "prev");
        actionMap.put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：上一页键被按下");
                previousSlide();
            }
        });
        
        // F5键 - 从头开始
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "restart");
        actionMap.put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：F5键被按下，从头开始");
                goToSlide(0);
            }
        });
        
        // Home键 - 第一页
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "first");
        actionMap.put("first", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：Home键被按下，跳到第一页");
                goToSlide(0);
            }
        });
        
        // End键 - 最后一页
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "last");
        actionMap.put("last", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("演示模式：End键被按下，跳到最后一页");
                goToSlide(presentation.getSlides().size() - 1);
            }
        });
    }
    
    private void paintSlide(Graphics2D g2d) {
        if (currentSlide == null) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String message = "没有幻灯片";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
            return;
        }
        
        // 启用反锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 计算缩放比例
        double scaleX = (double) getWidth() / SlideCanvas.CANVAS_WIDTH;
        double scaleY = (double) getHeight() / SlideCanvas.CANVAS_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        
        // 计算居中位置
        int offsetX = (int) ((getWidth() - SlideCanvas.CANVAS_WIDTH * scale) / 2);
        int offsetY = (int) ((getHeight() - SlideCanvas.CANVAS_HEIGHT * scale) / 2);
        
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        if (isAnimating && previousSlideImage != null && currentSlideImage != null) {
            // 绘制动画
            renderAnimation(g2d);
        } else {
            // 绘制当前幻灯片
            renderSlide(g2d, currentSlide);
        }
    }
    
    private void renderSlide(Graphics2D g2d, Slide slide) {
        // 绘制背景
        g2d.setColor(slide.getBackgroundColor());
        g2d.fillRect(0, 0, SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT);
        
        // 绘制所有元素
        List<SlideElement<?>> elements = slide.getElements();
        elements.stream()
                .sorted((a, b) -> Integer.compare(a.getZIndex(), b.getZIndex()))
                .forEach(element -> element.draw(g2d));
    }
    
    private void renderAnimation(Graphics2D g2d) {
        switch (currentAnimation) {
            case FADE:
                renderFadeAnimation(g2d);
                break;
            case SLIDE_LEFT:
                renderSlideAnimation(g2d, -1, 0);
                break;
            case SLIDE_RIGHT:
                renderSlideAnimation(g2d, 1, 0);
                break;
            case SLIDE_UP:
                renderSlideAnimation(g2d, 0, -1);
                break;
            case SLIDE_DOWN:
                renderSlideAnimation(g2d, 0, 1);
                break;
            case ZOOM_IN:
                renderZoomAnimation(g2d, true);
                break;
            case ZOOM_OUT:
                renderZoomAnimation(g2d, false);
                break;
            default:
                renderSlide(g2d, currentSlide);
                break;
        }
    }
    
    private void renderFadeAnimation(Graphics2D g2d) {
        if (previousSlideImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                (float)(1.0 - animationProgress)));
            g2d.drawImage(previousSlideImage, 0, 0, SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
        }
        
        if (currentSlideImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                (float)animationProgress));
            g2d.drawImage(currentSlideImage, 0, 0, SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void renderSlideAnimation(Graphics2D g2d, int dirX, int dirY) {
        int offsetX = (int)(dirX * SlideCanvas.CANVAS_WIDTH * (1.0 - animationProgress));
        int offsetY = (int)(dirY * SlideCanvas.CANVAS_HEIGHT * (1.0 - animationProgress));
        
        if (previousSlideImage != null) {
            g2d.drawImage(previousSlideImage, 
                offsetX - dirX * SlideCanvas.CANVAS_WIDTH, 
                offsetY - dirY * SlideCanvas.CANVAS_HEIGHT, 
                SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
        }
        
        if (currentSlideImage != null) {
            g2d.drawImage(currentSlideImage, offsetX, offsetY, 
                SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
        }
    }
    
    private void renderZoomAnimation(Graphics2D g2d, boolean zoomIn) {
        double scale = zoomIn ? animationProgress : (1.0 - animationProgress);
        double inverseScale = zoomIn ? (1.0 - animationProgress) : animationProgress;
        
        // 绘制前一张幻灯片（缩放出去）
        if (previousSlideImage != null) {
            Graphics2D g2dPrev = (Graphics2D) g2d.create();
            g2dPrev.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)inverseScale));
            
            int centerX = SlideCanvas.CANVAS_WIDTH / 2;
            int centerY = SlideCanvas.CANVAS_HEIGHT / 2;
            g2dPrev.translate(centerX, centerY);
            g2dPrev.scale(zoomIn ? (1.0 + animationProgress) : (1.0 - animationProgress), 
                         zoomIn ? (1.0 + animationProgress) : (1.0 - animationProgress));
            g2dPrev.translate(-centerX, -centerY);
            
            g2dPrev.drawImage(previousSlideImage, 0, 0, SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
            g2dPrev.dispose();
        }
        
        // 绘制当前幻灯片（缩放进来）
        if (currentSlideImage != null) {
            Graphics2D g2dCurrent = (Graphics2D) g2d.create();
            g2dCurrent.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)scale));
            
            int centerX = SlideCanvas.CANVAS_WIDTH / 2;
            int centerY = SlideCanvas.CANVAS_HEIGHT / 2;
            g2dCurrent.translate(centerX, centerY);
            g2dCurrent.scale(zoomIn ? animationProgress : (1.0 + animationProgress), 
                           zoomIn ? animationProgress : (1.0 + animationProgress));
            g2dCurrent.translate(-centerX, -centerY);
            
            g2dCurrent.drawImage(currentSlideImage, 0, 0, SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, null);
            g2dCurrent.dispose();
        }
    }
    
    private BufferedImage createSlideImage(Slide slide) {
        BufferedImage image = new BufferedImage(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT, 
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderSlide(g2d, slide);
        g2d.dispose();
        return image;
    }
    
    private void loadCurrentSlide() {
        if (presentation.getSlides().isEmpty()) {
            currentSlide = null;
            updateSlideNumber();
            repaint();
            return;
        }
        
        currentSlideIndex = Math.max(0, Math.min(currentSlideIndex, presentation.getSlides().size() - 1));
        currentSlide = presentation.getSlides().get(currentSlideIndex);
        updateSlideNumber();
        repaint();
    }
    
    private void updateSlideNumber() {
        if (presentation.getSlides().isEmpty()) {
            slideNumberLabel.setText("0 / 0");
        } else {
            slideNumberLabel.setText((currentSlideIndex + 1) + " / " + presentation.getSlides().size());
        }
    }
    
    public void nextSlide() {
        if (isAnimating || presentation.getSlides().isEmpty()) return;
        
        if (currentSlideIndex < presentation.getSlides().size() - 1) {
            animateToSlide(currentSlideIndex + 1);
        }
    }
    
    public void previousSlide() {
        if (isAnimating || presentation.getSlides().isEmpty()) return;
        
        if (currentSlideIndex > 0) {
            animateToSlide(currentSlideIndex - 1);
        }
    }
    
    public void goToSlide(int index) {
        if (isAnimating || presentation.getSlides().isEmpty()) return;
        
        if (index >= 0 && index < presentation.getSlides().size() && index != currentSlideIndex) {
            animateToSlide(index);
        }
    }
    
    private void animateToSlide(int targetIndex) {
        Slide targetSlide = presentation.getSlides().get(targetIndex);
        
        // 准备动画
        previousSlideImage = currentSlide != null ? createSlideImage(currentSlide) : null;
        currentSlideImage = createSlideImage(targetSlide);
        currentAnimation = targetSlide.getTransitionAnimation();
        animationProgress = 0.0;
        isAnimating = true;
        
        // 更新当前幻灯片
        currentSlideIndex = targetIndex;
        currentSlide = targetSlide;
        updateSlideNumber();
        
        // 启动动画定时器
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        animationTimer = new Timer();
        TimerTask animationTask = new TimerTask() {
            @Override
            public void run() {
                animationProgress += 0.05; // 动画速度
                
                if (animationProgress >= 1.0) {
                    animationProgress = 1.0;
                    isAnimating = false;
                    previousSlideImage = null;
                    currentSlideImage = null;
                    animationTimer.cancel();
                }
                
                SwingUtilities.invokeLater(() -> repaint());
            }
        };
        
        animationTimer.scheduleAtFixedRate(animationTask, 0, 16); // ~60fps
    }
    
    public void exitPresentation() {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        // 退出全屏
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.getFullScreenWindow() == this) {
            gd.setFullScreenWindow(null);
        }
        
        dispose();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("演示模式：keyPressed被调用，按键代码: " + e.getKeyCode() + " (" + KeyEvent.getKeyText(e.getKeyCode()) + ")");
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                System.out.println("演示模式：ESC键处理（备用方法）");
                exitPresentation();
                break;
                
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_PAGE_DOWN:
                System.out.println("演示模式：下一页键处理（备用方法）");
                nextSlide();
                break;
                
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_PAGE_UP:
                System.out.println("演示模式：上一页键处理（备用方法）");
                previousSlide();
                break;
                
            case KeyEvent.VK_HOME:
                System.out.println("演示模式：Home键处理（备用方法）");
                goToSlide(0);
                break;
                
            case KeyEvent.VK_END:
                System.out.println("演示模式：End键处理（备用方法）");
                goToSlide(presentation.getSlides().size() - 1);
                break;
                
            case KeyEvent.VK_F5:
                System.out.println("演示模式：F5键处理（备用方法）");
                goToSlide(0);
                break;
                
            default:
                // 数字键直接跳转
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
                    int slideNumber = e.getKeyCode() - KeyEvent.VK_1;
                    if (slideNumber < presentation.getSlides().size()) {
                        System.out.println("演示模式：数字键跳转到第" + (slideNumber + 1) + "页");
                        goToSlide(slideNumber);
                    }
                }
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
} 