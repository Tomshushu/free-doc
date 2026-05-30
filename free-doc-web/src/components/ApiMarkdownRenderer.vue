<template>
  <div class="api-markdown-shell" :class="{ 'no-toc': tocItems.length === 0 }">
    <article ref="articleRef" class="api-markdown-body doc-markdown-content" v-html="renderedHtml"></article>
    <aside v-if="tocItems.length > 0" class="api-markdown-toc">
      <div class="api-markdown-toc__title">
        <i class="fa-solid fa-list-ul mr-1.5 text-[11px] opacity-60"></i>{{ $t('api.tocNav') }}
      </div>
      <nav class="api-markdown-toc__nav">
        <a
          v-for="item in tocItems"
          :key="item.id"
          :href="`#${item.id}`"
          :class="['api-markdown-toc__item', `is-level-${item.level}`, { 'is-active': activeHeadingId === item.id }]"
          @click.prevent="handleTocClick(item.id)"
        >
          <span class="toc-dot" v-if="activeHeadingId === item.id"></span>
          {{ item.text }}
        </a>
      </nav>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { marked } from 'marked'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import css from 'highlight.js/lib/languages/css'
import xml from 'highlight.js/lib/languages/xml'
import json from 'highlight.js/lib/languages/json'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
import shell from 'highlight.js/lib/languages/shell'
import cpp from 'highlight.js/lib/languages/cpp'
import csharp from 'highlight.js/lib/languages/csharp'
import go from 'highlight.js/lib/languages/go'
import rust from 'highlight.js/lib/languages/rust'
import php from 'highlight.js/lib/languages/php'
import ruby from 'highlight.js/lib/languages/ruby'
import yaml from 'highlight.js/lib/languages/yaml'
import markdown from 'highlight.js/lib/languages/markdown'
import dockerfile from 'highlight.js/lib/languages/dockerfile'
import nginx from 'highlight.js/lib/languages/nginx'
import diff from 'highlight.js/lib/languages/diff'
import plaintext from 'highlight.js/lib/languages/plaintext'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('css', css)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('json', json)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('shell', shell)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c', cpp)
hljs.registerLanguage('csharp', csharp)
hljs.registerLanguage('cs', csharp)
hljs.registerLanguage('go', go)
hljs.registerLanguage('rust', rust)
hljs.registerLanguage('php', php)
hljs.registerLanguage('ruby', ruby)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('yml', yaml)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('md', markdown)
hljs.registerLanguage('dockerfile', dockerfile)
hljs.registerLanguage('nginx', nginx)
hljs.registerLanguage('diff', diff)
hljs.registerLanguage('plaintext', plaintext)
hljs.registerLanguage('text', plaintext)
import DOMPurify from 'dompurify'
import 'highlight.js/styles/github-dark.css'

const emojiMap: Record<string, string> = {
  smile: '😊', grin: '😁', joy: '😂', rofl: '🤣', wink: '😉',
  blush: '😊', heart_eyes: '😍', kissing_heart: '😘', thinking: '🤔',
  neutral_face: '😐', expressionless: '😑', unamused: '😒', sweat: '😓',
  sad: '😢', cry: '😭', angry: '😠', rage: '🤬',
  smirk: '😏', tongue: '😛', tongue_out_wink: '😜', zipper_mouth: '🤐',
  sleepy: '😪', dizzy: '😵', face_with_cowboy_hat: '🤠', party: '🥳',
  cold_face: '🥶', hot_face: '🥵', nauseated: '🤢', sneezing: '🤧',
  hugging: '🤗', clapping_hands: '👏', waving_hand: '👋', ok_hand: '👌',
  thumbsup: '👍', thumbsdown: '👎', fist: '✊', palm: '🖐️',
  dog: '🐶', cat: '🐱', mouse: '🐭', rabbit: '🐰', bear: '🐻',
  panda: '🐼', koala: '🐨', tiger: '🐯', lion: '🦁', cow: '🐮',
  pig: '🐷', frog: '🐸', snake: '🐍', bird: '🐦', chicken: '🐔',
  penguin: '🐧', butterfly: '🦋', bug: '🐛', snail: '🐌', bee: '🐝',
  rose: '🌹', tulip: '🌷', cherry_blossom: '🌸', sunflower: '🌻', bouquet: '💐',
  apple: '🍎', banana: '🍌', watermelon: '🍉', grape: '🍇',
  strawberry: '🍓', peach: '🍑', cherries: '🍒', leaf: '🍃',
  seedling: '🌱', evergreen_tree: '🌲',
  coffee: '☕', tea: '🍵', beer: '🍺', wine: '🍷', cocktail: '🍸',
  cake: '🎂', cookie: '🍪', candy: '🍬', lollipop: '🍭', chocolate_bar: '🍫',
  pizza: '🍔', fries: '🍟', taco: '🌮', sushi: '🍣', rice: '🍚',
  egg: '🥚', milk: '🥛', bread: '🍞', croissant: '🥐', pancake: '🥞',
  ice_cream: '🍨', icecream: '🍦', dumpling: '🥟', sandwich: '🥪', salad: '🥗',
  popcorn: '🍿', cheese: '🧀', bacon: '🥓', steak: '🥩', shrimp: '🦐',
  soccer: '⚽', basketball: '🏀', football: '🏈', baseball: '⚾', tennis: '🎾',
  volleyball: '🏐', rugby_football: '🏉', golf: '⛳', checkered_flag: '🏁', trophy: '🏆',
  musical_keyboard: '🎹', guitar: '🎸', violin: '🎻', drum: '🥁', headphones: '🎧',
  video_game: '🎮', joystick: '🕹️', dart: '🎯', bowling: '🎳', fishing_pole_and_fish: '🎣',
  bicycle: '🚲', car: '🚗', bus: '🚌', airplane: '✈️', rocket: '🚀',
  fire: '🔥', star: '⭐', sparkles: '✨',
  heart: '❤️', orange_heart: '🧡', yellow_heart: '💛',
  green_heart: '💚', blue_heart: '💙', purple_heart: '💜', broken_heart: '💔', two_hearts: '💕',
  '100': '💯', copyright: '©️', registered: '®️', tm: '™️',
  check: '✅', x: '❌', warning: '⚠️', bulb: '💡',
  lock: '🔒', unlock: '🔓', key: '🔑', link: '🔗',
  arrow_up: '⬆️', arrow_down: '⬇️', arrow_left: '⬅️', arrow_right: '➡️',
  recycle: '♻️', no_entry: '⛔', exclamation: '❗', question: '❓',
}

