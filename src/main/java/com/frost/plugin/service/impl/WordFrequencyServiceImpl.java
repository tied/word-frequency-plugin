package com.frost.plugin.service.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.Query;
import com.frost.plugin.service.WordFrequencyService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named("wordFrequencyServiceImpl")
public class WordFrequencyServiceImpl implements WordFrequencyService {

    private static final String SPLIT_REGEXP = "[^\\w]";

    @JiraImport
    private JiraAuthenticationContext authenticationContext;

    @JiraImport
    private SearchService searchService;

    @Override
    public Map<String, Long> getFrequency() {
        Map<String, Long> frequency = new LinkedHashMap<>();
        List<Issue> issues = getIssues(authenticationContext, searchService);
        List<String> words = getWordsFromIssues(issues);

        for (String word : words) {
            frequency.merge(word, 1L, Long::sum);
        }

        return frequency.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Returns all words from description and summary of the issues.
     *
     * @param issues - list of Issue
     * @return - list of String
     */
    private List<String> getWordsFromIssues(List<Issue> issues) {
        List<String> words = new LinkedList<>();

        for (Issue issue : issues) {
            String[] wordsFromDescription = issue.getDescription().split(SPLIT_REGEXP);
            String[] wordsFromSummary = issue.getSummary().split(SPLIT_REGEXP);
            words.addAll(
                    mergeArrays(wordsFromDescription, wordsFromSummary)
            );
        }
        return words;
    }

    /**
     * Merges specific arrays and returns list of String from the arrays.
     *
     * @param arrays - arrays of String
     * @return - list of String
     */
    private List<String> mergeArrays(String[]... arrays) {
        return Stream.of(arrays)
                .flatMap(Stream::of)
                .filter(this::notEmpty)
                .collect(Collectors.toList());
    }

    /**
     * Returns true - if the String is not empty,
     * false - if the String is empty or equals null.
     */
    private boolean notEmpty(String str) {
        if (Objects.isNull(str)) {
            return false;
        }
        return !str.isEmpty();
    }

    /**
     * Returns all issues from all projects in system.
     *
     * @param authenticationContext - {@link JiraAuthenticationContext}
     * @param searchService         - {@link SearchService}
     * @return - list of Issues
     */
    private List<Issue> getIssues(JiraAuthenticationContext authenticationContext,
                                  SearchService searchService) {
        List<Issue> issues = new LinkedList<>();

        ApplicationUser user = authenticationContext.getLoggedInUser();

        List<Project> projects = ComponentAccessor.getProjectManager().getProjects();
        for (Project project : projects) {
            Query query = JqlQueryBuilder.newClauseBuilder().project(project.getId()).buildQuery();
            PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
            try {
                issues.addAll(searchService.search(user, query, pagerFilter).getIssues());
            } catch (SearchException e) {
                e.printStackTrace();
            }
        }
        return issues;
    }

    @Inject
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Inject
    public void setAuthenticationContext(JiraAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }
}
