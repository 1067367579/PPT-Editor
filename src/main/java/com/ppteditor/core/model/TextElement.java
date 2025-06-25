package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.ppteditor.core.enums.ElementType;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本元素类
 * 继承自SlideElement并使用TextStyle作为样式类型
 */
public class TextElement extends SlideElement<TextStyle> implements java.io.Serializable {
    
    @Serializable(required = true)
    private String text;
    
    @Serializable
    private boolean autoSize; // 自动调整大小
    
    @Serializable
    private List<TextSegment> textSegments; // 文本片段列表，支持部分文字超链接
    
    @Serializable
    private boolean useSegments; // 是否使用文本片段模式
    
    public TextElement() {
        super(ElementType.TEXT);
        this.text = "文本";
        this.autoSize = false;
        this.style = createDefaultStyle();
        this.width = 100;
        this.height = 30;
        this.textSegments = new ArrayList<>();
        this.useSegments = false;
        initializeSegments();
    }
    
    public TextElement(String text) {
        this();
        this.text = text;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (style == null) {
            style = createDefaultStyle();
        }
        
        // 设置渲染提示以获得更好的文本质量
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制背景（只有当背景色不是白色或透明时才绘制）
        if (style.getBackgroundColor() != null && 
            !style.getBackgroundColor().equals(Color.WHITE) &&
            style.getBackgroundColor().getAlpha() > 0) {
            g2d.setColor(style.getBackgroundColor());
            g2d.fillRect((int)x, (int)y, (int)width, (int)height);
        }
        
        // 绘制文本
        g2d.setFont(style.getFont());
        
        // 如果有超链接，使用超链接样式
        if (hyperlink != null && !hyperlink.trim().isEmpty()) {
            g2d.setColor(new Color(0, 102, 204)); // 蓝色
        } else {
            g2d.setColor(style.getTextColor());
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        
        // 如果启用自动调整大小
        if (autoSize) {
            Rectangle2D textBounds = fm.getStringBounds(text, g2d);
            this.width = textBounds.getWidth() + 10; // 添加一些边距
            this.height = textBounds.getHeight() + 5;
        }
        
        // 计算文本位置
        int textY = calculateTextY(fm);
        
        // 绘制文本
        if (useSegments && !textSegments.isEmpty()) {
            drawSegmentedText(g2d, textY);
        } else {
            drawText(g2d, textY);
            
            // 绘制下划线（原有的下划线样式或超链接下划线）
            if (style.isUnderline() || (hyperlink != null && !hyperlink.trim().isEmpty())) {
                drawUnderline(g2d, textY, fm);
            }
        }
    }
    
    private int calculateTextX(FontMetrics fm, String line) {
        switch (style.getAlignment()) {
            case TextStyle.ALIGN_CENTER: // 居中
                return (int)(x + (width - fm.stringWidth(line)) / 2);
            case TextStyle.ALIGN_RIGHT: // 右对齐
                return (int)(x + width - fm.stringWidth(line) - 5);
            default: // 左对齐
                return (int)x + 5;
        }
    }
    
    private int calculateTextY(FontMetrics fm) {
        String[] lines = text.split("\n");
        int totalTextHeight = lines.length * (int)(fm.getHeight() * style.getLineSpacing());
        
        // 如果是左对齐的文本（通常是正文），从顶部开始显示
        if (style.getAlignment() == TextStyle.ALIGN_LEFT) {
            return (int)(y + fm.getAscent() + 5); // 顶部对齐，加5像素边距
        } else {
            // 其他对齐方式（居中、右对齐）保持垂直居中
            return (int)(y + fm.getAscent() + (height - totalTextHeight) / 2);
        }
    }
    
    private void drawText(Graphics2D g2d, int baseY) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = (int)(fm.getHeight() * style.getLineSpacing());
        
        // 自动换行处理
        java.util.List<String> wrappedLines = wrapText(text, fm, (int)width - 10); // 减去边距
        
        for (int i = 0; i < wrappedLines.size(); i++) {
            int currentY = baseY + i * lineHeight;
            if (currentY <= y + height) { // 确保文本在边界内
                String line = wrappedLines.get(i);
                int textX = calculateTextX(fm, line);
                g2d.drawString(line, textX, currentY);
            }
        }
    }
    