function preprocessEmojiShortcodes(text: string): string {
  return text.replace(/:([a-z0-9_+-]+):/g, (match, code) => {
    return emojiMap[code] || match
  })
}

let markedExtensionsInitialized = false
if (!markedExtensionsInitialized) {
  marked.use({
    renderer: {
      code(code: unknown, language?: string) {
        let normalizedCode = ''
        let normalizedLanguage = (language || '').trim()

        if (typeof code === 'string') {
          normalizedCode = code
        } else if (code && typeof code === 'object') {
          const token = code as { text?: string; raw?: string; lang?: string; language?: string }
          normalizedCode = token.text ?? token.raw ?? ''
          if (!normalizedLanguage) {
            normalizedLanguage = (token.lang || token.language || '').trim()
          }
        } else {
          normalizedCode = String(code ?? '')
        }

        const validLang = normalizedLanguage && hljs.getLanguage(normalizedLanguage) ? normalizedLanguage : ''
        const highlighted = validLang
          ? hljs.highlight(normalizedCode, { language: validLang }).value
          : hljs.highlightAuto(normalizedCode).value

        return `<pre><code class="hljs language-${validLang || 'plaintext'}">${highlighted}</code></pre>`
      }
    }
  })
  markedExtensionsInitialized = true
}

const { t } = useI18n()

type TocItem = {
  id: string
  text: string
  level: number
}

const props = withDefaults(defineProps<{
  content: string
}>(), {
  content: ''
})

const articleRef = ref<HTMLElement | null>(null)
const activeHeadingId = ref('')
let headingObserver: IntersectionObserver | null = null

function escapeHtml(raw: string): string {
  return raw.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function slugify(text: string): string {
  return text
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5- ]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}

function escapeAttr(raw: string): string {
  return raw.replace(/"/g, '&quot;').replace(/'/g, '&#39;')
}

function parseKeyValueLines(blockContent: string): Record<string, string> {
  const lines = blockContent.split('\n').map((line) => line.trim()).filter(Boolean)
  const map: Record<string, string> = {}
  for (const line of lines) {
    const separatorIdx = line.indexOf(':')
    if (separatorIdx <= 0) continue
    const key = line.slice(0, separatorIdx).trim().toLowerCase()
    const value = line.slice(separatorIdx + 1).trim()
    map[key] = value
  }
  return map
}

// ===== 接口端点卡片（顶部核心信息） =====
function renderEndpointCard(
  title: string,
  method: string,
  path: string,
  desc: string,
  auth: string,
  fields: Record<string, string>
): string {
  const methodClass = `is-${(method || 'GET').toLowerCase()}`
  const status = fields.status || ''
  const contentType = fields.contenttype || fields['content-type'] || ''

  return [
    `<div class="api-endpoint-card">`,
    `<div class="api-endpoint-card__header">`,
    `<div class="api-endpoint-card__method-row">`,
    `<span class="api-method-badge ${methodClass}">${escapeHtml(method || 'GET')}</span>`,
    `<code class="api-url-path">${escapeHtml(path)}</code>`,
    `<button class="api-copy-url-btn" data-copy-url="${escapeAttr(path)}" title="${t('api.copyUrl')}">`,
    `<i class="fa-regular fa-copy"></i>`,
    `</button>`,
    `</div>`,
    title ? `<h4 class="api-endpoint-card__title">${escapeHtml(title)}</h4>` : '',
    `</div>`,
    desc ? `<p class="api-endpoint-card__desc">${escapeHtml(desc)}</p>` : '',
    `<div class="api-endpoint-meta">`,
    auth !== undefined && auth !== '' ? `<span class="meta-tag"><i class="fa-solid fa-key mr-1 text-[10px]"></i>${auth === t('api.none') ? t('api.noAuth') : escapeHtml(auth)}</span>` : '',
    status ? `<span class="meta-tag"><i class="fa-solid fa-signal mr-1 text-[10px]"></i>${escapeHtml(status)}</span>` : '',
    contentType ? `<span class="meta-tag"><i class="fa-solid fa-code mr-1 text-[10px]"></i>${escapeHtml(contentType)}</span>` : '',
    `</div>`,
    `</div>`
  ].join('')
}

// ===== 信息卡片（请求/响应/错误） =====
function renderInfoCard(
  label: string,
  type: 'request' | 'response' | 'error' | 'note' | 'warning',
  fields: Record<string, string>
): string {
  const method = (fields.method || '').toUpperCase()
  const path = fields.path || fields.url || ''
  const desc = fields.description || fields.desc || fields.content || ''
  const contentType = fields.contenttype || fields['content-type'] || ''
  const status = fields.status || ''

  // 图标映射
  const iconMap: Record<string, string> = { request: 'fa-arrow-right', response: 'fa-reply', error: 'fa-triangle-exclamation', note: 'fa-circle-info', warning: 'fa-lightbulb' }
  const iconName = iconMap[type] || 'fa-circle-info'

  return [
    `<div class="api-info-card is-${type}">`,
    `<div class="api-info-card__header">`,
    `<i class="fa-solid ${iconName} api-info-icon"></i>`,
    `<strong class="api-info-title">${escapeHtml(label)}</strong>`,
    `</div>`,
    (method || path) ? `<div class="api-info-card__row">${method ? `<span class="api-method-badge is-${method.toLowerCase()}">${escapeHtml(method)}</span>` : ''}${path ? `<code class="api-info-path">${escapeHtml(path)}</code>` : ''}</div>` : '',
    status ? `<div class="api-info-meta"><span>Status:</span> <code>${escapeHtml(status)}</code></div>` : '',
    contentType ? `<div class="api-info-meta"><span>Content-Type:</span> <code>${escapeHtml(contentType)}</code></div>` : '',
    desc ? `<div class="api-info-card__body">${desc}</div>` : '',
    `</div>`
  ].join('')
}

// ===== 参数表格（增强版：支持示例值列） =====
function renderParamTable(blockContent: string): string {
  const rows = blockContent
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => line.split('|').map((cell) => cell.trim()))
    .filter((cells) => cells.length >= 3)

  if (rows.length === 0) return ''

  // 检查是否有5列（含示例值）
  const hasExample = rows[0].length >= 5

  const body = rows.map((cells) => {
    const [name, required, type, desc, example] = cells
    const isRequired = required.toLowerCase() === 'true' || required === '是' || required === t('api.yes')
    const badgeClass = isRequired ? 'is-required' : 'is-optional'
    const badgeText = isRequired ? t('api.required') : t('api.optional')
    let exampleCell = hasExample ? `<td><code class="example-val">${escapeHtml(example || '-')}</code></td>` : ''
    return `<tr data-required="${isRequired}">
      <td class="param-name-cell"><code>${escapeHtml(name)}</code></td>
      <td class="param-req-cell"><span class="api-param-badge ${badgeClass}">${badgeText}</span></td>
      <td class="param-type-cell">${escapeHtml(type)}</td>
      <td class="param-desc-cell">${escapeHtml(desc) || '-'}</td>
      ${exampleCell}
    </tr>`
  }).join('')

  const exampleHeader = hasExample ? `<th>${t('api.exampleValue')}</th>` : ''

  return [
    '<div class="api-param-table-wrap">',
    '<table class="api-param-table">',
    `<thead><tr><th>${t('api.paramName')}</th><th>${t('api.required')}</th><th>${t('api.type')}</th><th>${t('api.description')}</th>${exampleHeader}</tr></thead>`,
    `<tbody>${body}</tbody>`,
    '</table>',
    '</div>'
  ].join('')
}

