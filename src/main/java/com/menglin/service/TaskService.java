package com.menglin.service;

import com.github.pagehelper.PageInfo;
import com.menglin.entity.Task;

public interface TaskService {
    /**
     * 返回相应的数据集合
     *
     * @return
     */
    public PageInfo<Task> getTasksByPage(int start, int pageSize, Task task);

    /**
     * 数据数目
     *
     * @return
     */
    public Long getTotalTask();

    /**
     * 添加文章
     *
     * @param task
     * @return
     */
    public int addTask(Task task);

    /**
     * 修改文章
     *
     * @param task
     * @return
     */
    public int updateTask(Task task);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    public int deleteTaskById(Long id);

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    public Task getById(Long id);
}