    /**
     * 文本自动换行处理
     */
    private java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> wrappedLines = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            wrappedLines.add("");
            return wrappedLines;
        }
        
        // 按原有换行符分割
        String[] paragraphs = text.split("\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                wrappedLines.add("");
                continue;
            }
            
            // 检查段落是否需要换行
            if (fm.stringWidth(paragraph) <= maxWidth) {
                wrappedLines.add(paragraph);
                continue;
            }
            
            // 需要换行处理
            String[] words = paragraph.split("\\s+");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                String testLine = currentLine.length() > 0 ? 
                    currentLine.toString() + " " + word : word;
                
                if (fm.stringWidth(testLine) <= maxWidth) {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    // 当前行已满，开始新行
                    if (currentLine.length() > 0) {
                        wrappedLines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // 单个词太长，强制换行
                        wrappedLines.add(word);
                    }
                }
            }
            
            // 添加最后一行
            if (currentLine.length() > 0) {
                wrappedLines.add(currentLine.toString());
            }
        }
        
        return wrappedLines;
    }
    
    private void drawUnderline(Graphics2D g2d, int baseY, FontMetrics fm) {
        String[] lines = text.split("\n");
        int lineHeight = (int)(fm.getHeight() * style.getLineSpacing());
        
        for (int i = 0; i < lines.length; i++) {
            int currentY = baseY + i * lineHeight;
            if (currentY <= y + height) {
                int textX = calculateTextX(fm, lines[i]);
                int lineWidth = fm.stringWidth(lines[i]);
                g2d.drawLine(textX, currentY + 2, textX + lineWidth, currentY + 2);
            }
        }
    }
    
    @Override
    public TextStyle createDefaultStyle() {
        return new TextStyle();
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, (int)width, (int)height);
    }
    
    @Override
    public boolean contains(Point point) {
        return point.x >= x && point.x <= x + width && 
               point.y >= y && point.y <= y + height;
    }
    
    // 静态工厂方法
    public static TextElement createTitle(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(24)
                .bold(true)
                .alignment(1) // 居中
                .build();
        return element;
    }
    
    public static TextElement createSubtitle(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(18)
                .italic(true)
                .alignment(1) // 居中
                .build();
        return element;
    }
    
    public static TextElement createBodyText(String text) {
        TextElement element = new TextElement(text);
        element.style = new TextStyle.Builder()
                .fontSize(14)
                .alignment(0) // 左对齐
                .build();
        return element;
    }
    
    // Getter和Setter方法
    public String getText() { return text; }
    
    public boolean isAutoSize() { return autoSize; }
    public void setAutoSize(boolean autoSize) { this.autoSize = autoSize; }
    
    /**
     * 初始化文本片段
     */
    private void initializeSegments() {
        if (text != null && !text.isEmpty()) {
            textSegments.clear();
            textSegments.add(new TextSegment(text));
        }
    }
    
    /**
     * 绘制分段文本（支持部分超链接和对齐）
     */
    private void drawSegmentedText(Graphics2D g2d, int baseY) {
        FontMetrics baseFm = g2d.getFontMetrics();
        int lineHeight = (int)(baseFm.getHeight() * style.getLineSpacing());
        
        // 首先构建所有行的内容，以便计算对齐位置
        List<SegmentLine> lines = buildSegmentLines();
        
        int currentY = baseY;
        for (SegmentLine line : lines) {
            // 计算整行的对齐位置
            int lineX = calculateLineX(line);
            int currentX = lineX;
            
            // 绘制该行的所有片段
            for (SegmentPart part : line.parts) {
                // 设置片段样式
                Font segmentFont = createSegmentFont(part.segment);
                g2d.setFont(segmentFont);
                FontMetrics fm = g2d.getFontMetrics();
                
                // 设置片段颜色
                Color segmentColor = part.segment.getTextColor();
                if (segmentColor == null) {
                    segmentColor = part.segment.isHyperlink() ? new Color(0, 102, 204) : style.getTextColor();
                }
                g2d.setColor(segmentColor);
                
                // 绘制文本
                g2d.drawString(part.text, currentX, currentY);
                
                // 绘制下划线
                if (part.segment.isUnderline() || part.segment.isHyperlink()) {
                    int lineWidth = fm.stringWidth(part.text);
                    g2d.drawLine(currentX, currentY + 2, currentX + lineWidth, currentY + 2);
                }
                
                // 更新X位置
                currentX += fm.stringWidth(part.text);
            }
            
            currentY += lineHeight;
        }
    }
    
    // 辅助类：表示一行中的一个文本片段部分
    private static class SegmentPart {
        TextSegment segment;
        String text;
        
        SegmentPart(TextSegment segment, String text) {
            this.segment = segment;
            this.text = text;
        }
    }
    
    // 辅助类：表示一行文本
    private static class SegmentLine {
        List<SegmentPart> parts = new ArrayList<>();
        String fullLineText = "";
        
        void addPart(SegmentPart part) {
            parts.add(part);
            fullLineText += part.text;
        }
    }
    
    // 构建分段行
    private List<SegmentLine> buildSegmentLines() {
        List<SegmentLine> lines = new ArrayList<>();
        SegmentLine currentLine = new SegmentLine();
        
        for (TextSegment segment : textSegments) {
            String segmentText = segment.getText();
            if (segmentText == null || segmentText.isEmpty()) continue;
            
            // 按换行符分割
            String[] segmentLines = segmentText.split("\n", -1); // -1 保留空字符串
            
            for (int i = 0; i < segmentLines.length; i++) {
                if (i > 0) {
                    // 需要换行
                    lines.add(currentLine);
                    currentLine = new SegmentLine();
                }
                
                if (!segmentLines[i].isEmpty() || i == segmentLines.length - 1) {
                    currentLine.addPart(new SegmentPart(segment, segmentLines[i]));
                }
            }
        }
        
        if (!currentLine.parts.isEmpty() || lines.isEmpty()) {
            lines.add(currentLine);
        }
        
        return lines;
    }
    
    // 计算行的X位置（支持对齐）
    private int calculateLineX(SegmentLine line) {
        // 创建临时Graphics2D计算文本宽度
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = img.createGraphics();
        tempG2d.setFont(style.getFont());
        FontMetrics fm = tempG2d.getFontMetrics();
        
        int totalWidth = 0;
        for (SegmentPart part : line.parts) {
            Font segmentFont = createSegmentFont(part.segment);
            tempG2d.setFont(segmentFont);
            FontMetrics segmentFm = tempG2d.getFontMetrics();
            totalWidth += segmentFm.stringWidth(part.text);
        }
        
        tempG2d.dispose();
        
        // 根据对齐方式计算X位置
        switch (style.getAlignment()) {
            case TextStyle.ALIGN_CENTER: // 居中
                return (int)(x + (width - totalWidth) / 2);
            case TextStyle.ALIGN_RIGHT: // 右对齐
                return (int)(x + width - totalWidth - 5);
            default: // 左对齐
                return (int)x + 5;
        }
    }
    
    /**
     * 为文本片段创建字体
     */
    private Font createSegmentFont(TextSegment segment) {
        int fontStyle = Font.PLAIN;
        if (style.isBold() || segment.isBold()) fontStyle |= Font.BOLD;
        if (style.isItalic() || segment.isItalic()) fontStyle |= Font.ITALIC;
        
        return new Font(style.getFontFamily(), fontStyle, style.getFontSize());
    }
    
    /**
     * 设置文本（保留现有的文本段设置）
     */
    public void setText(String text) {
        this.text = text;
        // 只有在没有使用文本段模式时才初始化片段
        if (!useSegments && (textSegments == null || textSegments.isEmpty())) {
            initializeSegments();
        }
    }
    
    /**
     * 为选中的文字设置超链接
     */
    public void setHyperlinkForSelection(int startIndex, int endIndex, String hyperlink) {
        if (startIndex < 0 || endIndex > text.length() || startIndex >= endIndex) {
            return;
        }
        
        useSegments = true;
        
        // 重新构建文本片段
        List<TextSegment> newSegments = new ArrayList<>();
        
        // 添加超链接前的文本
        if (startIndex > 0) {
            newSegments.add(new TextSegment(text.substring(0, startIndex)));
        }
        
        // 添加超链接文本
        String linkText = text.substring(startIndex, endIndex);
        newSegments.add(new TextSegment(linkText, hyperlink));
        
        // 添加超链接后的文本
        if (endIndex < text.length()) {
            newSegments.add(new TextSegment(text.substring(endIndex)));
        }
        
        this.textSegments = newSegments;
    }
    
    /**
     * 获取点击位置的超链接
     */
    public String getHyperlinkAtPoint(Point point) {
        if (!useSegments || textSegments.isEmpty()) {
            return hyperlink; // 返回整个元素的超链接
        }
        
        // 创建临时Graphics2D用于计算文本位置
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(style.getFont());
        FontMetrics fm = g2d.getFontMetrics();
        
        // 计算基础Y位置
        int baseY = calculateTextY(fm);
        int lineHeight = (int)(fm.getHeight() * style.getLineSpacing());
        
        // 遍历文本片段，找到点击位置的片段
        int currentX = (int)x + 5;
        int currentY = baseY;
        
        for (TextSegment segment : textSegments) {
            String segmentText = segment.getText();
            if (segmentText == null || segmentText.isEmpty()) continue;
            
            // 设置片段字体
            Font segmentFont = createSegmentFont(segment);
            g2d.setFont(segmentFont);
            fm = g2d.getFontMetrics();
            
            // 处理换行
            String[] lines = segmentText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                if (i > 0) {
                    // 换行
                    currentY += lineHeight;
                    currentX = (int)x + 5;
                }
                
                // 检查点击位置是否在当前行的片段范围内
                int lineWidth = fm.stringWidth(line);
                Rectangle segmentBounds = new Rectangle(currentX, currentY - fm.getAscent(), 
                                                       lineWidth, fm.getHeight());
                
                if (segmentBounds.contains(point) && segment.isHyperlink()) {
                    g2d.dispose();
                    return segment.getHyperlink();
                }
                
                // 更新X位置
                currentX += lineWidth;
            }
        }
        
        g2d.dispose();
        return null;
    }
    
    // Getter和Setter方法
    public List<TextSegment> getTextSegments() { return textSegments; }
    public void setTextSegments(List<TextSegment> textSegments) { this.textSegments = textSegments; }
    
    public boolean isUseSegments() { return useSegments; }
    public void setUseSegments(boolean useSegments) { this.useSegments = useSegments; }
    
    @Override
    public TextElement clone() {
        TextElement cloned = (TextElement) super.clone();
        cloned.text = this.text;
        cloned.autoSize = this.autoSize;
        cloned.useSegments = this.useSegments;
        
        if (this.textSegments != null) {
            cloned.textSegments = new ArrayList<>();
            for (TextSegment segment : this.textSegments) {
                cloned.textSegments.add(segment.clone());
            }
        }
        
        return cloned;
    }
} 