package com.menglin.entity;

import java.util.Date;

public class MajorCourse {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.id
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.major_id
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private Long majorId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.course_id
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private Long courseId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.is_delete
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private String isDelete = "N";

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.creator
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private String creator;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.modifier
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private String modifier = "";

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.gmt_create
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private Date gmtCreate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column major_course.gmt_modify
     *
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    private Date gmtModify;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.id
     *
     * @return the value of major_course.id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.id
     *
     * @param id the value for major_course.id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.major_id
     *
     * @return the value of major_course.major_id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public Long getMajorId() {
        return majorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.major_id
     *
     * @param majorId the value for major_course.major_id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.course_id
     *
     * @return the value of major_course.course_id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public Long getCourseId() {
        return courseId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.course_id
     *
     * @param courseId the value for major_course.course_id
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.is_delete
     *
     * @return the value of major_course.is_delete
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public String getIsDelete() {
        return isDelete;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.is_delete
     *
     * @param isDelete the value for major_course.is_delete
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete == null ? null : isDelete.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.creator
     *
     * @return the value of major_course.creator
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public String getCreator() {
        return creator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.creator
     *
     * @param creator the value for major_course.creator
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.modifier
     *
     * @return the value of major_course.modifier
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.modifier
     *
     * @param modifier the value for major_course.modifier
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setModifier(String modifier) {
        this.modifier = modifier == null ? null : modifier.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.gmt_create
     *
     * @return the value of major_course.gmt_create
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.gmt_create
     *
     * @param gmtCreate the value for major_course.gmt_create
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column major_course.gmt_modify
     *
     * @return the value of major_course.gmt_modify
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public Date getGmtModify() {
        return gmtModify;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column major_course.gmt_modify
     *
     * @param gmtModify the value for major_course.gmt_modify
     * @mbggenerated Sat Apr 28 16:42:08 CST 2018
     */
    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }
}