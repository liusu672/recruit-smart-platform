package com.recruit.ai.algorithm.question;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.prompt.InterviewQuestionPrompts;
import com.recruit.feign.dto.response.InterviewQuestionItemResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterviewQuestionAlgorithm {

    public InterviewQuestionResponse generate(
            InterviewQuestionRequest request
    ) {
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
        String category = detectCategory(jobText);
        String round = normalizeRound(request.getInterviewRound());

        switch (round) {
            case "SECOND" -> addSecondRoundQuestions(
                    category,
                    resumeText,
                    questions
            );
            case "HR" -> addHrRoundQuestions(questions);
            default -> addFirstRoundQuestions(
                    category,
                    resumeText,
                    questions
            );
        }

        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setCategory(category);
        response.setSummary(
                "已根据"
                        + InterviewQuestionPrompts.roundLabel(round)
                        + "要求、岗位信息和候选人简历生成推荐面试题。"
        );
        response.setQuestions(questions);
        return response;
    }

    private void addFirstRoundQuestions(
            String category,
            String resumeText,
            List<InterviewQuestionItemResponse> questions
    ) {
        if ("Java后端".equals(category)) {
            questions.add(question(
                    "Java项目职责",
                    "请介绍你在 Java 后端项目中的主要职责和实际交付内容。",
                    "简历真实性",
                    "EASY"
            ));
            questions.add(question(
                    "Java基础",
                    "请说明集合、异常处理和并发基础在项目中的典型用法。",
                    "Java基础",
                    "MEDIUM"
            ));
            questions.add(question(
                    "Spring Boot接口",
                    "你在 Spring Boot 项目中如何设计和开发接口？",
                    "Spring Boot",
                    "MEDIUM"
            ));
            questions.add(question(
                    "MySQL索引",
                    "MySQL 索引在什么情况下可能失效？",
                    "MySQL",
                    "MEDIUM"
            ));
            questions.add(question(
                    "Redis应用",
                    "Redis 在你的项目中主要解决了什么问题？",
                    "Redis",
                    "MEDIUM"
            ));
        } else if ("AI算法".equals(category)) {
            questions.add(question(
                    "AI项目经历",
                    "请介绍你实际参与的机器学习或大模型项目。",
                    "项目真实性",
                    "EASY"
            ));
            questions.add(question(
                    "RAG基础",
                    "请说明 RAG 的核心流程。",
                    "RAG",
                    "MEDIUM"
            ));
            questions.add(question(
                    "向量检索",
                    "向量检索和关键词检索有什么区别？",
                    "向量检索",
                    "MEDIUM"
            ));
            questions.add(question(
                    "Python技术栈",
                    "你常用哪些数据处理或模型开发库？",
                    "Python",
                    "EASY"
            ));
        } else if ("HR业务".equals(category)) {
            questions.add(question(
                    "招聘流程",
                    "请介绍你对招聘全流程的理解。",
                    "招聘业务",
                    "EASY"
            ));
            questions.add(question(
                    "候选人沟通",
                    "候选人在 Offer 阶段犹豫时你会如何沟通？",
                    "沟通能力",
                    "MEDIUM"
            ));
            questions.add(question(
                    "任务优先级",
                    "多个岗位同时推进时你如何安排优先级？",
                    "时间管理",
                    "MEDIUM"
            ));
        } else {
            questions.add(question(
                    "核心项目",
                    "请介绍你的核心项目经历和个人职责。",
                    "项目真实性",
                    "EASY"
            ));
            questions.add(question(
                    "岗位匹配",
                    "你认为自己最匹配岗位的能力是什么？",
                    "岗位匹配",
                    "EASY"
            ));
            questions.add(question(
                    "问题解决",
                    "请介绍一次你独立解决问题的经历。",
                    "问题解决",
                    "MEDIUM"
            ));
        }

        addResumeQuestions(resumeText, questions);
    }

    private void addSecondRoundQuestions(
            String category,
            String resumeText,
            List<InterviewQuestionItemResponse> questions
    ) {
        if ("Java后端".equals(category)) {
            questions.add(question(
                    "JVM与故障分析",
                    "线上服务频繁 Full GC 时，你会如何定位原因并验证优化效果？",
                    "JVM与可观测性",
                    "HARD"
            ));
            questions.add(question(
                    "并发与线程池",
                    "如何根据业务特征设计线程池，并处理任务堆积和拒绝？",
                    "并发设计",
                    "HARD"
            ));
            questions.add(question(
                    "事务边界",
                    "Spring 事务失效有哪些常见原因？跨服务事务如何权衡？",
                    "事务设计",
                    "HARD"
            ));
            questions.add(question(
                    "数据库优化",
                    "面对高并发慢查询，你会如何从 SQL、索引和架构层逐步优化？",
                    "MySQL性能",
                    "HARD"
            ));
            questions.add(question(
                    "缓存一致性",
                    "请设计数据库与 Redis 的一致性方案，并说明异常场景处理。",
                    "缓存一致性",
                    "HARD"
            ));
            questions.add(question(
                    "系统设计",
                    "请设计一个高并发核心业务接口，说明限流、降级和幂等方案。",
                    "系统设计",
                    "HARD"
            ));
        } else if ("AI算法".equals(category)) {
            questions.add(question(
                    "RAG评估",
                    "如何建立 RAG 检索和生成效果的离线与在线评估体系？",
                    "评估体系",
                    "HARD"
            ));
            questions.add(question(
                    "召回优化",
                    "检索结果不准确时，你会如何定位切分、向量化和重排问题？",
                    "检索质量",
                    "HARD"
            ));
            questions.add(question(
                    "模型工程",
                    "请说明模型服务在并发、延迟和成本之间的权衡方案。",
                    "模型工程",
                    "HARD"
            ));
            questions.add(question(
                    "生产故障",
                    "请分析一次 AI 系统效果突然下降时的排查路径。",
                    "故障分析",
                    "HARD"
            ));
        } else {
            questions.add(question(
                    "复杂项目复盘",
                    "请选择最复杂的项目，说明关键决策、权衡和最终结果。",
                    "项目深度",
                    "HARD"
            ));
            questions.add(question(
                    "方案权衡",
                    "请介绍一个存在多种方案的问题，以及你的选择依据。",
                    "决策能力",
                    "HARD"
            ));
            questions.add(question(
                    "故障处理",
                    "遇到重大线上或业务故障时，你如何定位并推动解决？",
                    "故障分析",
                    "HARD"
            ));
            questions.add(question(
                    "系统性改进",
                    "你如何将一次问题处理沉淀为长期机制？",
                    "复盘能力",
                    "MEDIUM"
            ));
        }

        addResumeQuestions(resumeText, questions);
    }

    private void addHrRoundQuestions(
            List<InterviewQuestionItemResponse> questions
    ) {
        questions.add(question(
                "求职动机",
                "你选择当前岗位和公司的主要原因是什么？",
                "求职动机",
                "EASY"
        ));
        questions.add(question(
                "离职原因",
                "请说明最近一次离职或考虑机会的原因。",
                "稳定性",
                "MEDIUM"
        ));
        questions.add(question(
                "职业规划",
                "你未来三年的职业目标是什么？",
                "职业规划",
                "EASY"
        ));
        questions.add(question(
                "协作方式",
                "与同事或负责人意见不一致时，你通常如何处理？",
                "协作与价值观",
                "MEDIUM"
        ));
        questions.add(question(
                "压力应对",
                "面对紧急任务和较大压力时，你会如何安排工作？",
                "压力管理",
                "MEDIUM"
        ));
        questions.add(question(
                "岗位期望",
                "你对岗位职责、团队和成长机会有哪些期望？",
                "岗位期望",
                "EASY"
        ));
        questions.add(question(
                "薪资期望",
                "你的薪资期望及其考虑依据是什么？",
                "薪资匹配",
                "EASY"
        ));
        questions.add(question(
                "到岗条件",
                "如果双方达成一致，你预计何时可以到岗？",
                "到岗时间",
                "EASY"
        ));
    }

    private void addResumeQuestions(
            String resumeText,
            List<InterviewQuestionItemResponse> questions
    ) {
        if (resumeText.contains("项目")) {
            questions.add(question(
                    "项目追问",
                    "请说明简历项目的背景、个人职责、关键难点和结果。",
                    "项目真实性",
                    "MEDIUM"
            ));
        }
        if (resumeText.contains("负责")) {
            questions.add(question(
                    "职责边界",
                    "请举例说明你负责模块的具体工作和个人贡献。",
                    "职责边界",
                    "MEDIUM"
            ));
        }
    }

    private String detectCategory(String jobText) {
        if (containsAny(
                jobText,
                "Java",
                "Spring",
                "MySQL",
                "Redis",
                "微服务"
        )) {
            return "Java后端";
        }
        if (containsAny(
                jobText,
                "Python",
                "机器学习",
                "RAG",
                "向量检索",
                "大模型"
        )) {
            return "AI算法";
        }
        if (containsAny(
                jobText,
                "招聘流程",
                "候选人沟通",
                "Offer",
                "入职办理"
        )) {
            return "HR业务";
        }
        return "通用岗位";
    }

    private InterviewQuestionItemResponse question(
            String title,
            String content,
            String focus,
            String difficulty
    ) {
        InterviewQuestionItemResponse question =
                new InterviewQuestionItemResponse();
        question.setTitle(title);
        question.setContent(content);
        question.setFocus(List.of(focus));
        question.setDifficulty(difficulty);
        question.setAnswerPoints(List.of());
        return question;
    }

    private String normalizeRound(String round) {
        if (round == null || round.isBlank()) {
            return "FIRST";
        }
        return round.trim().toUpperCase();
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null) {
                builder.append(value).append(" ");
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
