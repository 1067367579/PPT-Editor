package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import com.ppteditor.core.enums.AnimationType;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 幻灯片类
 * 包含多个元素和幻灯片的基本属性
 */
public class Slide implements Cloneable {
    
    @Serializable(required = true)
    private String id;
    
    @Serializable
    private String name;
    
    @Serializable
    private List<SlideElement<?>> elements;
    
    @Serializable
    private Color backgroundColor;
    
    @Serializable
    private String backgroundImagePath;
    
    @Serializable
    private String notes; // 演讲者备注
    
    @Serializable
    private Dimension size; // 幻灯片尺寸
    
    // 非序列化字段
    private transient BufferedImage backgroundImage;
    private transient Set<SlideElement<?>> selectedElements;
    
    public Slide() {
        this.id = UUID.randomUUID().toString();
        this.name = "幻灯片";
        this.elements = new ArrayList<>();
        this.backgroundColor = Color.WHITE;
        this.notes = "";
        this.size = new Dimension(SlideMaster.STANDARD_WIDTH, SlideMaster.STANDARD_HEIGHT);
        this.selectedElements = new HashSet<>();
    }
    
    public Slide(String name) {
        this();
        this.name = name;
    }
    
    // 元素管理方法
    public void addElement(SlideElement<?> element) {
        if (element != null) {
            elements.add(element);
            // 自动设置z-index
            element.setZIndex(elements.size());
        }
    }
    
    public void removeElement(SlideElement<?> element) {
        elements.remove(element);
        selectedElements.remove(element);
        // 重新排序z-index
        reorderZIndex();
    }
    
    public void removeElement(String elementId) {
        elements.removeIf(element -> element.getId().equals(elementId));
        selectedElements.removeIf(element -> element.getId().equals(elementId));
        reorderZIndex();
    }
    
    public void removeSelectedElements() {
        elements.removeAll(selectedElements);
        selectedElements.clear();
        reorderZIndex();
    }
    
    public void removeMasterElements() {
        // 移除所有锁定的元素（母版元素）
        elements.removeIf(element -> element.isLocked());
        selectedElements.removeIf(element -> element.isLocked());
        reorderZIndex();
    }
    
