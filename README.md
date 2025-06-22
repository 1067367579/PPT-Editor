# PPT编辑器

一个功能完整的Java桌面幻灯片演示编辑器，基于Swing GUI框架开发。

## 功能特性

### 基本图形支持
- ✅ 创建和编辑文本框（TextElement）
- ✅ 创建图形（矩形、椭圆、图标等）
- ✅ 支持图片导入
- ✅ 元素属性编辑（位置、大小、颜色）
- ✅ 文本样式设置（字体、字号、颜色、加粗/斜体/下划线）

### 元素交互
- ✅ 鼠标拖放操作
- ✅ 元素选中和多选
- ✅ 元素拖动、缩放、旋转
- ✅ 快捷键支持：
  - `Ctrl+C/V` - 复制/粘贴
  - `Delete` - 删除
  - `Ctrl+Z/Y` - 撤销/重做
  - 方向键 - 微调元素位置
- ✅ 自动对齐和网格吸附

### 多页结构与动画
- ✅ 多页幻灯片支持
- ✅ 幻灯片操作：拖动排序、重命名、复制、删除
- ✅ 切换动画效果（淡入、滑动等）

### 文件操作与持久化
- ✅ 保存/加载项目（JSON格式）
- 🔲 导出为图片（PNG/JPEG）
- 🔲 导出为PDF
- ✅ 超链接功能

### 配色风格与母版
- ✅ 多套配色主题
- ✅ 幻灯片母版系统
- ✅ 统一背景和页眉页脚样式

### 展示模式
- 🔲 演示模式播放

## 技术架构

### 使用的编程技术（4种以上）
1. **泛型（Generics）** - `SlideElement<T extends ElementStyle>`
2. **枚举（Enums）** - `ElementType`, `AnimationType`
3. **注解（Annotations）** - `@Serializable` 自定义注解
4. **方法重载（Method Overloading）** - `setPosition()`, `setSize()` 等
5. **抽象类（Abstract Classes）** - `SlideElement` 抽象基类
6. **Lambda表达式和Stream API** - 元素查找、筛选、操作
7. **接口（Interfaces）** - `ElementStyle`, `Command`

### 设计模式（2种以上）
1. **静态工厂模式（Static Factory）** - `ColorTheme.createDefaultTheme()`, `TextElement.createTitle()`
2. **Builder模式** - `TextStyle.Builder`
3. **单例模式（Singleton）** - `CommandManager`
4. **命令模式（Command Pattern）** - 撤销/重做系统
5. **模板方法模式（Template Method）** - `SlideElement.draw()`

### 核心类结构

```
com.ppteditor/
├── core/
│   ├── annotations/
│   │   └── Serializable.java          # 自定义序列化注解
│   ├── enums/
│   │   ├── ElementType.java           # 元素类型枚举
│   │   └── AnimationType.java         # 动画类型枚举
│   ├── model/
│   │   ├── ElementStyle.java          # 样式接口
│   │   ├── TextStyle.java             # 文本样式（Builder模式）
│   │   ├── ShapeStyle.java            # 图形样式
│   │   ├── SlideElement.java          # 抽象元素基类（泛型）
│   │   ├── TextElement.java           # 文本元素（静态工厂）
│   │   ├── RectangleElement.java      # 矩形元素
│   │   ├── ColorTheme.java            # 配色主题（静态工厂）
│   │   ├── SlideMaster.java           # 幻灯片母版
│   │   ├── Slide.java                 # 幻灯片类（Stream API）
│   │   └── Presentation.java          # 演示文档类
│   └── command/
│       ├── Command.java               # 命令接口
│       ├── CommandManager.java        # 命令管理器（单例）
│       └── AddElementCommand.java     # 添加元素命令
├── ui/
│   └── MainWindow.java                # 主窗口界面
└── PPTEditorApplication.java          # 应用程序入口
```

## 环境要求

- JDK 17+
- Maven 3.6+

## 运行方式

### 编译项目
```bash
mvn compile
```

### 运行应用程序
```bash
mvn exec:java -Dexec.mainClass="com.ppteditor.PPTEditorApplication"
```

### 打包
```bash
mvn package
```

## 主要依赖

- **Jackson** - JSON序列化/反序列化
- **iText7** - PDF生成
- **Apache Commons Imaging** - 图像处理
- **JUnit 5** - 单元测试

## 架构特点

### 面向对象设计
- 使用继承和多态实现不同类型的幻灯片元素
- 接口定义规范，抽象类提供骨架实现
- 泛型确保类型安全

### 功能模块化
- 核心模型与UI分离
- 命令模式实现操作的撤销/重做
- 策略模式处理不同样式和动画

### 扩展性
- 新元素类型易于添加
- 样式系统支持主题切换
- 命令系统支持批量操作

## 开发状态

- ✅ 核心架构完成
- ✅ 基础元素系统
- ✅ 撤销/重做功能
- ✅ 样式和主题系统
- ✅ 基础UI框架
- 🔲 完整的元素编辑器
- 🔲 文件导入/导出
- 🔲 演示模式
- 🔲 动画效果实现

## 贡献

这是一个演示项目，展示了Java面向对象编程、设计模式和GUI开发的最佳实践。 