package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.roylenferink.bitbucket.logger.PluginLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class UpdateCopyrightHook implements PostRepositoryHook<PullRequestMergeHookRequest> {

    private PluginLoggerFactory lf = PluginLoggerFactory.getInstance();
    private final Logger log = lf.getLoggerForThis(this);

    @Override
    public void postUpdate(@Nonnull PostRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest request) {
        if (request.isDryRun())
            return; //Don't do anything if a dryrun is being executed

        log.info("YEAHOOOOOO!!!!");
        log.info("[{}] {} updated [{}]",
                request.getRepository(),
                request.getTrigger().getId(),
                request.getRefChanges().stream()
                    .map(change -> change.getRef().getId())
                    .collect(Collectors.joining(", ")));
    }

}
