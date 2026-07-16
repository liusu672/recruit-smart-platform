package com.recruit.biz.security;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;

public final class UserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser currentUser) {
        HOLDER.set(currentUser);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static CurrentUser getRequired() {
        CurrentUser currentUser = HOLDER.get();

        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录状态失效"
            );
        }

        return currentUser;
    }

    public static Long getUserId() {
        return getRequired().getUserId();
    }

    public static String getRoleCode() {
        return getRequired().getRoleCode();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
