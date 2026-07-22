package com.recruit.ai.algorithm.question;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.feign.dto.response.InterviewQuestionItemResponse;
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

        List<InterviewQuestionItemResponse> questions = new ArrayList<>();
        String category = "通用岗位";

        if (containsAny(jobText, "Java", "Spring", "Spring Boot", "MySQL", "Redis", "微服务")) {
            category = "Java后端";
            questions.add(question("Java项目职责", "请介绍一下你在 Java 后端项目中的主要职责。", "项目真实性"));
            questions.add(question("接口设计", "你在 Spring Boot 项目中是如何设计和开发接口的？", "Spring Boot"));
            questions.add(question("索引优化", "MySQL 索引在什么情况下会失效？", "MySQL"));
            questions.add(question("缓存应用", "Redis 在你的项目里主要解决了什么问题？", "Redis"));
            questions.add(question("微服务设计", "如果系统拆分成微服务，你认为会遇到哪些问题？", "微服务"));
        } else if (containsAny(jobText, "Python", "机器学习", "RAG", "向量检索", "大模型")) {
            category = "AI算法";
            questions.add(question("AI项目经历", "请介绍一下你做过的机器学习或大模型相关项目。", "项目真实性"));
            questions.add(question("RAG流程", "RAG 的核心流程是什么？", "RAG"));
            questions.add(question("检索方式", "向量检索和传统关键词检索有什么区别？", "向量检索"));
            questions.add(question("Python技术栈", "你在 Python 项目中常用哪些数据处理或模型库？", "Python"));
            questions.add(question("检索优化", "如果检索结果不准确，你会从哪些角度优化？", "检索质量"));
        } else if (containsAny(jobText, "招聘流程", "候选人沟通", "Offer", "入职办理")) {
            category = "HR业务";
            questions.add(question("招聘流程", "请介绍你对招聘全流程的理解。", "招聘业务"));
            questions.add(question("候选人沟通", "如果候选人临近 offer 阶段突然犹豫，你会怎么沟通？", "沟通能力"));
            questions.add(question("面试跟进", "你如何跟进候选人的面试安排和反馈？", "流程推进"));
            questions.add(question("Offer与入职", "你做过哪些入职办理或 offer 管理相关工作？", "Offer管理"));
            questions.add(question("任务优先级", "如果同时有多个岗位在推进，你如何安排优先级？", "时间管理"));
        } else {
            questions.add(question("核心项目", "请介绍一下你的核心项目经历。", "项目真实性"));
            questions.add(question("岗位匹配", "你认为自己最匹配这个岗位的能力是什么？", "岗位匹配"));
            questions.add(question("问题解决", "你在过往工作中遇到的最大挑战是什么？", "问题解决"));
            questions.add(question("能力提升", "如果加入这个岗位，你认为自己最需要补足的能力是什么？", "自我认知"));
        }

        if (resumeText.contains("项目")) {
            questions.add(question("项目追问", "请详细说明你简历中提到的项目背景、你的职责以及最终成果。", "项目真实性"));
        }

        if (resumeText.contains("负责")) {
            questions.add(question("职责追问", "你提到自己负责过某些模块，请举例说明你具体做了哪些工作。", "职责边界"));
        }

        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setCategory(category);
        response.setSummary("已根据岗位要求和候选人简历生成推荐面试题。");
        response.setQuestions(questions);
        return response;
    }

    private InterviewQuestionItemResponse question(
            String title,
            String content,
            String focus
    ) {
        InterviewQuestionItemResponse question =
                new InterviewQuestionItemResponse();
        question.setTitle(title);
        question.setContent(content);
        question.setFocus(List.of(focus));
        question.setDifficulty("MEDIUM");
        question.setAnswerPoints(List.of());
        return question;
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
