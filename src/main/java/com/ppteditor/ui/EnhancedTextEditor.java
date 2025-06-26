package com.ppteditor.ui;

import com.ppteditor.core.model.TextElement;
import com.ppteditor.core.model.TextSegment;
import com.ppteditor.core.model.TextStyle;
import com.ppteditor.core.command.EditTextCommand;
import com.ppteditor.core.command.CommandManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 增强的文本编辑器
 * 支持富文本编辑、超链接添加等功能
 */
public class EnhancedTextEditor extends JDialog {
    
    private TextElement textElement;
    private JTextPane textPane;
    private StyledDocument document;
    private CommandManager commandManager;
    private boolean confirmed = false;
    
    // 工具栏组件
    private JButton boldButton;
    private JButton italicButton;
    private JButton underlineButton;
    private JButton colorButton;
    private JButton fontSizeButton;
    private JButton fontFamilyButton;
    private JButton hyperlinkButton;
    private JButton alignLeftButton;
    private JButton alignCenterButton;
    private JButton alignRightButton;
    
    public EnhancedTextEditor(Frame parent, TextElement textElement, CommandManager commandManager) {
        super(parent, "编辑文本", true);
        this.textElement = textElement;
        this.commandManager = commandManager;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTextContent();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // 创建文本编辑区域
        textPane = new JTextPane();
        textPane.setPreferredSize(new Dimension(500, 300));
        textPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        document = textPane.getStyledDocument();
        
        // 创建工具栏按钮
        boldButton = new JButton("粗体");
        italicButton = new JButton("斜体");
        underlineButton = new JButton("下划线");
        colorButton = new JButton("颜色");
        fontSizeButton = new JButton("字号");
        fontFamilyButton = new JButton("字体");
        hyperlinkButton = new JButton("超链接");
        alignLeftButton = new JButton("左对齐");
        alignCenterButton = new JButton("居中");
        alignRightButton = new JButton("右对齐");
        
        // 设置按钮样式
        styleToolbarButton(boldButton);
        styleToolbarButton(italicButton);
        styleToolbarButton(underlineButton);
        styleToolbarButton(colorButton);
        styleToolbarButton(fontSizeButton);
        styleToolbarButton(fontFamilyButton);
        styleToolbarButton(hyperlinkButton);
        styleToolbarButton(alignLeftButton);
        styleToolbarButton(alignCenterButton);
        styleToolbarButton(alignRightButton);
    }
    
    private void styleToolbarButton(JButton button) {
        button.setPreferredSize(new Dimension(80, 30));
        button.setFocusable(false);
        button.setMargin(new Insets(2, 4, 2, 4));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(fontFamilyButton);
        toolBar.add(fontSizeButton);
        toolBar.add(colorButton);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(alignLeftButton);
        toolBar.add(alignCenterButton);
        toolBar.add(alignRightButton);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(hyperlinkButton);
        
        // 文本编辑区域
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createTitledBorder("文本内容"));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 按钮事件
        okButton.addActionListener(e -> confirmChanges());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void setupEventHandlers() {
        // 粗体按钮
        boldButton.addActionListener(e -> toggleBold());
        
        // 斜体按钮
        italicButton.addActionListener(e -> toggleItalic());
        
        // 下划线按钮
        underlineButton.addActionListener(e -> toggleUnderline());
        
        // 颜色按钮
        colorButton.addActionListener(e -> chooseColor());
        
        // 字号按钮
        fontSizeButton.addActionListener(e -> chooseFontSize());
        
        // 字体按钮
        fontFamilyButton.addActionListener(e -> chooseFontFamily());
        
        // 超链接按钮
        hyperlinkButton.addActionListener(e -> addHyperlink());
        
        // 对齐按钮
        alignLeftButton.addActionListener(e -> setAlignment(TextStyle.ALIGN_LEFT));
        alignCenterButton.addActionListener(e -> setAlignment(TextStyle.ALIGN_CENTER));
        alignRightButton.addActionListener(e -> setAlignment(TextStyle.ALIGN_RIGHT));
        
        // 键盘快捷键
        setupKeyboardShortcuts();
        
        // 选择变化监听器
        textPane.addCaretListener(e -> updateToolbarState());
    }
    
