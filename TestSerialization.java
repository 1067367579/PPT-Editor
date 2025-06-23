import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class TestSerialization {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // 创建一个简单的ColorTheme
            ColorTheme theme = new ColorTheme();
            theme.setName("测试主题");
            
            // 序列化到文件
            mapper.writeValue(new File("test_theme.json"), theme);
            System.out.println("序列化成功");
            
            // 反序列化
            ColorTheme loaded = mapper.readValue(new File("test_theme.json"), ColorTheme.class);
            System.out.println("反序列化成功: " + loaded.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ColorTheme {
    private String name;
    
    public ColorTheme() {}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
} 