// ===== 注意事项 / 错误码 / 关联接口 块 =====
function renderNoteBlock(title: string, type: 'notice' | 'error-codes' | 'related', content: string): string {
  const icons: Record<string, string> = {
    notice: 'fa-circle-exclamation',
    'error-codes': 'fa-bug',
    related: 'fa-link'
  }
  const labels: Record<string, string> = {
    notice: t('api.notice'),
    'error-codes': t('api.errorCodes'),
    related: t('api.relatedApis')
  }

  // 解析列表项（每行一项）
  const items = content.split('\n').map((l) => l.trim()).filter(Boolean)

  const listHtml = items.length > 0
    ? '<ul class="api-note-list">' + items.map(item => {
        // 支持 - code: message 格式
        if (type === 'error-codes') {
          const sepIdx = item.indexOf(':')
          if (sepIdx > 0) {
            const code = item.slice(0, sepIdx).trim()
            const msg = item.slice(sepIdx + 1).trim()
            return `<li class="error-code-item"><code class="err-code">${escapeHtml(code)}</code><span class="err-msg">${escapeHtml(msg)}</span></li>`
          }
        } else if (type === 'related') {
          // 支持链接格式 - [文字](url) 或纯文本
          const linkMatch = item.match(/^\[(.+?)\]\((.+)\)$/)
          if (linkMatch) {
            return `<li><a href="${linkMatch[2]}" target="_blank" rel="noopener" class="related-link"><i class="fa-solid fa-arrow-up-right-from-square mr-1 text-[10px]"></i>${linkMatch[1]}</a></li>`
          }
        }
        return `<li>${escapeHtml(item)}</li>`
      }).join('') + '</ul>'
    : `<p class="api-note-text">${escapeHtml(content)}</p>`

  return [
    `<div class="api-note-block is-${type}">`,
    `<div class="api-note-block__header">`,
    `<i class="fa-solid ${icons[type] || 'fa-circle-info'}"></i>`,
    `<strong>${labels[type] || title}</strong>`,
    `</div>`,
    `<div class="api-note-block__body">${listHtml}</div>`,
    `</div>`
  ].join('')
}

// ===== Markdown 预处理 =====
function preprocessMarkdownBlocks(markdown: string): string {
  let output = markdown

  // :::api ... ::: 接口端点卡片
  output = output.replace(/:::api\s*\n([\s\S]*?)\n:::/g, (_, blockContent) => {
    const map = parseKeyValueLines(blockContent)
    return renderEndpointCard(
      map.title || t('api.apiEndpoint'),
      (map.method || 'GET').toUpperCase(),
      map.path || map.url || '/',
      map.description || map.desc || '',
      map.auth || t('api.none'),
      map
    )
  })

  // :::request ... ::: 请求信息卡片
  output = output.replace(/:::request\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderInfoCard(t('api.requestDesc'), 'request', parseKeyValueLines(block))
  )

  // :::response ... ::: 响应信息卡片
  output = output.replace(/:::response\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderInfoCard(t('api.responseDesc'), 'response', parseKeyValueLines(block))
  )

  // :::error ... ::: 错误信息卡片
  output = output.replace(/:::error\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderInfoCard(t('api.errorDesc'), 'error', parseKeyValueLines(block))
  )

  // :::params ... ::: 参数表格
  output = output.replace(/:::params\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderParamTable(block)
  )

  // :::notice ... ::: 注意事项
  output = output.replace(/:::notice\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderNoteBlock(t('api.notice'), 'notice', block.trim())
  )

  // :::error-codes ... ::: 错误码列表
  output = output.replace(/:::error-codes\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderNoteBlock(t('api.errorCodes'), 'error-codes', block.trim())
  )

  // :::related ... ::: 关联接口
  output = output.replace(/:::related\s*\n([\s\S]*?)\n:::/g, (_, block) =>
    renderNoteBlock(t('api.relatedApis'), 'related', block.trim())
  )

  return output
}

