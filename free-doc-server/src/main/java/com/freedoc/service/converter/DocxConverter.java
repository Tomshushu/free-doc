package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * DOCX格式转换器
 * 使用Apache POI和Flexmark将Markdown转换为Word文档
 * 支持标题层级、列表格式、表格和文本样式
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocxConverter implements FormatConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ExportConfig exportConfig;
    
    // Flexmark配置
    private static final MutableDataSet OPTIONS = new MutableDataSet();
    
    static {
        // 配置Flexmark扩展
        OPTIONS.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create()
        ));
    }
    
    // 懒加载的Parser
    private volatile Parser parser;
    
    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.DOCX;
    }
    
    @Override
    public byte[] convert(Doc doc, ConversionOptions options) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        
        log.debug("开始转换文档 {} 为DOCX格式", doc.getDocId());
        
        try {
            // 创建Word文档
            XWPFDocument document = new XWPFDocument();
            
            // 添加文档标题
            if (doc.getDocTitle() != null && !doc.getDocTitle().trim().isEmpty()) {
                addDocumentTitle(document, doc.getDocTitle());
            }
            
            // 添加文档元数据（如果启用）
            if (options.isIncludeMetadata()) {
                addMetadataSection(document, doc);
            }
            
            // 解析并添加Markdown内容
            String markdownContent = doc.getDocContent();
            if (markdownContent != null && !markdownContent.trim().isEmpty()) {
                Parser markdownParser = getParser();
                Node documentNode = markdownParser.parse(markdownContent);
                
                // 遍历AST节点并转换为Word元素
                processNode(document, documentNode);
            } else {
                // 如果内容为空，添加提示信息
                XWPFParagraph emptyPara = document.createParagraph();
                XWPFRun emptyRun = emptyPara.createRun();
                emptyRun.setText("此文档暂无内容");
                emptyRun.setItalic(true);
                emptyRun.setColor("808080");
            }
            
            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();
            
            byte[] result = outputStream.toByteArray();
            log.debug("DOCX转换完成，文档 {} 大小: {} 字节", doc.getDocId(), result.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("DOCX转换失败，文档ID: {}", doc.getDocId(), e);
            throw new RuntimeException("DOCX conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 添加文档标题
     */
    private void addDocumentTitle(XWPFDocument document, String title) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("Arial");
        
        // 添加标题后的空行
        document.createParagraph();
    }
    
    /**
     * 添加文档元数据部分
     */
    private void addMetadataSection(XWPFDocument document, Doc doc) {
        // 添加分隔线
        XWPFParagraph separatorPara = document.createParagraph();
        separatorPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun separatorRun = separatorPara.createRun();
        separatorRun.setText("─".repeat(50));
        separatorRun.setFontSize(10);
        
        // 文档信息标题
        XWPFParagraph metadataTitlePara = document.createParagraph();
        XWPFRun metadataTitleRun = metadataTitlePara.createRun();
        metadataTitleRun.setText("文档信息");
        metadataTitleRun.setBold(true);
        metadataTitleRun.setFontSize(14);
        
        // 文档信息内容
        if (doc.getDocTitle() != null) {
            addMetadataItem(document, "标题", doc.getDocTitle());
        }
        
        addMetadataItem(document, "文档ID", doc.getDocId());
        
        if (doc.getCreateUser() != null) {
            addMetadataItem(document, "创建者", doc.getCreateUser());
        }
        
        if (doc.getCreateTime() != null) {
            addMetadataItem(document, "创建时间", doc.getCreateTime().format(DATE_FORMATTER));
        }
        
        if (doc.getUpdateTime() != null) {
            addMetadataItem(document, "更新时间", doc.getUpdateTime().format(DATE_FORMATTER));
        }
        
        // 添加分隔线
        XWPFParagraph separatorPara2 = document.createParagraph();
        separatorPara2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun separatorRun2 = separatorPara2.createRun();
        separatorRun2.setText("─".repeat(50));
        separatorRun2.setFontSize(10);
        
        // 添加空行
        document.createParagraph();
    }
    
    /**
     * 添加元数据项
     */
    private void addMetadataItem(XWPFDocument document, String label, String value) {
        XWPFParagraph para = document.createParagraph();
        
        XWPFRun labelRun = para.createRun();
        labelRun.setText(label + ": ");
        labelRun.setBold(true);
        labelRun.setFontSize(10);
        
        XWPFRun valueRun = para.createRun();
        valueRun.setText(value);
        valueRun.setFontSize(10);
    }
    
    /**
     * 处理Markdown AST节点
     */
    private void processNode(XWPFDocument document, Node node) {
        for (Node child : node.getChildren()) {
            processNodeRecursive(document, child);
        }
    }
    
    /**
     * 递归处理节点
     */
    private void processNodeRecursive(XWPFDocument document, Node node) {
        if (node instanceof Heading) {
            processHeading(document, (Heading) node);
        } else if (node instanceof com.vladsch.flexmark.ast.Paragraph) {
            processParagraph(document, (com.vladsch.flexmark.ast.Paragraph) node);
        } else if (node instanceof FencedCodeBlock) {
            processCodeBlock(document, (FencedCodeBlock) node);
        } else if (node instanceof BlockQuote) {
            processBlockQuote(document, (BlockQuote) node);
        } else if (node instanceof BulletList) {
            processBulletList(document, (BulletList) node);
        } else if (node instanceof OrderedList) {
            processOrderedList(document, (OrderedList) node);
        } else if (node instanceof com.vladsch.flexmark.ext.tables.TableBlock) {
            processTable(document, (com.vladsch.flexmark.ext.tables.TableBlock) node);
        } else {
            // 递归处理子节点
            for (Node child : node.getChildren()) {
                processNodeRecursive(document, child);
            }
        }
    }
    
    /**
     * 处理标题
     */
    private void processHeading(XWPFDocument document, Heading heading) {
        int level = heading.getLevel();
        String text = getTextContent(heading);
        
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
        
        // 根据标题级别设置字体大小
        int fontSize = switch (level) {
            case 1 -> 16;
            case 2 -> 14;
            case 3 -> 12;
            case 4 -> 11;
            default -> 10;
        };
        run.setFontSize(fontSize);
        
        // 设置标题样式
        para.setStyle("Heading" + Math.min(level, 6));
        
        // 添加标题后的空行（除了最小标题）
        if (level <= 3) {
            document.createParagraph();
        }
    }
    
    /**
     * 处理段落
     */
    private void processParagraph(XWPFDocument document, com.vladsch.flexmark.ast.Paragraph para) {
        XWPFParagraph wordPara = document.createParagraph();
        
        // 处理段落中的内联元素
        processInlineElements(wordPara, para);
    }
    
    /**
     * 处理内联元素
     */
    private void processInlineElements(XWPFParagraph paragraph, Node node) {
        for (Node child : node.getChildren()) {
            if (child instanceof com.vladsch.flexmark.ast.Text) {
                XWPFRun run = paragraph.createRun();
                run.setText(child.getChars().toString());
            } else if (child instanceof StrongEmphasis) {
                XWPFRun run = paragraph.createRun();
                run.setText(getTextContent(child));
                run.setBold(true);
            } else if (child instanceof Emphasis) {
                XWPFRun run = paragraph.createRun();
                run.setText(getTextContent(child));
                run.setItalic(true);
            } else if (child instanceof Code) {
                XWPFRun run = paragraph.createRun();
                run.setText(child.getChars().toString());
                run.setFontFamily("Courier New");
                // 设置代码的背景色（通过高亮）
                run.getCTR().addNewRPr().addNewHighlight().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor.LIGHT_GRAY);
            } else if (child instanceof Link) {
                processLink(paragraph, (Link) child);
            } else {
                // 递归处理其他内联元素
                processInlineElements(paragraph, child);
            }
        }
    }
    
    /**
     * 处理链接
     */
    private void processLink(XWPFParagraph paragraph, Link link) {
        String url = link.getUrl().toString();
        String text = getTextContent(link);
        
        // 创建超链接
        String id = paragraph.getDocument().getPackagePart().addExternalRelationship(
            url, XWPFRelation.HYPERLINK.getRelation()).getId();
        
        XWPFHyperlinkRun hyperlinkRun = paragraph.createHyperlinkRun(id);
        hyperlinkRun.setText(text);
        hyperlinkRun.setColor("0000FF");
        hyperlinkRun.setUnderline(UnderlinePatterns.SINGLE);
    }
    
    /**
     * 处理代码块
     */
    private void processCodeBlock(XWPFDocument document, FencedCodeBlock codeBlock) {
        // 获取代码内容（不包括围栏标记）
        String code = codeBlock.getContentChars().toString();
        
        if (code == null || code.trim().isEmpty()) {
            return;
        }
        
        // 按行处理代码，保留格式
        String[] lines = code.split("\n", -1);  // -1 保留尾部空行
        
        for (String line : lines) {
            XWPFParagraph para = document.createParagraph();
            para.setStyle("Code");
            
            // 设置段落背景色
            para.getCTP().addNewPPr().addNewShd().setFill("F5F5F5");
            
            XWPFRun run = para.createRun();
            
            // 如果是空行，添加一个空格以保持行高
            if (line.isEmpty()) {
                run.setText(" ");
            } else {
                run.setText(line);
            }
            
            run.setFontFamily("Courier New");
            run.setFontSize(9);
        }
        
        // 添加代码块后的空行
        document.createParagraph();
    }
    
    /**
     * 处理引用块
     */
    private void processBlockQuote(XWPFDocument document, BlockQuote blockQuote) {
        XWPFParagraph para = document.createParagraph();
        para.setIndentationLeft(720); // 0.5英寸缩进
        
        // 设置左边框
        para.setBorderLeft(Borders.SINGLE);
        
        // 处理引用块中的内容
        for (Node child : blockQuote.getChildren()) {
            if (child instanceof com.vladsch.flexmark.ast.Paragraph) {
                processInlineElements(para, child);
            }
        }
        
        // 设置斜体样式
        for (XWPFRun run : para.getRuns()) {
            run.setItalic(true);
        }
    }
    
    /**
     * 处理无序列表
     */
    private void processBulletList(XWPFDocument document, BulletList list) {
        processList(document, list, "•");
    }
    
    /**
     * 处理有序列表
     */
    private void processOrderedList(XWPFDocument document, OrderedList list) {
        processList(document, list, null); // null表示使用数字编号
    }
    
    /**
     * 处理列表（通用方法）
     */
    private void processList(XWPFDocument document, ListBlock list, String bulletSymbol) {
        int itemNumber = 1;
        
        for (Node item : list.getChildren()) {
            if (item instanceof ListItem) {
                XWPFParagraph para = document.createParagraph();
                para.setIndentationLeft(360); // 0.25英寸缩进
                
                XWPFRun run = para.createRun();
                
                // 设置列表符号或编号
                String prefix;
                if (bulletSymbol != null) {
                    prefix = bulletSymbol + " ";
                } else {
                    prefix = itemNumber + ". ";
                    itemNumber++;
                }
                
                run.setText(prefix + getTextContent(item));
            }
        }
        
        // 添加列表后的空行
        document.createParagraph();
    }
    
    /**
     * 处理表格
     */
    private void processTable(XWPFDocument document, com.vladsch.flexmark.ext.tables.TableBlock table) {
        if (!table.hasChildren()) {
            return;
        }
        
        // 计算列数
        int columnCount = 0;
        for (Node child : table.getChildren()) {
            if (child instanceof com.vladsch.flexmark.ext.tables.TableHead) {
                com.vladsch.flexmark.ext.tables.TableHead head = (com.vladsch.flexmark.ext.tables.TableHead) child;
                for (Node row : head.getChildren()) {
                    if (row instanceof com.vladsch.flexmark.ext.tables.TableRow) {
                        int rowColumnCount = 0;
                        for (Node cell : row.getChildren()) {
                            if (cell instanceof com.vladsch.flexmark.ext.tables.TableCell) {
                                rowColumnCount++;
                            }
                        }
                        columnCount = Math.max(columnCount, rowColumnCount);
                    }
                }
            }
        }
        
        if (columnCount == 0) {
            return;
        }
        
        // 创建表格
        XWPFTable wordTable = document.createTable();
        
        // 设置表格样式
        CTTblPr tblPr = wordTable.getCTTbl().getTblPr();
        if (tblPr == null) {
            tblPr = wordTable.getCTTbl().addNewTblPr();
        }
        
        CTTblWidth tblWidth = tblPr.getTblW();
        if (tblWidth == null) {
            tblWidth = tblPr.addNewTblW();
        }
        tblWidth.setW(BigInteger.valueOf(5000));
        tblWidth.setType(STTblWidth.PCT);
        
        boolean isFirstRow = true;
        int rowIndex = 0;
        
        // 处理表格内容
        for (Node child : table.getChildren()) {
            if (child instanceof com.vladsch.flexmark.ext.tables.TableHead ||
                child instanceof com.vladsch.flexmark.ext.tables.TableBody) {
                
                for (Node row : child.getChildren()) {
                    if (row instanceof com.vladsch.flexmark.ext.tables.TableRow) {
                        XWPFTableRow wordRow;
                        
                        if (rowIndex == 0) {
                            wordRow = wordTable.getRow(0);
                        } else {
                            wordRow = wordTable.createRow();
                        }
                        
                        int cellIndex = 0;
                        for (Node cell : row.getChildren()) {
                            if (cell instanceof com.vladsch.flexmark.ext.tables.TableCell) {
                                XWPFTableCell wordCell;
                                
                                if (cellIndex < wordRow.getTableCells().size()) {
                                    wordCell = wordRow.getCell(cellIndex);
                                } else {
                                    wordCell = wordRow.addNewTableCell();
                                }
                                
                                // 清除默认内容
                                wordCell.removeParagraph(0);
                                
                                XWPFParagraph cellPara = wordCell.addParagraph();
                                XWPFRun cellRun = cellPara.createRun();
                                cellRun.setText(getTextContent(cell));
                                
                                // 如果是表头，设置粗体
                                if (isFirstRow && child instanceof com.vladsch.flexmark.ext.tables.TableHead) {
                                    cellRun.setBold(true);
                                }
                                
                                cellIndex++;
                            }
                        }
                        
                        rowIndex++;
                    }
                }
                
                if (child instanceof com.vladsch.flexmark.ext.tables.TableHead) {
                    isFirstRow = false;
                }
            }
        }
        
        // 添加表格后的空行
        document.createParagraph();
    }
    
    /**
     * 获取节点的文本内容
     */
    private String getTextContent(Node node) {
        StringBuilder text = new StringBuilder();
        extractText(node, text);
        return text.toString().trim();
    }
    
    /**
     * 递归提取文本
     */
    private void extractText(Node node, StringBuilder text) {
        if (node instanceof com.vladsch.flexmark.ast.Text) {
            text.append(node.getChars().toString());
        } else {
            for (Node child : node.getChildren()) {
                extractText(child, text);
            }
        }
    }
    
    /**
     * 获取Markdown解析器（懒加载）
     */
    private Parser getParser() {
        if (parser == null) {
            synchronized (this) {
                if (parser == null) {
                    parser = Parser.builder(OPTIONS).build();
                }
            }
        }
        return parser;
    }
}