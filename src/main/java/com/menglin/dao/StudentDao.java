package com.menglin.dao;

import com.menglin.entity.Student;
import org.apache.ibatis.annotations.Param;

public interface StudentDao {

    Student selectByUsername(String username);

    Student selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    int insert(Student record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    int insertSelective(Student record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    Student selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    int updateByPrimaryKeySelective(Student record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table student
     *
     * @mbggenerated Mon Apr 16 18:22:40 CST 2018
     */
    int updateByPrimaryKey(Student record);
}