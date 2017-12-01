/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.reporting.internal;

import com.googlecode.jatl.Html;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.reporting.HtmlPageBuilder;
import org.gradle.reporting.HtmlReportRenderer;
import org.gradle.reporting.ReportRenderer;
import org.gradle.util.GFileUtils;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class BuildDashboardGenerator extends ReportRenderer<Collection<Report>, File> {
    private Set<Report> reports;
    private File outputFile;

    @Override
    public void render(Collection<Report> reports, final File outputFile) {
        this.reports = new TreeSet<Report>(new Comparator<Report>() {
            public int compare(Report o1, Report o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        this.reports.addAll(reports);
        this.outputFile = outputFile;

        HtmlReportRenderer renderer = new HtmlReportRenderer();
        renderer.renderRawSinglePage(reports, new ReportRenderer<Collection<Report>, HtmlPageBuilder<Writer>>() {
            @Override
            public void render(Collection<Report> model, HtmlPageBuilder<Writer> builder) throws IOException {
                generate(builder);
            }
        }, outputFile);
    }

    private void generate(final HtmlPageBuilder<Writer> builder) {
        final String baseCssLink = builder.requireResource(getClass().getResource("/org/gradle/reporting/base-style.css"));
        final String cssLink = builder.requireResource(getClass().getResource("style.css"));
        new Html(builder.getOutput()) {{
            html();
                head();
                    meta().httpEquiv("Content-Type").content("text/html; charset=utf-8");
                    meta().httpEquiv("x-ua-compatible").content("IE=edge");
                    link().rel("stylesheet").type("text/css").href(baseCssLink).end();
                    link().rel("stylesheet").type("text/css").href(cssLink).end();
                    title().text("Build dashboard").end();
                end();
                body();
                div().id("content");
                    if (reports.size() > 0) {
                        h1().text("Build reports").end();
                        ul();
                        for (Report report : reports) {
                            li();
                            if (report.getDestination().exists()) {
                                a().href(GFileUtils.relativePath(outputFile.getParentFile(), getHtmlLinkedFileFromReport(report))).text(report.getDisplayName());
                            } else {
                                span().classAttr("unavailable").text(report.getDisplayName());
                            }
                            end(2);
                        }
                        end();
                    } else {
                        h1().text("There are no build reports available.").end();
                    }
                    div().id("footer");
                        p();
                            text("Generated by ");
                            a().href("http://www.gradle.org").text(GradleVersion.current().toString()).end();
                            text(" at " + builder.formatDate(new Date()));
                        end();
                    end();
                end();
            endAll();
        }};
    }

    private File getHtmlLinkedFileFromReport(Report report) {
        if (report instanceof DirectoryReport) {
            return ((DirectoryReport) report).getEntryPoint();
        } else {
            return report.getDestination();
        }
    }
}
