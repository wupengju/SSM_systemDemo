package com.menglin.entity;

public class Student {
    private Long id;
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column student.username
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    private String username;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column student.password
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    private String password;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column student.username
     *
     * @return the value of student.username
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column student.username
     *
     * @param username the value for student.username
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column student.password
     *
     * @return the value of student.password
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column student.password
     *
     * @param password the value for student.password
     *
     * @mbggenerated Mon Apr 16 18:30:05 CST 2018
     */
    public void setPassword(String password) {
        this.password = password;
    }
}