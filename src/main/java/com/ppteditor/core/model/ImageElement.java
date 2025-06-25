package com.ppteditor.core.model;

import com.ppteditor.core.enums.ElementType;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图片元素类
 * 支持加载和显示各种格式的图片
 */
public class ImageElement extends SlideElement<ShapeStyle> implements java.io.Serializable {
    
    private String imagePath;
    private transient BufferedImage image;
    private boolean maintainAspectRatio;
    
    public ImageElement() {
        super(ElementType.IMAGE);
        this.style = createDefaultStyle();
        this.maintainAspectRatio = true;
    }
    
    public ImageElement(String imagePath) throws IOException {
        this();
        loadImage(imagePath);
    }
    
    public ImageElement(File imageFile) throws IOException {
        this();
        loadImage(imageFile.getAbsolutePath());
    }
    
    public void loadImage(String imagePath) throws IOException {
        File file = new File(imagePath);
        if (!file.exists()) {
            throw new IOException("图片文件不存在: " + imagePath);
        }
        
        this.imagePath = imagePath;
        this.image = ImageIO.read(file);
        
        if (image == null) {
            throw new IOException("无法读取图片文件: " + imagePath);
        }
        
        // 自动设置元素大小为图片原始大小（可能需要缩放）
        if (width == 0 || height == 0) {
            width = image.getWidth();
            height = image.getHeight();
            
            // 如果图片太大，缩放到合适大小
            double maxWidth = 400;
            double maxHeight = 300;
            
            if (width > maxWidth || height > maxHeight) {
                double scaleX = maxWidth / width;
                double scaleY = maxHeight / height;
                double scale = Math.min(scaleX, scaleY);
                
                width *= scale;
                height *= scale;
            }
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (image == null && imagePath != null) {
            try {
                loadImage(imagePath);
            } catch (IOException e) {
                // 加载失败，绘制错误提示
                renderErrorPlaceholder(g2d);
                return;
            }
        }
        
        if (image == null) {
            renderPlaceholder(g2d);
            return;
        }
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制图片
        g2d.drawImage(image, (int)x, (int)y, (int)width, (int)height, null);
        
        // 绘制边框（如果有）
        if (style != null) {
            Color borderColor = style.getEffectiveBorderColor();
            if (borderColor != null && style.getBorderWidth() > 0) {
                g2d.setColor(borderColor);
                g2d.setStroke(style.getBorderStroke());
                g2d.drawRect((int)x, (int)y, (int)width, (int)height);
            }
        }
    }
    
    private void renderPlaceholder(Graphics2D g2d) {
        // 绘制图片占位符
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect((int)x, (int)y, (int)width, (int)height);
        
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
        g2d.drawRect((int)x, (int)y, (int)width, (int)height);
        
        // 绘制图片图标
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "图片";
        int textX = (int)(x + (width - fm.stringWidth(text)) / 2);
        int textY = (int)(y + (height + fm.getAscent()) / 2);
        g2d.drawString(text, textX, textY);
    }
    
    private void renderErrorPlaceholder(Graphics2D g2d) {
        // 绘制错误占位符
        g2d.setColor(new Color(255, 200, 200));
        g2d.fillRect((int)x, (int)y, (int)width, (int)height);
        
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int)x, (int)y, (int)width, (int)height);
        
        // 绘制错误信息
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "图片加载失败";
        int textX = (int)(x + (width - fm.stringWidth(text)) / 2);
        int textY = (int)(y + (height + fm.getAscent()) / 2);
        g2d.drawString(text, textX, textY);
    }
    
    @Override
    public ShapeStyle createDefaultStyle() {
        ShapeStyle style = new ShapeStyle();
        style.setFillColor(null); // 图片不需要填充色
        style.setBorderColor(Color.GRAY);
        style.setBorderWidth(1.0f);
        return style;
    }
    
    @Override
    public Rectangle getBounds() {
        return getSimpleBounds();
    }
    
    @Override
    public boolean contains(Point point) {
        return getBounds().contains(point);
    }
    
    @Override
    public void setSize(double width, double height) {
        if (maintainAspectRatio && image != null) {
            // 保持宽高比
            double imageRatio = (double)image.getWidth() / image.getHeight();
            double requestedRatio = width / height;
            
            if (requestedRatio > imageRatio) {
                // 以高度为准
                this.height = height;
                this.width = height * imageRatio;
            } else {
                // 以宽度为准
                this.width = width;
                this.height = width / imageRatio;
            }
        } else {
            this.width = width;
            this.height = height;
        }
    }
    
    // 静态工厂方法
    public static ImageElement fromFile(String imagePath) throws IOException {
        return new ImageElement(imagePath);
    }
    
    public static ImageElement fromFile(File imageFile) throws IOException {
        return new ImageElement(imageFile);
    }
    
    @Override
    public ImageElement clone() {
        ImageElement cloned = (ImageElement) super.clone();
        // 图片对象可以共享，不需要深拷贝
        return cloned;
    }
    
    // Getter和Setter
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        this.image = null; // 清除缓存，下次渲染时重新加载
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public BufferedImage getImage() {
        return image;
    }
    
    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio;
    }
    
    public void setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
    }
    
    /**
     * 检查图片文件是否支持
     */
    public static boolean isSupportedImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") || 
               name.endsWith(".bmp") || name.endsWith(".wbmp");
    }
} 