package com.ppteditor.ui;

import com.ppteditor.core.model.Presentation;
import com.ppteditor.core.model.Slide;
import com.ppteditor.core.enums.AnimationType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
                nextSlide();
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
        // We will use a simulated full-screen (undecorated frame set to screen size) 
        // as it's more reliable across different platforms than the exclusive full-screen mode.
        setBounds(0, 0, screenSize.width, screenSize.height);
        setLocationRelativeTo(null); // This is somewhat redundant but harmless
        setVisible(true);

        canvas.setCurrentSlide(presentation.getSlides().get(currentSlideIndex));
    }

    private void exitPresentation() {
        // No longer need to exit full-screen mode, just dispose the frame.
        dispose();
    }

    private void nextSlide() {
        if (currentSlideIndex < presentation.getSlides().size() - 1) {
            Slide oldSlide = presentation.getSlides().get(currentSlideIndex);
            currentSlideIndex++;
            Slide newSlide = presentation.getSlides().get(currentSlideIndex);
            
            // Use the global transition settings from the presentation object
            AnimationType animationType = presentation.getTransitionAnimation();
            int duration = presentation.getTransitionDuration();
            
            canvas.playTransition(oldSlide, newSlide, animationType, duration);
        } else {
            // Reached the end
            exitPresentation();
        }
    }

    private void previousSlide() {
        if (currentSlideIndex > 0) {
            currentSlideIndex--;
            Slide newSlide = presentation.getSlides().get(currentSlideIndex);
            // Go back instantly without any transition
            canvas.setCurrentSlide(newSlide);
        }
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

            System.out.println("PlayerCanvas paintComponent called");
            System.out.println("Current slide: " + (currentSlide != null ? currentSlide.getName() : "null"));
            System.out.println("Panel size: " + getWidth() + "x" + getHeight());
            System.out.println("Slide render size: " + slideRenderSize.width + "x" + slideRenderSize.height);

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

            System.out.println("Elements in slide: " + currentSlide.getElements().size());
            System.out.println("Animation type: " + currentAnimationType);
            System.out.println("Animation progress: " + animationProgress);

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
    }
} 