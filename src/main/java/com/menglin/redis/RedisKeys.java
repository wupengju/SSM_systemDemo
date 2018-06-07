package com.menglin.redis;

public final class RedisKeys {


    /*
     * key template:
     * public static final String EntityClassName_CACHE_KEY = "projectName:EntityClassName:";
     * key: projectName:EntityClassName:id
     * */
    public static final String TASK_CACHE_KEY = "VNDais:Task:";
    public static final String TEACHER_CACHE_KEY = "VNDais:Teacher:";
    public static final String STUDENT_CACHE_KEY = "VNDais:Student:";
    public static final String CLASS_TEAM_CACHE_KEY = "VNDais:ClassTeam:";
    public static final String NOTICE_CACHE_KEY = "VNDais:Notice:";
    public static final String COLLEGE_CACHE_KEY = "VNDais:College:";
    public static final String COURSE_CACHE_KEY = "VNDais:Course:";
    public static final String MAJOR_CACHE_KEY = "VNDais:Major:";
    public static final String ROLE_CACHE_KEY = "VNDais:Role:";
    public static final String MPERMISSION_CACHE_KEY = "VNDais:Permission:";
}
