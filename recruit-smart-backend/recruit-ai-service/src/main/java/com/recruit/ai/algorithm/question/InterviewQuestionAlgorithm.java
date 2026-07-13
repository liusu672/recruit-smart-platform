package com.recruit.ai.algorithm.question;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterviewQuestionAlgorithm {

    public InterviewQuestionResponse generate(InterviewQuestionRequest request){
        String jobText = joinText(
                request.getJobTitle(),
                request.getRequirements(),
                request.getResponsibilities()
        );

        String resumeText = joinText(
                request.getResumeText(),
                request.getSkills(),
                request.getProjectExperience(),
                request.getWorkExperience()
        );

        List<String> questions = new ArrayList<>();
        String category = "通用岗位";

        if (containsAny(jobText, "Java", "Spring", "Spring Boot", "MySQL", "Redis", "微服务")) {
            category = "Java后端";
            questions.add("请介绍一下你在 Java 后端项目中的主要职责。");
            questions.add("你在 Spring Boot 项目中是如何设计和开发接口的？");
            questions.add("MySQL 索引在什么情况下会失效？");
            questions.add("Redis 在你的项目里主要解决了什么问题？");
            questions.add("如果系统拆分成微服务，你认为会遇到哪些问题？");
        } else if (containsAny(jobText, "Python", "机器学习", "RAG", "向量检索", "大模型")) {
            category = "AI算法";
            questions.add("请介绍一下你做过的机器学习或大模型相关项目。");
            questions.add("RAG 的核心流程是什么？");
            questions.add("向量检索和传统关键词检索有什么区别？");
            questions.add("你在 Python 项目中常用哪些数据处理或模型库？");
            questions.add("如果检索结果不准确，你会从哪些角度优化？");
        } else if (containsAny(jobText, "招聘流程", "候选人沟通", "Offer", "入职办理")) {
            category = "HR业务";
            questions.add("请介绍你对招聘全流程的理解。");
            questions.add("如果候选人临近 offer 阶段突然犹豫，你会怎么沟通？");
            questions.add("你如何跟进候选人的面试安排和反馈？");
            questions.add("你做过哪些入职办理或 offer 管理相关工作？");
            questions.add("如果同时有多个岗位在推进，你如何安排优先级？");
        } else {
            questions.add("请介绍一下你的核心项目经历。");
            questions.add("你认为自己最匹配这个岗位的能力是什么？");
            questions.add("你在过往工作中遇到的最大挑战是什么？");
            questions.add("如果加入这个岗位，你认为自己最需要补足的能力是什么？");
        }

        if (resumeText.contains("项目")) {
            questions.add("请详细说明你简历中提到的项目背景、你的职责以及最终成果。");
        }

        if (resumeText.contains("负责")) {
            questions.add("你提到自己负责过某些模块，请举例说明你具体做了哪些工作。");
        }

        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setCategory(category);
        response.setSummary("已根据岗位要求和候选人简历生成推荐面试题。");
        response.setQuestions(questions);
        return response;
    }

    private String joinText(String... values){
        StringBuilder builder = new StringBuilder();
        for(String value: values){
            if(value != null){
                builder.append(value).append("");
            }
        }
        return builder.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