// ===== 渲染 Markdown =====
function renderMarkdown(content: string): { html: string; toc: TocItem[] } {
  let source = preprocessMarkdownBlocks(content || '')
  source = preprocessEmojiShortcodes(source)

  // 保护代码块，避免自定义语法在代码块内被替换
  const codeBlocks: string[] = []
  let processed = source.replace(/```[\s\S]*?```/g, (match) => {
    codeBlocks.push(match)
    return `__CODE_BLOCK_${codeBlocks.length - 1}__`
  })
  processed = processed.replace(/`[^`]+?`/g, (match) => {
    codeBlocks.push(match)
    return `__CODE_BLOCK_${codeBlocks.length - 1}__`
  })

  // 在 marked 解析前替换自定义语法为 HTML 标签
  processed = processed
    .replace(/==([^=]+)==/g, '<mark>$1</mark>')
    .replace(/\^([^\^]+)\^/g, '<sup>$1</sup>')
    .replace(/(?<!~)~([^~]+)~(?!~)/g, '<sub>$1</sub>')

  // 恢复代码块
  codeBlocks.forEach((block, i) => {
    processed = processed.replace(`__CODE_BLOCK_${i}__`, block)
  })

  const html = marked.parse(processed, {
    gfm: true,
    breaks: true
  }) as string

  const safeHtml = DOMPurify.sanitize(html, {
    USE_PROFILES: { html: true },
    ADD_ATTR: ['target', 'rel', 'class', 'id', 'checked', 'disabled', 'type'],
    ADD_TAGS: ['mark', 'sup', 'sub', 'input', 'section']
  })

  const parser = new DOMParser()
  const doc = parser.parseFromString(safeHtml, 'text/html')
  const toc: TocItem[] = []
  const slugCounter = new Map<string, number>()

  doc.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((heading) => {
    let text = (heading.textContent || '').trim()
    if (!text) return

    const idMatch = text.match(/\{#([^}]+)\}\s*$/)
    let customId = ''
    if (idMatch) {
      customId = idMatch[1].trim()
      text = text.replace(/\s*\{#[^}]+\}\s*$/, '').trim()
      heading.innerHTML = heading.innerHTML.replace(/\s*\{#[^}]+\}\s*/, '')
    }

    const baseId = customId || slugify(text) || 'section'
    const count = slugCounter.get(baseId) || 0
    slugCounter.set(baseId, count + 1)
    const id = count === 0 ? baseId : `${baseId}-${count + 1}`
    const level = Number(heading.tagName.slice(1))

    heading.setAttribute('id', id)
    heading.classList.add('api-markdown-heading')
    heading.innerHTML = `<a href="#${id}" class="heading-anchor">#</a>${heading.innerHTML}`

    toc.push({ id, text, level })
  })

  // 外链新窗口打开
  doc.querySelectorAll('a').forEach((link) => {
    const href = link.getAttribute('href') || ''
    if (href.startsWith('http://') || href.startsWith('https://')) {
      link.setAttribute('target', '_blank')
      link.setAttribute('rel', 'noopener noreferrer')
    }
  })

  // 表格包裹（横向滚动）
  doc.querySelectorAll('table').forEach((table) => {
    if (!table.closest('.api-param-table-wrap')) {
      const wrapper = doc.createElement('div')
      wrapper.className = 'api-table-wrap'
      table.parentNode?.insertBefore(wrapper, table)
      wrapper.appendChild(table)
    }
  })

  // 代码块美化（语言标签 + 复制按钮 + 行号提示）
  doc.querySelectorAll('pre').forEach((pre) => {
    const codeEl = pre.querySelector('code')
    const lang = codeEl ? Array.from(codeEl.classList).find(c => c.startsWith('language-'))?.replace('language-', '') : ''
    const rawCode = pre.textContent || ''

    const wrapper = doc.createElement('div')
    wrapper.className = 'api-code-wrapper'

    // 计算行数
    const lineCount = rawCode.split('\n').length

    // 创建头部
    const header = doc.createElement('div')
    header.className = 'api-code-header'
    header.innerHTML = `
      <div class="api-code-header-left">
        <span class="api-code-lang">${lang || 'plaintext'}</span>
        <span class="api-code-lines">${lineCount} ${t('api.lines')}</span>
      </div>
      <button class="api-copy-btn" data-copy-content="${escapeAttr(encodeURIComponent(rawCode))}">
        <i class="fa-regular fa-copy mr-1"></i>${t('common.copy')}
      </button>
    `

    pre.parentNode?.insertBefore(wrapper, pre)
    wrapper.appendChild(header)
    wrapper.appendChild(pre)
  })

  return {
    html: doc.body.innerHTML,
    toc
  }
}

const rendered = computed(() => renderMarkdown(props.content))
const renderedHtml = computed(() => rendered.value.html)
const tocItems = computed(() => rendered.value.toc)

// ===== TOC 观察 =====
function disconnectHeadingObserver() {
  if (headingObserver) {
    headingObserver.disconnect()
    headingObserver = null
  }
}

function bindHeadingObserver() {
  disconnectHeadingObserver()
  if (!articleRef.value) return

  const headingEls = articleRef.value.querySelectorAll<HTMLElement>('h1, h2, h3, h4, h5, h6')
  if (headingEls.length === 0) return

  headingObserver = new IntersectionObserver((entries) => {
    const visible = entries
      .filter((entry) => entry.isIntersecting)
      .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
    if (visible.length > 0) {
      activeHeadingId.value = (visible[0].target as HTMLElement).id
    }
  }, {
    root: null,
    rootMargin: '-80px 0px -60% 0px',
    threshold: [0, 1]
  })

  headingEls.forEach((el) => headingObserver?.observe(el))
  if (!activeHeadingId.value && headingEls.length > 0) {
    activeHeadingId.value = headingEls[0].id
  }
}

