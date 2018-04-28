package com.menglin.common;

import com.google.common.annotations.GwtCompatible;
import com.menglin.exception.ArgumentsException;

/**
 * 断言类
 *
 * @author menglin
 */
@GwtCompatible
public final class AssertArguments {

    /*
     * Preconditions
     * */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new ArgumentsException("checkArgument 断言的参数是 false.");
        }
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new ArgumentsException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new ArgumentsException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static void checkState(boolean expression) {
        if (!expression) {
            throw new ArgumentsException("checkState 断言的参数是 false.");
        }
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new ArgumentsException(String.valueOf(errorMessage));
        }
    }

    public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new ArgumentsException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new ArgumentsException("checkNotNull 断言的参数是 null.");
        } else {
            return reference;
        }
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new ArgumentsException(String.valueOf(errorMessage));
        } else {
            return reference;
        }
    }

    public static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null) {
            throw new ArgumentsException(format(errorMessageTemplate, errorMessageArgs));
        } else {
            return reference;
        }
    }

    public static int checkElementIndex(int index, int size) {
        return checkElementIndex(index, size, "index");
    }

    public static int checkElementIndex(int index, int size, String desc) {
        if (index >= 0 && index < size) {
            return index;
        } else {
            throw new ArgumentsException(badElementIndex(index, size, desc));
        }
    }

    private static String badElementIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new ArgumentsException("negative size: " + size);
        } else {
            return format("%s (%s) must be less than size (%s)", desc, index, size);
        }
    }

    public static int checkPositionIndex(int index, int size) {
        return checkPositionIndex(index, size, "index");
    }

    public static int checkPositionIndex(int index, int size, String desc) {
        if (index >= 0 && index <= size) {
            return index;
        } else {
            throw new ArgumentsException(badPositionIndex(index, size, desc));
        }
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new ArgumentsException("negative size: " + size);
        } else {
            return format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }

    public static void checkPositionIndexes(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            throw new ArgumentsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start >= 0 && start <= size) {
            return end >= 0 && end <= size ? format("end index (%s) must not be less than start index (%s)", end, start) : badPositionIndex(end, size, "end index");
        } else {
            return badPositionIndex(start, size, "start index");
        }
    }

    private static String format(String template, Object... args) {
        template = String.valueOf(template);
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;

        int i;
        int placeholderStart;
        for (i = 0; i < args.length; templateStart = placeholderStart + 2) {
            placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }

            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
        }

        builder.append(template.substring(templateStart));
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);

            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }

            builder.append(']');
        }

        return builder.toString();
    }


    /**
     * 断言数字不能为零，若数字为零则报异常
     *
     * @param num 待校验数字
     */
    public static void checkNotZero(Object num) {
        num = AssertArguments.checkNotNull(num);
        if ((num instanceof Integer && (int) num == 0) || (num instanceof Long && (Long) num == 0L)) {
            throw new ArgumentsException("checkNotZero 断言的参数是 0.");
        }
    }

    /**
     * 断言数字不能为零，若数字为零则报异常
     *
     * @param num          待校验数字
     * @param errorMessage 异常信息
     */
    public static void checkNotZero(Object num, String errorMessage) {
        num = AssertArguments.checkNotNull(num, errorMessage);
        if ((num instanceof Integer && (int) num == 0) || (num instanceof Long && (Long) num == 0L)) {
            throw new ArgumentsException(errorMessage);
        }
    }

    /**
     * 断言数字大于或等于零，若数字小于零则报异常
     *
     * @param num 待校验数字
     */
    public static void checkGreaterThanOrIsZero(Object num) {
        num = AssertArguments.checkNotNull(num);
        if ((num instanceof Integer && (int) num < 0) || (num instanceof Long && (Long) num < 0L)) {
            throw new ArgumentsException("checkGreaterThanZero 断言的参数小于零.");
        }
    }

    /**
     * 断言数字大于或等于零，若数字小于零则报异常
     *
     * @param num          待校验数字
     * @param errorMessage 异常信息
     */
    public static void checkGreaterThanOrIsZero(Object num, String errorMessage) {
        num = AssertArguments.checkNotNull(num, errorMessage);
        if ((num instanceof Integer && (int) num < 0) || (num instanceof Long && (Long) num < 0L)) {
            throw new ArgumentsException(errorMessage);
        }
    }

    /**
     * 断言数字大于零，若数字小于或等于零则报异常
     *
     * @param num 待校验数字
     */
    public static void checkGreaterThanZero(Object num) {
        num = AssertArguments.checkNotNull(num);
        if ((num instanceof Integer && (int) num <= 0) || (num instanceof Long && (Long) num <= 0L)) {
            throw new ArgumentsException("checkGreaterThanZero 断言的参数小于或等于零.");
        }
    }

    /**
     * 断言数字大于零，若数字小于或等于零则报异常
     *
     * @param num          待校验数字
     * @param errorMessage 异常信息
     */
    public static void checkGreaterThanZero(Object num, String errorMessage) {
        num = AssertArguments.checkNotNull(num, errorMessage);
        if ((num instanceof Integer && (int) num <= 0) || (num instanceof Long && (Long) num <= 0L)) {
            throw new ArgumentsException(errorMessage);
        }
    }

    /**
     * 断言字符串不能为空，若字符串为空则报异常
     *
     * @param string 待校验字符串
     */
    public static void checkNotEmpty(String string) {
        string = AssertArguments.checkNotNull(string);
        if ("".equals(string)) {
            throw new ArgumentsException("checkNotEmpty 断言的参数是空字符串.");
        }
    }

    /**
     * 断言字符串不能为空，若字符串为空则报异常
     *
     * @param string       待校验字符串
     * @param errorMessage 异常信息
     */
    public static void checkNotEmpty(String string, String errorMessage) {
        string = AssertArguments.checkNotNull(string, errorMessage);
        if ("".equals(string)) {
            throw new ArgumentsException(errorMessage);
        }
    }

    /**
     * 断言字符串长度为某一个值，不等于则报异常
     *
     * @param string       待校验字符串
     * @param stringLength 待校验长度
     */
    public static void checkNotEqualLength(String string, int stringLength) {
        string = AssertArguments.checkNotNull(string);
        stringLength = AssertArguments.checkNotNull(stringLength);
        if (string.length() != stringLength) {
            throw new ArgumentsException("checkNotEqualLength 断言的字符串长度不符合要求.");
        }
    }

    /**
     * 断言字符串不能为空，若字符串为空则报异常
     *
     * @param string       待校验字符串
     * @param stringLength 待校验长度
     * @param errorMessage 异常信息
     */
    public static void checkNotEqualLength(String string, int stringLength, String errorMessage) {
        string = AssertArguments.checkNotNull(string);
        stringLength = AssertArguments.checkNotNull(stringLength);
        if (string.length() != stringLength) {
            throw new ArgumentsException(errorMessage);
        }
    }
}