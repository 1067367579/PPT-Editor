package com.ppteditor.core.model;

import com.ppteditor.core.annotations.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演示文档类
 * 管理整个PPT文档，包含多个幻灯片
 */
public class Presentation implements Cloneable {
    
    @Serializable(required = true)
    private String id;
    
    @Serializable(required = true)
    private String title;
    
    @Serializable
    private String author;
    
    @Serializable
    private Date createdTime;
    
    @Serializable
    private Date modifiedTime;
    
    @Serializable
    private List<Slide> slides;
    
    @Serializable
    private ColorTheme colorTheme;
    
    @Serializable
    private SlideMaster slideMaster; // 母版
    
    @Serializable
    private Map<String, Object> metadata; // 元数据
    
    private transient int currentSlideIndex;
    private transient String filePath;
    private transient boolean modified;
    
    public Presentation() {
        this.id = UUID.randomUUID().toString();
        this.title = "新建演示文稿";
        this.author = System.getProperty("user.name");
        this.createdTime = new Date();
        this.modifiedTime = new Date();
        this.slides = new ArrayList<>();
        this.colorTheme = ColorTheme.createDefaultTheme();
        this.metadata = new HashMap<>();
        this.currentSlideIndex = 0;
        this.modified = false;
        
        // 创建默认幻灯片
        addSlide(new Slide("标题幻灯片"));
    }
    
    public Presentation(String title) {
        this();
        this.title = title;
    }
    
    // 幻灯片管理方法
    public void addSlide(Slide slide) {
        if (slide != null) {
            slides.add(slide);
            markAsModified();
        }
    }
    
    public void addSlide(int index, Slide slide) {
        if (slide != null && index >= 0 && index <= slides.size()) {
            slides.add(index, slide);
            markAsModified();
        }
    }
    
    public void insertSlide(int index, Slide slide) {
        addSlide(index, slide);
    }
    
    public void removeSlide(int index) {
        if (index >= 0 && index < slides.size()) {
            slides.remove(index);
            // 调整当前幻灯片索引
            if (currentSlideIndex >= slides.size()) {
                currentSlideIndex = Math.max(0, slides.size() - 1);
            }
            markAsModified();
        }
    }
    
    public void removeSlide(Slide slide) {
        int index = slides.indexOf(slide);
        if (index >= 0) {
            removeSlide(index);
        }
    }
    