// ===== 复制 & 点击事件 =====
function handleArticleClick(event: Event) {
  const target = event.target as HTMLElement | null
  if (!target) return

  // 复制按钮
  if (target.classList.contains('api-copy-btn')) {
    const encoded = target.getAttribute('data-copy-content') || ''
    if (!encoded) return
    const text = decodeURIComponent(encoded)

    copyToClipboard(text).then((ok) => {
      if (ok) {
        target.innerHTML = `<i class="fa-solid fa-check mr-1"></i>${t('common.copied')}`
      } else {
        target.innerHTML = `<i class="fa-solid fa-xmark mr-1"></i>${t('common.failed')}`
      }
      window.setTimeout(() => {
        target.innerHTML = `<i class="fa-regular fa-copy mr-1"></i>${t('common.copy')}`
      }, 1500)
    })
    return
  }

  // 复制 URL 按钮
  if (target.classList.contains('api-copy-url-btn') || target.closest('.api-copy-url-btn')) {
    const btn = target.classList.contains('api-copy-url-btn') ? target : target!.closest('.api-copy-url-btn')
    const url = btn?.getAttribute('data-copy-url') || ''
    copyToClipboard(url).then((ok) => {
      const oldIcon = btn?.innerHTML || ''
      if (btn) btn.innerHTML = ok ? '<i class="fa-solid fa-check"></i>' : '<i class="fa-solid fa-xmark"></i>'
      window.setTimeout(() => { if (btn) btn.innerHTML = oldIcon }, 1200)
    })
    return
  }
}

function handleTocClick(id: string) {
  if (!articleRef.value) return
  const heading = articleRef.value.querySelector<HTMLElement>(`#${id}`)
  if (!heading) return
  activeHeadingId.value = id
  heading.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function bindRuntimeBehavior() {
  await nextTick()
  if (!articleRef.value) return
  bindHeadingObserver()
}

onMounted(() => {
  if (articleRef.value) {
    articleRef.value.addEventListener('click', handleArticleClick)
  }
  void bindRuntimeBehavior()
})

watch(renderedHtml, async () => {
  await bindRuntimeBehavior()
})

onBeforeUnmount(() => {
  disconnectHeadingObserver()
  if (articleRef.value) {
    articleRef.value.removeEventListener('click', handleArticleClick)
  }
})

async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    try {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.left = '-9999px'
      textarea.style.top = '-9999px'
      document.body.appendChild(textarea)
      textarea.select()
      textarea.setSelectionRange(0, textarea.value.length)
      const result = document.execCommand('copy')
      document.body.removeChild(textarea)
      return result
    } catch {
      return false
    }
  }
}
</script>

<style scoped lang="scss">
/* ========== 整体布局 ========== */
.api-markdown-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  width: 100%;
  min-width: 0;
  min-height: 100%;
  box-sizing: border-box;
}

/* 当有目录时使用 flex 布局 */
.api-markdown-shell:not(.no-toc) {
  grid-template-columns: minmax(0, 1fr) 240px;
  align-items: stretch;
  width: 100%;
  min-width: 0;
  gap: 28px;
}

.api-markdown-body {
  width: 100%;
  min-width: 0;
  max-width: 100%;
  min-height: 100%;
  box-sizing: border-box;
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px 40px;
  display: block;
  font-size: var(--font-size-base) !important;
  font-family: var(--font-family-base) !important;
  color: #334155;
  line-height: 1.75;
}

/* 有目录时，body 占据剩余空间 */
.api-markdown-shell:not(.no-toc) .api-markdown-body {
  width: 100%;
}

.api-markdown-toc {
  width: 100%;
  position: sticky;
  top: 16px;
  height: fit-content;
  align-self: start;
  border: 1px solid #e8ecf0;
  border-radius: 12px;
  padding: 14px 12px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}

/* 如果没有 TOC，body 仍然撑满 */
.api-markdown-shell.no-toc .api-markdown-body {
  width: 100%;
}

/* ========== 标题层级 ========== */
.api-markdown-body :deep(h1),
.api-markdown-body :deep(h2),
.api-markdown-body :deep(h3),
.api-markdown-body :deep(h4),
.api-markdown-body :deep(h5),
.api-markdown-body :deep(h6) {
  scroll-margin-top: 20px;
  &:first-child { margin-top: 0 !important; }
}

.api-markdown-body :deep(h1) {
  font-size: calc(var(--font-size-base) * 2) !important;
  font-weight: 800;
  color: #0f172a;
  border-bottom: 2.5px solid #e2e8f0;
  padding-bottom: 0.35em;
  margin-top: 1.3em;
  margin-bottom: 18px;
  letter-spacing: -0.02em;
}

.api-markdown-body :deep(h2) {
  font-size: calc(var(--font-size-base) * 1.75) !important;
  font-weight: 700;
  color: #1e293b;
  border-bottom: 1.5px solid #e8ecf0;
  padding-bottom: 0.35em;
  margin-top: 1.25em;
  margin-bottom: 16px;
}

.api-markdown-body :deep(h3) {
  font-size: calc(var(--font-size-base) * 1.5) !important;
  font-weight: 650;
  color: #293548;
  margin-top: 1.15em;
  margin-bottom: 12px;
}

.api-markdown-body :deep(h4) {
  font-size: calc(var(--font-size-base) * 1.25) !important;
  font-weight: 600;
  color: #374151;
  margin-top: 1em;
  margin-bottom: 10px;
}

.api-markdown-body :deep(h5) {
  font-size: calc(var(--font-size-base) * 1.125) !important;
  font-weight: 600;
  color: #475569;
  margin-top: 0.95em;
  margin-bottom: 8px;
}

