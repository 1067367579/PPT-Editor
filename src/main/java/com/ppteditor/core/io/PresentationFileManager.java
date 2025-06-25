package com.ppteditor.core.io;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ppteditor.core.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.awt.Rectangle;

/**
 * 演示文稿文件管理器
 * 负责保存和加载演示文稿，使用JSON格式避免序列化问题
 */
public class PresentationFileManager {
    
    private static final String PRESENTATION_EXTENSION = ".pptx";
    private static final String JSON_EXTENSION = ".pptj"; // PPT JSON格式
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        // 禁用多态类型信息，减少文件大小
        // objectMapper.activateDefaultTyping() - 这会导致文件过大
        
        // 配置序列化选项
        objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略AWT相关对象，避免序列化Font等大对象
        objectMapper.addMixIn(java.awt.Font.class, IgnoreTypeMixin.class);
        // 暂时移除Color的Mixin，避免序列化冲突
        // objectMapper.addMixIn(java.awt.Color.class, ColorMixin.class);
        objectMapper.addMixIn(Rectangle.class, IgnoreRectangleMixin.class);
    }
    
    // 忽略类型信息的Mixin
    @com.fasterxml.jackson.annotation.JsonIgnoreType
    public static class IgnoreTypeMixin {
    }
    
    // Color对象的简化序列化
    public static abstract class ColorMixin {
        // 移除@JsonCreator注解，避免与默认构造函数冲突
        // @com.fasterxml.jackson.annotation.JsonCreator
        // public ColorMixin(@com.fasterxml.jackson.annotation.JsonProperty("rgb") int rgb) {}
        
        @com.fasterxml.jackson.annotation.JsonProperty("rgb")
        public abstract int getRGB();
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract int getRed();
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract int getGreen();
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract int getBlue();
        
        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract int getAlpha();
    }
    
    // Mixin class to ignore Rectangle type during serialization
    @JsonIgnoreType
    public static class IgnoreRectangleMixin {}
    
    /**
     * 保存演示文稿到JSON格式文件
     */
    public static void savePresentation(Presentation presentation, File file) throws IOException {
        if (presentation == null || file == null) {
            throw new IllegalArgumentException("Presentation and file cannot be null");
        }
        
        // 确保文件扩展名正确
        String filePath = file.getAbsolutePath();
        if (!filePath.endsWith(JSON_EXTENSION)) {
            filePath += JSON_EXTENSION;
            file = new File(filePath);
        }
        
        try {
            // 创建包装对象，包含版本信息
            PresentationWrapper wrapper = new PresentationWrapper();
            wrapper.version = "1.0";
            wrapper.presentation = presentation;
            wrapper.savedTime = System.currentTimeMillis();
            
            objectMapper.writeValue(file, wrapper);
            System.out.println("演示文稿已保存为: " + file.getAbsolutePath());
            
        } catch (Exception e) {
            throw new IOException("保存演示文稿失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从JSON格式文件加载演示文稿
     */
    public static Presentation loadPresentation(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        
        try {
            PresentationWrapper wrapper = objectMapper.readValue(file, PresentationWrapper.class);
            System.out.println("演示文稿已加载，版本: " + wrapper.version);
            return wrapper.presentation;
            
        } catch (Exception e) {
            throw new IOException("加载演示文稿失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否为支持的格式
     */
    public static boolean isSupportedFile(File file) {
        if (file == null) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(JSON_EXTENSION) || name.endsWith(".json");
    }
    
    /**
     * 获取推荐的文件扩展名
     */
    public static String getRecommendedExtension() {
        return JSON_EXTENSION;
    }
    
    /**
     * 导出演示文稿为XML格式（备用格式）
     */
    public static void exportToXML(Presentation presentation, File file) throws IOException {
        if (presentation == null || file == null) {
            throw new IllegalArgumentException("Presentation and file cannot be null");
        }
        
        String filePath = file.getAbsolutePath();
        if (!filePath.endsWith(".xml")) {
            filePath += ".xml";
            file = new File(filePath);
        }
        
        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<presentation>\n");
            writer.write("  <metadata>\n");
            writer.write("    <title><![CDATA[" + presentation.getTitle() + "]]></title>\n");
            writer.write("    <author><![CDATA[" + presentation.getAuthor() + "]]></author>\n");
                         writer.write("    <createTime>" + presentation.getCreatedTime() + "</createTime>\n");
             writer.write("  </metadata>\n");
             writer.write("  <slides>\n");
             
             for (int i = 0; i < presentation.getTotalSlides(); i++) {
                 Slide slide = presentation.getSlide(i);
                 writer.write("    <slide id=\"" + i + "\" title=\"" + escapeXML(slide.getName()) + "\">\n");
                
                for (SlideElement<?> element : slide.getElements()) {
                    writeElementToXML(writer, element, "      ");
                }
                
                writer.write("    </slide>\n");
            }
            
            writer.write("  </slides>\n");
            writer.write("</presentation>\n");
            
            System.out.println("演示文稿已导出为XML: " + file.getAbsolutePath());
        }
    }
    
    private static void writeElementToXML(FileWriter writer, SlideElement<?> element, String indent) throws IOException {
        writer.write(indent + "<element type=\"" + element.getType() + "\" id=\"" + element.getId() + "\">\n");
        writer.write(indent + "  <bounds x=\"" + element.getX() + "\" y=\"" + element.getY() + 
                    "\" width=\"" + element.getWidth() + "\" height=\"" + element.getHeight() + "\"/>\n");
        writer.write(indent + "  <rotation>" + element.getRotation() + "</rotation>\n");
        writer.write(indent + "  <visible>" + element.isVisible() + "</visible>\n");
        writer.write(indent + "  <zIndex>" + element.getZIndex() + "</zIndex>\n");
        
        if (element.getHyperlink() != null) {
            writer.write(indent + "  <hyperlink><![CDATA[" + element.getHyperlink() + "]]></hyperlink>\n");
        }
        
        if (element instanceof TextElement) {
            TextElement textElement = (TextElement) element;
            writer.write(indent + "  <text><![CDATA[" + textElement.getText() + "]]></text>\n");
        }
        
        writer.write(indent + "</element>\n");
    }
    
    private static String escapeXML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    /**
     * 包装类，用于JSON序列化时包含版本和元数据信息
     */
    public static class PresentationWrapper {
        public String version;
        public long savedTime;
        public Presentation presentation;
        
        public PresentationWrapper() {
            // 默认构造函数供Jackson使用
        }
    }
    
    /**
     * 创建演示文稿的备份
     */
    public static void createBackup(Presentation presentation, File originalFile) {
        try {
            String backupPath = originalFile.getParent() + File.separator + 
                               originalFile.getName().replace(JSON_EXTENSION, ".backup" + JSON_EXTENSION);
            File backupFile = new File(backupPath);
            savePresentation(presentation, backupFile);
            System.out.println("备份已创建: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("创建备份失败: " + e.getMessage());
        }
    }
} 