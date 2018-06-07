package com.menglin.common;

import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.menglin.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.menglin.common.AssertArguments.checkNotEmpty;

public class OfficeToPDFConverter {
    private static Logger logger = LoggerFactory.getLogger(OfficeToPDFConverter.class);


    /**
     * DOC 转换成 PDF
     *
     * @param docFilePath "F:\\Server\\OfficeToPDF\\uploadFile\\12345678_learn_links.md"
     * @param realPdfPath 绝对路径"webapp/pdfs/"
     * @return pdfPath 生成的临时预览的 pdf 文件路径
     */
    public static void docToPDF(String docFilePath, String realPdfPath) throws IOException {
        checkNotEmpty(docFilePath, "要转换的office文档的路径不能为空");
        checkNotEmpty(realPdfPath, "要存储转换后PDF文档的绝对路径不能为空");

        validateWordsLicense();
        String pdfPath = getPdfPath(docFilePath, realPdfPath);
        FileOutputStream fileOutputStream = null;
        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                fileOutputStream = new FileOutputStream(pdfFile);
                Document document = new Document(docFilePath);

                // 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
                document.save(fileOutputStream, com.aspose.words.SaveFormat.PDF);
                logger.info("docToPDF success, pdfPath:{}", pdfPath);
            }
        } catch (Exception e) {
            logger.info("docToPDF fail, e:{}", e);
            throw new ServiceException("文件格式转换失败");
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public static void excelToPDF(String excelFilePath, String realPdfPath) throws IOException {
        checkNotEmpty(excelFilePath, "要转换的office文档的路径不能为空");
        checkNotEmpty(realPdfPath, "要存储转换后PDF文档的绝对路径不能为空");

        validateCellsLicense();
        String pdfPath = getPdfPath(excelFilePath, realPdfPath);
        FileOutputStream fileOutputStream = null;
        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                fileOutputStream = new FileOutputStream(pdfFile);
                Workbook workbook = new Workbook(excelFilePath);
                workbook.save(fileOutputStream, com.aspose.cells.SaveFormat.PDF);
                logger.info("excelToPDF success, pdfPath:{}", pdfPath);
            }
        } catch (Exception e) {
            logger.info("excelToPDF fail, e:{}", e);
            throw new ServiceException("文件格式转换失败");
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public static void pptToPDF(String pptFilePath, String realPdfPath) throws IOException {
        checkNotEmpty(pptFilePath, "要转换的office文档的路径不能为空");
        checkNotEmpty(realPdfPath, "要存储转换后PDF文档的绝对路径不能为空");

        validateSlidesLicense();
        String pdfPath = getPdfPath(pptFilePath, realPdfPath);
        FileOutputStream fileOutputStream = null;
        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                fileOutputStream = new FileOutputStream(pdfFile);
                Presentation presentation = new Presentation(pptFilePath);
                presentation.save(fileOutputStream, com.aspose.slides.SaveFormat.Pdf);
                logger.info("pptToPDF success, pdfPath:{}", pdfPath);
            }
        } catch (Exception e) {
            logger.info("pptToPDF fail, e:{}", e);
            throw new ServiceException("文件格式转换失败");
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private static String getPdfPath(String docFilePath, String realPdfPath) {
        return realPdfPath + "\\" + getFileNameFromOriginFilePath(docFilePath) + ".pdf";
    }

    private static String getFileNameFromOriginFilePath(String originFilePath) {
        return originFilePath.substring(originFilePath.lastIndexOf("/") + 1, originFilePath.lastIndexOf("."));
    }


    /**
     * 校验 word to pdf license
     */
    private static void validateWordsLicense() {
        try {
            // license.xml 应放 resource 路径下
            InputStream licenseInputStream = OfficeToPDFConverter.class.getClassLoader().getResourceAsStream("license.xml");
            com.aspose.words.License license = new com.aspose.words.License();
            license.setLicense(licenseInputStream);
        } catch (Exception e) {
            logger.info("validateWordsLicense fail, e:{}", e);
            throw new ServiceException("校验 License 失败");
        }
    }


    /**
     * 校验 excel to pdf license
     */
    private static void validateCellsLicense() {
        try {
            // license.xml 应放 resource 路径下
            InputStream licenseInputStream = OfficeToPDFConverter.class.getClassLoader().getResourceAsStream("license.xml");
            com.aspose.cells.License license = new com.aspose.cells.License();
            license.setLicense(licenseInputStream);
        } catch (Exception e) {
            logger.info("validatecellsLicense fail, e:{}", e);
            throw new ServiceException("校验 License 失败");
        }
    }


    /**
     * 校验 word to pdf license
     */
    private static void validateSlidesLicense() {
        try {
            // license.xml 应放 resource 路径下
            InputStream licenseInputStream = OfficeToPDFConverter.class.getClassLoader().getResourceAsStream("license.xml");
            com.aspose.slides.License license = new com.aspose.slides.License();
            license.setLicense(licenseInputStream);
        } catch (Exception e) {
            logger.info("validateSlidesLicense fail, e:{}", e);
            throw new ServiceException("校验 License 失败");
        }
    }
}