    public void moveSlide(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < slides.size() && 
            toIndex >= 0 && toIndex < slides.size() && 
            fromIndex != toIndex) {
            
            Slide slide = slides.remove(fromIndex);
            slides.add(toIndex, slide);
            
            // 更新当前索引
            if (currentSlideIndex == fromIndex) {
                currentSlideIndex = toIndex;
            } else if (fromIndex < currentSlideIndex && toIndex >= currentSlideIndex) {
                currentSlideIndex--;
            } else if (fromIndex > currentSlideIndex && toIndex <= currentSlideIndex) {
                currentSlideIndex++;
            }
            
            markAsModified();
        }
    }
    
    public void duplicateSlide(int index) {
        if (index >= 0 && index < slides.size()) {
            Slide original = slides.get(index);
            Slide cloned = original.clone();
            slides.add(index + 1, cloned);
            markAsModified();
        }
    }
    
    // 导航方法
    public boolean hasNextSlide() {
        return currentSlideIndex < slides.size() - 1;
    }
    
    public boolean hasPreviousSlide() {
        return currentSlideIndex > 0;
    }
    
    public void nextSlide() {
        if (hasNextSlide()) {
            currentSlideIndex++;
        }
    }
    
    public void previousSlide() {
        if (hasPreviousSlide()) {
            currentSlideIndex--;
        }
    }
    
    public void goToSlide(int index) {
        if (index >= 0 && index < slides.size()) {
            currentSlideIndex = index;
        }
    }
    
    public Slide getCurrentSlide() {
        if (slides.isEmpty()) {
            return null;
        }
        return slides.get(currentSlideIndex);
    }
    
    public Slide getSlide(int index) {
        if (index >= 0 && index < slides.size()) {
            return slides.get(index);
        }
        return null;
    }
    
    // 查找方法 - 使用Stream API
    public List<Slide> findSlidesByName(String namePattern) {
        return slides.stream()
                .filter(slide -> slide.getName().toLowerCase()
                    .contains(namePattern.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<Slide> findSlidesWithText(String text) {
        return slides.stream()
                .filter(slide -> slide.getElements().stream()
                    .filter(element -> element instanceof TextElement)
                    .map(element -> (TextElement) element)
                    .anyMatch(textElement -> textElement.getText()
                        .toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    public long countElementsByType(Class<?> elementType) {
        return slides.stream()
                .flatMap(slide -> slide.getElements().stream())
                .filter(elementType::isInstance)
                .count();
    }
    
    // 主题应用方法
    public void applyColorTheme(ColorTheme theme) {
        this.colorTheme = theme;
        
        // 应用到所有幻灯片
        slides.forEach(slide -> {
            slide.setBackgroundColor(theme.getBackgroundColor());
            
            // 应用到所有元素
            slide.getElements().forEach(element -> {
                if (element.getStyle() != null) {
                    element.getStyle().applyColorTheme(theme);
                }
            });
        });
        
        markAsModified();
    }
    
    // 统计信息
    public int getTotalSlides() {
        return slides.size();
    }
    
    public int getTotalElements() {
        return slides.stream()
                .mapToInt(Slide::getElementCount)
                .sum();
    }
    
    public Map<Class<?>, Long> getElementStatistics() {
        return slides.stream()
                .flatMap(slide -> slide.getElements().stream())
                .collect(Collectors.groupingBy(
                    Object::getClass,
                    Collectors.counting()
                ));
    }
    
    // 修改状态管理
    private void markAsModified() {
        this.modified = true;
        this.modifiedTime = new Date();
    }
    
    public void markAsSaved() {
        this.modified = false;
    }
    
    @Override
    public Presentation clone() {
        try {
            Presentation cloned = (Presentation) super.clone();
            cloned.id = UUID.randomUUID().toString();
            cloned.title = this.title + " 副本";
            cloned.createdTime = new Date();
            cloned.modifiedTime = new Date();
            cloned.slides = new ArrayList<>();
            cloned.metadata = new HashMap<>(this.metadata);
            cloned.currentSlideIndex = 0;
            cloned.modified = true;
            
            // 克隆所有幻灯片
            this.slides.forEach(slide -> 
                cloned.slides.add(slide.clone()));
            
            // 克隆主题
            if (this.colorTheme != null) {
                // ColorTheme需要实现克隆方法
                cloned.colorTheme = this.colorTheme;
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("克隆失败", e);
        }
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        markAsModified();
    }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { 
        this.author = author; 
        markAsModified();
    }
    
    public Date getCreatedTime() { return createdTime; }
    
    public Date getModifiedTime() { return modifiedTime; }
    
    public List<Slide> getSlides() { return new ArrayList<>(slides); }
    public void setSlides(List<Slide> slides) { 
        this.slides = new ArrayList<>(slides);
        this.currentSlideIndex = 0;
        markAsModified();
    }
    
    public ColorTheme getColorTheme() { return colorTheme; }
    public void setColorTheme(ColorTheme colorTheme) { 
        this.colorTheme = colorTheme; 
        markAsModified();
    }
    
    public SlideMaster getSlideMaster() { return slideMaster; }
    public void setSlideMaster(SlideMaster slideMaster) { 
        this.slideMaster = slideMaster; 
        markAsModified();
    }
    
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void setMetadata(Map<String, Object> metadata) { 
        this.metadata = new HashMap<>(metadata); 
        markAsModified();
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
        markAsModified();
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public int getCurrentSlideIndex() { return currentSlideIndex; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public boolean isModified() { return modified; }
} 