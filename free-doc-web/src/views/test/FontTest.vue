<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-4xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">字体测试页面</h1>

        <!-- 控制面板 -->
        <div class="flex items-center gap-4 mb-8 p-4 bg-gray-50 rounded-lg">
          <span class="text-sm font-medium text-gray-700">字体控制：</span>
          <FontSizeToggle />
          <FontFamilyToggle />
        </div>

        <!-- 字体展示区域 -->
        <div class="space-y-8">
          <!-- 中文文本 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">中文文本测试</h2>
            <div class="space-y-2">
              <p class="text-lg">这是大号中文文本：春江潮水连海平，海上明月共潮生。</p>
              <p class="text-base">这是中号中文文本：滟滟随波千万里，何处春江无月明。</p>
              <p class="text-sm">这是小号中文文本：江流宛转绕芳甸，月照花林皆似霰。</p>
            </div>
          </div>

          <!-- 英文文本 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">English Text Test</h2>
            <div class="space-y-2">
              <p class="text-lg">Large English text: The quick brown fox jumps over the lazy dog.</p>
              <p class="text-base">Medium English text: Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
              <p class="text-sm">Small English text: Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
            </div>
          </div>

          <!-- 混合文本 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">混合文本测试 Mixed Text</h2>
            <div class="space-y-2">
              <p class="text-lg">FreeDoc 是一个现代化的文档协作平台 (Modern Document Collaboration Platform)</p>
              <p class="text-base">支持实时编辑 Real-time editing、版本控制 Version control、团队协作 Team collaboration</p>
              <p class="text-sm">使用 Vue 3 + TypeScript + Tailwind CSS 构建 Built with modern web technologies</p>
            </div>
          </div>

          <!-- 特殊字符 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">特殊字符测试</h2>
            <div class="space-y-2">
              <p class="text-lg">数字：1234567890 符号：!@#$%^&*()_+-=[]{}|;':\",./<>?</p>
              <p class="text-base">中文标点：，。！？；：""''（）【】《》</p>
              <p class="text-sm">Unicode: ★☆♠♣♥♦♪♫☀☁☂☃❄⚡⭐🌟💫✨</p>
            </div>
          </div>

          <!-- 代码文本 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">代码文本测试</h2>
            <div class="bg-gray-100 rounded-lg p-4">
              <pre class="text-sm"><code>function greet(name: string): string {
  return `Hello, ${name}! 你好，${name}！`;
}

const message = greet("World");
console.log(message);</code></pre>
            </div>
          </div>

          <!-- 文档内容测试区域 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-800">文档内容字体测试</h2>
            <div class="border border-gray-200 rounded-lg p-6">
              <ApiMarkdownRenderer :content="markdownContent" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FontSizeToggle from '@/components/FontSizeToggle.vue'
import FontFamilyToggle from '@/components/FontFamilyToggle.vue'
import ApiMarkdownRenderer from '@/components/ApiMarkdownRenderer.vue'

const markdownContent = `# 文档标题测试

这是一个测试文档，用于验证字体大小设置是否正确应用到文档内容区域。

## 二级标题

这是正文段落，应该使用基础字体大小。当你调整字体大小设置时，这段文字的大小应该会相应变化。

### 三级标题

- 这是一个列表项
- 另一个列表项
- 第三个列表项

#### 四级标题

1. 有序列表第一项
2. 有序列表第二项
3. 有序列表第三项

##### 五级标题

这是一段包含 \`内联代码\` 的文本。

###### 六级标题

\`\`\`javascript
// 这是代码块
function testFunction() {
  console.log("Hello, World!");
  return "测试代码块字体大小";
}
\`\`\`

> 这是一个引用块，用于测试引用文本的字体大小。
> 
> 引用块中的文字也应该跟随全局字体大小设置。

| 表头1 | 表头2 | 表头3 |
|-------|-------|-------|
| 单元格1 | 单元格2 | 单元格3 |
| 数据1 | 数据2 | 数据3 |

**粗体文本** 和 *斜体文本* 也应该正确应用字体大小。

---

这是分隔线下方的内容，用于测试各种 Markdown 元素的字体大小是否正确应用。
`
</script>