    private void reorderZIndex() {
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).setZIndex(i + 1);
        }
    }
    
    // 元素查找方法 - 使用Stream API
    public SlideElement<?> findElementById(String id) {
        return elements.stream()
                .filter(element -> element.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public List<SlideElement<?>> findElementsAt(Point point) {
        return elements.stream()
                .filter(element -> element.contains(point))
                .sorted((e1, e2) -> Integer.compare(e2.getZIndex(), e1.getZIndex())) // 按z-index降序
                .collect(Collectors.toList());
    }
    
    public List<SlideElement<?>> findElementsInArea(Rectangle area) {
        return elements.stream()
                .filter(element -> area.intersects(element.getBounds()))
                .collect(Collectors.toList());
    }
    
    public List<SlideElement<?>> findElementsByType(Class<?> elementType) {
        return elements.stream()
                .filter(elementType::isInstance)
                .collect(Collectors.toList());
    }
    
    // 选择管理方法
    public void selectElement(SlideElement<?> element) {
        clearSelection();
        if (element != null) {
            element.setSelected(true);
            selectedElements.add(element);
        }
    }
    
    public void addToSelection(SlideElement<?> element) {
        if (element != null) {
            element.setSelected(true);
            selectedElements.add(element);
        }
    }
    
    public void removeFromSelection(SlideElement<?> element) {
        if (element != null) {
            element.setSelected(false);
            selectedElements.remove(element);
        }
    }
    
    public void clearSelection() {
        selectedElements.forEach(element -> element.setSelected(false));
        selectedElements.clear();
    }
    
    public void selectAll() {
        clearSelection();
        elements.forEach(this::addToSelection);
    }
    
    // 层级操作方法
    public void bringToFront(SlideElement<?> element) {
        if (elements.contains(element)) {
            elements.remove(element);
            elements.add(element);
            reorderZIndex();
        }
    }
    
    public void sendToBack(SlideElement<?> element) {
        if (elements.contains(element)) {
            elements.remove(element);
            elements.add(0, element);
            reorderZIndex();
        }
    }
    
    public void bringForward(SlideElement<?> element) {
        int index = elements.indexOf(element);
        if (index >= 0 && index < elements.size() - 1) {
            Collections.swap(elements, index, index + 1);
            reorderZIndex();
        }
    }
    
    public void sendBackward(SlideElement<?> element) {
        int index = elements.indexOf(element);
        if (index > 0) {
            Collections.swap(elements, index, index - 1);
            reorderZIndex();
        }
    }
    
    // 元素复制方法
    public void duplicateElement(SlideElement<?> element) {
        SlideElement<?> cloned = element.clone();
        cloned.move(10, 10); // 稍微偏移位置
        addElement(cloned);
    }
    
    public void duplicateSelectedElements() {
        List<SlideElement<?>> toClone = new ArrayList<>(selectedElements);
        clearSelection();
        toClone.forEach(this::duplicateElement);
    }
    
    // 渲染方法
    public void render(Graphics2D g2d, Dimension slideSize) {
        // 绘制背景
        renderBackground(g2d, slideSize);
        
        // 按z-index排序后绘制元素
        elements.stream()
                .sorted(Comparator.comparingInt(SlideElement::getZIndex))
                .forEach(element -> element.draw(g2d));
    }
    
    private void renderBackground(Graphics2D g2d, Dimension slideSize) {
        // 绘制背景色
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, slideSize.width, slideSize.height);
        
        // 绘制背景图片
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, slideSize.width, slideSize.height, null);
        }
    }
    
    // 对齐功能
    public void alignLeft() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素对齐到画布左边
            selectedElements.forEach(element -> element.setX(0));
        } else {
            // 多个元素对齐到最左边的元素
            double minX = selectedElements.stream()
                    .mapToDouble(SlideElement::getX)
                    .min()
                    .orElse(0);
            selectedElements.forEach(element -> element.setX(minX));
        }
    }
    
    public void alignRight() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素对齐到画布右边（假设画布宽度800）
            selectedElements.forEach(element -> 
                element.setX(800 - element.getWidth()));
        } else {
            // 多个元素对齐到最右边的元素
            double maxX = selectedElements.stream()
                    .mapToDouble(element -> element.getX() + element.getWidth())
                    .max()
                    .orElse(0);
            selectedElements.forEach(element -> 
                element.setX(maxX - element.getWidth()));
        }
    }
    
    public void alignTop() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素对齐到画布顶部
            selectedElements.forEach(element -> element.setY(0));
        } else {
            // 多个元素对齐到最顶部的元素
            double minY = selectedElements.stream()
                    .mapToDouble(SlideElement::getY)
                    .min()
                    .orElse(0);
            selectedElements.forEach(element -> element.setY(minY));
        }
    }
    
    public void alignBottom() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素对齐到画布底部（假设画布高度450）
            selectedElements.forEach(element -> 
                element.setY(450 - element.getHeight()));
        } else {
            // 多个元素对齐到最底部的元素
            double maxY = selectedElements.stream()
                    .mapToDouble(element -> element.getY() + element.getHeight())
                    .max()
                    .orElse(0);
            selectedElements.forEach(element -> 
                element.setY(maxY - element.getHeight()));
        }
    }
    
    public void alignCenterHorizontal() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素水平居中到画布
            selectedElements.forEach(element -> 
                element.setX((800 - element.getWidth()) / 2));
        } else {
            // 多个元素水平居中对齐
            double avgX = selectedElements.stream()
                    .mapToDouble(element -> element.getX() + element.getWidth() / 2)
                    .average()
                    .orElse(0);
            selectedElements.forEach(element -> 
                element.setX(avgX - element.getWidth() / 2));
        }
    }
    
    public void alignCenterVertical() {
        if (selectedElements.isEmpty()) return;
        
        if (selectedElements.size() == 1) {
            // 单个元素垂直居中到画布
            selectedElements.forEach(element -> 
                element.setY((450 - element.getHeight()) / 2));
        } else {
            // 多个元素垂直居中对齐
            double avgY = selectedElements.stream()
                    .mapToDouble(element -> element.getY() + element.getHeight() / 2)
                    .average()
                    .orElse(0);
            selectedElements.forEach(element -> 
                element.setY(avgY - element.getHeight() / 2));
        }
    }
    
    @Override
    public Slide clone() {
        try {
            Slide cloned = (Slide) super.clone();
            cloned.id = UUID.randomUUID().toString();
            cloned.name = this.name + " 副本";
            cloned.elements = new ArrayList<>();
            cloned.selectedElements = new HashSet<>();
            
            // 克隆所有元素
            this.elements.forEach(element -> 
                cloned.elements.add(element.clone()));
                
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆失败", e);
        }
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<SlideElement<?>> getElements() { return new ArrayList<>(elements); }
    public void setElements(List<SlideElement<?>> elements) { 
        this.elements = new ArrayList<>(elements);
        this.selectedElements = new HashSet<>();
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    public String getBackgroundImagePath() { return backgroundImagePath; }
    public void setBackgroundImagePath(String backgroundImagePath) { 
        this.backgroundImagePath = backgroundImagePath;
        // TODO: 加载背景图片
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Set<SlideElement<?>> getSelectedElements() { 
        return new HashSet<>(selectedElements); 
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasSelection() { return !selectedElements.isEmpty(); }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getElementCount() { return elements.size(); }
    
    public Dimension getSize() { return new Dimension(size); }
    
    public void setSize(Dimension size) { 
        this.size = new Dimension(size);
    }
    
    public int getWidth() { return size.width; }
    
    public int getHeight() { return size.height; }
} 