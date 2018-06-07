package com.menglin.controller;

import com.alibaba.fastjson.JSONObject;
import com.menglin.common.ActionResult;
import com.menglin.common.CommonConst;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.service.StudentTaskService;
import com.menglin.service.TaskService;
import com.menglin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/file")
@ResponseBody
public class FileController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(FileController.class);

    @Resource
    private TaskService taskService;
    @Resource
    private StudentTaskService studentTaskService;

    @RequestMapping(value = "/taskAttachment", consumes = "multipart/form-data", method = RequestMethod.POST)
    public ActionResult<?> addTaskAttachment(HttpServletRequest request,
                                             @RequestParam("taskId") Long taskId,
                                             @RequestParam("file") MultipartFile file) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        String taskAttachmentUrl = FileUtil.saveFile(file, JWTPayloadDto.getUsername());
        logger.info("request: file/taskAttachment, file:{}", JSONObject.toJSONString(file));

        int updateTaskId = taskService.updateTaskUrlAndWriteTaskAttachment(taskId, taskAttachmentUrl, JWTPayloadDto.getUsername(), false);
        if (updateTaskId > 0) {
            return createSuccessActionResult("上传成功");
        } else {
            return createFailActionResult("上传失败");
        }
    }

    @RequestMapping(value = "/taskAnswerAttachment", consumes = "multipart/form-data", method = RequestMethod.POST)
    public ActionResult<?> addTaskAnswerAttachment(HttpServletRequest request,
                                                   @RequestParam("taskId") Long taskId,
                                                   @RequestParam("file") MultipartFile file) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        String taskAnswerAttachmentUrl = FileUtil.saveFile(file, JWTPayloadDto.getUsername());
        logger.info("request: file/taskAttachment, taskAnswerAttachmentUrl:{}", taskAnswerAttachmentUrl);

        int updatedStudentTaskId = studentTaskService.updateStudentTaskUrlAndWriteTaskAttachment(taskId, taskAnswerAttachmentUrl, JWTPayloadDto.getUsername(), false);
        if (updatedStudentTaskId > 0) {
            return createSuccessActionResult("上传成功");
        } else {
            return createFailActionResult("上传失败");
        }
    }

    @RequestMapping(value = "/taskAttachment", method = RequestMethod.DELETE)
    public ActionResult<?> deleteTaskAttachment(HttpServletRequest request,
                                                @RequestParam("taskId") Long taskId,
                                                @RequestParam("filePath") String filePath) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.TEACHER_IDENTITY);

        int updateTaskId = taskService.updateTaskUrlAndWriteTaskAttachment(taskId, filePath, JWTPayloadDto.getUsername(), true);
        if (updateTaskId > 0) {
            return createSuccessActionResult("删除成功");
        } else {
            return createFailActionResult("删除失败");
        }
    }

    @RequestMapping(value = "/taskAnswerAttachment", method = RequestMethod.DELETE)
    public ActionResult<?> deleteTaskAnswerAttachment(HttpServletRequest request,
                                                      @RequestParam("studentTaskId") Long studentTaskId,
                                                      @RequestParam("filePath") String filePath) {
        JWTPayloadDto JWTPayloadDto = validateCurrentUserIdentityAndGetPayloadInfoFromRequest(request, CommonConst.STUDENT_IDENTITY);

        int updateTaskId = studentTaskService.updateStudentTaskUrlAndWriteTaskAttachment(studentTaskId, filePath, JWTPayloadDto.getUsername(), true);
        if (updateTaskId > 0) {
            return createSuccessActionResult("删除成功");
        } else {
            return createFailActionResult("删除失败");
        }
    }

    @RequestMapping(value = "/taskAttachment", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadTaskAttachment(@RequestParam("fileName") String fileName,
                                                         @RequestParam("teacherUsername") String teacherUsername) {
        logger.info("request: file/downloadTask, fileName:{}", fileName);

        return FileUtil.downLoadFile(fileName, teacherUsername);
    }

    @RequestMapping(value = "/taskAnswerAttachment", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadTaskAnswerAttachment(@RequestParam("fileName") String fileName,
                                                               @RequestParam("studentUsername") String studentUsername) {
        logger.info("request: file/downloadTask, fileName:{}", fileName);

        return FileUtil.downLoadFile(fileName, studentUsername);
    }

    @RequestMapping(value = "/taskAttachment/{teacherUsername}", method = RequestMethod.GET)
    public ActionResult<?> previewTaskAttachment(HttpServletRequest request,
                                                 @RequestParam("fileName") String fileName,
                                                 @PathVariable String teacherUsername) {
        return previewAttachment(request, fileName, teacherUsername);
    }

    @RequestMapping(value = "/taskAnswerAttachment/{studentUsername}", method = RequestMethod.GET)
    public ActionResult<?> previewTaskAnswerAttachment(HttpServletRequest request,
                                                       @RequestParam("fileName") String fileName,
                                                       @PathVariable String studentUsername) {
        return previewAttachment(request, fileName, studentUsername);
    }

    private String getRequestDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    private String createPreviewPDFFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf(".")) + ".pdf";
    }

    private ActionResult<?> previewAttachment(HttpServletRequest request, String fileName, String username) {
        JWTPayloadDto JWTPayloadDto = getPayloadInfoFromRequest(request);

        String realPath = request.getServletContext().getRealPath("/pdfs/");
        realPath = FileUtil.createPreviewPDFPath(realPath, JWTPayloadDto.getUsername());
        FileUtil.previewFileConverter(fileName, username, realPath);
        String previewFilePath = getRequestDomain(request) + "/pdfs/" + JWTPayloadDto.getUsername() + "/" + createPreviewPDFFileName(fileName);
        return createSuccessActionResult(previewFilePath);
    }
}