.api-markdown-body :deep(h6) {
  font-size: var(--font-size-base) !important;
  font-weight: 600;
  color: #64748b;
  margin-top: 0.9em;
  margin-bottom: 8px;
}

.api-markdown-body :deep(.api-markdown-heading) {
  position: relative;
  &:hover .heading-anchor { opacity: 1; }
}

.api-markdown-body :deep(.heading-anchor) {
  position: absolute;
  left: -22px;
  padding-right: 6px;
  color: #3b82f6;
  text-decoration: none;
  opacity: 0;
  transition: all 0.2s;
  font-weight: 400;
  font-size: 0.78em;
  cursor: pointer;
  border: none !important;

  &:hover { color: #2563eb; }
}

/* ========== 段落、列表、引用 ========== */
.api-markdown-body :deep(p) {
  margin: 10px 0 14px;
  line-height: 1.85;
  font-size: var(--font-size-base) !important;
}

.api-markdown-body :deep(ul), .api-markdown-body :deep(ol) {
  margin: 10px 0 18px;
  padding-left: 1.8em;
  line-height: 1.8;
  font-size: var(--font-size-base) !important;
}
.api-markdown-body :deep(li) { 
  margin: 5px 0; 
  font-size: var(--font-size-base) !important;
}
.api-markdown-body :deep(li > p) { 
  margin: 4px 0; 
  font-size: var(--font-size-base) !important;
}

.api-markdown-body :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  background: linear-gradient(135deg, #eff6ff, #f0f7ff);
  color: #475569;
  padding: 14px 18px;
  margin: 18px 0;
  border-radius: 0 10px 10px 0;
  font-size: var(--font-size-base) !important;
}

/* ========== 链接 & 图片 ========== */
.api-markdown-body :deep(a:not(.heading-anchor)) {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.15s;
  &:hover { border-bottom-color: #2563eb; color: #1d4ed8; }
}

.api-markdown-body :deep(img) {
  max-width: 100%; height: auto;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,.07);
  margin: 16px auto;
  display: block;
}

.api-markdown-body :deep(hr) {
  height: 1px; border: none;
  background: linear-gradient(to right, transparent, #cbd5e1, transparent);
  margin: 32px 0;
}

/* ========== 接口端点卡片（顶部核心信息） ========== */
.api-markdown-body :deep(.api-endpoint-card) {
  display: block !important;
  width: 100% !important;
  min-width: 100% !important;
  box-sizing: border-box;
  border: 1px solid #bfdbfe;
  background: linear-gradient(135deg, #f0f7ff, #fafcff);
  border-radius: 14px;
  padding: 18px 20px;
  margin: 14px 0 24px;
  box-shadow: 0 2px 8px rgba(59,130,246,.06);
}

.api-markdown-body :deep(.api-endpoint-card__header) {
  margin-bottom: 10px;
}

.api-markdown-body :deep(.api-endpoint-card__method-row) {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.api-markdown-body :deep(.api-method-badge) {
  display: inline-flex;
  align-items: center;
  border-radius: 7px;
  font-size: var(--font-size-sm) !important;
  font-weight: 750;
  padding: 5px 12px;
  color: #fff;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  min-width: 58px;
  justify-content: center;
  &.is-get { background: #16a34a; box-shadow: 0 2px 6px rgba(22,163,74,.25); }
  &.is-post { background: #2563eb; box-shadow: 0 2px 6px rgba(37,99,235,.25); }
  &.is-put { background: #d97706; box-shadow: 0 2px 6px rgba(217,119,6,.25); }
  &.is-delete { background: #dc2626; box-shadow: 0 2px 6px rgba(220,38,38,.25); }
  &.is-patch { background: #7c3aed; box-shadow: 0 2px 6px rgba(124,58,237,.25); }
}

.api-markdown-body :deep(.api-url-path) {
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  font-size: var(--font-size-sm) !important;
  padding: 5px 12px;
  color: #1e293b;
  font-weight: 500;
  flex: 1;
  word-break: break-all;
}

.api-markdown-body :deep(.api-copy-url-btn) {
  background: none; border: 1px solid #d1d5db;
  border-radius: 7px; color: #64748b;
  padding: 5px 8px; cursor: pointer;
  transition: all 0.2s;
  font-size: var(--font-size-sm) !important;
  &:hover { background: #f1f5f9; color: #2563eb; border-color: #93c5fd; }
}

.api-markdown-body :deep(.api-endpoint-card__title) {
  font-weight: 700; color: #0f172a; font-size: var(--font-size-lg) !important; margin-bottom: 4px;
}

.api-markdown-body :deep(.api-endpoint-card__desc) {
  color: #475569; font-size: var(--font-size-base) !important; margin-bottom: 10px; line-height: 1.65;
}

.api-markdown-body :deep(.api-endpoint-meta) {
  display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px;
}

.api-markdown-body :deep(.meta-tag) {
  display: inline-flex; align-items: center;
  font-size: var(--font-size-sm) !important; color: #64748b;
  background: #fff; border: 1px solid #e2e8f0;
  border-radius: 6px; padding: 3px 9px;
}

/* ========== 信息卡片（请求/响应/错误/注意） ========== */
.api-markdown-body :deep(.api-info-card) {
  display: block !important;
  width: 100% !important;
  min-width: 100% !important;
  box-sizing: border-box;
  border-radius: 12px; padding: 14px 18px; margin: 16px 0;
  border: 1px solid #e2e8f0; transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 8px rgba(0,0,0,.04); }
}

.api-markdown-body :deep(.api-info-card.is-request) {
  border-color: #93c5fd; background: linear-gradient(135deg, #eff6ff, #faffff);
}
.api-markdown-body :deep(.api-info-card.is-response) {
  border-color: #86efac; background: linear-gradient(135deg, #f0fdf4, #fafffe);
}
.api-markdown-body :deep(.api-info-card.is-error) {
  border-color: #fca5a5; background: linear-gradient(135deg, #fef2f2, #fffafa);
}
.api-markdown-body :deep(.api-info-card.is-note) {
  border-color: #93c5fd; background: linear-gradient(135deg, #eff6ff, #f0f7ff);
}
.api-markdown-body :deep(.api-info-card.is-warning) {
  border-color: #fcd34d; background: linear-gradient(135deg, #fefce8, #fffef5);
}

.api-markdown-body :deep(.api-info-card__header) {
  display: flex; align-items: center; gap: 7px; margin-bottom: 10px;
}

.api-markdown-body :deep(.api-info-icon) {
  font-size: var(--font-size-base) !important; width: 20px; text-align: center;
  &.is-request { color: #2563eb; }
  &.is-response { color: #16a34a; }
  &.is-error { color: #dc2626; }
  &.is-note { color: #2563eb; }
  &.is-warning { color: #d97706; }
}

.api-markdown-body :deep(.api-info-title) {
  font-size: var(--font-size-base) !important; font-weight: 700; color: #0f172a;
}

.api-markdown-body :deep(.api-info-card__row) {
  display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap;
}

.api-markdown-body :deep(.api-method-badge) {
  display: inline-flex; align-items: center; border-radius: 5px;
  font-size: var(--font-size-sm) !important; font-weight: 700; padding: 2px 7px; color: #fff;
  &.is-get { background: #16a34a; }
  &.is-post { background: #2563eb; }
  &.is-put { background: #d97706; }
  &.is-delete { background: #dc2626; }
}

.api-markdown-body :deep(.api-info-path) {
  background: #fff; border-radius: 5px; font-size: var(--font-size-sm) !important; padding: 2px 8px;
  color: #334155;
}

.api-markdown-body :deep(.api-info-meta) {
  font-size: var(--font-size-sm) !important; color: #475569; margin-top: 6px;
  span { color: #94a3b8; }
  code { background: #fff; padding: 1px 6px; border-radius: 4px; font-size: var(--font-size-sm) !important; color: #1e293b; }
}

.api-markdown-body :deep(.api-info-card__body) {
  font-size: var(--font-size-base) !important; color: #475569; line-height: 1.7;
}

/* ========== 参数表格 ========== */
.api-markdown-body :deep(.api-param-table-wrap) {
  width: 100%; overflow-x: auto; margin: 18px 0;
}

.api-markdown-body :deep(.api-param-table) {
  width: 100%; min-width: 100%; border-collapse: collapse;
  font-size: var(--font-size-base) !important;
}

.api-markdown-body :deep(.api-param-table thead th) {
  background: #f8fafc;
  font-weight: 700; color: #374151;
  text-align: left; padding: 11px 14px;
  border-bottom: 2px solid #e2e8f0; white-space: nowrap;
  position: sticky; top: 0; z-index: 1;
  font-size: var(--font-size-sm) !important; text-transform: uppercase; letter-spacing: 0.03em;
}

.api-markdown-body :deep(.api-param-table tbody td) {
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  color: #374151;
}

.api-markdown-body :deep(.api-param-table tbody tr:hover) {
  background: #fafbfc;
}

.api-markdown-body :deep(.api-param-table tbody tr:last-child td) {
  border-bottom: none;
}

.api-markdown-body :deep(.param-name-cell) {
  white-space: nowrap;
  code { background: #f1f5f9; padding: 2px 8px; border-radius: 5px; font-size: var(--font-size-sm) !important; color: #1e40af; font-weight: 500; }
}

.api-markdown-body :deep(.param-desc-cell) {
  max-width: 300px;
  line-height: 1.55;
}

.api-markdown-body :deep(.api-param-badge) {
  display: inline-flex; align-items: center; border-radius: 999px;
  font-size: var(--font-size-sm) !important; padding: 2px 9px; font-weight: 600;
  &.is-required { background: #fee2e2; color: #dc2626; }
  &.is-optional { background: #f1f5f9; color: #64748b; }
}

.api-markdown-body :deep(.example-val) {
  background: #f8fafc; padding: 1px 6px; border-radius: 4px;
  font-size: var(--font-size-sm) !important; color: #059669;
}

/* ========== 标准表格 ========== */
.api-markdown-body :deep(.api-table-wrap) {
  display: block !important;
  width: 100% !important;
  min-width: 100% !important;
  overflow-x: auto;
  margin: 16px 0;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.api-markdown-body :deep(table) {
  width: 100% !important;
  min-width: 100% !important;
  border-collapse: collapse;
  table-layout: auto; /* 改为 auto，让每列根据内容自适应宽度 */
  margin: 0; /* 移除外边距，让表格完全填充容器 */
}

.api-markdown-body :deep(table:not(.api-param-table)) {
  width: 100%;
  border-collapse: collapse;
  margin: 0; /* 移除外边距 */
  font-size: var(--font-size-base) !important;
  th {
    background: #f8fafc;
    font-weight: 700;
    color: #0f172a;
    text-align: left;
    padding: 11px 14px;
    border-bottom: 2px solid #e2e8f0;
    font-size: var(--font-size-sm) !important;
    white-space: nowrap; /* 表头不换行，保持紧凑 */
  }
  td {
    padding: 10px 14px;
    border-bottom: 1px solid #f1f5f9;
    color: #374151;
    word-wrap: break-word; /* 内容过长时自动换行 */
  }
  tbody tr:hover { background: #fafbfc; }
  tbody tr:last-child td { border-bottom: none; } /* 最后一行去掉底部边框 */
}

/* ========== 代码块 ========== */
.api-markdown-body :deep(.api-code-wrapper) {
  display: block !important; width: 100% !important;
  min-width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box; margin: 22px 0;
  background: #0d1117; border-radius: 12px;
  overflow: hidden; border: 1px solid #30363d;
  box-shadow: 0 4px 16px rgba(0,0,0,.12);
}

.api-markdown-body :deep(.api-code-header) {
  display: flex; justify-content: space-between; align-items: center;
  padding: 9px 16px; background: #161b22; border-bottom: 1px solid #30363d;
}

.api-markdown-body :deep(.api-code-header-left) {
  display: flex; align-items: center; gap: 12px;
}

.api-markdown-body :deep(.api-code-lang) {
  font-size: var(--font-size-sm) !important; color: #8b949e; text-transform: uppercase;
  font-weight: 700; letter-spacing: 0.8px; font-family: 'Inter', sans-serif;
}

.api-markdown-body :deep(.api-code-lines) {
  font-size: var(--font-size-sm) !important; color: #636c76; font-family: monospace;
}

.api-markdown-body :deep(.api-copy-btn) {
  display: flex; align-items: center; gap: 5px;
  background: transparent; border: 1px solid #30363d; border-radius: 6px;
  color: #c9d1d9; font-size: var(--font-size-sm) !important; padding: 4px 12px;
  cursor: pointer; transition: all 0.2s; font-family: inherit;
  &:hover { background: #21262d; border-color: #8b949e; color: #f0f6fc; }
  &:active { background: #30363d; transform: scale(0.97); }
}

.api-markdown-body :deep(pre) {
  width: 100%; margin: 0 !important; padding: 18px !important;
  background: transparent !important; overflow-x: auto;
  box-sizing: border-box; display: block;

  &::-webkit-scrollbar { height: 8px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: #30363d; border-radius: 10px; &:hover { background: #484f58; } }
}

.api-markdown-body :deep(pre code) {
  display: block; white-space: pre;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, 'Courier New', monospace;
  font-size: var(--font-size-sm) !important; line-height: 1.65; color: #c9d1d9;
}

.api-markdown-body :deep(code:not(pre code)) {
  background: #eef2ff; border-radius: 5px;
  padding: 2px 7px; font-size: var(--font-size-sm) !important; color: #1e40af;
  font-family: 'Fira Code', monospace;
}

/* ========== 注意事项 / 错误码 / 关联接口块 ========== */
.api-markdown-body :deep(.api-note-block) {
  border-radius: 10px; padding: 14px 18px; margin: 18px 0;
  border: 1px solid #e8ecf0;
  &.is-notice { border-color: #93c5fd; background: linear-gradient(135deg, #eff6ff, #fafffe); }
  &.is-error-codes { border-color: #fca5a5; background: linear-gradient(135deg, #fef2f2, #fffaf8); }
  &.is-related { border-color: #a5b4fc; background: linear-gradient(135deg, #eef2ff, #f8faff); }
}

.api-markdown-body :deep(.api-note-block__header) {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 10px; font-weight: 700; font-size: var(--font-size-base) !important; color: #1e293b;
  i { font-size: var(--font-size-lg) !important; }
  &.is-notice i { color: #2563eb; }
  &.is-error-codes i { color: #dc2626; }
  &.is-related i { color: #6366f1; }
}

.api-markdown-body :deep(.api-note-list) {
  margin: 0; padding-left: 0; list-style: none;
  li {
    padding: 6px 0 6px 18px; position: relative;
    border-bottom: 1px dashed #e8ecf0;
    &:last-child { border-bottom: none; }
    &::before { content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 6px; height: 6px; border-radius: 50%; background: currentColor; opacity: 0.35; }
  }
}

.api-markdown-body :deep(.error-code-item) {
  display: flex; align-items: baseline; gap: 10px;
  .err-code {
    background: #fef2f2; color: #dc2626; padding: 2px 8px; border-radius: 5px;
    font-size: var(--font-size-sm) !important; font-weight: 600; white-space: nowrap; font-family: monospace;
  }
  .err-msg { color: #475569; }
}

.api-markdown-body :deep(.related-link) {
  color: #6366f1; font-weight: 500;
  &:hover { color: #4f46e5; text-decoration: underline; }
}

.api-markdown-body :deep(.api-note-text) {
  color: #475569; line-height: 1.7; margin: 0; font-size: var(--font-size-base) !important;
}

/* ========== 右侧目录导航 TOC ========== */
.api-markdown-toc__title {
  font-size: var(--font-size-sm) !important; font-weight: 700; color: #94a3b8;
  margin-bottom: 10px; padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
  text-transform: uppercase; letter-spacing: 0.06em;
}

.api-markdown-toc__nav {
  max-height: calc(100vh - 160px); overflow-y: auto;
  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 3px; }
}

.api-markdown-toc__item {
  display: flex; align-items: flex-start; gap: 6px;
  font-size: var(--font-size-sm) !important; color: #4b5563; text-decoration: none;
  margin: 5px 0; line-height: 1.55;
  border-left: 2px solid transparent;
  padding: 3px 8px 3px 10px; border-radius: 0 5px 5px 0;
  transition: all 0.2s;
  &:hover { color: #2563eb; background: #f8faff; border-left-color: #93c5fd; }
}

.toc-dot {
  display: inline-block; width: 5px; height: 5px;
  border-radius: 50%; background: #2563eb; flex-shrink: 0;
  margin-top: 7px;
}

.api-markdown-toc__item.is-active {
  color: #1d4ed8; font-weight: 600;
  border-left-color: #2563eb;
  background: linear-gradient(to right, #eff6ff, transparent);
}

.api-markdown-toc__item.is-level-2 { padding-left: 14px; }
.api-markdown-toc__item.is-level-3 { padding-left: 22px; font-size: calc(var(--font-size-sm) * 0.95) !important; }
.api-markdown-toc__item.is-level-4 { padding-left: 30px; font-size: calc(var(--font-size-sm) * 0.9) !important; color: #6b7280; }

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .api-markdown-shell:not(.no-toc) {
    flex-direction: column;
  }
  .api-markdown-toc {
    display: none;
  }
  .api-markdown-body {
    padding: 20px 24px 28px;
  }
}
</style>
