package com.menglin.util;

import com.menglin.common.CommonConst;
import com.menglin.common.OfficeToPDFConverter;
import com.menglin.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.menglin.common.AssertArguments.checkNotEmpty;
import static com.menglin.common.AssertArguments.checkNotNull;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String saveFile(MultipartFile file, String username) {
        checkNotNull(file, "保存的文件不能为空");
        checkNotEmpty(username, "用户名不能为空");

        String fileSuffix = getFileSuffix(file.getOriginalFilename());
        if (!CommonConst.SUPPORT_FILE_TYPE.contains(fileSuffix)) {
            throw new ServiceException("不支持上传的文件类型");
        }
        if (!file.isEmpty()) {
            String saveFilePath = createSaveFilePath(file.getOriginalFilename(), username);
            File localFile = new File(saveFilePath);
            try {
                file.transferTo(localFile);
            } catch (IllegalStateException | IOException e) {
                logger.info("saveFile fail. e:{}", e);
                throw new ServiceException("保存文件失败");
            }
            return saveFilePath;
        } else {
            logger.info("saveFile fail, file is empty. file:{}", file);
            throw new ServiceException("保存的文件不能为空");
        }
    }

    public static List<String> saveFileList(List<MultipartFile> multipartFileList, String username) {
        checkNotNull(multipartFileList, "保存的文件列表不能为空");
        checkNotEmpty(username, "用户名不能为空");

        List<String> saveFileUrlList = new ArrayList<>();
        if (!multipartFileList.isEmpty()) {
            for (MultipartFile multipartFile : multipartFileList) {
                saveFileUrlList.add(saveFile(multipartFile, username));
            }
            return saveFileUrlList;
        } else {
            logger.info("saveFileList fail, multipartFileList is empty. multipartFileList:{}", multipartFileList.toString());
            throw new ServiceException("保存的文件列表不能为空");
        }
    }

    public static void deleteFile(String deleteFilePath) {
        checkNotEmpty(deleteFilePath, "删除的文件的路径不能为空");

        File deleteFile = new File(deleteFilePath);
        if (deleteFile.exists()) {
            logger.info("deleteFile success, deleteFilePath:{}", deleteFilePath);
            deleteFile.delete();
        } else {
            throw new ServiceException("删除的文件不存在");
        }
    }

    public static void deleteFileList(List<String> deleteFilePathList) {
        checkNotNull(deleteFilePathList, "删除的文件列表不能为空");

        if (!deleteFilePathList.isEmpty()) {
            for (String deleteFilePath : deleteFilePathList) {
                deleteFile(deleteFilePath);
            }
        } else {
            logger.info("deleteFileList fail, deleteFilePathList is empty. deleteFilePathList:{}", deleteFilePathList.toString());
            throw new ServiceException("删除的文件列表不能为空");
        }
    }

    public static ResponseEntity<byte[]> downLoadFile(String fileName, String username) {
        checkNotEmpty(fileName, "下载的文件名不能为空");
        checkNotEmpty(username, "用户名不能为空");

        String downLoadFilePath = createDownloadFilePath(fileName, username);
        File downloadFile = new File(downLoadFilePath);

        if (downloadFile.exists()) {
            //创建springframework的HttpHeaders对象
            HttpHeaders headers = new HttpHeaders();
            String downloadFilename;
            try {
                //下载显示的文件名
                downloadFilename = new String(fileName.getBytes(), "iso-8859-1");
            } catch (UnsupportedEncodingException e) {
                logger.info("encoding downloadFilename  fail, fileName:{}", fileName);
                throw new ServiceException("文件名编码出错");
            }
            //通知浏览器下载文件
            headers.setContentDispositionFormData("attachment", downloadFilename);
            //二进制流数据
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            ResponseEntity<byte[]> responseEntity;
            try {
                responseEntity = new ResponseEntity<>(org.apache.commons.io.FileUtils.readFileToByteArray(downloadFile),
                        headers, HttpStatus.CREATED);
            } catch (IOException e) {
                logger.info("init ResponseEntity<byte[]> fail, downloadFile:{}", downloadFile);
                throw new ServiceException("下载文件失败");
            }
            return responseEntity;
        } else {
            logger.info("downLoadFile fail, downLoadFile is not exist.");
            throw new ServiceException("下载文件失败, 文件不存在");
        }
    }

    public static void previewFileConverter(String fileName, String username, String realPath) {
        checkNotEmpty(fileName, "下载的文件名不能为空");
        checkNotEmpty(username, "用户名不能为空");

        String previewFilePath = createPreviewFilePath(fileName, username);
        File previewFile = new File(previewFilePath);
        String fileSuffix = getFileSuffix(fileName);
        if (previewFile.exists()) {
            try {
                if ("doc".equals(fileSuffix) || "docx".equals(fileSuffix)) {
                    OfficeToPDFConverter.docToPDF(previewFilePath, realPath);
                } else if ("xls".equals(fileSuffix) || "xlsx".equals(fileSuffix)) {
                    OfficeToPDFConverter.excelToPDF(previewFilePath, realPath);
                } else if ("ppt".equals(fileSuffix) || "pptx".equals(fileSuffix)) {
                    OfficeToPDFConverter.pptToPDF(previewFilePath, realPath);
                } else {
                    logger.info("getPreviewFilePath fail, fileSuffix is not support.");
                    throw new ServiceException("预览的文件不存在");
                }
            } catch (IOException e) {
                logger.info("getPreviewFilePath fail, OfficeToPDFConverter run fail.");
                throw new ServiceException("文件格式转换失败");
            }
        } else {
            logger.info("getPreviewFilePath fail, previewFile is not exist.");
            throw new ServiceException("预览的文件不存在");
        }
    }

    private static String createSaveFilePath(String fileName, String username) {
        String serverPath = CommonConst.PATH_FOLDER + username;
        File dir = new File(serverPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return serverPath + "\\" + fileName;
    }

    private static String createDownloadFilePath(String fileName, String username) {
        String saveFilePath = CommonConst.PATH_FOLDER + username + "/" + fileName;

        return saveFilePath.replaceAll("\\\\", "/");
    }

    private static String createPreviewFilePath(String fileName, String username) {
        String serverPath = CommonConst.PATH_FOLDER + username + "/" + fileName;

        return serverPath.replaceAll("\\\\", "/");
    }

    private static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static void deleteDir(File dir) {
        checkNotNull(dir, "删除的文件夹不能为空");

        if (dir.isDirectory()) {
            String[] children = dir.list();

            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }

        // 目录此时为空，可以删除
        dir.delete();
    }

    public static String createPreviewPDFPath(String realPath, String username) {
        checkNotEmpty(realPath, "绝对路径不能为空");
        checkNotEmpty(username, "用户名不能为空");

        String serverPath = realPath + username;
        File dir = new File(serverPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return serverPath;
    }
}
