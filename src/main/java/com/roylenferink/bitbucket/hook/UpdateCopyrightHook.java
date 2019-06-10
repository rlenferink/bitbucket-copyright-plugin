package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.commit.Changeset;
import com.atlassian.bitbucket.commit.ChangesetsRequest;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.Path;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.roylenferink.bitbucket.logger.PluginLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class UpdateCopyrightHook implements PostRepositoryHook<PullRequestMergeHookRequest> {

    private final Logger log;
    private final CommitService commitService;

    public UpdateCopyrightHook(@ComponentImport final CommitService commitService) {
        PluginLoggerFactory lf = PluginLoggerFactory.getInstance();
        this.log =  lf.getLoggerForThis(this);
        this.commitService = commitService;
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

        Page<Changeset> changesets = commitService.getChangesets(csr, new PageRequestImpl(0, 9999));
        for (Changeset cs : changesets.getValues()) {
            Page<Change> changes = cs.getChanges();
            for (Change c : changes.getValues()) {
                changedFiles.add(c.getPath());
            }
        }

        for (Path p : changedFiles) {
            log.debug("File changed in PR: {}", p.toString());
        }
    }

}
