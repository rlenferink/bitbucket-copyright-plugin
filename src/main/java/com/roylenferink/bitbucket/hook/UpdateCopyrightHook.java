package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class UpdateCopyrightHook implements PostRepositoryHook<PullRequestMergeHookRequest> {

    private static final Logger log = LoggerFactory.getLogger(UpdateCopyrightHook.class);

    @Override
    public void postUpdate(@Nonnull PostRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest hookRequest) {
        if (hookRequest.isDryRun())
            return; //Don't do anything if a dryrun is being executed

        log.info("[{}] {} updated [{}]",
                hookRequest.getRepository(),
                hookRequest.getTrigger().getId(),
                hookRequest.getRefChanges().stream()
                        .map(change -> change.getRef().getId())
                        .collect(Collectors.joining(", ")));
    }

}
