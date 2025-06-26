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
        
        // 启用多态类型信息以支持抽象类反序列化
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
            JsonTypeInfo.As.PROPERTY
        );
        
        // 配置序列化选项
        objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略AWT相关对象，避免序列化Font等大对象
        objectMapper.addMixIn(java.awt.Font.class, IgnoreTypeMixin.class);
        objectMapper.addMixIn(Rectangle.class, IgnoreRectangleMixin.class);
        objectMapper.addMixIn(java.awt.image.BufferedImage.class, IgnoreTypeMixin.class);
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
        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            // 创建包装对象，包含版本信息
            PresentationWrapper wrapper = new PresentationWrapper();
            wrapper.version = "1.0";
            wrapper.presentation = presentation;
            wrapper.savedTime = System.currentTimeMillis();
            // 首先写入临时文件
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            System.out.println("尝试写入临时文件: " + tempFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                // 写入文件
                objectMapper.writeValue(bos, wrapper);
                bos.flush();
                fos.flush();
                // 尝试强制同步到磁盘（可选操作，失败不影响保存）
                try {
                    fos.getFD().sync();
                } catch (Exception syncEx) {
                    System.out.println("警告：无法强制同步到磁盘，但文件已写入: " + syncEx.getMessage());
                }
            }
            // 验证临时文件
            if (!tempFile.exists()) {
                throw new IOException("临时文件创建失败");
            }
            if (tempFile.length() == 0) {
                tempFile.delete();
                throw new IOException("临时文件为空");
            }
            // 如果目标文件已存在，先删除
            if (file.exists()) {
                file.delete();
            }
            // 重命名临时文件为目标文件
            if (!tempFile.renameTo(file)) {
                // 如果重命名失败，使用复制方式
                try {
                    java.nio.file.Files.copy(tempFile.toPath(), file.toPath());
                    tempFile.delete();
                } catch (Exception copyEx) {
                    tempFile.delete();
                    throw new IOException("文件保存失败：无法移动临时文件到目标位置: " + copyEx.getMessage());
                }
            }
            // 最终验证
            if (!file.exists()) {
                throw new IOException("文件保存失败：目标文件不存在");
            }
            if (file.length() == 0) {
                file.delete();
                throw new IOException("文件保存失败：目标文件为空");
            }
            System.out.println("演示文稿已保存为: " + file.getAbsolutePath() + " (大小: " + file.length() + " 字节)");
        } catch (Exception e) {
            // 清理任何可能存在的临时文件或空文件
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            if (tempFile.exists()) {
                tempFile.delete();
            }
            if (file.exists() && file.length() == 0) {
                file.delete();
            }
            System.err.println("保存文件时发生错误: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("开始加载文件: " + file.getAbsolutePath() + " (大小: " + file.length() + " 字节)");
        try {
            // 检查文件是否为空
            if (file.length() == 0) {
                throw new IOException("文件为空");
            }
            // 检查文件头部
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                byte[] header = new byte[10];
                int read = fis.read(header);
                if (read < 1 || header[0] != '{') {
                    throw new IOException("文件格式无效：不是有效的JSON文件");
                }
            }
            System.out.println("开始解析JSON...");
            PresentationWrapper wrapper = objectMapper.readValue(file, PresentationWrapper.class);
            
            if (wrapper == null) {
                throw new IOException("解析结果为空");
            }
            
            if (wrapper.presentation == null) {
                throw new IOException("演示文稿数据为空");
            }
            System.out.println("演示文稿已加载，版本: " + wrapper.version + 
                              "，标题: " + wrapper.presentation.getTitle() + 
                              "，幻灯片数量: " + wrapper.presentation.getTotalSlides());
            return wrapper.presentation;
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            System.err.println("JSON解析错误: " + e.getMessage());
            throw new IOException("文件格式错误：JSON解析失败 - " + e.getMessage(), e);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            System.err.println("JSON映射错误: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("数据结构错误：反序列化失败 - " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("加载文件时发生未知错误: " + e.getMessage());
            e.printStackTrace();
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