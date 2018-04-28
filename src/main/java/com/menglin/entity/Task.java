package com.menglin.entity;

import java.io.Serializable;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column task.name
     *
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    private String name;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column task.author
     *
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    private String author;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column task.name
     *
     * @return the value of task.name
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column task.name
     *
     * @param name the value for task.name
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column task.author
     *
     * @return the value of task.author
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    public String getAuthor() {
        return author;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column task.author
     *
     * @param author the value for task.author
     * @mbggenerated Thu Apr 19 20:24:40 CST 2018
     */
    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }
}