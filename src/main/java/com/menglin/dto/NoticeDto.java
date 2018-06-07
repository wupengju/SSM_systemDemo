package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Notice;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class NoticeDto extends BaseTableDto {
    private String content;
    private String url;
    private Long adminId;
    private String adminUserName;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public Notice convertToNotice() {
        NoticeDto.NoticeDtoToNoticeConverter noticeDtoToNoticeConverter = new NoticeDto.NoticeDtoToNoticeConverter();
        return noticeDtoToNoticeConverter.convert(this);
    }

    public NoticeDto convertFor(Notice notice) {
        NoticeDto.NoticeDtoToNoticeConverter noticeDtoToNoticeConverter = new NoticeDto.NoticeDtoToNoticeConverter();
        return noticeDtoToNoticeConverter.reverse().convert(notice);
    }

    private static class NoticeDtoToNoticeConverter extends Converter<NoticeDto, Notice> {

        @Override
        protected Notice doForward(NoticeDto noticeDto) {
            Notice notice = new Notice();
            BeanUtils.copyProperties(noticeDto, notice);
            return notice;
        }

        @Override
        protected NoticeDto doBackward(Notice notice) {
            NoticeDto noticeDto = new NoticeDto();
            BeanUtils.copyProperties(notice, noticeDto);
            return noticeDto;
        }
    }
}