    private void setupKeyboardShortcuts() {
        InputMap inputMap = textPane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textPane.getActionMap();
        
        // Ctrl+B - 粗体
        inputMap.put(KeyStroke.getKeyStroke("ctrl B"), "bold");
        actionMap.put("bold", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleBold();
            }
        });
        
        // Ctrl+I - 斜体
        inputMap.put(KeyStroke.getKeyStroke("ctrl I"), "italic");
        actionMap.put("italic", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleItalic();
            }
        });
        
        // Ctrl+U - 下划线
        inputMap.put(KeyStroke.getKeyStroke("ctrl U"), "underline");
        actionMap.put("underline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleUnderline();
            }
        });
        
        // Ctrl+K - 超链接
        inputMap.put(KeyStroke.getKeyStroke("ctrl K"), "hyperlink");
        actionMap.put("hyperlink", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHyperlink();
            }
        });
        
        // Enter - 确定
        inputMap.put(KeyStroke.getKeyStroke("ctrl ENTER"), "confirm");
        actionMap.put("confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmChanges();
            }
        });
        
        // Escape - 取消
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        actionMap.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void loadTextContent() {
        try {
            document.remove(0, document.getLength());
            
            if (textElement.getTextSegments() != null && !textElement.getTextSegments().isEmpty()) {
                // 加载分段文本
                for (TextSegment segment : textElement.getTextSegments()) {
                    SimpleAttributeSet attrs = createAttributeSetFromSegment(segment);
                    document.insertString(document.getLength(), segment.getText(), attrs);
                }
            } else {
                // 加载普通文本
                SimpleAttributeSet attrs = createAttributeSet(textElement.getStyle(), textElement.getHyperlink());
                document.insertString(0, textElement.getText(), attrs);
            }
            
            // 设置段落对齐方式
            if (textElement.getStyle() != null) {
                SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
                int alignment = textElement.getStyle().getAlignment();
                
                switch (alignment) {
                    case TextStyle.ALIGN_LEFT:
                        StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_LEFT);
                        break;
                    case TextStyle.ALIGN_CENTER:
                        StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_CENTER);
                        break;
                    case TextStyle.ALIGN_RIGHT:
                        StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_RIGHT);
                        break;
                }
                
                // 应用段落样式到整个文档
                document.setParagraphAttributes(0, document.getLength(), paragraphAttrs, false);
            }
            
            textPane.setCaretPosition(0);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private SimpleAttributeSet createAttributeSet(TextStyle style, String hyperlink) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        
        if (style != null) {
            StyleConstants.setFontFamily(attrs, style.getFontFamily());
            StyleConstants.setFontSize(attrs, style.getFontSize());
            StyleConstants.setBold(attrs, style.isBold());
            StyleConstants.setItalic(attrs, style.isItalic());
            StyleConstants.setUnderline(attrs, style.isUnderline());
            
            if (style.getTextColor() != null) {
                StyleConstants.setForeground(attrs, style.getTextColor());
            }
        }
        
        // 设置超链接样式
        if (hyperlink != null && !hyperlink.trim().isEmpty()) {
            StyleConstants.setForeground(attrs, Color.BLUE);
            StyleConstants.setUnderline(attrs, true);
            attrs.addAttribute("hyperlink", hyperlink);
        }
        
        return attrs;
    }
    
    private SimpleAttributeSet createAttributeSetFromSegment(TextSegment segment) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        
        // 使用原有文本元素的样式作为基础
        TextStyle baseStyle = textElement.getStyle();
        if (baseStyle != null) {
            StyleConstants.setFontFamily(attrs, baseStyle.getFontFamily());
            StyleConstants.setFontSize(attrs, baseStyle.getFontSize());
        } else {
            StyleConstants.setFontFamily(attrs, "微软雅黑");
            StyleConstants.setFontSize(attrs, 14);
        }
        
        // 应用文本段的样式覆盖
        StyleConstants.setBold(attrs, segment.isBold());
        StyleConstants.setItalic(attrs, segment.isItalic());
        StyleConstants.setUnderline(attrs, segment.isUnderline());
        
        if (segment.getTextColor() != null) {
            StyleConstants.setForeground(attrs, segment.getTextColor());
        } else if (baseStyle != null && baseStyle.getTextColor() != null) {
            StyleConstants.setForeground(attrs, baseStyle.getTextColor());
        }
        
        // 设置超链接样式
        if (segment.getHyperlink() != null && !segment.getHyperlink().trim().isEmpty()) {
            StyleConstants.setForeground(attrs, Color.BLUE);
            StyleConstants.setUnderline(attrs, true);
            attrs.addAttribute("hyperlink", segment.getHyperlink());
        }
        
        return attrs;
    }
    
    private void toggleBold() {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        
        if (start == end) {
            // 没有选择文本，设置当前插入点的样式
            MutableAttributeSet attrs = textPane.getInputAttributes();
            boolean isBold = StyleConstants.isBold(attrs);
            StyleConstants.setBold(attrs, !isBold);
        } else {
            // 有选择文本，切换选择文本的粗体状态
            StyledDocument doc = textPane.getStyledDocument();
            boolean isBold = StyleConstants.isBold(doc.getCharacterElement(start).getAttributes());
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBold(attrs, !isBold);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }
        
        updateToolbarState();
    }
    
    private void toggleItalic() {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        
        if (start == end) {
            MutableAttributeSet attrs = textPane.getInputAttributes();
            boolean isItalic = StyleConstants.isItalic(attrs);
            StyleConstants.setItalic(attrs, !isItalic);
        } else {
            StyledDocument doc = textPane.getStyledDocument();
            boolean isItalic = StyleConstants.isItalic(doc.getCharacterElement(start).getAttributes());
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setItalic(attrs, !isItalic);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }
        
        updateToolbarState();
    }
    
    private void toggleUnderline() {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        
        if (start == end) {
            MutableAttributeSet attrs = textPane.getInputAttributes();
            boolean isUnderline = StyleConstants.isUnderline(attrs);
            StyleConstants.setUnderline(attrs, !isUnderline);
        } else {
            StyledDocument doc = textPane.getStyledDocument();
            boolean isUnderline = StyleConstants.isUnderline(doc.getCharacterElement(start).getAttributes());
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setUnderline(attrs, !isUnderline);
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }
        
        updateToolbarState();
    }
    
    private void chooseColor() {
        Color currentColor = StyleConstants.getForeground(textPane.getInputAttributes());
        Color newColor = JColorChooser.showDialog(this, "选择文字颜色", currentColor != null ? currentColor : Color.BLACK);
        
        if (newColor != null) {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, newColor);
            
            if (start == end) {
                textPane.getInputAttributes().addAttributes(attrs);
            } else {
                textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
            }
        }
    }
    
    private void chooseFontSize() {
        String[] sizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "24", "28", "32", "36", "48", "72"};
        int currentSize = StyleConstants.getFontSize(textPane.getInputAttributes());
        
        String selected = (String) JOptionPane.showInputDialog(this, "选择字体大小:", "字体大小", 
                JOptionPane.QUESTION_MESSAGE, null, sizes, String.valueOf(currentSize));
        
        if (selected != null) {
            try {
                int newSize = Integer.parseInt(selected);
                int start = textPane.getSelectionStart();
                int end = textPane.getSelectionEnd();
                
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setFontSize(attrs, newSize);
                
                if (start == end) {
                    textPane.getInputAttributes().addAttributes(attrs);
                } else {
                    textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "无效的字体大小", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void chooseFontFamily() {
        String[] fonts = {
            "微软雅黑", "宋体", "黑体", "楷体", "仿宋", 
            "Arial", "Times New Roman", "Helvetica", "Verdana", "Calibri"
        };
        String currentFont = StyleConstants.getFontFamily(textPane.getInputAttributes());
        
        String selected = (String) JOptionPane.showInputDialog(this, "选择字体:", "字体", 
                JOptionPane.QUESTION_MESSAGE, null, fonts, currentFont);
        
        if (selected != null) {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, selected);
            
            if (start == end) {
                textPane.getInputAttributes().addAttributes(attrs);
            } else {
                textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
            }
        }
    }
    
    private void addHyperlink() {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        
        if (start == end) {
            JOptionPane.showMessageDialog(this, "请先选择要添加超链接的文字", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String selectedText = textPane.getSelectedText();
        String url = JOptionPane.showInputDialog(this, "请输入超链接地址:", "添加超链接", JOptionPane.QUESTION_MESSAGE);
        
        if (url != null && !url.trim().isEmpty()) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, Color.BLUE);
            StyleConstants.setUnderline(attrs, true);
            attrs.addAttribute("hyperlink", url.trim());
            
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
        }
    }
    
    private void setAlignment(int alignment) {
        // 获取当前段落
        int caretPos = textPane.getCaretPosition();
        Element paragraph = document.getParagraphElement(caretPos);
        // 创建段落样式
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        switch (alignment) {
            case TextStyle.ALIGN_LEFT:
                StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_LEFT);
                break;
            case TextStyle.ALIGN_CENTER:
                StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_CENTER);
                break;
            case TextStyle.ALIGN_RIGHT:
                StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_RIGHT);
                break;
        }
        // 应用段落样式
        document.setParagraphAttributes(paragraph.getStartOffset(), 
                                      paragraph.getEndOffset() - paragraph.getStartOffset(), 
                                      paragraphAttrs, false);
        // 更新工具栏状态
        updateToolbarState();
    }
    
    private void updateToolbarState() {
        int caretPos = textPane.getCaretPosition();
        // 获取字符属性
        AttributeSet attrs = textPane.getInputAttributes();
        // 更新样式按钮状态
        boldButton.setBackground(StyleConstants.isBold(attrs) ? Color.LIGHT_GRAY : null);
        italicButton.setBackground(StyleConstants.isItalic(attrs) ? Color.LIGHT_GRAY : null);
        underlineButton.setBackground(StyleConstants.isUnderline(attrs) ? Color.LIGHT_GRAY : null);
        // 获取段落属性
        AttributeSet paraAttrs = document.getParagraphElement(caretPos).getAttributes();
        int align = StyleConstants.getAlignment(paraAttrs);
        // 更新对齐按钮状态
        alignLeftButton.setBackground(align == StyleConstants.ALIGN_LEFT ? Color.LIGHT_GRAY : null);
        alignCenterButton.setBackground(align == StyleConstants.ALIGN_CENTER ? Color.LIGHT_GRAY : null);
        alignRightButton.setBackground(align == StyleConstants.ALIGN_RIGHT ? Color.LIGHT_GRAY : null);
    }
    
    private void confirmChanges() {
        try {
            // 获取文档内容并转换为文本段
            String fullText = document.getText(0, document.getLength());
            List<TextSegment> segments = new ArrayList<>();
            // 解析样式化文档
            Element root = document.getDefaultRootElement();
            parseElementToSegments(root, segments);
            // 获取第一段的对齐方式作为文本元素的对齐方式
            Element firstParagraph = document.getParagraphElement(0);
            AttributeSet paraAttrs = firstParagraph.getAttributes();
            int swingAlign = StyleConstants.getAlignment(paraAttrs);
            // 转换为TextStyle对齐方式
            int textAlignment = TextStyle.ALIGN_LEFT; // 默认左对齐
            switch (swingAlign) {
                case StyleConstants.ALIGN_LEFT:
                    textAlignment = TextStyle.ALIGN_LEFT;
                    break;
                case StyleConstants.ALIGN_CENTER:
                    textAlignment = TextStyle.ALIGN_CENTER;
                    break;
                case StyleConstants.ALIGN_RIGHT:
                    textAlignment = TextStyle.ALIGN_RIGHT;
                    break;
            }
            // 直接更新文本元素，不使用命令（因为命令会重置文本段）
            String oldText = textElement.getText();
            // 更新文本内容
            textElement.setText(fullText);
            // 更新文本样式的对齐方式
            if (textElement.getStyle() != null) {
                textElement.getStyle().setAlignment(textAlignment);
            }
            // 如果有分段信息，设置分段
            if (!segments.isEmpty()) {
                textElement.setTextSegments(segments);
                textElement.setUseSegments(true);
            } else {
                textElement.setUseSegments(false);
            }
            confirmed = true;
            dispose();
        } catch (BadLocationException e) {
            JOptionPane.showMessageDialog(this, "保存文本时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void parseElementToSegments(Element element, List<TextSegment> segments) {
        if (element.isLeaf()) {
            try {
                int start = element.getStartOffset();
                int end = element.getEndOffset();
                String text = document.getText(start, end - start);
                
                // 保留所有文本，包括换行符，但排除文档末尾的单独换行符
                if (!text.isEmpty()) {
                    // 检查是否是文档末尾的换行符
                    boolean isEndNewline = (end == document.getLength()) && text.equals("\n");
                    
                    if (!isEndNewline) {
                        AttributeSet attrs = element.getAttributes();
                        TextStyle style = createTextStyleFromAttributes(attrs);
                        String hyperlink = (String) attrs.getAttribute("hyperlink");
                        
                        TextSegment segment = new TextSegment(text);
                        // 设置样式属性
                        if (style != null) {
                            segment.setBold(style.isBold());
                            segment.setItalic(style.isItalic());
                            segment.setUnderline(style.isUnderline());
                            segment.setTextColor(style.getTextColor());
                        }
                        if (hyperlink != null) {
                            segment.setHyperlink(hyperlink);
                        }
                        segments.add(segment);
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            for (int i = 0; i < element.getElementCount(); i++) {
                parseElementToSegments(element.getElement(i), segments);
            }
        }
    }
    
    private TextStyle createTextStyleFromAttributes(AttributeSet attrs) {
        return new TextStyle.Builder()
                .fontFamily(StyleConstants.getFontFamily(attrs))
                .fontSize(StyleConstants.getFontSize(attrs))
                .bold(StyleConstants.isBold(attrs))
                .italic(StyleConstants.isItalic(attrs))
                .underline(StyleConstants.isUnderline(attrs))
                .textColor(StyleConstants.getForeground(attrs))
                .build();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
} 