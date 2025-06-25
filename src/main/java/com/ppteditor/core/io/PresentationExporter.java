package com.ppteditor.core.io;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.ppteditor.core.model.*;
import com.ppteditor.ui.SlideCanvas;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * 演示文稿导出器
 * 支持导出为图片和PDF
 */
public class PresentationExporter {
    
    private static final int EXPORT_WIDTH = 1920;
    private static final int EXPORT_HEIGHT = 1080;
    
    /**
     * 导出单张幻灯片为图片
     */
    public void exportSlideAsImage(Slide slide, String filePath, String format) throws IOException {
        BufferedImage image = renderSlideToImage(slide);
        
        // 确保文件扩展名正确
        if (!filePath.toLowerCase().endsWith("." + format.toLowerCase())) {
            filePath += "." + format.toLowerCase();
        }
        
        File outputFile = new File(filePath);
        
        // 确保父目录存在
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 写入图片文件
        boolean success = ImageIO.write(image, format.toUpperCase(), outputFile);
        
        if (!success) {
            throw new IOException("不支持的图片格式: " + format);
        }
        
        // 验证文件是否被创建
        if (!outputFile.exists()) {
            throw new IOException("图片文件创建失败: " + filePath);
        }
        
        if (outputFile.length() == 0) {
            throw new IOException("图片文件为空: " + filePath);
        }
        
        System.out.println("幻灯片已导出为图片: " + filePath + " (大小: " + outputFile.length() + " 字节)");
    }
    
    /**
     * 导出所有幻灯片为图片
     */
    public void exportAllSlidesAsImages(Presentation presentation, String outputDir, String format) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        List<Slide> slides = presentation.getSlides();
        for (int i = 0; i < slides.size(); i++) {
            Slide slide = slides.get(i);
            String fileName = String.format("幻灯片_%03d.%s", i + 1, format.toLowerCase());
            String filePath = new File(dir, fileName).getAbsolutePath();
            
            exportSlideAsImage(slide, filePath, format);
        }
        
        System.out.println("所有幻灯片已导出到目录: " + outputDir);
    }
    
    /**
     * 导出演示文稿为PDF
     */
    public void exportAsPDF(Presentation presentation, String filePath) throws IOException {
        // 确保文件扩展名
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }
        
        File outputFile = new File(filePath);
        
        // 确保父目录存在
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            List<Slide> slides = presentation.getSlides();
            
            if (slides.isEmpty()) {
                throw new IOException("没有幻灯片可以导出");
            }
            
            for (int i = 0; i < slides.size(); i++) {
                if (i > 0) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
                
                // 渲染幻灯片为图片
                BufferedImage slideImage = renderSlideToImage(slides.get(i));
                
                // 转换为字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                boolean success = ImageIO.write(slideImage, "PNG", baos);
                
                if (!success) {
                    throw new IOException("幻灯片渲染失败: 第 " + (i + 1) + " 页");
                }
                
                byte[] imageData = baos.toByteArray();
                
                if (imageData.length == 0) {
                    throw new IOException("幻灯片图片数据为空: 第 " + (i + 1) + " 页");
                }
                
                // 添加到PDF
                Image pdfImage = new Image(ImageDataFactory.create(imageData));
                
                // 调整图片大小以适应页面
                float pageWidth = pdfDoc.getDefaultPageSize().getWidth() - 72; // 减去边距
                float pageHeight = pdfDoc.getDefaultPageSize().getHeight() - 72;
                
                float imageWidth = slideImage.getWidth();
                float imageHeight = slideImage.getHeight();
                
                float scaleX = pageWidth / imageWidth;
                float scaleY = pageHeight / imageHeight;
                float scale = Math.min(scaleX, scaleY);
                
                pdfImage.setWidth(imageWidth * scale);
                pdfImage.setHeight(imageHeight * scale);
                
                document.add(pdfImage);
            }
        } catch (Exception e) {
            // 如果导出失败，删除可能存在的空文件
            if (outputFile.exists() && outputFile.length() == 0) {
                outputFile.delete();
            }
            throw new IOException("导出PDF失败: " + e.getMessage(), e);
        }
        
        // 验证文件是否被创建
        if (!outputFile.exists()) {
            throw new IOException("PDF文件创建失败: " + filePath);
        }
        
        if (outputFile.length() == 0) {
            throw new IOException("PDF文件为空: " + filePath);
        }
        
        System.out.println("演示文稿已导出为PDF: " + filePath + " (大小: " + outputFile.length() + " 字节)");
    }
    
    /**
     * 将幻灯片渲染为高分辨率图片
     */
    private BufferedImage renderSlideToImage(Slide slide) {
        BufferedImage image = new BufferedImage(EXPORT_WIDTH, EXPORT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        try {
            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // 计算缩放比例
            double scaleX = (double) EXPORT_WIDTH / SlideCanvas.CANVAS_WIDTH;
            double scaleY = (double) EXPORT_HEIGHT / SlideCanvas.CANVAS_HEIGHT;
            g2d.scale(scaleX, scaleY);
            
            // 渲染幻灯片
            Dimension slideSize = new Dimension(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT);
            slide.render(g2d, slideSize);
            
        } finally {
            g2d.dispose();
        }
        
        return image;
    }
    
    /**
     * 检查格式是否支持
     */
    public boolean isSupportedImageFormat(String format) {
        String[] supportedFormats = {"PNG", "JPEG", "JPG", "BMP"};
        for (String supported : supportedFormats) {
            if (supported.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取推荐的图片格式
     */
    public String[] getSupportedImageFormats() {
        return new String[]{"PNG", "JPEG", "BMP"};
    }
    
    /**
     * 预览导出效果（缩略图）
     */
    public BufferedImage createPreview(Slide slide, int width, int height) {
        BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = preview.createGraphics();
        
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            double scaleX = (double) width / SlideCanvas.CANVAS_WIDTH;
            double scaleY = (double) height / SlideCanvas.CANVAS_HEIGHT;
            g2d.scale(scaleX, scaleY);
            
            Dimension slideSize = new Dimension(SlideCanvas.CANVAS_WIDTH, SlideCanvas.CANVAS_HEIGHT);
            slide.render(g2d, slideSize);
            
        } finally {
            g2d.dispose();
        }
        
        return preview;
    }
} 