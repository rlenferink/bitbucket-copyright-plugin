package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.commit.Changeset;
import com.atlassian.bitbucket.commit.ChangesetsRequest;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.*;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.io.TypeAwareOutputSupplier;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.roylenferink.bitbucket.logger.PluginLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class UpdateCopyrightHook implements PostRepositoryHook<PullRequestMergeHookRequest> {

    private final Logger log;
    private final CommitService commitService;
    private final ContentService contentService;

    public UpdateCopyrightHook(@ComponentImport final CommitService commitService,
                               @ComponentImport final ContentService contentService) {
        PluginLoggerFactory lf = PluginLoggerFactory.getInstance();
        this.log =  lf.getLoggerForThis(this);

        this.commitService = commitService;
        this.contentService = contentService;
    }

    @Override
    public void postUpdate(@Nonnull PostRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest request) {
        if (request.isDryRun())
            return; // Don't do anything if a dryrun is being executed

        if (!request.getMergeHash().isPresent())
            return; // No merge commit available

        String mergeCommit = request.getMergeHash().get();

        log.debug("Pull request in [{}] merged from [{}] to [{}] in commit [{}]",
                request.getRepository(),
                request.getFromRef().getId(),
                request.getToRef().getId(),
                mergeCommit);

        ChangesetsRequest csr = new ChangesetsRequest.Builder(request.getRepository())
                                    .commitId(mergeCommit)
                                    .build();

        Set<Path> changedFiles = new HashSet<>();

        Page<Changeset> changeSets = commitService.getChangesets(csr, new PageRequestImpl(0, 9999));
        for (Changeset cs : changeSets.getValues()) {
            Page<Change> changes = cs.getChanges();
            for (Change c : changes.getValues()) {
                changedFiles.add(c.getPath());
            }
        }

        for (Path p : changedFiles) {
            log.debug("File changed in PR: {}", p.toString());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            contentService.streamFile(request.getRepository(), mergeCommit, p.toString(), new TypeAwareOutputSupplier() {
                @Override
                @Nonnull
                public OutputStream getStream(@Nonnull String s) throws IOException {
                    return os;
                }
            });

            try {
                String fileContent = new String(os.toByteArray(), "UTF-8");
                log.debug("==================");
                log.debug(fileContent);
                log.debug("==================");
            } catch (UnsupportedEncodingException e) {
                log.error("Error parsing file content: {}", e.getMessage());
            }
        }
    }

}
