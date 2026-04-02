package com.arelore.data.sec.umbrella.server.dto.request;

import java.util.List;

/**
 * 测试规则请求DTO
 */
public class DatabasePolicyTestRulesRequest {
    /**
     * 分类规则列表
     */
    private List<ClassificationRule> classificationRules;

    /**
     * 规则表达式
     */
    private String ruleExpression;

    /**
     * AI规则
     */
    private String aiRule;

    /**
     * 测试数据列表
     */
    private List<TestData> testData;
    
    /**
     * 数据库类型
     */
    private String databaseType;

    /**
     * 分类规则
     */
    public static class ClassificationRule {
        /**
         * 条件对象
         */
        private String conditionObject;

        /**
         * 条件类型
         */
        private String conditionType;

        /**
         * 表达式
         */
        private String expression;

        /**
         * 比例
         */
        private Integer ratio;

        // Getters and Setters
        public String getConditionObject() {
            return conditionObject;
        }

        public void setConditionObject(String conditionObject) {
            this.conditionObject = conditionObject;
        }

        public String getConditionType() {
            return conditionType;
        }

        public void setConditionType(String conditionType) {
            this.conditionType = conditionType;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public Integer getRatio() {
            return ratio;
        }

        public void setRatio(Integer ratio) {
            this.ratio = ratio;
        }
    }

    /**
     * 测试数据
     */
    public static class TestData {
        /**
         * 库名
         */
        private String databaseName;

        /**
         * 库描述
         */
        private String databaseDescription;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 表描述
         */
        private String tableDescription;

        /**
         * 列名
         */
        private String columnName;

        /**
         * 列描述
         */
        private String columnDescription;

        /**
         * 列值列表
         */
        private List<String> columnValues;

        // Getters and Setters
        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getDatabaseDescription() {
            return databaseDescription;
        }

        public void setDatabaseDescription(String databaseDescription) {
            this.databaseDescription = databaseDescription;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTableDescription() {
            return tableDescription;
        }

        public void setTableDescription(String tableDescription) {
            this.tableDescription = tableDescription;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnDescription() {
            return columnDescription;
        }

        public void setColumnDescription(String columnDescription) {
            this.columnDescription = columnDescription;
        }

        public List<String> getColumnValues() {
            return columnValues;
        }

        public void setColumnValues(List<String> columnValues) {
            this.columnValues = columnValues;
        }
    }

    // Getters and Setters
    public List<ClassificationRule> getClassificationRules() {
        return classificationRules;
    }

    public void setClassificationRules(List<ClassificationRule> classificationRules) {
        this.classificationRules = classificationRules;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public String getAiRule() {
        return aiRule;
    }

    public void setAiRule(String aiRule) {
        this.aiRule = aiRule;
    }

    public List<TestData> getTestData() {
        return testData;
    }

    public void setTestData(List<TestData> testData) {
        this.testData = testData;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
}