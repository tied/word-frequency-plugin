package com.frost.plugin.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.frost.plugin.service.WordFrequencyService;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Scanned
public class WordFrequencyServlet extends HttpServlet {

    private VelocityManager velocityManager;
    private PageBuilderService pageBuilderService;

    @Inject
    private WordFrequencyService wordFrequencyService;

    public WordFrequencyServlet(@ComponentImport VelocityManager velocityManager,
                                @ComponentImport PageBuilderService pageBuilderService,
                                WordFrequencyService wordFrequencyService) {
        this.velocityManager = velocityManager;
        this.pageBuilderService = pageBuilderService;
        this.wordFrequencyService = wordFrequencyService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        pageBuilderService
                .assembler()
                .resources()
                .requireWebResource("com.frost.plugin.word-frequency-plugin:word-frequency-plugin-resources");

        Map<String, Long> frequency = wordFrequencyService.getFrequency();

        Map<String, Object> context = new HashMap<>();
        context.put("frequency", frequency);
        String content = velocityManager.getEncodedBody(
                "/templates/", "word-frequency.vm", "UTF-8", context
        );
        response.getWriter().write(content);
        response.getWriter().close();
    }
}
