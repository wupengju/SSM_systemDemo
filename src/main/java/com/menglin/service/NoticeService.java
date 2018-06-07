package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.dto.NoticeDto;
import com.menglin.dto.SearchConditionsDto;

public interface NoticeService {
    int addNotice(NoticeDto noticeDto);

    int updateNotice(NoticeDto noticeDto);

    void deleteNoticeById(Long id);

    void batchDeleteNoticesByIds(String ids);

    NoticeDto getNoticeById(Long id);

    NoticeDto getNoticeByName(String name);

    PageInfo<NoticeDto> getNoticesByPage(int start, int pageSize, SearchConditionsDto searchConditionsDto);
}

