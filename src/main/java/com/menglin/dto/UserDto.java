package com.menglin.dto;

import org.springframework.stereotype.Component;

@Component
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private Long roleId;
    private String roleName;
    private String gmtCreate;
    private String gmtModify;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(String gmtModify) {
        this.gmtModify = gmtModify;
    }
    //    public Student convertToStudent() {
//        UserDtoToStudentConverter userDtoToStudentConverter = new UserDtoToStudentConverter();
//        return userDtoToStudentConverter.convert(this);
//    }
//
//    public Teacher convertToTeacher() {
//        UserDtoToTeacherConverter userDtoToTeacherConverter = new UserDtoToTeacherConverter();
//        return userDtoToTeacherConverter.convert(this);
//    }
//
//    public Admin convertToAdmin() {
//        UserDtoToAdminConverter userDtoToAdminConverter = new UserDtoToAdminConverter();
//        return userDtoToAdminConverter.convert(this);
//    }
//
//    public UserDto convertFor(Student student) {
//        UserDtoToStudentConverter userDtoToStudentConverter = new UserDtoToStudentConverter();
//        return userDtoToStudentConverter.reverse().convert(student);
//    }
//
//    public UserDto convertFor(Teacher teacher) {
//        UserDtoToTeacherConverter userDtoToTeacherConverter = new UserDtoToTeacherConverter();
//        return userDtoToTeacherConverter.reverse().convert(teacher);
//    }
//
//    public UserDto convertFor(Admin admin) {
//        UserDtoToAdminConverter userDtoToAdminConverter = new UserDtoToAdminConverter();
//        return userDtoToAdminConverter.reverse().convert(admin);
//    }
//
//    private static class UserDtoToStudentConverter extends Converter<UserDto, Student> {
//
//        @Override
//        protected Student doForward(UserDto userDto) {
//            Student student = new Student();
//            BeanUtils.copyProperties(userDto, student);
//            return student;
//        }
//
//        @Override
//        protected UserDto doBackward(Student student) {
//            UserDto userDto = new UserDto();
//            BeanUtils.copyProperties(student, userDto);
//            return userDto;
//        }
//    }
//
//    private static class UserDtoToTeacherConverter extends Converter<UserDto, Teacher> {
//
//        @Override
//        protected Teacher doForward(UserDto userDto) {
//            Teacher teacher = new Teacher();
//            BeanUtils.copyProperties(userDto, teacher);
//            return teacher;
//        }
//
//        @Override
//        protected UserDto doBackward(Teacher teacher) {
//            UserDto userDto = new UserDto();
//            BeanUtils.copyProperties(teacher, userDto);
//            return userDto;
//        }
//    }
//
//    private static class UserDtoToAdminConverter extends Converter<UserDto, Admin> {
//
//        @Override
//        protected Admin doForward(UserDto userDto) {
//            Admin admin = new Admin();
//            BeanUtils.copyProperties(userDto, admin);
//            return admin;
//        }
//
//        @Override
//        protected UserDto doBackward(Admin admin) {
//            UserDto userDto = new UserDto();
//            BeanUtils.copyProperties(admin, userDto);
//            return userDto;
//        }
//    }
}
