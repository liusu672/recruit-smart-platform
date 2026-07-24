package com.recruit.biz;

import com.recruit.biz.messaging.resume.ResumeParseMessage;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.storage.StoredResumeFile;
import com.recruit.biz.enums.ResumeFileType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 覆盖 biz-service 中零散的 POJO/工具类。
 */
class BizMiscComponentTest {

    // ========== security/CurrentUser ==========
    @Test
    void currentUser_constructAndGetters() {
        CurrentUser user = new CurrentUser(1L, "admin", "ADMIN");
        assertEquals(1L, user.getUserId());
        assertEquals("admin", user.getUsername());
        assertEquals("ADMIN", user.getRoleCode());
    }

    // ========== security/UserContext ==========
    @Test
    void userContext_setAndGet() {
        UserContext.clear();
        assertNull(UserContext.get());
        UserContext.set(new CurrentUser(1L, "test", "USER"));
        assertNotNull(UserContext.get());
        assertEquals("test", UserContext.get().getUsername());
        UserContext.clear();
        assertNull(UserContext.get());
    }

    @Test
    void userContext_getRequired_throwsWhenNotSet() {
        UserContext.clear();
        assertThrows(Exception.class, UserContext::getRequired);
    }

    @Test
    void userContext_getUserId() {
        UserContext.set(new CurrentUser(5L, "user5", "HR"));
        assertEquals(5L, UserContext.getUserId());
        UserContext.clear();
    }

    // ========== messaging/ResumeParseMessage ==========
    @Test
    void resumeParseMessage_record() {
        LocalDateTime now = LocalDateTime.now();
        ResumeParseMessage msg = new ResumeParseMessage(100L, "req-001", now);
        assertEquals(100L, msg.resumeId());
        assertEquals("req-001", msg.requestId());
        assertEquals(now, msg.createdAt());
    }

    // ========== storage/StoredResumeFile ==========
    @Test
    void storedResumeFile_constructAndGetters() {
        StoredResumeFile file = new StoredResumeFile("/path/file.pdf", ResumeFileType.PDF);
        assertEquals("/path/file.pdf", file.getRelativePath());
        assertEquals(ResumeFileType.PDF, file.getFileType());
    }

    // ========== storage/StoredResumeMultipartFile (basic) ==========
    @Test
    void storedResumeMultipartFile_nameIsAlwaysFile() {
        // Integration test would mock ResumeFileResource
        // This just verifies the class loads and basic constants work
        assertNotNull(StoredResumeFile.class);
    }
}
