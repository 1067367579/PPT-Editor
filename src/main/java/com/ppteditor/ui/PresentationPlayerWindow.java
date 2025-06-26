package com.ppteditor.ui;

import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.model.Slide;
import com.ppteditor.core.model.SlideElement;
import com.ppteditor.core.model.TextElement;
import com.ppteditor.core.enums.AnimationType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;

public class PresentationPlayerWindow extends JFrame {
    private Presentation presentation;
    private int currentSlideIndex;
    private PlayerCanvas canvas;
    private GraphicsDevice graphicsDevice;
    private Dimension screenSize;
    public PresentationPlayerWindow(Presentation presentation, int startSlideIndex) {
        this.presentation = presentation;
        this.currentSlideIndex = startSlideIndex;
        setTitle("演示模式");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsDevice = ge.getDefaultScreenDevice();
        screenSize = graphicsDevice.getDefaultConfiguration().getBounds().getSize();
        canvas = new PlayerCanvas(screenSize);
        add(canvas, BorderLayout.CENTER);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!handleHyperlinkClick(e)) {
                    nextSlide();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    exitPresentation();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    nextSlide();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_UP) {
                    previousSlide();
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    public void start() {
        setBounds(0, 0, screenSize.width, screenSize.height);
        setLocationRelativeTo(null); 
        setVisible(true);
        canvas.setCurrentSlide(presentation.getSlides().get(currentSlideIndex));
    }

    private void exitPresentation() {
        dispose();
    }

    private void nextSlide() {
        if (currentSlideIndex < presentation.getSlides().size() - 1) {
            Slide oldSlide = presentation.getSlides().get(currentSlideIndex);
            currentSlideIndex++;
            Slide newSlide = presentation.getSlides().get(currentSlideIndex);
            AnimationType animationType = presentation.getTransitionAnimation();
            int duration = presentation.getTransitionDuration();
            canvas.playTransition(oldSlide, newSlide, animationType, duration);
        } else {
            exitPresentation();
        }
    }

    private void previousSlide() {
        if (currentSlideIndex > 0) {
            currentSlideIndex--;
            Slide newSlide = presentation.getSlides().get(currentSlideIndex);
            canvas.setCurrentSlide(newSlide);
        }
    }
    
    /**
     * 处理超链接点击事件
     * @param e 鼠标事件
     * @return 如果点击了超链接并处理成功，返回true；否则返回false
     */
    private boolean handleHyperlinkClick(MouseEvent e) {
        if (currentSlideIndex >= presentation.getSlides().size()) {
            return false;
        }
        
        Slide currentSlide = presentation.getSlides().get(currentSlideIndex);
        if (currentSlide == null) {
            return false;
        }
        
        // 将屏幕坐标转换为幻灯片坐标
        Point slidePoint = convertScreenToSlideCoordinates(e.getPoint());
        if (slidePoint == null) {
            return false;
        }
        
        // 检查是否点击了任何元素的超链接
        for (SlideElement<?> element : currentSlide.getElements()) {
            if (element instanceof TextElement) {
                TextElement textElement = (TextElement) element;
                String hyperlink = textElement.getHyperlinkAtPoint(slidePoint);
                if (hyperlink != null && !hyperlink.trim().isEmpty()) {
                    openHyperlink(hyperlink);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 将屏幕坐标转换为幻灯片坐标
     */
    private Point convertScreenToSlideCoordinates(Point screenPoint) {
        try {
            // 计算缩放比例和偏移量（与渲染时的计算一致）
            double designWidth = SlideCanvas.CANVAS_WIDTH;
            double designHeight = SlideCanvas.CANVAS_HEIGHT;
            double scaleX = screenSize.width / designWidth;
            double scaleY = screenSize.height / designHeight;
            double scale = Math.min(scaleX, scaleY);
            
            double scaledWidth = designWidth * scale;
            double scaledHeight = designHeight * scale;
            double dx = (screenSize.width - scaledWidth) / 2;
            double dy = (screenSize.height - scaledHeight) / 2;
            
            // 转换坐标
            double slideX = (screenPoint.x - dx) / scale;
            double slideY = (screenPoint.y - dy) / scale;
            
            return new Point((int) slideX, (int) slideY);
        } catch (Exception ex) {
            System.err.println("坐标转换失败: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * 打开超链接
     */
    private void openHyperlink(String hyperlink) {
        try {
            System.out.println("打开超链接: " + hyperlink);
            
            // 确保URL格式正确
            String url = hyperlink;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            
            // 使用系统默认浏览器打开链接
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("成功打开超链接: " + url);
                } else {
                    System.err.println("系统不支持浏览器操作");
                    showHyperlinkMessage(url);
                }
            } else {
                System.err.println("系统不支持Desktop操作");
                showHyperlinkMessage(url);
            }
        } catch (Exception ex) {
            System.err.println("打开超链接失败: " + ex.getMessage());
            showHyperlinkMessage(hyperlink);
        }
    }
    
    /**
     * 显示超链接信息（当无法打开浏览器时）
     */
    private void showHyperlinkMessage(String url) {
        JOptionPane.showMessageDialog(this, 
            "无法自动打开浏览器，请手动访问:\n" + url, 
            "超链接", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private class PlayerCanvas extends JPanel {
        private Slide currentSlide;
        private Timer animationTimer;
        private float animationProgress = 0f;
        private BufferedImage previousSlideImage;
        private AnimationType currentAnimationType = AnimationType.NONE;
        private Dimension slideRenderSize;

        public PlayerCanvas(Dimension renderSize) {
            this.slideRenderSize = renderSize;
            setBackground(Color.BLACK);
            setPreferredSize(renderSize);
            
            // 添加鼠标移动监听器，用于处理超链接悬停效果
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    handleMouseMove(e);
                }
            });
        }

        public void setCurrentSlide(Slide slide) {
            this.currentSlide = slide;
            this.currentAnimationType = AnimationType.NONE;
            repaint();
        }

        public void playTransition(Slide from, Slide to, AnimationType type, int duration) {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            this.currentAnimationType = type;
            if (currentAnimationType == AnimationType.NONE) {
                setCurrentSlide(to);
                return;
            }
            previousSlideImage = new BufferedImage(slideRenderSize.width, slideRenderSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = previousSlideImage.createGraphics();
            applyRenderTransform(g2d, slideRenderSize);
            from.render(g2d, new Dimension(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT));
            g2d.dispose();
            this.currentSlide = to;
            this.animationProgress = 0f;
            int delay = 1000 / 60;
            int steps = duration / delay;
            animationTimer = new Timer(delay, e -> {
                animationProgress += 1.0f / steps;
                if (animationProgress >= 1.0f) {
                    animationProgress = 1.0f;
                    ((Timer) e.getSource()).stop();
                    currentAnimationType = AnimationType.NONE;
                    previousSlideImage = null;
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentSlide == null) {
                System.out.println("Current slide is null, drawing red rectangle as debug");
                g.setColor(Color.RED);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.drawString("NO SLIDE", 50, 50);
                return;
            }
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setColor(Color.BLUE);
            g2d.fillRect(10, 10, 100, 100);
            g2d.setColor(Color.WHITE);
            g2d.drawString("DEBUG", 20, 50);
            Dimension designSize = new Dimension(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT);
            if (currentAnimationType != AnimationType.NONE && animationProgress < 1.0f && previousSlideImage != null) {
                System.out.println("Rendering animation");
                renderTransitionAnimation(g2d, designSize);
            } else {
                System.out.println("Rendering normal slide");
                Graphics2D slideG2d = (Graphics2D) g2d.create();
                applyRenderTransform(slideG2d, slideRenderSize);
                currentSlide.render(slideG2d, designSize);
                slideG2d.dispose();
            }
            g2d.dispose();
        }
        
        private void renderTransitionAnimation(Graphics2D g2d, Dimension designSize) {
            switch (currentAnimationType) {
                case FADE:
                    renderFadeTransition(g2d, designSize);
                    break;
                case SLIDE_LEFT:
                    renderSlideTransition(g2d, designSize, -1, 0);
                    break;
                case SLIDE_RIGHT:
                    renderSlideTransition(g2d, designSize, 1, 0);
                    break;
                case SLIDE_UP:
                    renderSlideTransition(g2d, designSize, 0, -1);
                    break;
                case SLIDE_DOWN:
                    renderSlideTransition(g2d, designSize, 0, 1);
                    break;
                case ZOOM_IN:
                    renderZoomTransition(g2d, designSize, true);
                    break;
                case ZOOM_OUT:
                    renderZoomTransition(g2d, designSize, false);
                    break;
                case DISSOLVE:
                    renderDissolveTransition(g2d, designSize);
                    break;
                default:
                    renderFadeTransition(g2d, designSize);
                    break;
            }
        }
        
        private void renderFadeTransition(Graphics2D g2d, Dimension designSize) {
            g2d.drawImage(previousSlideImage, 0, 0, this);
            Graphics2D slideG2d = (Graphics2D) g2d.create();
            slideG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animationProgress));
            applyRenderTransform(slideG2d, slideRenderSize);
            currentSlide.render(slideG2d, designSize);
            slideG2d.dispose();
        }
        
        private void renderSlideTransition(Graphics2D g2d, Dimension designSize, int dirX, int dirY) {
            // 计算偏移量
            int offsetX = (int) (slideRenderSize.width * dirX * (1 - animationProgress));
            int offsetY = (int) (slideRenderSize.height * dirY * (1 - animationProgress));
            // 绘制旧幻灯片
            int oldOffsetX = (int) (slideRenderSize.width * (-dirX) * animationProgress);
            int oldOffsetY = (int) (slideRenderSize.height * (-dirY) * animationProgress);
            g2d.drawImage(previousSlideImage, oldOffsetX, oldOffsetY, this);
            // 绘制新幻灯片
            Graphics2D slideG2d = (Graphics2D) g2d.create();
            slideG2d.translate(offsetX, offsetY);
            applyRenderTransform(slideG2d, slideRenderSize);
            currentSlide.render(slideG2d, designSize);
            slideG2d.dispose();
        }
        
        private void renderZoomTransition(Graphics2D g2d, Dimension designSize, boolean zoomIn) {
            g2d.drawImage(previousSlideImage, 0, 0, this);
            Graphics2D slideG2d = (Graphics2D) g2d.create();
            // 计算缩放和透明度
            float scale = zoomIn ? animationProgress : (2.0f - animationProgress);
            float alpha = animationProgress;
            // 计算居中位置
            int centerX = slideRenderSize.width / 2;
            int centerY = slideRenderSize.height / 2;
            slideG2d.translate(centerX, centerY);
            slideG2d.scale(scale, scale);
            slideG2d.translate(-centerX, -centerY);
            slideG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            applyRenderTransform(slideG2d, slideRenderSize);
            currentSlide.render(slideG2d, designSize);
            slideG2d.dispose();
        }
        
        private void renderDissolveTransition(Graphics2D g2d, Dimension designSize) {
            // 创建随机点阵溶解效果
            BufferedImage newSlideImage = new BufferedImage(slideRenderSize.width, slideRenderSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D newG2d = newSlideImage.createGraphics();
            applyRenderTransform(newG2d, slideRenderSize);
            currentSlide.render(newG2d, designSize);
            newG2d.dispose();
            // 绘制旧幻灯片
            g2d.drawImage(previousSlideImage, 0, 0, this);
            // 基于进度创建溶解效果
            int pixelThreshold = (int) (255 * animationProgress);
            for (int x = 0; x < slideRenderSize.width; x += 2) {
                for (int y = 0; y < slideRenderSize.height; y += 2) {
                    // 使用简单的伪随机函数
                    int randomValue = ((x * 31 + y * 17) % 256);
                    if (randomValue < pixelThreshold) {
                        // 绘制新幻灯片的像素块
                        if (x < newSlideImage.getWidth() && y < newSlideImage.getHeight()) {
                            int rgb = newSlideImage.getRGB(x, y);
                            g2d.setColor(new Color(rgb, true));
                            g2d.fillRect(x, y, 2, 2);
                        }
                    }
                }
            }
        }
        
        private void applyRenderTransform(Graphics2D g2d, Dimension targetSize) {
            double designWidth = SlideCanvas.CANVAS_WIDTH;
            double designHeight = SlideCanvas.CANVAS_HEIGHT;
            double scaleX = targetSize.width / designWidth;
            double scaleY = targetSize.height / designHeight;
            double scale = Math.min(scaleX, scaleY);
            double scaledWidth = designWidth * scale;
            double scaledHeight = designHeight * scale;
            double dx = (targetSize.width - scaledWidth) / 2;
            double dy = (targetSize.height - scaledHeight) / 2;
            g2d.translate(dx, dy);
            g2d.scale(scale, scale);
        }
        
        /**
         * 处理鼠标移动事件，用于超链接悬停效果
         */
        private void handleMouseMove(MouseEvent e) {
            if (currentSlide == null) {
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // 将屏幕坐标转换为幻灯片坐标
            Point slidePoint = convertCanvasToSlideCoordinates(e.getPoint());
            if (slidePoint == null) {
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // 检查是否悬停在超链接上
            boolean overHyperlink = false;
            for (SlideElement<?> element : currentSlide.getElements()) {
                if (element instanceof TextElement) {
                    TextElement textElement = (TextElement) element;
                    String hyperlink = textElement.getHyperlinkAtPoint(slidePoint);
                    if (hyperlink != null && !hyperlink.trim().isEmpty()) {
                        overHyperlink = true;
                        break;
                    }
                }
            }
            
            // 设置鼠标光标
            setCursor(overHyperlink ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) 
                                   : Cursor.getDefaultCursor());
        }
        
        /**
         * 将画布坐标转换为幻灯片坐标
         */
        private Point convertCanvasToSlideCoordinates(Point canvasPoint) {
            try {
                // 计算缩放比例和偏移量（与渲染时的计算一致）
                double designWidth = SlideCanvas.CANVAS_WIDTH;
                double designHeight = SlideCanvas.CANVAS_HEIGHT;
                double scaleX = slideRenderSize.width / designWidth;
                double scaleY = slideRenderSize.height / designHeight;
                double scale = Math.min(scaleX, scaleY);
                
                double scaledWidth = designWidth * scale;
                double scaledHeight = designHeight * scale;
                double dx = (slideRenderSize.width - scaledWidth) / 2;
                double dy = (slideRenderSize.height - scaledHeight) / 2;
                
                // 转换坐标
                double slideX = (canvasPoint.x - dx) / scale;
                double slideY = (canvasPoint.y - dy) / scale;
                
                return new Point((int) slideX, (int) slideY);
            } catch (Exception ex) {
                System.err.println("画布坐标转换失败: " + ex.getMessage());
                return null;
            }
        }
    }
} 